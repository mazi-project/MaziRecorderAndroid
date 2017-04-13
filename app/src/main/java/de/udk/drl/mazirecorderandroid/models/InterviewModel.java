package de.udk.drl.mazirecorderandroid.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Date;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 * Created by lutz on 11/04/17.
 */

public class InterviewModel {

    public String name = "";
    public String role = "";
    public String text = "";
    public ArrayList<AttachmentModel> attachments = new ArrayList<>();
    public String imageFile = "";

    public String serverId = null;
    public boolean isNew = true;
}
