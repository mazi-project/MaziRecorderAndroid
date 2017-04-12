package de.udk.drl.mazirecorderandroid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import de.udk.drl.mazirecorderandroid.models.InterviewStorage;
import de.udk.drl.mazirecorderandroid.models.QuestionStorage;
import io.reactivex.Observable;

import de.udk.drl.mazirecorderandroid.R;

public class MainActivity extends BaseActivity {


    public InterviewStorage interviewStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        QuestionStorage.createInstance(getPreferences(MODE_PRIVATE));
        interviewStorage = InterviewStorage.getInstance();
    }

    public void onStartButtonClicked(View view) {

        interviewStorage.interview.name = ((TextView)findViewById(R.id.edit_text_name)).getText().toString();
        interviewStorage.interview.role = ((TextView)findViewById(R.id.edit_text_role)).getText().toString();

        Intent intent = new Intent(this, QuestionListActivity.class);
        startActivity(intent);
    }
}
