package de.udk.drl.mazirecorderandroid.models;

import android.content.SharedPreferences;

import com.google.gson.Gson;

import org.reactivestreams.Subscription;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 * Created by lutz on 11/04/17.
 */

public class InterviewStorage extends Observable<InterviewModel> {

    private static InterviewStorage instance = null;

    private SharedPreferences storage;
    public static final String INTERVIEW_STORAGE_ITEM = "interview";
    public InterviewModel interview = null;

    Subject<InterviewModel> observable = BehaviorSubject.create();

    public InterviewStorage(SharedPreferences storage) {
        this.storage = storage;
        load();
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

    public void createNew() {
        interview = new InterviewModel();
    }

    public void save() {
        observable.onNext(interview);
        SharedPreferences.Editor prefsEditor = storage.edit();
        Gson gson = new Gson();
        String json = gson.toJson(interview);
        prefsEditor.putString(INTERVIEW_STORAGE_ITEM, json);
        prefsEditor.commit();

        interview.isNew = false;
    }

    public void load() {
        // load from storage
        if (storage.contains(INTERVIEW_STORAGE_ITEM)) {
            Gson gson = new Gson();
            String json = storage.getString(INTERVIEW_STORAGE_ITEM, "");
            interview = gson.fromJson(json, InterviewModel.class);
        } else {
            interview = new InterviewModel();
        }
        observable.onNext(interview);

    }

    @Override
    protected void subscribeActual(Observer<? super InterviewModel> observer) {
        observable.subscribe(observer);
    }


}
