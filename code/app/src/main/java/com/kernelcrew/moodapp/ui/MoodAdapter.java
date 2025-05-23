package com.kernelcrew.moodapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.kernelcrew.moodapp.R;
import com.kernelcrew.moodapp.data.MoodEvent;
import com.kernelcrew.moodapp.data.UserProvider;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MoodAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // Define view types for non-empty and empty states.
    private static final int VIEW_TYPE_EMPTY = 0;
    private static final int VIEW_TYPE_ITEM = 1;

    private List<MoodEvent> moods = new ArrayList<>();
    private OnMoodClickListener onMoodClickListener;
    private UserProvider userProvider;

    public MoodAdapter() {
        this.userProvider = UserProvider.getInstance();
    }

    // Callback interface to handle clicks on the "View Details" button.
    public interface OnMoodClickListener {
        void onViewDetails(MoodEvent mood);
        void onViewComments(MoodEvent mood);
    }

    public void setOnMoodClickListener(OnMoodClickListener listener) {
        this.onMoodClickListener = listener;
    }

    public void setMoods(List<MoodEvent> moods) {
        this.moods = (moods == null) ? new ArrayList<>() : moods;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (moods == null || moods.isEmpty()) {
            return VIEW_TYPE_EMPTY;
        }
        return VIEW_TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_EMPTY) {
            TextView emptyView = new TextView(parent.getContext());
            emptyView.setLayoutParams(new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            emptyView.setText("There is nothing here!");
            emptyView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            int padding = (int) (16 * parent.getContext().getResources().getDisplayMetrics().density);
            emptyView.setPadding(padding, padding, padding, padding);
            return new RecyclerView.ViewHolder(emptyView) {};
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_mood, parent, false);
            return new MoodViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_EMPTY) {
            return;
        }
        MoodEvent mood = moods.get(position);
        MoodViewHolder viewHolder = (MoodViewHolder) holder;

        viewHolder.moodTypeTextView.setText(mood.getEmotion().toString());

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE ha", Locale.getDefault());
        String formattedTime = dateFormat.format(mood.getCreated());
        viewHolder.dayTimeTextView.setText(formattedTime);

        int iconRes = MoodIconUtil.getMoodIconResource(mood.getEmotion().toString());
        viewHolder.moodImageView.setImageResource(iconRes);

        UserProvider.getInstance().fetchUsername(mood.getUid())
                .addOnSuccessListener(username -> {
                    viewHolder.usernameText.setText("@" + username);

                    // Replace requireView with v in the click callback
                    viewHolder.usernameText.setOnClickListener(v -> {
                        Bundle args = new Bundle();
                        args.putString("uid", mood.getUid());
                        // Use Navigation.findNavController(...) with the clicked view
                        Navigation.findNavController(v)
                                .navigate(R.id.otherUserProfile, args);
                    });
                })
                .addOnFailureListener(e -> {
                    viewHolder.usernameText.setText(R.string.error_loading_user);
                });

        viewHolder.viewDetailsButton.setOnClickListener(v -> {
            if (onMoodClickListener != null) {
                onMoodClickListener.onViewDetails(mood);
            }
        });

        // Set click listener for the comment layout
        viewHolder.commentLayout.setOnClickListener(v -> {
            if (onMoodClickListener != null) {
                onMoodClickListener.onViewComments(mood);
            }
        });

        switch (mood.getVisibility()) {
            case PUBLIC:
                viewHolder.visibilityIcon.setImageResource(R.drawable.ic_public);
                break;
            case PRIVATE:
                viewHolder.visibilityIcon.setImageResource(R.drawable.ic_lock);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return (moods == null || moods.isEmpty()) ? 1 : moods.size();
    }

    public static class MoodViewHolder extends RecyclerView.ViewHolder {
        ImageView moodImageView;
        TextView moodTypeTextView;
        TextView dayTimeTextView;
        View viewDetailsButton; // Reference to the "View Details" button
        TextView usernameText;

        ImageView purple_icon_arrow;
        ImageView comments_bubble;
        View commentLayout;

        ImageView visibilityIcon;

        public MoodViewHolder(@NonNull View itemView) {
            super(itemView);
            moodImageView = itemView.findViewById(R.id.moodImage);
            moodTypeTextView = itemView.findViewById(R.id.moodTypeText);
            dayTimeTextView = itemView.findViewById(R.id.dayTimeText);
            viewDetailsButton = itemView.findViewById(R.id.viewDetailsButton);
            usernameText = itemView.findViewById(R.id.usernameText);
            purple_icon_arrow = itemView.findViewById(R.id.purpleIconArrow);
            comments_bubble = itemView.findViewById(R.id.comments_bubble);
            commentLayout = itemView.findViewById(R.id.commentLayout);
            visibilityIcon = itemView.findViewById(R.id.visibility_icon);
        }
    }
}
