package de.udk.drl.mazirecorderandroid.recorder;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import de.udk.drl.mazirecorderandroid.utils.Utils;

/**
 * Created by lutz on 13/04/15.
 * Code taken and modified from
 * http://stackoverflow.com/questions/25727535/record-audio-save-in-wav-file-format-in-android
 * and ?
 */
public class SoundRecorderWav extends SoundRecorder {

    private static final String LOG_TAG = "WAV_RECORDER";

    private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
    private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
    private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";

    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;


    private AudioRecord recorder = null;
    private int bufferSize = 0;
    private Thread recordingThread = null;

    // Buffer for output
    private short[] buffer;
    private int sampleRate;

    //size of the record
    int bytesRecorded = 0;

    public SoundRecorderWav(Context context) {
        super(context);

        ArrayList<Integer> validRates = getValidSampleRates();
        if (validRates.isEmpty())
            sampleRate = 8000; //set to lowest
        else
            sampleRate = validRates.get(validRates.size() -1);

        bufferSize = AudioRecord.getMinBufferSize(sampleRate,RECORDER_CHANNELS,RECORDER_AUDIO_ENCODING);

        Log.e(LOG_TAG,"Recorder initialized with sample rate:"+ sampleRate + ", buffer size:"+ bufferSize);

        // clear temporary file
        deleteTempFile();
    }

    @Override
    public void prepare() throws Exception {

        // init recorder
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,sampleRate,
                RECORDER_CHANNELS,RECORDER_AUDIO_ENCODING, bufferSize);

        //init buffer
        buffer = new short[bufferSize];
    }

    @Override
    public void startRecording() throws Exception {
        if (recorder == null)
            throw new Exception("Recorder not prepared.");

        //open temp file for writing
        final RandomAccessFile randomAccessWriter = new RandomAccessFile(getTempFile(), "rw");

        //go to end of file
        randomAccessWriter.seek(randomAccessWriter.length());

        recorder.startRecording();

        recordingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                writeAudioDataToFile(randomAccessWriter);
            }
        },"AudioRecorder Thread");
        recordingThread.start();
    }

    @Override
    public void stopRecording() {
        if (recorder != null && recorder.getState() == AudioRecord.STATE_INITIALIZED) {
            recorder.stop();
            // wait for recording thread to end
            while (recordingThread != null) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void reset() {
        stopRecording();
        deleteTempFile();
        bytesRecorded = 0;
    }

    @Override
    public File save() throws IOException {

        String filename = System.currentTimeMillis() + AUDIO_RECORDER_FILE_EXT_WAV;
        File output = new File(getPath(),filename);

        Log.e("RECORDER","Recording saved to "+output.getAbsolutePath().toString());

        saveRawFileToWav(getTempFile(), output);
        deleteTempFile();
        return output;
    }

    @Override
    public void release() {
        reset();
    }

    private File getTempFile() {
        File file = new File(getPath(),AUDIO_RECORDER_TEMP_FILE);
        return file;
    }

    public File saveRawFileToWav(File srcRaw, File dst) throws IOException {

        InputStream in = new FileInputStream(srcRaw);
        OutputStream out = new FileOutputStream(dst);

        //first write header
        writeWaveFileHeader(new DataOutputStream(out));

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();

        return dst;
    }

    private File getPath() {
        String baseDir = Environment.getExternalStorageDirectory().getPath();
        File storageDir = new File(baseDir,AUDIO_RECORDER_FOLDER);
        //create dir if non existent
        storageDir.mkdirs();
        return storageDir;
    }

    private void writeAudioDataToFile(RandomAccessFile fileWriter){

        while (recorder.read(buffer, 0, buffer.length) > 0) {


            callback.onNewData(Utils.getMaxAbs(buffer));

            // write buffer to file
            try {
                byte[] byteBuffer = short2byte(buffer);
                fileWriter.write(byteBuffer);
            } catch (IOException e) {
                e.printStackTrace();
            }

            bytesRecorded += buffer.length*2; //each short contains two bytes
        }

        // release hardware
        recorder.release();

        recordingThread = null;

        Log.e(LOG_TAG, "Recorded " + bytesRecorded + " bytes");
    }

    private byte[] short2byte(short[] sData) {
        int shortArrsize = sData.length;
        byte[] bytes = new byte[shortArrsize * 2];
        for (int i = 0; i < shortArrsize; i++) {
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;

    }

    private void deleteTempFile() {
        File file = getTempFile();
        if (file.exists())
            file.delete();
    }

    private void writeWaveFileHeader(DataOutputStream fileWriter) throws IOException {

        short bSamples;
        if (RECORDER_AUDIO_ENCODING == AudioFormat.ENCODING_PCM_16BIT) {
            bSamples = 16;
        } else {
            bSamples = 8;
        }

        short nChannels;
        if (RECORDER_CHANNELS == AudioFormat.CHANNEL_IN_MONO) {
            nChannels = 1;
        } else {
            nChannels = 2;
        }

        int sRate = sampleRate;

        fileWriter.writeBytes("RIFF");
        fileWriter.writeInt(Integer.reverseBytes(36 + bytesRecorded)); // Final file size not known yet, write 0
        fileWriter.writeBytes("WAVE");
        fileWriter.writeBytes("fmt ");
        fileWriter.writeInt(Integer.reverseBytes(16)); // Sub-chunk size, 16 for PCM
        fileWriter.writeShort(Short.reverseBytes((short) 1)); // AudioFormat, 1 for PCM
        fileWriter.writeShort(Short.reverseBytes(nChannels));// Number of channels, 1 for mono, 2 for stereo
        fileWriter.writeInt(Integer.reverseBytes(sRate)); // Sample rate
        fileWriter.writeInt(Integer.reverseBytes(sRate * bSamples * nChannels / 8)); // Byte rate, SampleRate*NumberOfChannels*BitsPerSample/8
        fileWriter.writeShort(Short.reverseBytes((short) (nChannels * bSamples / 8))); // Block align, NumberOfChannels*BitsPerSample/8
        fileWriter.writeShort(Short.reverseBytes(bSamples)); // Bits per sample
        fileWriter.writeBytes("data");
        fileWriter.writeInt(Integer.reverseBytes(bytesRecorded)); // Data chunk size not known yet, write 0
    }

    public ArrayList<Integer> getValidSampleRates() {

        int sampleRates[] = {8000, 11025, 16000, 22050, 44100};
        if (Utils.isEmulator())
            sampleRates = new int[] { 16000 };

        ArrayList<Integer> passedRates =new ArrayList<Integer>();

        for (int rate : sampleRates) {  // add the rates you wish to check against
            int bufferSize = AudioRecord.getMinBufferSize(rate, RECORDER_CHANNELS , RECORDER_AUDIO_ENCODING);
            if (bufferSize > 0) {
                passedRates.add(rate);
            }
        }
        return passedRates;
    }
}
