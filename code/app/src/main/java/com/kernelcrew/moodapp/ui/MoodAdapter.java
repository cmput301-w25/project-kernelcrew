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
import com.kernelcrew.moodapp.data.UserProvider;

import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class MoodAdapter extends RecyclerView.Adapter<MoodAdapter.MoodViewHolder> {

    private List<MoodEvent> moods = new ArrayList<>();

    private OnMoodClickListener onMoodClickListener;
    private UserProvider userProvider;

    public MoodAdapter() {
        this.userProvider = UserProvider.getInstance();
    }

    // Callback interface to handle clicks on the "View Details" button
    public interface OnMoodClickListener {
        void onViewDetails(MoodEvent mood);
        void onViewComments(MoodEvent mood);
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

        holder.usernameText.setText(mood.getUsername());

        // Set click listener for the "View Details" button
        holder.viewDetailsButton.setOnClickListener(v -> {
            if (onMoodClickListener != null) {
                onMoodClickListener.onViewDetails(mood);
            }
        });

        // Set click listener for the comment layout
        holder.commentLayout.setOnClickListener(v -> {
            if (onMoodClickListener != null) {
                onMoodClickListener.onViewComments(mood);
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
        TextView usernameText;

        ImageView purple_icon_arrow;
        ImageView comments_bubble;
        View commentLayout;

        public MoodViewHolder(@NonNull View itemView) {
            super(itemView);
            moodImageView = itemView.findViewById(R.id.moodImage); // initialize new view
            moodTypeTextView = itemView.findViewById(R.id.moodTypeText);
            dayTimeTextView = itemView.findViewById(R.id.dayTimeText);
            viewDetailsButton = itemView.findViewById(R.id.viewDetailsButton);
            usernameText = itemView.findViewById(R.id.usernameText);
            purple_icon_arrow = itemView.findViewById(R.id.purpleIconArrow);
            comments_bubble = itemView.findViewById(R.id.comments_bubble);
            commentLayout = itemView.findViewById(R.id.commentLayout);
        }
    }
}
