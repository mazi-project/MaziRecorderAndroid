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

    public String _id;
    public Date creationDate;
    public String name = "";
    public String role;
    public String text;
    public ArrayList<AttachmentModel> attachments;
    public Uri imageUrl;
}
