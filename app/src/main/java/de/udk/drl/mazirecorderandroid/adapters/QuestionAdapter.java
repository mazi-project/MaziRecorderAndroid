package de.udk.drl.mazirecorderandroid.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.udk.drl.mazirecorderandroid.R;
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
        View view = LayoutInflater.from(this.getContext()).inflate(resourceId, parent, false);

        TextView textView = (TextView) view.findViewById(R.id.question_text);
        textView.setText(this.getItem(position).text);

        // supply position for deletion
        View button = view.findViewById(R.id.question_delete_button);
        button.setTag(position);

        //view.findViewById(R.id.question_delete_button).setTag(VIEW_TAG_INDEX,position);


        return view;
    }
}
