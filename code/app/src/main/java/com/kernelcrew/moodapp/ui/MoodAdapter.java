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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MoodAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<MoodEvent> moods = new ArrayList<>();

    private OnMoodClickListener onMoodClickListener;

    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_EMPTY = 1;

    public interface OnMoodClickListener {
        void onViewDetails(MoodEvent mood);
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

    @Override
    public int getItemCount() {
        return (moods == null || moods.isEmpty()) ? 1 : moods.size();
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

        viewHolder.viewDetailsButton.setOnClickListener(v -> {
            if (onMoodClickListener != null) {
                onMoodClickListener.onViewDetails(mood);
            }
        });
    }

    public static class MoodViewHolder extends RecyclerView.ViewHolder {
        ImageView moodImageView;
        TextView moodTypeTextView;
        TextView dayTimeTextView;
        View viewDetailsButton;

        public MoodViewHolder(@NonNull View itemView) {
            super(itemView);
            moodImageView = itemView.findViewById(R.id.moodImage);
            moodTypeTextView = itemView.findViewById(R.id.moodTypeText);
            dayTimeTextView = itemView.findViewById(R.id.dayTimeText);
            viewDetailsButton = itemView.findViewById(R.id.viewDetailsButton);
        }
    }
}
