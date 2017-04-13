package de.udk.drl.mazirecorderandroid.models;

import android.content.SharedPreferences;

import com.google.gson.Gson;

/**
 * Created by lutz on 11/04/17.
 */

public class SharedPreferencesStorage<T> {

//    private static SharedPreferencesStorage instance = null;
//
//    private SharedPreferences storage;
//    private String storageString;
//
//    final Class<T> typeParameterClass;
//
//    public SharedPreferencesStorage(Class<T> typeParameterClass, SharedPreferences storage , String storageString) {
//        this.storage = storage;
//        this.storageString = storageString;
//        this.typeParameterClass = typeParameterClass;
//    }
//
//    public static SharedPreferencesStorage getInstance() {
//        if (instance == null) {
//            throw new Error("Class need to be intantiated with shared preferences Object");
//        }
//        return instance;
//    }
//
//    public void createInstance(Class<T> typeParameterClass, SharedPreferences storage, String storageString) {
//        if (instance == null) {
//            instance = new SharedPreferencesStorage(typeParameterClass, storage, storageString);
//        }
//    }
//
//    private void save(T model) {
//        SharedPreferences.Editor prefsEditor = storage.edit();
//        Gson gson = new Gson();
//        String json = gson.toJson(model);
//        prefsEditor.putString(storageString, json);
//        prefsEditor.commit();
//    }
//
//    private T load() {
//        // load from storage
//        if (storage.contains(storageString)) {
//            Gson gson = new Gson();
//            String json = storage.getString(storageString, "");
//            return gson.fromJson(json, typeParameterClass);
//        }
//    }
}
