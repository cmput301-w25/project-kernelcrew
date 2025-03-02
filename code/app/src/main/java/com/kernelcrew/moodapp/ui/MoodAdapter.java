package com.kernelcrew.moodapp.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kernelcrew.moodapp.R;

import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MoodAdapter extends RecyclerView.Adapter<MoodAdapter.MoodViewHolder> {

    private List<Mood> moods = new ArrayList<>();

    public void setMoods(List<Mood> moods) {
        this.moods = moods;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mood, parent, false);
        return new MoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MoodViewHolder holder, int position) {
        Mood mood = moods.get(position);

        // 1. Display the mood text in the top TextView
        holder.moodTypeTextView.setText(mood.getMoodText());

        // 2. Format the timestamp for the second TextView(Ex: "Thu 10am")
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE ha", Locale.getDefault());
        String formattedTime = dateFormat.format(new Date(mood.getTimestamp()));

        // Optional: convert AM/PM to lowercase to match out figma mockup/design
        formattedTime = formattedTime.replace("AM", "am").replace("PM", "pm");

        holder.dayTimeTextView.setText(formattedTime);
    }

    @Override
    public int getItemCount() {
        return moods.size();
    }

    public static class MoodViewHolder extends RecyclerView.ViewHolder {
        TextView moodTypeTextView;
        TextView dayTimeTextView;

        public MoodViewHolder(@NonNull View itemView) {
            super(itemView);
            // Use the IDs defined in item_mood.xml
            moodTypeTextView = itemView.findViewById(R.id.moodTypeText);
            dayTimeTextView = itemView.findViewById(R.id.dayTimeText);
        }
    }
}
