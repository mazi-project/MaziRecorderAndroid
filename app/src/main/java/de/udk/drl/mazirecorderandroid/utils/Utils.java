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
}
