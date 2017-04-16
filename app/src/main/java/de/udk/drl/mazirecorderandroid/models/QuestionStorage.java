package de.udk.drl.mazirecorderandroid.models;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;

import de.udk.drl.mazirecorderandroid.activities.BaseActivity;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;

/**
 * Created by lutz on 11/04/17.
 */

public class QuestionStorage extends Observable<ArrayList<QuestionModel>> {

    private static QuestionStorage instance = null;

    private SharedPreferences storage;
    public static final String QUESTION_STORAGE_ITEM = "questions";

    public ArrayList<QuestionModel> questions;

    Subject<ArrayList<QuestionModel>> observable = BehaviorSubject.create();

    protected QuestionStorage(SharedPreferences storage) {
        this.storage = storage;
        load();
    }

    public static QuestionStorage getInstance(Context context) {
        if (instance == null) {
            instance = new QuestionStorage(context.getSharedPreferences(BaseActivity.APP_STRING, Context.MODE_PRIVATE));
        }
        return instance;
    }

    public ArrayList<QuestionModel> getAll() {
        return questions;
    }

    public void add(QuestionModel question) {
        questions.add(question);
        save();
    }

    public void delete(int index) {
        questions.remove(index);
        save();
    }

    private void save() {
        SharedPreferences.Editor prefsEditor = storage.edit();
        Gson gson = new Gson();
        String json = gson.toJson(questions.toArray());
        prefsEditor.putString(QUESTION_STORAGE_ITEM, json);
        prefsEditor.commit();
        observable.onNext(questions);
    }

    private void load() {
        // load from storage
        if (storage.contains(QUESTION_STORAGE_ITEM)) {
            Gson gson = new Gson();
            String json = storage.getString(QUESTION_STORAGE_ITEM, "");
            QuestionModel[] models = gson.fromJson(json, QuestionModel[].class);
            questions = new ArrayList<>(Arrays.asList(models));
        } else {
            questions = new ArrayList<>();
        }
        observable.onNext(questions);
    }

    @Override
    protected void subscribeActual(Observer<? super ArrayList<QuestionModel>> observer) {
        observable.subscribe(observer);
    }
}

