package de.udk.drl.mazirecorderandroid.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.webkit.MimeTypeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

    public static List<JSONObject> jsonArrayToList(JSONArray array) throws JSONException {
        List<JSONObject> list = new ArrayList<>();
        for(int i=0; i < array.length(); i++ ) {
            list.add(array.getJSONObject(i));
        }
        return list;
    }

    public static String getMimeType(Context context, Uri uri) {
        String mimeType = null;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = context.getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }
}
