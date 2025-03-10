/**
 * //Anthropic, Claude, "Generate source code descriptive comments for 301 rubric", 03-10-2025
 *
 * This adapter manages the display of mood events in the home feed.
 * It binds mood event data to list item views and handles the
 * presentation of mood types, icons, timestamps, and interaction events.
 * Enables viewing detailed information about each mood through click listeners.
 */
package com.kernelcrew.moodapp.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kernelcrew.moodapp.R;
import com.kernelcrew.moodapp.data.MoodEvent;

import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class MoodAdapter extends RecyclerView.Adapter<MoodAdapter.MoodViewHolder> {

    private List<MoodEvent> moods = new ArrayList<>();

    private OnMoodClickListener onMoodClickListener;

    // Callback interface to handle clicks on the "View Details" button
    public interface OnMoodClickListener {
        void onViewDetails(MoodEvent mood);
    }

    public void setOnMoodClickListener(OnMoodClickListener listener) {
        this.onMoodClickListener = listener;
    }

    public void setMoods(List<MoodEvent> moods) {
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
        MoodEvent mood = moods.get(position);

        // Display the mood text in the top TextView
        holder.moodTypeTextView.setText(mood.getEmotion().toString());

        // Format the timestamp for the second TextView(Ex: "Thu 10am")
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE ha", Locale.getDefault());
        String formattedTime = dateFormat.format(mood.getCreated());
        holder.dayTimeTextView.setText(formattedTime);

        // Set the mood image using MoodIconUtil
        int iconRes = MoodIconUtil.getMoodIconResource(mood.getEmotion().toString());
        holder.moodImageView.setImageResource(iconRes);

        // Set click listener for the "View Details" button
        holder.viewDetailsButton.setOnClickListener(v -> {
            if (onMoodClickListener != null) {
                onMoodClickListener.onViewDetails(mood);
            }
        });
    }

    @Override
    public int getItemCount() {
        return moods.size();
    }

    public static class MoodViewHolder extends RecyclerView.ViewHolder {
        ImageView moodImageView; // new
        TextView moodTypeTextView;
        TextView dayTimeTextView;
        View viewDetailsButton; // Reference to the "View Details" button

        public MoodViewHolder(@NonNull View itemView) {
            super(itemView);
            moodImageView = itemView.findViewById(R.id.moodImage); // initialize new view
            moodTypeTextView = itemView.findViewById(R.id.moodTypeText);
            dayTimeTextView = itemView.findViewById(R.id.dayTimeText);
            viewDetailsButton = itemView.findViewById(R.id.viewDetailsButton);
        }
    }
}
