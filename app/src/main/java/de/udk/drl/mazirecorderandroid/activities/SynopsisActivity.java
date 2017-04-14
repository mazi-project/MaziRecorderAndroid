package de.udk.drl.mazirecorderandroid.activities;

import android.Manifest;
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
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.jakewharton.rxbinding2.widget.RxTextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import de.udk.drl.mazirecorderandroid.R;
import de.udk.drl.mazirecorderandroid.models.InterviewModel;
import de.udk.drl.mazirecorderandroid.models.InterviewStorage;
import de.udk.drl.mazirecorderandroid.utils.Utils;
import io.reactivex.Maybe;
import io.reactivex.MaybeObserver;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

public class SynopsisActivity extends BaseActivity {

    public static final String PICTURE_FILES_DIRECTORY = "Attachments";

    private static final int REQUEST_IMAGE_CAPTURE_CODE = 3;

    public File imageFile;
    private InterviewStorage interviewStorage;

    private Disposable interviewSubscription = null;
    private Disposable editTextSubscription = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_synopsis);

        // request permissions
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS_SYNOPSIS, REQUEST_SYNOPSIS_PERMISSIONS);
        }

        interviewStorage = InterviewStorage.getInstance();

        final EditText editTextSynopsis = (EditText) findViewById(R.id.edit_text_synopsis);
        final ImageView pictureView = (ImageView) findViewById(R.id.picture_view);
        editTextSynopsis.setText(interviewStorage.interview.text);

        // set up rx patterns
        final Observable<CharSequence> editTextObservable = RxTextView.textChanges(editTextSynopsis);

        // save synopsis to interview model
        editTextSubscription = editTextObservable.debounce(1, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<CharSequence>() {
                    @Override
                    public void accept(CharSequence charSequence) throws Exception {
                        interviewStorage.interview.text = charSequence.toString();
                        interviewStorage.save();
                    }
                });

        // update image
        interviewSubscription = interviewStorage.skipWhile(new Predicate<InterviewModel>() {
            @Override
            public boolean test(InterviewModel interviewModel) throws Exception {
                return interviewModel.imageFile == null;
            }
        }).map(new Function<InterviewModel, String>() {
            @Override
            public String apply(InterviewModel interviewModel) throws Exception {
                return interviewModel.imageFile;
            }
        }).distinctUntilChanged().map(new Function<String, Bitmap>() {
            @Override
            public Bitmap apply(String path) throws Exception {
                Bitmap bmp = BitmapFactory.decodeFile(path);
                Bitmap scaledBmp = Utils.scaleBitmap(bmp, 256, 256);
                return scaledBmp;
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Bitmap>() {
            @Override
            public void accept(Bitmap bitmap) throws Exception {
                pictureView.setImageBitmap(bitmap);
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (editTextSubscription != null && !editTextSubscription.isDisposed()) {
            editTextSubscription.dispose();
        }
        if (interviewSubscription != null && !interviewSubscription.isDisposed()) {
            interviewSubscription.dispose();
        }
    }

    public void onUploadButtonClicked(View view) {
        this.interviewStorage.save();

        Intent intent = new Intent(this, UploadActivity.class);
        startActivity(intent);
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
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
