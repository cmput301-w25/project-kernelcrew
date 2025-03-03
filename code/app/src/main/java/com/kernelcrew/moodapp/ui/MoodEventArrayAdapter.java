package com.kernelcrew.moodapp.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kernelcrew.moodapp.R;

import java.util.ArrayList;

public class MoodEventArrayAdapter extends ArrayAdapter<MoodEvent> {
    private Context context;
    private ArrayList<MoodEvent> moodEvents;

    public MoodEventArrayAdapter(Context context, ArrayList<MoodEvent> moodEvents) {
        super(context, 0, moodEvents);
        this.context = context;
        this.moodEvents = moodEvents;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_mood_history_mood, parent, false);
        }

        MoodEvent moodEvent = moodEvents.get(position);

        // Convert month number to abbreviation
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        String formattedDate = months[moodEvent.getMonth() - 1] + " " + moodEvent.getDay() + ", " + moodEvent.getYear();

        // Set the formatted date
        TextView textDate = convertView.findViewById(R.id.textDate);
        textDate.setText(formattedDate);

        TextView textMoodEventNumber = convertView.findViewById(R.id.textMoodEventNumber);
        textMoodEventNumber.setText("Mood Event " + moodEvent.getMoodEventNumber());

        return convertView;
    }
}
