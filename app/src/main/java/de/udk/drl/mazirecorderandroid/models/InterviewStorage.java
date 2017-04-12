package de.udk.drl.mazirecorderandroid.models;

/**
 * Created by lutz on 11/04/17.
 */

public class InterviewStorage {

    private static InterviewStorage instance = null;

    public InterviewModel interview;

    protected InterviewStorage() {

        interview = new InterviewModel();
    }

    public static InterviewStorage getInstance() {
        if(instance == null) {
            instance = new InterviewStorage();
        }
        return instance;
    }

    public void load() {

    }

    public void save() {

    }
}
