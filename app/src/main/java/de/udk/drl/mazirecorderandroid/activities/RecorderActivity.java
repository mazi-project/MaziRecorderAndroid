package de.udk.drl.mazirecorderandroid.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.CountDownTimer;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;


import java.io.File;
import java.io.IOException;

import de.udk.drl.mazirecorderandroid.models.AttachmentModel;
import de.udk.drl.mazirecorderandroid.recorder.SoundRecorder;
import de.udk.drl.mazirecorderandroid.recorder.SoundRecorderWav;
import de.udk.drl.mazirecorderandroid.utils.ObservableProperty;
import de.udk.drl.mazirecorderandroid.view.WaveformView;

import de.udk.drl.mazirecorderandroid.R;
import io.reactivex.functions.Consumer;

public class RecorderActivity extends BaseActivity {

    public enum RecorderState {
        INIT, RECORDING, STOPPED
    }

    public static final int RECORDING_MAX_TIME = 1000 * 60 * 5;

    ObservableProperty<RecorderState> state = new ObservableProperty<>(RecorderState.INIT);

    CountDownTimer timer = null;
    long recordTime = 0;

    SoundRecorder recorder;
    WaveformView waveformView;
    TextView timerView;
    ProgressBar progressBar;

    PowerManager.WakeLock wakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorder);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS_AUDIO_RECORD, REQUEST_AUDIO_RECORD);
        }

        if (getIntent().hasExtra("question")) {
            String questionText = getIntent().getStringExtra("question");
            ((TextView) findViewById(R.id.question_text)).setText(questionText);
        }

        timerView = (TextView) findViewById(R.id.elapsed_time);
        waveformView = (WaveformView) findViewById(R.id.waveform_view);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        // init soundrecorder
        recorder = new SoundRecorderWav(getApplicationContext());
        recorder.setAudioBufferCallback(new SoundRecorder.AudioBufferCallback() {
            @Override
            public void onNewData(int max) {
                waveformView.updateAudioData(max);
            }
        });

        // init wakelock
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Recorder Wake Lock");

        // keep screen always on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        final TextView textView = (TextView) findViewById(R.id.text_view);
        final View saveButton = findViewById(R.id.save_button);
        final View cancelButton = findViewById(R.id.cancel_button);
        final ToggleButton recordButton = (ToggleButton) findViewById(R.id.record_button);
        final EditText tagField = ((EditText) findViewById(R.id.edit_text_tags));

        //add input filter to tagfield to allow only letters, space and _
        InputFilter filter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {

                if (!source.toString().matches("^[a-zA-Z0-9_ ]*"))
                    return "";
                return null;
            }
        };
        tagField.setFilters(new InputFilter[] { filter });


        // update ui according to recorder state
        subscribers.add(
                state.distinct().subscribe(new Consumer<RecorderState>() {
                    @Override
                    public void accept(RecorderState state) throws Exception {
                        if (state == RecorderState.RECORDING) {
                            textView.setText("Stop Recording");
                            saveButton.setVisibility(View.INVISIBLE);
                            cancelButton.setVisibility(View.INVISIBLE);
                            recordButton.setChecked(true);
                        } else if (state == RecorderState.STOPPED) {
                            textView.setText("Continue Recording");
                            saveButton.setVisibility(View.VISIBLE);
                            cancelButton.setVisibility(View.VISIBLE);
                            recordButton.setChecked(false);
                            waveformView.clearAudioData();
                        } else {
                            textView.setText("Start Recording");
                            progressBar.setProgress(0);
                            timerView.setText(convertTimeString(0));
                            saveButton.setVisibility(View.INVISIBLE);
                            cancelButton.setVisibility(View.INVISIBLE);
                            recordButton.setChecked(false);
                            waveformView.clearAudioData();
                        }
                    }
                })
        );

//
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (wakeLock.isHeld())
            wakeLock.release();
        recorder.release();
    }

    public void startRecording() {

        //start Counting Time, set maximum time
        timer = new CountDownTimer(RECORDING_MAX_TIME - recordTime, 25) {

            long startRecordTime = recordTime;
            long startTime = System.currentTimeMillis();

            public void onTick(long millisUntilFinished) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                recordTime = startRecordTime + elapsedTime;
                timerView.setText(convertTimeString(recordTime));

                float progress = recordTime/(float)RECORDING_MAX_TIME;
                progressBar.setProgress((int)(progress*100));
            }

            public void onFinish() {
                stopRecording();
            }
        }.start();

        try {
            recorder.prepare();
            recorder.startRecording();
            state.set(RecorderState.RECORDING);
            wakeLock.acquire();
        } catch (Exception e) {
            showAlert("Error", e.getMessage());
            stopRecording();
        }
    }

    public void stopRecording() {

        //stop timer
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        try {
            recorder.stopRecording();
            state.set(RecorderState.STOPPED);
            wakeLock.release();
        } catch (IOException e) {
            showAlert("Error", e.getMessage());
        }
    }

    public void resetRecording() {

        //stop timer
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        recordTime = 0;

        try {
            recorder.reset();
            state.set(RecorderState.INIT);
        } catch (IOException e) {
            showAlert("Error", e.getMessage());
        }
    }

    public File saveRecording() {

        File file = null;

        try {
            file = recorder.save();
            recorder.reset();
        } catch (Exception e) {
            showAlert("Error",e.getMessage());
        }

        return file;
    }

    public void onRecordButtonClicked(View view) {
        if (state.get() == RecorderState.INIT)
            startRecording();
        else if (state.get() == RecorderState.STOPPED) {
            startRecording();
        } else if (state.get() == RecorderState.RECORDING)
            stopRecording();
    }

    public void onSaveButtonClicked(View view) {
        if (state.get() == RecorderState.STOPPED) {
            showOverlay("Recording gets saved...",(ViewGroup)findViewById(R.id.main_layout));
            File file = saveRecording();
            if (file != null) {

                String question = getIntent().getStringExtra("question");
                String tags = ((EditText) findViewById(R.id.edit_text_tags)).getText().toString();

                AttachmentModel attachment = new AttachmentModel();

                attachment.text = question;
                attachment.tags = tags.split("\\s+");
                attachment.file = file.getAbsolutePath();

                Intent intent = new Intent();
                intent.putExtra("attachment",attachment);
                setResult(Activity.RESULT_OK,intent);
                finish();
            } else
                finish();
        }
    }

    public void onCancelButtonClicked(View view) {
        resetRecording();
    }

    private String convertTimeString(long milliseconds) {

        long hsecs = (milliseconds % 1000 ) / 10;
        long seconds = (milliseconds / 1000) % 60;
        long minutes = (milliseconds / 1000) / 60;

        return String.format("%02d:%02d.%02d", minutes, seconds, hsecs);
    }
}
