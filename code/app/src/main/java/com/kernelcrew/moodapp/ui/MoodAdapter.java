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
        holder.moodTypeTextView.setText(mood.getUserName());
        holder.dayTimeTextView.setText(mood.getMoodText());
    }

    @Override
    public int getItemCount() {
        return moods.size();
    }

    public class MoodViewHolder extends RecyclerView.ViewHolder {
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
