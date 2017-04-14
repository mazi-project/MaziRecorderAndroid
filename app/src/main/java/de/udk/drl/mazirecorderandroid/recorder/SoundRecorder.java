package de.udk.drl.mazirecorderandroid.recorder;

import android.content.Context;

import java.io.File;
import java.io.IOException;

/**
 * Created by lutz on 13/04/15.
 */
abstract public class SoundRecorder {

    protected Context context;

    // callback for visualisation
    public interface AudioBufferCallback {
        void onNewData(int max);
    }
    AudioBufferCallback callback = null;

    public SoundRecorder(Context context) {
        this.context = context;
    }

    // prepare has to be called once before recording
    public void prepare() throws Exception { };

    abstract public void startRecording() throws Exception;

    abstract public void stopRecording() throws IOException;

    abstract public void reset() throws IOException;

    abstract public File save() throws IOException;

    // releases the recorder and deletes tmp file
    abstract public void release();

    public void setAudioBufferCallback(AudioBufferCallback callback) {
        this.callback = callback;
    }

}
