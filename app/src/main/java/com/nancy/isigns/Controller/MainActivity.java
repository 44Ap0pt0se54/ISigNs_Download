package com.nancy.isigns.Controller;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;

import com.nancy.isigns.R;

public class MainActivity extends AppCompatActivity  {

    private Button bGallery;
    private Button bStream;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bGallery = findViewById(R.id.main_activity_button_1);
        bStream = findViewById(R.id.main_activity_button_2);

        bGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent galleryActivityintent = new Intent(MainActivity.this, GalleryActivity.class);
                MainActivityResultLauncher.launch(galleryActivityintent);

            }
        });

        bStream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent streamActivityintent = new Intent(MainActivity.this, VideoStreamDetectionActivity.class);
                MainActivityResultLauncher.launch(streamActivityintent);


            }
        });
    }

    ActivityResultLauncher<Intent> MainActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                    }
                }
            });


}