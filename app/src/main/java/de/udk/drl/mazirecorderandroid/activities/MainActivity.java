package de.udk.drl.mazirecorderandroid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.jakewharton.rxbinding2.widget.RxTextView;

import de.udk.drl.mazirecorderandroid.models.InterviewModel;
import de.udk.drl.mazirecorderandroid.models.InterviewStorage;
import de.udk.drl.mazirecorderandroid.models.QuestionStorage;

import de.udk.drl.mazirecorderandroid.R;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;

public class MainActivity extends BaseActivity {

    public InterviewStorage interviewStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init storage
        QuestionStorage.createInstance(getPreferences(MODE_PRIVATE));
        InterviewStorage.createInstance(getPreferences(MODE_PRIVATE));

        // load data
        interviewStorage = InterviewStorage.getInstance();

        final Button continueButton = (Button) findViewById(R.id.continueButton);
        final Button newButton = (Button) findViewById(R.id.newButton);
        final EditText editTextName = (EditText) findViewById(R.id.edit_text_name);
        final EditText editTextRole = (EditText) findViewById(R.id.edit_text_role);

        // set up rx patterns
        final Observable<CharSequence> editNameObservable = RxTextView.textChanges(editTextName);
        Observable<CharSequence> editRoleObservable = RxTextView.textChanges(editTextRole);

        subscribers.add(
            Observable.combineLatest(editNameObservable, editRoleObservable,
                new BiFunction<CharSequence, CharSequence, Boolean>() {
                    @Override
                    public Boolean apply(CharSequence name, CharSequence role) throws Exception {
                        return name.length() >= MIN_INPUT_LENGTH && role.length() >= MIN_INPUT_LENGTH;
                    }
                }).subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean b) throws Exception {
                        continueButton.setEnabled(b);
                    }
                })
        );

        subscribers.add(
            interviewStorage.subscribe(new Consumer<InterviewModel>() {
                @Override
                public void accept(InterviewModel model) throws Exception {
                    if (model.isNew) {
                        continueButton.setText("START INTEFVIEW");
                    } else {
                        continueButton.setText("CONTINUE INTERVIEW");
                    }
                    newButton.setEnabled(!model.isNew);
                    editTextName.setEnabled(model.isNew);
                    editTextRole.setEnabled(model.isNew);

                    editTextName.setText(interviewStorage.interview.name);
                    editTextRole.setText(interviewStorage.interview.role);
                }
            })
        );

    }

    public void onNewButtonClicked(View view) {
        interviewStorage.createNew();
    }

    public void onContinueButtonClicked(View view) {

        interviewStorage.interview.name = ((TextView)findViewById(R.id.edit_text_name)).getText().toString();
        interviewStorage.interview.role = ((TextView)findViewById(R.id.edit_text_role)).getText().toString();
        interviewStorage.save();

        Intent intent = new Intent(this, QuestionListActivity.class);
        startActivity(intent);
    }
}
