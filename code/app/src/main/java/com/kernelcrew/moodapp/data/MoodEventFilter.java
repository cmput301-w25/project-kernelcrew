package com.kernelcrew.moodapp.data;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class MoodEventFilter {
    private final CollectionReference collectionReference;
    private final Set<Emotion> emotions = new HashSet<>();
    private String userId;
    private Date startDate;
    private Date endDate;
    private String sortField;
    private Query.Direction sortDirection;
    private Double latitude;
    private Double longitude;
    private Double radius;

    /**
     * Constructor accepting a CollectionReference directly.
     * This is useful if you have the Firestore collection from your provider.
     */
    public MoodEventFilter(CollectionReference collectionReference) {
        this.collectionReference = collectionReference;
    }

    /**
     * Constructor accepting a MoodEventProvider.
     * This uses the provider's underlying collection reference.
     */
    public MoodEventFilter(MoodEventProvider provider) {
        this.collectionReference = provider.getCollectionReference();
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
    public MoodEventFilter addEmotions(Set<Emotion> emotions) {
        this.emotions.addAll(emotions);
        return this;
    }

    public MoodEventFilter setEmotions(Set<Emotion> emotions) {
        this.emotions.clear();
        this.emotions.addAll(emotions);
        return this;
    }

    public MoodEventFilter setLocation(Double latitude, Double longitude, double radius) {
        if (latitude == null || longitude == null || radius <= 0) {
            // Clear location filter if parameters are invalid
            this.latitude = null;
            this.longitude = null;
            this.radius = null;
        } else {
            this.latitude = latitude;
            this.longitude = longitude;
            this.radius = radius;
        }
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
        if (startDate != null && endDate != null && startDate.after(endDate)) {
            throw new IllegalArgumentException("Start date must not be after end date.");
        }

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
        if (field == null || field.isBlank()) {
            throw new IllegalArgumentException("Sort field must be a non-empty string.");
        }
        if (direction == null) {
            throw new IllegalArgumentException("Sort direction must not be null.");
        }
        this.sortField = field;
        this.sortDirection = direction;
        return this;
    }

    /**
     * Set a user to filter by (ie if you are on a persons profile you get only their posts).
     *
     * @param userId The user you want to filter by.
     *
     * @return Current instance.
     */
    public MoodEventFilter setUser(String userId) {
        this.userId = userId;
        return this;
    }

    public int count() {
        int c = 0;

        if (userId != null)
            ++c;

        if (!emotions.isEmpty())
            ++c;

        if (startDate != null || endDate != null)
            ++c;

        if (sortField != null)
            ++c;

        if (longitude != null || latitude != null || radius != null)
            ++c;

        return c;
    }

    /**
     * Clears all applied filters.
     *
     * @return Current instance.
     */
    public MoodEventFilter clearFilters() {
        userId = null;
        emotions.clear();
        startDate = null;
        endDate = null;
        sortField = null;
        sortDirection = null;
        latitude = null;
        longitude = null;
        radius = null;
        return this;
    }

    /**
     * Returns a summary of the currently applied filters.
     *
     * @return A summary string.
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        if (userId != null) {
            sb.append("User: ").append(userId).append("\n");
        }
        if (!emotions.isEmpty()) {
            sb.append("Emotions: ").append(emotions.toString()).append("\n");
        }
        if (startDate != null) {
            sb.append("Start Date: ").append(startDate).append("\n");
        }
        if (endDate != null) {
            sb.append("End Date: ").append(endDate).append("\n");
        }
        if (sortField != null) {
            sb.append("Sort: ").append(sortField).append(" (").append(sortDirection).append(")\n");
        }
        if (sb.length() == 0) {
            sb.append("No filters applied.");
        }
        return sb.toString();
    }

    /**
     * Builds a Firestore Query using the applied filters and sort orders.
     * This query can then be used by a provider (calling .get() or adding a snapshot listener).
     *
     * @return A query with applied filtering and sorting.
     */
    public Query buildQuery() {
        // Start with the collection reference
        Query query = collectionReference;

        if (userId != null) {
            query = query.whereEqualTo("uid", userId);
        }

        if (!emotions.isEmpty()) {
            query = query.whereIn("emotion", new ArrayList<>(emotions));
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

        if (latitude != null && longitude != null && radius != null) {
            // Taha used the following resources,
            // https://en.wikipedia.org/wiki/Great-circle_distance
            // https://stackoverflow.com/questions/27928/calculate-distance-between-two-latitude-longitude-points-haversine-formula
            // https://stackoverflow.com/questions/15372705/calculating-a-radius-with-longitude-and-latitude
            // Calculate bounding box for the given center and radius.
            double earthRadius = 6371.0;
            double latDelta = Math.toDegrees(radius / earthRadius);
            double lonDelta = Math.toDegrees(radius / (earthRadius * Math.cos(Math.toRadians(latitude))));

            // Getting the max/mins
            double minLat = latitude - latDelta;
            double maxLat = latitude + latDelta;
            double minLon = longitude - lonDelta;
            double maxLon = longitude + lonDelta;

            // Build Query
            query = query.whereGreaterThanOrEqualTo("latitude", minLat)
                    .whereLessThanOrEqualTo("latitude", maxLat)
                    .whereGreaterThanOrEqualTo("longitude", minLon)
                    .whereLessThanOrEqualTo("longitude", maxLon);
        }

        return query;
    }
}

