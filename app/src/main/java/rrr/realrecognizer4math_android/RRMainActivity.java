package rrr.realrecognizer4math_android;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Pair;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by waps12b on 16. 12. 13..
 */
public class RRMainActivity extends Activity {

    private static final String TAG = "RRMainActivity";

    public static final int REQUEST_PHOTO = 1;
    public static final int REQUEST_RECOGNIZE = 2;

    public static final int PERMISSION_CAMERA = 1;

    private TextureView mCameraTextureView;
    private RRPreview mPreview;

    private Button mBtnPhoto;
    private Button mBtnGallery;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rrmain);

        mBtnPhoto = (Button) findViewById(R.id.btn_photo);
        mBtnGallery = (Button) findViewById(R.id.btn_gallery);

        // Here, thisActivity is the current activity
        requestPermissions();

        mBtnPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPreview.takePicture();
            }
        });

        mBtnGallery.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK,  android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(photoPickerIntent, REQUEST_PHOTO);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        switch (requestCode)
        {
            case REQUEST_PHOTO:
                Log.i(TAG, "onActivityResult - RequestPhoto");
                if(resultCode == Activity.RESULT_OK)
                {
                    Uri selectedImage = data.getData();
                    onReceiveImage(selectedImage);
                }
                break;
            case REQUEST_RECOGNIZE:
                Log.i(TAG, "onActivityResult - RequestRecognize");
                if(resultCode == Activity.RESULT_OK)
                {

                }

                break;
        }
    }


    public synchronized void onReceiveImage(Uri imageUri)
    {
        Log.i(TAG, "onReceiveImage : [Uri]" + imageUri.toString());
        Intent intent = new Intent(this, RRRecognizeActivity.class);
        intent.setData(imageUri);
        startActivityForResult(intent, REQUEST_RECOGNIZE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mPreview!=null)
            mPreview.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mPreview!=null)
            mPreview.onPause();
    }

    private void requestPermissions()
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                Toast.makeText(this, "You should allow app to access your gallery and camera for recognition.", Toast.LENGTH_LONG);
            }
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_CAMERA);
        }else
        {
            initCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for(int i = 0; i < permissions.length; i++)
        {
            if( grantResults[i] == PackageManager.PERMISSION_DENIED )
            {
                finish();
            }
        }
        initCamera();
    }

    private synchronized void initCamera()
    {
        if(mPreview != null)
            return;

        Log.i(TAG, "Init camera");
        mCameraTextureView = (TextureView) findViewById(R.id.texture);
        mPreview = new RRPreview(this, mCameraTextureView);
        mPreview.openCamera();
    }
}
