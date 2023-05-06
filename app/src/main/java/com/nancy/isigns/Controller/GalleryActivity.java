package com.nancy.isigns.Controller;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.nancy.isigns.Model.SignsDetection;
import com.nancy.isigns.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class GalleryActivity extends AppCompatActivity {

    private static String LOGTAG = "OpenCV_log";

    private Button bOpenGallery;
    private ImageView imgGallery;

    private ImageView imgExtracted;

    private Bitmap bitmap1;

    private Bitmap bitmap2;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {

            switch(status){
                case LoaderCallbackInterface.SUCCESS:{
                    Log.v(LOGTAG,"OpenCV loaded");
                }break;
                default:{
                    super.onManagerConnected(status);
                }break;


            }
            super.onManagerConnected(status);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        bOpenGallery = findViewById(R.id.btnGallery);
        imgGallery = findViewById(R.id.imgGallery);
        imgExtracted = findViewById(R.id.imgExtracted);

        bOpenGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent iGallery = new Intent(Intent.ACTION_GET_CONTENT);
                iGallery.setType("image/*");
                //iGallery.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                GalleryActivityResultLauncher.launch(iGallery);

            }
        });


    }

    ActivityResultLauncher<Intent> GalleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {

                        Intent data = result.getData();
                        if(data != null) {

                            try{
                                bitmap1 = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                                imgGallery.setImageBitmap(bitmap1);

                                Mat mat = new Mat();
                                Bitmap bmp32 = bitmap1.copy(Bitmap.Config.ARGB_8888, true);
                                Utils.bitmapToMat(bmp32, mat);

                                //mat = SignsDetection.signsExtraction(mat);
                                mat = SignsDetection.GreenC(mat);
                                bitmap2 = bmp32;
                                Utils.matToBitmap(mat,bitmap2);

                                imgExtracted.setImageBitmap(bitmap2);

                            }catch (IOException e){
                                e.printStackTrace();
                            }

                            //imgGallery.setImageURI(data.getData());
                        }
                    }
                }
            });

    @Override
    protected void onResume() {
        super.onResume();

        if(! OpenCVLoader.initDebug()){

            Log.d(LOGTAG,"OpenCV not found, initializing");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        }
        else{
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }
}