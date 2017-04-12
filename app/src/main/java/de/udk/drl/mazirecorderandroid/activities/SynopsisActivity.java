package de.udk.drl.mazirecorderandroid.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.udk.drl.mazirecorderandroid.R;
import de.udk.drl.mazirecorderandroid.models.InterviewStorage;
import de.udk.drl.mazirecorderandroid.utils.Utils;

public class SynopsisActivity extends BaseActivity {

    public static final String PICTURE_FILES_DIRECTORY = "Attachments";

    private static final int REQUEST_IMAGE_CAPTURE_CODE = 3;

    public File imageFile;

    private InterviewStorage interviewStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_synopsis);

        interviewStorage = InterviewStorage.getInstance();

        updateUi();
    }

    public void updateUi() {

        final ImageView pictureView = (ImageView) findViewById(R.id.picture_view);

        //update image in background task
        AsyncTask task = new AsyncTask<Object, Void, Bitmap>() {

            @Override
            protected Bitmap doInBackground(Object... params) {
                if (interviewStorage.interview.imageFile != null && Utils.fileExists(interviewStorage.interview.imageFile)) {
                    Bitmap bmp = BitmapFactory.decodeFile(interviewStorage.interview.imageFile);
                    Bitmap scaledBmp = Utils.scaleBitmap(bmp, 256, 256);
                    return scaledBmp;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Bitmap result) {
                if (result != null);
                    pictureView.setImageBitmap(result);
            }
        };
        task.execute();
    }

    public void onUploadButtonClicked(View view) {

    }

    public void onImageButtonClicked(View view) {

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            showAlert("Warning", "Cant access the device's camera.");
            return;
        }

        // Create the File where the photo should go
        imageFile = getOutputMediaFileName();

        // start the image capture activity
        if (imageFile != null) {
            Uri file = Uri.fromFile(imageFile);
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, file);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null)
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE_CODE);
        } else {
            showAlert("Error", "Could not create image file.");
        }
    }

    private static File getOutputMediaFileName() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),PICTURE_FILES_DIRECTORY);

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //hide Spinner
        hideOverlay();

        if (requestCode == REQUEST_IMAGE_CAPTURE_CODE && resultCode == RESULT_OK) {

            if (imageFile.exists()) {
                interviewStorage.interview.imageFile = imageFile.getAbsolutePath();
                interviewStorage.save();
                updateUi();
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
