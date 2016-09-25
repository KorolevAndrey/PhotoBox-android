package com.jbeleno.photobox;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import com.jbeleno.photobox.util.AlbumStorageDirFactory;
import com.jbeleno.photobox.util.BaseAlbumDirFactory;
import com.jbeleno.photobox.util.Message;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class CameraActivity extends AppCompatActivity {

    // Declare variables for camera use
    static final int REQUEST_TAKE_PHOTO = 1;

    String mCurrentPhotoPath;

    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";


    // Declare global UI controls
    private AppCompatImageView imgPreview;
    private AppCompatButton btnSend;
    private AppCompatButton btnStore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // Mapping UI controls into variables in Java
        imgPreview = (AppCompatImageView) findViewById(R.id.img_preview);
        btnSend = (AppCompatButton) findViewById(R.id.btn_send);
        btnStore = (AppCompatButton) findViewById(R.id.btn_store);

        // If the user touch over the camera icon, actives
        // the camera to take a photo
        imgPreview.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                dispatchTakePictureIntent(REQUEST_TAKE_PHOTO);
                return false;
            }
        });
    }

    /**
     * This method fires an Intent to use the camera to take a photo
     * and after taking the photo the data flow comes again to the app
     * the app creates an image file
     *
     * @param actionCode for the moment the only value accepted is
     *                   REQUEST_TAKE_PHOTO = 1
     */
    private void dispatchTakePictureIntent(int actionCode) {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if(actionCode == REQUEST_TAKE_PHOTO){
            File f = null;

            try {
                f = createImageFile();
                mCurrentPhotoPath = f.getAbsolutePath();
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
            } catch (IOException e) {
                Message.show("Error occurred while creating the File", this);
                e.printStackTrace();
                f = null;
                mCurrentPhotoPath = null;
            }
        }

        startActivityForResult(takePictureIntent, actionCode);
    }


    /**
     * This method create an image from the photo taken in the Camera app
     * and stores it in a public directory using a collision-resistant file
     * name
     * @return A file with the image content
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
        File albumF = getAlbumDir();

        return File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
    }


    /**
     * This method gets the directory to store the photos generated in this app,
     * this is done to share the pictures with the galery
     *
     * @return a directory to store photos
     */
    private File getAlbumDir() {
        File storageDir = null;
        AlbumStorageDirFactory mAlbumStorageDirFactory = new BaseAlbumDirFactory();
        String albumName = getString(R.string.album_name);

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

            storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(albumName);

            if (storageDir != null) {
                if (! storageDir.mkdirs()) {
                    if (! storageDir.exists()){
                        Log.d("CameraSample", "failed to create directory");
                        return null;
                    }
                }
            }

        } else {
            Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
        }

        return storageDir;
    }


    private void galleryAddPic() {

        /* This method share the pics in the private directory of the app and */
        /* makes them public to access from the gallery and other apps */

        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }


    private void handleBigCameraPhoto() {

        /* Verify if exist a path for phot file and starts showing a preview */
        /* After that share the picture in the gallery and set the picture file to null */

        if (mCurrentPhotoPath != null) {
            setPic();
            galleryAddPic();
            mCurrentPhotoPath = null;
        }

    }


    private void setPic() {

		/* There isn't enough memory to open up more than a couple camera photos */
		/* So pre-scale the target bitmap into which the file is decoded */

        // Get the dimensions of the View
        int targetW = imgPreview.getWidth();
        int targetH = imgPreview.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        imgPreview.setImageBitmap(bitmap);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // We receive a thumbnail from the camera to show in an AppCompatImageView
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            handleBigCameraPhoto();
        }
    }
}
