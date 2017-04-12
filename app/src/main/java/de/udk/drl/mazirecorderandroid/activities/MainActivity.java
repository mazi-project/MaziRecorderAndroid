package de.udk.drl.mazirecorderandroid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import de.udk.drl.mazirecorderandroid.models.InterviewStorage;
import de.udk.drl.mazirecorderandroid.models.QuestionStorage;
import io.reactivex.*;

import de.udk.drl.mazirecorderandroid.R;

public class MainActivity extends BaseActivity {


    public InterviewStorage interviewStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // load data
        QuestionStorage.createInstance(getPreferences(MODE_PRIVATE));
        InterviewStorage.createInstance(getPreferences(MODE_PRIVATE));

        interviewStorage = InterviewStorage.getInstance();

        Button button = (Button) findViewById(R.id.statButton);

        ((TextView)findViewById(R.id.edit_text_name)).setText(interviewStorage.interview.name);
        ((TextView)findViewById(R.id.edit_text_role)).setText(interviewStorage.interview.role);
    }

    public void onStartButtonClicked(View view) {

        interviewStorage.interview.name = ((TextView)findViewById(R.id.edit_text_name)).getText().toString();
        interviewStorage.interview.role = ((TextView)findViewById(R.id.edit_text_role)).getText().toString();

        interviewStorage.save();

        Intent intent = new Intent(this, QuestionListActivity.class);
        startActivity(intent);
    }
}
