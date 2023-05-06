package com.nancy.isigns.Controller;

import static org.opencv.imgcodecs.Imgcodecs.imread;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.nancy.isigns.Model.CircularRedSign;
import com.nancy.isigns.Model.SignsDetection;
import com.nancy.isigns.R;
import com.nancy.isigns.ml.GammaModel;
import com.nancy.isigns.ml.ModelAlpha305070;
import com.nancy.isigns.ml.ModelBeta203040;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VideoStreamDetectionActivity extends CameraActivity {

    private TextView probaPred;

    private ImageView imgPred;

    private ImageView imgTF;

    private Bitmap bitmap3;

    private static String LOGTAG = "OpenCV_log";
    private CameraBridgeViewBase mOpenCvCameraView;

    private static int frameRate;
    private static ArrayList<CircularRedSign> signsList;

    private static Mat matRef;

    private final DigitClassifier digitClassifier = new DigitClassifier(this);

    
    private final BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {

            switch(status){
                case LoaderCallbackInterface.SUCCESS:{
                    Log.v(LOGTAG,"OpenCV loaded");
                    mOpenCvCameraView.enableView();
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
        setContentView(R.layout.activity_videostreamdetection);

        probaPred = findViewById(R.id.digit);

        imgPred = findViewById(R.id.imgTFoutput);

        imgTF = findViewById(R.id.imgTFinput);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.opencv_surfaceView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(cvCameraViewListener);

        frameRate = 0;
        signsList = null;
        //matRef = imread("refj30.jpg");

        digitClassifier.initialize();

    }

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(mOpenCvCameraView);
    }

    private final CameraBridgeViewBase.CvCameraViewListener2 cvCameraViewListener = new CameraBridgeViewBase.CvCameraViewListener2() {
        @Override
        public void onCameraViewStarted(int width, int height) {

        }

        @Override
        public void onCameraViewStopped() {

        }

        @Override
        public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
            Mat m = inputFrame.rgba();
            Point center;
            float radius;

            frameRate+=1;

            if(frameRate == 10) {

                frameRate = 0;
                signsList = SignsDetection.greenCircles(m);

                //Bitmap bitmap1 = getBitmapFromAsset(getApplicationContext(), "ref30.png" );

                /*if(bitmap1 != null){
                    Bitmap bmp32Ref = bitmap1.copy(Bitmap.Config.ARGB_8888, true);
                    Utils.bitmapToMat(bmp32Ref, matRef);
                }*/

                Mat sObject = SignsDetection.signsExtraction(m);
                if(sObject!= null){

                    //matRef = imread("refj30.jpg");

                    // Extraction & Resizing du panneau
                    //Mat sObject2 = new Mat();
                    //Imgproc.resize(sObject,sObject2, matRef.size() );
                    // Niveaux de gris

                    Mat grayRef = new Mat();
                    Imgproc.cvtColor(sObject, grayRef, Imgproc.COLOR_BGRA2GRAY);
                    Core.normalize(grayRef, grayRef,0,255,Core.NORM_MINMAX);

                    Mat binImg = new Mat();
                    Imgproc.threshold(grayRef, binImg, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);

                    // Crop /2 de Mat

                    //Mat ROI = binImg.submat((int)Math.round(binImg.size().height/5),(int)Math.round(binImg.size().height/5)*4 ,(int)Math.round(binImg.size().width/11),(int)Math.round(binImg.size().width/2) );
                    Mat ROI = binImg;
                    // Conversion en bitmap

                    bitmap3 = Bitmap.createBitmap(ROI.cols(),ROI.rows(),Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(ROI,bitmap3);

                    setImgTF(imgTF, bitmap3);

                    // Classification

                    try {
                        GammaModel model = GammaModel.newInstance(getApplicationContext()); // Model load

                        // Creates inputs for reference.
                        TensorImage image = TensorImage.fromBitmap(bitmap3); // bitmap3 = Sign detected

                        // Runs model inference and gets result.
                        GammaModel.Outputs outputs = model.process(image);
                        List<Category> probability = outputs.getProbabilityAsCategoryList();

                        Category cat = SignsDetection.categoryRecognizer(probability);

                        setPredTF(probaPred, cat ); // Display highest probability on screen

                        setImgTF(imgPred, SignsDetection.refDisplay(cat, getApplicationContext()));
                        // Display Sign's label on screen

                        // Releases model resources if no longer used.
                        model.close();
                    } catch (IOException e) {
                        // TODO Handle the exception
                    }

                    /*if(bitmap3!=null){
                        Log.d(LOGTAG,"bitmap3 non null");
                        VideoStreamDetectionActivity.this.classifyDigit(bitmap3);
                    }*/
                }

            }

            if(signsList != null){
                for (int i = 0; i < signsList.size(); i++) {
                    center = signsList.get(i).getCenter();
                    radius = signsList.get(i).getRadius();
                    Imgproc.circle(m, center, (int)radius, new Scalar(0, 255, 0), 3);
                }
            }


            return m;
        }
    };

    private void classifyDigit(Bitmap b) {

        if (b != null && this.digitClassifier.isInitialized()) {

            this.digitClassifier.classifyAsync(b).addOnSuccessListener(new OnSuccessListener() {

                public void onSuccess(Object var1) {
                    this.onSuccess((String)var1);

                }

                public void onSuccess(String resultText) {
                    probaPred.setText(resultText);
                }
            }).addOnFailureListener((OnFailureListener)(new OnFailureListener() {
                public final void onFailure(Exception e) {
                    probaPred.setText("Error");

                }
            }));
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        if(mOpenCvCameraView != null){

            mOpenCvCameraView.disableView();
        }
    }

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

    @Override
    protected void onDestroy() {
        this.digitClassifier.close();
        super.onDestroy();

        if(mOpenCvCameraView != null){

            mOpenCvCameraView.disableView();
        }
    }

    public static Bitmap getBitmapFromAsset(Context context, String filePath) {
        AssetManager assetManager = context.getAssets();

        InputStream istr;
        Bitmap bitmap = null;
        try {

            istr = assetManager.open(filePath);

            bitmap = BitmapFactory.decodeStream(istr);

        } catch (IOException e) {
            // handle exception
        }

        return bitmap;
    }

    private void setImgTF(final ImageView img,final Bitmap bitmap){
        runOnUiThread(() -> img.setImageBitmap(bitmap));
    }

    private void setPredTF(final TextView digitPred,final Category cat){
        //runOnUiThread(() -> digitPred.setText(cat.getLabel()));
        runOnUiThread(() -> digitPred.setText(Float.toString(cat.getScore())));
    }
}