package com.cloudifyimage.cloudifyimage;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.cloudifyimage.lib.CloudifyImageListener;
import com.cloudifyimage.lib.CloudifyImageUtil;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int SELECT_PICTURE = 100;
    private static final String TAG = "MainActivity";

    CoordinatorLayout coordinatorLayout;
    FloatingActionButton btnSelectImage;
    AppCompatImageView imgView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Find the views...
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        btnSelectImage = (FloatingActionButton) findViewById(R.id.btnSelectImage);
        imgView = (AppCompatImageView) findViewById(R.id.imgView);

        btnSelectImage.setOnClickListener(this);

    }

    /* Choose an image from Gallery */
    void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                // Get the url from data
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    // Get the path from the Uri
                    String path = getPathFromURI(selectedImageUri);
                    Log.i(TAG, "Image Path : " + path);
                    // Set the image in ImageView
                    imgView.setImageURI(selectedImageUri);

                    ProgressDialog pDialog = new ProgressDialog(this);
                    pDialog.setIndeterminate(false);
                    pDialog.setMax(100);
                    pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    pDialog.setCancelable(true);
                    try {
                        new CloudifyImageUtil()
                                .addServerUrl("http://192.168.43.7:8080/upload")
                                .addCredentials("kamalesh82", "cd9xdjfnrdds7kij1")
                                .addListener(new MyListener())
                                .addProgressDialog(pDialog)
                                .send("Rahul", path);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /* Get the real path from the URI */
    public String getPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                res = cursor.getString(column_index);
            }
            cursor.close();
        }
        return res;
    }

    @Override
    public void onClick(View v) {
        openImageChooser();
    }

    class MyListener implements CloudifyImageListener {

        @Override
        public void onSuccess(String response) {
            Log.d("MainActivity", response);
            Toast.makeText(MainActivity.this, response, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onFailure(String errorMessage) {
            Log.d("MainActivity", errorMessage);
            Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
        }
    }

}
