package de.udk.drl.mazirecorderandroid.utils;

import android.graphics.Bitmap;
import android.os.Build;

import java.io.File;

/**
 * Created by lutz on 03/11/16.
 */
public class Utils {

    public static Bitmap createSquareBitmap(Bitmap bm, int size) {

        int width = bm.getWidth();
        int height = bm.getHeight();

        //first cut out square
        if (width > height) {
            double offset = (width - height)/2;
            bm = Bitmap.createBitmap(bm,(int)offset,0,height,height);
        } else if (height > width) {
            double offset = (height - width)/2;
            bm = Bitmap.createBitmap(bm,0,(int)offset,width,width);
        }

        //resize
        bm = Bitmap.createScaledBitmap(bm,size,size,false);
        return bm;
    }

    public static Bitmap scaleBitmap(Bitmap bm, int maxWidth, int maxHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();

        if (width > height) {
            // landscape
            float ratio = (float) width / maxWidth;
            width = maxWidth;
            height = (int)(height / ratio);
        } else if (height > width) {
            // portrait
            float ratio = (float) height / maxHeight;
            height = maxHeight;
            width = (int)(width / ratio);
        } else {
            // square
            height = maxHeight;
            width = maxWidth;
        }
        bm = Bitmap.createScaledBitmap(bm, width, height, true);
        return bm;
    }

    public static boolean fileExists(String path) {
        File file = new File(path);
        return file.exists();
    }

    public static boolean isEmulator() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT);
    }

    public static int getMaxAbs(short[] array) {
        int max = 0;
        for (int i=0;i<array.length; i++ ) {
            if (Math.abs(array[i]) > max)
                max = Math.abs(array[i]);
        }
        return max;
    }
}
