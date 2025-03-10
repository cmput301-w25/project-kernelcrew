package com.kernelcrew.moodapp.data;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MoodEventFilter {
    private final CollectionReference collectionReference;
    private final List<Emotion> emotions;
    private Date startDate;
    private Date endDate;
    private String sortField;
    private Query.Direction sortDirection;

    /**
     * Constructor accepting a CollectionReference directly.
     * This is useful if you have the Firestore collection from your provider.
     */
    public MoodEventFilter(CollectionReference collectionReference) {
        this.collectionReference = collectionReference;
        this.emotions = new ArrayList<>();
    }

    /**
     * Constructor accepting a MoodEventProvider.
     * This uses the provider's underlying collection reference.
     */
    public MoodEventFilter(MoodEventProvider provider) {
        this.collectionReference = provider.getCollectionReference();
        this.emotions = new ArrayList<>();
    }

    /**
     * Add an emotion.
     *
     * @param emotion The emotion to filter by.
     * @return Current instance.
     */
    public MoodEventFilter addEmotion(Emotion emotion) {
        this.emotions.add(emotion);
        return this;
    }

    /**
     * Add multiple emotions.
     *
     * @param emotions The list of emotions to filter by.
     * @return Current instance.
     */
    public MoodEventFilter addEmotions(List<Emotion> emotions) {
        this.emotions.addAll(emotions);
        return this;
    }

    /**
     * Sets the date range filter by providing both the start and end dates.
     *
     * @param startDate The lower bound (inclusive) for the "created" date (make null to ignore).
     * @param endDate   The upper bound (inclusive) for the "created" date (make null to ignore).
     *
     * @return Current instance.
     */
    public MoodEventFilter setDateRange(@Nullable Date startDate, @Nullable Date endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
        return this;
    }

    /**
     * Set a sort field.
     *
     * @param field     The document field to sort by (e.g., "created").
     * @param direction Query.Direction.ASCENDING or Query.Direction.DESCENDING
     *
     * @return Current instance.
     */
    public MoodEventFilter setSortField(String field, Query.Direction direction) {
        this.sortField = field;
        this.sortDirection = direction;
        return this;
    }

    /**
     * Builds a Firestore Query using the applied filters and sort orders.
     * This query can then be used by a provider (calling .get() or adding a snapshot listener).
     *
     * @return A query with applied filtering and sorting.
     */
    public Query buildQuery() {
        Query query = collectionReference;

        // Filter by emotions if provided.
        if (emotions != null && !emotions.isEmpty()) {
            List<String> emotionStrings = new ArrayList<>();
            for (Emotion emotion : emotions) {
                emotionStrings.add(emotion.name());
            }
            query = query.whereIn("emotion", emotionStrings);
        }

        if (startDate != null) {
            query = query.whereGreaterThanOrEqualTo("created", startDate);
        }

        if (endDate != null) {
            query = query.whereLessThanOrEqualTo("created", endDate);
        }

        if (sortField != null) {
            query = query.orderBy(sortField, sortDirection);
        }
        return query;
    }
}

