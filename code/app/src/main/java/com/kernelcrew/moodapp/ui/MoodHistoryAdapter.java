package com.kernelcrew.moodapp.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kernelcrew.moodapp.data.MoodEvent;
import com.kernelcrew.moodapp.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * Adapter class for binding mood event data to the RecyclerView in the mood history screen.
 * <p>
 * This adapter manages the display of mood events in a list format. It shows the date
 * and sequential numbering of each mood event. It also handles click events on items
 * through the {@link OnItemClickListener} interface.
 *
 */
public class MoodHistoryAdapter extends RecyclerView.Adapter<MoodHistoryAdapter.MoodViewHolder> {
    /** List of mood events to display */
    private List<MoodEvent> moods;

    /** Date formatter for displaying the mood event creation date */
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH);

    /**
     * Interface for handling click events on mood items in the RecyclerView.
     * <p>
     * Implementing classes can receive callbacks when a user taps on a mood event.
     * </p>
     */
    public interface OnItemClickListener {
        void onItemClick(String moodEventId);
    }

    /** Listener for item click events */
    private OnItemClickListener listener;

    /**
     * Constructs a new MoodHistoryAdapter with the specified data and listener.
     *
     * @param moods List of mood events to display; if null, an empty list is used
     * @param listener Callback for item click events
     */
    public MoodHistoryAdapter(List<MoodEvent> moods, OnItemClickListener listener) {
        this.moods = moods != null ? moods : new ArrayList<>();
        this.listener = listener;
    }

    /**
     * Updates the adapter's data set with a new list of mood events.
     * <p>
     * Replaces the existing list of mood events and notifies the adapter to
     * refresh the RecyclerView.
     * </p>
     *
     * @param moods New list of mood events to display
     */
    public void setMoods(List<MoodEvent> moods) {
        this.moods = moods;
        notifyDataSetChanged();
    }


    /**
     * Creates a new ViewHolder when needed by the RecyclerView.
     * <p>
     * Inflates the mood item layout and wraps it in a ViewHolder.
     * </p>
     *
     * @param parent The ViewGroup into which the new View will be added
     * @param viewType The view type of the new View
     * @return A new ViewHolder that holds the View for each mood item
     */
    @NonNull
    @Override
    public MoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mood_history_mood, parent, false);
        return new MoodViewHolder(view);
    }


    /**
     * Binds data to the ViewHolder at the specified position.
     * <p>
     * Sets the date and event number text, and configures the click listener
     * for the item view.
     * </p>
     *
     * @param holder The ViewHolder to bind data to
     * @param position The position of the item in the data set
     */
    @Override
    public void onBindViewHolder(@NonNull MoodViewHolder holder, int position) {
        MoodEvent mood = moods.get(position);

        // Convert timestamp to formatted date
        String formattedDate = dateFormat.format(mood.getCreated());

        holder.textDate.setText(formattedDate);
        holder.textMoodEventNumber.setText("Mood Event " + (position + 1));

        // Set click listener on the item view
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(mood.getId());
            }
        });
    }


    /**
     * Returns the total number of items in the data set.
     *
     * @return The number of mood events in the list
     */
    @Override
    public int getItemCount() {
        return moods.size();
    }


    /**
     * Returns a copy of the current list of mood events.
     * <p>
     * Creates a new ArrayList to avoid exposing the internal list to modification.
     * </p>
     *
     * @return A copy of the current list of mood events
     */
    public List<MoodEvent> getItems() {
        return new ArrayList<>(moods);
    }


    /**
     * ViewHolder class for mood event items.
     * <p>
     * Holds references to the views within each mood item layout.
     * </p>
     */
    class MoodViewHolder extends RecyclerView.ViewHolder {
        TextView textDate, textMoodEventNumber;

        public MoodViewHolder(@NonNull View itemView) {
            super(itemView);
            textDate = itemView.findViewById(R.id.textDate);
            textMoodEventNumber = itemView.findViewById(R.id.textMoodEventNumber);
        }
    }
}