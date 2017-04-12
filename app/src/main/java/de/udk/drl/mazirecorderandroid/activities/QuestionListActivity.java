package de.udk.drl.mazirecorderandroid.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import de.udk.drl.mazirecorderandroid.R;
import de.udk.drl.mazirecorderandroid.adapters.QuestionAdapter;
import de.udk.drl.mazirecorderandroid.models.QuestionModel;
import de.udk.drl.mazirecorderandroid.models.QuestionStorage;

public class QuestionListActivity extends AppCompatActivity {

    private QuestionStorage storage;
    private ListView questionListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_list);

        storage = QuestionStorage.getInstance();

        //setup listview
        questionListView = (ListView) findViewById(R.id.questionList);

        TextView emptyText = (TextView)findViewById(R.id.empty_text);
        questionListView.setEmptyView(emptyText);

        updateUi();
    }

    public void updateUi() {
        QuestionAdapter adapter = new QuestionAdapter(this, R.layout.item_question, storage.getAll());
        questionListView.setAdapter(adapter);
    }

    public void onFabButtonClicked(final View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Question");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                QuestionModel question = new QuestionModel();
                question.text = input.getText().toString();
                storage.add(question);
                updateUi();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

    }

    public void onItemDeleteButtonClicked(View view) {

        int index = (int)view.getTag();

        storage.delete(index);
        updateUi();

        Log.e("INFO","DELTE!");
    }
}
