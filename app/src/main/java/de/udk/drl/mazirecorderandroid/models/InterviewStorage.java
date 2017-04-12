package de.udk.drl.mazirecorderandroid.models;

import android.content.SharedPreferences;

import com.google.gson.Gson;

/**
 * Created by lutz on 11/04/17.
 */

public class InterviewStorage {

    private static InterviewStorage instance = null;

    private SharedPreferences storage;
    public static final String INTERVIEW_STORAGE_ITEM = "interview";
    public InterviewModel interview = new InterviewModel();

    public InterviewStorage(SharedPreferences storage) {
        this.storage = storage;
        loadFromStorage();
    }


    public static InterviewStorage getInstance() {
        if (instance == null) {
            throw new Error("Class need to be intantiated with shared preferences Object");
        }
        return instance;
    }

    public static void createInstance(SharedPreferences storage) {
        if (instance == null) {
            instance = new InterviewStorage(storage);
        }
    }

    public void save() {
        saveToStorage();
    }

    private void saveToStorage() {
        SharedPreferences.Editor prefsEditor = storage.edit();
        Gson gson = new Gson();
        String json = gson.toJson(interview);
        prefsEditor.putString(INTERVIEW_STORAGE_ITEM, json);
        prefsEditor.commit();
    }

    private void loadFromStorage() {
        // load from storage
        if (storage.contains(INTERVIEW_STORAGE_ITEM)) {
            Gson gson = new Gson();
            String json = storage.getString(INTERVIEW_STORAGE_ITEM, "");
            interview = gson.fromJson(json, InterviewModel.class);
        }
    }
}
