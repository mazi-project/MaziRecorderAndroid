package de.udk.drl.mazirecorderandroid.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;


import org.json.JSONObject;


import de.udk.drl.mazirecorderandroid.R;
import de.udk.drl.mazirecorderandroid.models.InterviewModel;
import de.udk.drl.mazirecorderandroid.models.InterviewStorage;
import de.udk.drl.mazirecorderandroid.network.InterviewUploader;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import okhttp3.ResponseBody;
import retrofit2.Call;

public class UploadActivity extends BaseActivity {

    private static final String LOG_TAG = "UploadActivity";

    private Call<ResponseBody> httpRequest = null;

    private InterviewModel interview;

    private boolean uploading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        // hide done layout
        findViewById(R.id.done_layout).setVisibility(View.GONE);

        interview = InterviewStorage.getInstance(this).interview;

        if (!uploading)
            uploadStory();
    }

    public void uploadStory() {

        uploading = true;

        InterviewUploader uploader = new InterviewUploader(this);

        uploader.postInterview(this.interview).subscribe(new Observer<Boolean>() {

            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Boolean result) {
                //delete interview

                findViewById(R.id.uploading_layout).setVisibility(View.GONE);
                findViewById(R.id.done_layout).setVisibility(View.VISIBLE);

                TextView linkText = (TextView) findViewById(R.id.link_text);
                linkText.setText(InterviewUploader.APP_BASE_URL);
            }

            @Override
            public void onError(Throwable e) {
                Log.e("RESPONSE", e.toString());
                if (e instanceof java.net.ConnectException)
                    showAlert("ERROR", "Could not connect to server. Please connect to the MAZI Hotspot and try uploading again.", true);
                else
                    showAlert("ERROR", "Error: " + e.getMessage(), true);
            }

            @Override
            public void onComplete() {
                InterviewStorage storage = InterviewStorage.getInstance(UploadActivity.this);
                storage.reset();
                storage.save();
            }
        });
    }

    public void onUrlClicked(View view) {

        TextView textview = (TextView) view;
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(textview.getText().toString()));
        startActivity(browserIntent);
    }

    public void onBackButtonClicked(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }
}
