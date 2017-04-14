package de.udk.drl.mazirecorderandroid.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import de.udk.drl.mazirecorderandroid.R;
import de.udk.drl.mazirecorderandroid.models.AttachmentModel;
import de.udk.drl.mazirecorderandroid.models.InterviewStorage;
import de.udk.drl.mazirecorderandroid.models.QuestionModel;

/**
 * Created by lutz on 11/04/17.
 */

public class QuestionAdapter extends ArrayAdapter<QuestionModel> {

    private int resourceId;

    private ArrayList<QuestionModel> questions;

    public QuestionAdapter(Context context, int resource, List<QuestionModel> questions) {
        super(context, resource, questions);
        this.resourceId = resource;
    }

    @Override
    public View getView (int position, View convertView, ViewGroup parent) {

        String question = this.getItem(position).text;

        View view = LayoutInflater.from(this.getContext()).inflate(resourceId, parent, false);

        ArrayList<AttachmentModel> attachments = InterviewStorage.getInstance().interview.attachments;

        TextView textView = (TextView) view.findViewById(R.id.question_text);
        textView.setText(question);

        for (AttachmentModel model : attachments) {
            if (model.text.equals(question)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    textView.setTextAppearance(R.style.boldText);
                } else {
                    textView.setTextAppearance(parent.getContext(), R.style.boldText);
                }
            }
        }
        textView.setTag(question);

        // supply position for deletion
        View button = view.findViewById(R.id.question_delete_button);
        button.setTag(position);


        return view;
    }
}
