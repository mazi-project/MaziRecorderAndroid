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
        uploader.postInterview(this.interview).subscribe(new Observer<JSONObject>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(JSONObject json) {
                Log.e("RESPONSE",json.toString());
            }

            @Override
            public void onError(Throwable e) {
                Log.e("RESPONSE",e.toString());
            }

            @Override
            public void onComplete() {

            }
        });

//        httpRequest = InterviewUploader.postInterview(this.interview, new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(Call<ResponseBody> call,
//                                   Response<ResponseBody> response) {
//                hideOverlay();
//
//                JSONObject json;
//
//                try {
//                    String string = response.body().string();
//                    Log.e(LOG_TAG, "Upload response: " + string);
//                    json = new JSONObject(string);
//                    if (!json.has("interview")) {
//                        showAlert("Fehler", "Upload fehlgeschlagen: " + string, true);
//                        return;
//                    }
//                } catch (Exception e) {
//                    showAlert("Fehler", "Upload fehlgeschlagen: " + e.getMessage(), true);
//                    e.printStackTrace();
//                    return;
//                }
//
//
//                //delete story
//                //InterviewStorage.getInstance().createNew();
//
//                findViewById(R.id.uploading_layout).setVisibility(View.GONE);
//                findViewById(R.id.done_layout).setVisibility(View.VISIBLE);
//            }
//
//            @Override
//            public void onFailure(Call<ResponseBody> call, Throwable t) {
//                showAlert("Fehler", "Es konnte keine Verbindung zum Server hergestellt werden: " + t.getMessage(), true);
//            }
//        });
    }

    public void onUrlClicked(View view) {

        TextView textview = (TextView) view;
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(textview.getText().toString()));
        startActivity(browserIntent);
    }

    public void onBackButtonClicked(View view) {
        this.finish();
    }
}
