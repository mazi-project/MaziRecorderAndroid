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
import android.util.Log;
import android.view.View;
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
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
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

        interviewStorage = InterviewStorage.getInstance();

        final EditText editTextSynopsis = (EditText) findViewById(R.id.edit_text_synopsis);
        final ImageView pictureView = (ImageView) findViewById(R.id.picture_view);
        editTextSynopsis.setText(interviewStorage.interview.text);

        // set up rx patterns
        final Observable<CharSequence> editTextObservable = RxTextView.textChanges(editTextSynopsis);

        // save synopsis to interview model
        editTextSubscription = editTextObservable.debounce(1, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<CharSequence>() {
            @Override
            public void accept(CharSequence charSequence) throws Exception {
                interviewStorage.interview.text = charSequence.toString();
                interviewStorage.save();
            }
        });


        // update image
        Single<Bitmap> loadBitmapObservable = Single.fromCallable(new Callable<Bitmap>() {
            @Override
            public Bitmap call() throws Exception {
                if (interviewStorage.interview.imageFile != null && Utils.fileExists(interviewStorage.interview.imageFile)) {
                    Bitmap bmp = BitmapFactory.decodeFile(interviewStorage.interview.imageFile);
                    Bitmap scaledBmp = Utils.scaleBitmap(bmp, 256, 256);
                    return scaledBmp;
                }
                throw new Error("Interview does not contain any image");
            }
        });

//        interviewSubscription = interviewStorage.distinct(new Function<InterviewModel, InterviewModel>() {
//            @Override
//            public InterviewModel apply(InterviewModel interviewModel) throws Exception {
//                return interviewModel;
//            }
//        }).onErrorReturn(new Function<Throwable, InterviewModel>() {
//            @Override
//            public InterviewModel apply(Throwable throwable) throws Exception {
//                return null;
//            }
//        })..subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe(new SingleObserver<Bitmap>() {
//                @Override
//                public void onSubscribe(Disposable d) {
//
//                }
//
//                @Override
//                public void onSuccess(Bitmap value) {
//                    pictureView.setImageBitmap(value);
//                }
//
//                @Override
//                public void onError(Throwable e) {
//
//                }
//            });


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (editTextSubscription != null && !editTextSubscription.isDisposed()) {
            editTextSubscription.dispose();
        }
//        if (interviewSubscription != null && !interviewSubscription.isDisposed()) {
//            interviewSubscription.dispose();
//        }
    }

    public void updateUi() {

//        final ImageView pictureView = (ImageView) findViewById(R.id.picture_view);
//
//        Single<Bitmap> loadBitmapObservable = Single.fromCallable(new Callable<Bitmap>() {
//            @Override
//            public Bitmap call() throws Exception {
//                if (interviewStorage.interview.imageFile != null && Utils.fileExists(interviewStorage.interview.imageFile)) {
//                    Bitmap bmp = BitmapFactory.decodeFile(interviewStorage.interview.imageFile);
//                    Bitmap scaledBmp = Utils.scaleBitmap(bmp, 256, 256);
//                    return scaledBmp;
//                }
//                throw new Error("Interview does not contain any image");
//            }
//        });
//
//        loadBitmapObservable.subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new SingleObserver<Bitmap>() {
//                    @Override
//                    public void onSubscribe(Disposable d) {
//
//                    }
//
//                    @Override
//                    public void onSuccess(Bitmap value) {
//                        pictureView.setImageBitmap(value);
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//
//                    }
//                });

    }

    public void onUploadButtonClicked(View view) {
        this.interviewStorage.save();
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
