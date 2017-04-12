package de.udk.drl.mazirecorderandroid.models;

import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;

import io.reactivex.Observable;

/**
 * Created by lutz on 11/04/17.
 */

public class QuestionStorage {

    private static QuestionStorage instance = null;

    private SharedPreferences storage;
    public static final String QUESTION_STORAGE_ITEM = "questions";

    public ArrayList<QuestionModel> questions;

    protected QuestionStorage(SharedPreferences storage) {
        questions = new ArrayList<>();
        this.storage = storage;
        loadFromStorage();

    }

    public static QuestionStorage getInstance() {
        if (instance == null) {
            throw new Error("Class need to be intantiated with shared preferences Object");
        }
        return instance;
    }

    public static void createInstance(SharedPreferences storage) {
        if (instance == null) {
            instance = new QuestionStorage(storage);
        }
    }

    public ArrayList<QuestionModel> getAll() {
        return questions;
    }

    public void add(QuestionModel question) {
        questions.add(question);
        saveToStorage();
    }

    public void delete(int index) {
        questions.remove(index);
        saveToStorage();
    }

    private void saveToStorage() {
        SharedPreferences.Editor prefsEditor = storage.edit();
        Gson gson = new Gson();
        String json = gson.toJson(questions.toArray());
        prefsEditor.putString(QUESTION_STORAGE_ITEM, json);
        prefsEditor.commit();
    }

    private void loadFromStorage() {
        // load from storage
        if (storage.contains(QUESTION_STORAGE_ITEM)) {
            Gson gson = new Gson();
            String json = storage.getString(QUESTION_STORAGE_ITEM, "");
            QuestionModel[] models = gson.fromJson(json, QuestionModel[].class);
            questions = new ArrayList<>(Arrays.asList(models));
        }
    }
}

