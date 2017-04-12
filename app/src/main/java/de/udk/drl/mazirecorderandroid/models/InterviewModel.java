package de.udk.drl.mazirecorderandroid.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by lutz on 11/04/17.
 */

public class InterviewModel {

    public int _id = -1;
    public String name = "";
    public String role = "";
    public String text = "";
    public ArrayList<AttachmentModel> attachments = new ArrayList<>();
    public String imageFile = "null";

    public String serverId = null;

    @Override
    public boolean equals(Object object)
    {
        if (object != null && object instanceof InterviewModel)
        {
            InterviewModel model = (InterviewModel) object;
            return this._id == model._id;
        }
        return false;
    }

}
