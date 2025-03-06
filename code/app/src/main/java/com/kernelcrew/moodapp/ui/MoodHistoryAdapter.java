package com.kernelcrew.moodapp.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kernelcrew.moodapp.ui.Mood;
import com.kernelcrew.moodapp.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MoodHistoryAdapter extends RecyclerView.Adapter<MoodHistoryAdapter.MoodViewHolder> {
    private List<Mood> moods;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH);

    public interface OnItemClickListener {
        void onItemClick(String moodEventId);
    }

    private OnItemClickListener listener;

    public MoodHistoryAdapter(List<Mood> moods, OnItemClickListener listener) {
        this.moods = moods != null ? moods : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public MoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mood_history_mood, parent, false);
        return new MoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MoodViewHolder holder, int position) {
        Mood mood = moods.get(position);

        // Convert timestamp to formatted date
        String formattedDate = dateFormat.format(new Date(mood.getTimestamp()));

        holder.textDate.setText(formattedDate);
        holder.textMoodEventNumber.setText("Mood Event " + (position + 1));

        // Set click listener on the item view
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(mood.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return moods.size();
    }

    public List<Mood> getItems() {
        return new ArrayList<>(moods);
    }

    static class MoodViewHolder extends RecyclerView.ViewHolder {
        TextView textDate, textMoodEventNumber;

        public MoodViewHolder(@NonNull View itemView) {
            super(itemView);
            textDate = itemView.findViewById(R.id.textDate);
            textMoodEventNumber = itemView.findViewById(R.id.textMoodEventNumber);
        }
    }
}