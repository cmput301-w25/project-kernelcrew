package com.kernelcrew.moodapp.data;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.Query;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * A class for filtering mood events through Firestore, used to create queries.
 */
public class MoodEventFilter {
    private final CollectionReference collectionReference;
    private final Set<Emotion> emotions = new HashSet<>();
    private final Set<String> socialSituations = new HashSet<>();
    private final Set<String> userIds = new HashSet<>();
    private Date startDate;
    private Date endDate;
    private String sortField;
    private Query.Direction sortDirection;
    private Double latitude;
    private Double longitude;
    private Double radius;
    private Integer limit;
    private String searchQuery;
    private Query customQuery;

    /**
     * Constructor accepting a CollectionReference directly.
     * This is useful if you have the Firestore collection from your provider.
     *
     * @param collectionReference The Firestore collection reference for mood events.
     */
    public MoodEventFilter(CollectionReference collectionReference) {
        this.collectionReference = collectionReference;
    }

    /**
     * Constructor accepting a MoodEventProvider.
     * Uses the provider's underlying collection reference.
     *
     * @param provider The MoodEventProvider instance to get the collection reference.
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
    public MoodEventFilter addEmotions(Emotion emotion) {
        this.emotions.add(emotion);
        return this;
    }

    /**
     * Add multiple emotions.
     *
     * @param emotions The set of emotions to filter by.
     * @return Current instance.
     */
    public MoodEventFilter addEmotions(Set<Emotion> emotions) {
        this.emotions.addAll(emotions);
        return this;
    }

    /**
     * Sets the emotions filter, replacing any previously set emotions.
     *
     * @param emotions The new set of emotions.
     * @return The current instance of MoodEventFilter.
     */
    public MoodEventFilter setEmotions(Set<Emotion> emotions) {
        this.emotions.clear();
        this.emotions.addAll(emotions);
        return this;
    }

    /**
     * Sets a location filter for mood events.
     *
     * @param latitude  The latitude coordinate.
     * @param longitude The longitude coordinate.
     * @param radius    The radius in kilometers.
     * @return The current instance of MoodEventFilter.
     */
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
     * Set a user to filter by (replacing any previous user filters).
     *
     * @param userId The user you want to filter by.
     *
     * @return Current instance.
     */
    public MoodEventFilter setUsers(String userId) {
        this.userIds.clear();
        if (userId != null && !userId.isBlank()) {
            this.userIds.add(userId);
        }
        return this;
    }

    /**
     * Add a single user to filter by.
     *
     * @param userId The user ID to add.
     *
     * @return Current instance.
     */
    public MoodEventFilter addUsers(String userId) {
        if (userId != null && !userId.isBlank()) {
            this.userIds.add(userId);
        }
        return this;
    }

    /**
     * Add multiple users to filter by.
     *
     * @param userIds The set of user IDs to add.
     *
     * @return Current instance.
     */
    public MoodEventFilter addUsers(Set<String> userIds) {
        for (String uid : userIds) {
            if (uid != null && !uid.isBlank()) {
                this.userIds.add(uid);
            }
        }
        return this;
    }

    /**
     * Sets the users filter, replacing any previously set user filters.
     *
     * @param userIds The new set of user IDs.
     *
     * @return Current instance.
     */
    public MoodEventFilter setUsers(Set<String> userIds) {
        this.userIds.clear();
        for (String uid : userIds) {
            if (uid != null && !uid.isBlank()) {
                this.userIds.add(uid);
            }
        }
        return this;
    }

    /**
     * Add a limiter on the query
     * @param limit The amount to limit the results by.
     * @return Current Instance
     */
    public MoodEventFilter setLimit(int limit) {
        this.limit = limit;
        return this;
    }

    /**
     * Set a search query to filter mood events by their trigger field.
     *
     * @param query The text to search for.
     * @return The current instance.
     */
    public MoodEventFilter setSearchQuery(String query) {
        this.searchQuery = query;
        return this;
    }
    
    /**
     * Add a single social instance to filter by 
     * 
     * @param socialSituation The single instance to add
     * @return The current instance.
     * */
    public MoodEventFilter setSocialSituations(String socialSituation) {
        this.socialSituations.add(socialSituation);
        return this;
    }

    /**
     * Add a single social instance to filter by
     *
     * @param socialSituation The single instance to add
     * @return The current instance.
     * */
    public MoodEventFilter setSocialSituations(Set<String> socialSituation) {
        this.socialSituations.addAll(socialSituation);
        return this;
    }

    /**
     * A custom query setter
     *
     * @param customQuery The custom query to add
     * @return The current instance.
     */
    public MoodEventFilter setCustomQuery(Query customQuery) {
        this.customQuery = customQuery;
        return this;
    }

    // Getters
    public String getSearchQuery() {
        return searchQuery;
    }

    public Set<Emotion> getEmotions() {
        return this.emotions;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    /**
     * Counts the number of filters applied.
     *
     * @return The number of active filters.
     */
    public int count() {
        int c = 0;
        if (!userIds.isEmpty()) c++;
        if (!emotions.isEmpty()) c++;
        if (startDate != null || endDate != null) c++;
        if (sortField != null) c++;
        if (longitude != null || latitude != null || radius != null) c++;
        if (limit != null) c++;
        if (searchQuery != null) c++;
        if (!socialSituations.isEmpty()) c++;
        if (customQuery != null) c++;
        return c;
    }

    /**
     * Clears all applied filters.
     */
    public void clearFilters() {
        userIds.clear();
        emotions.clear();
        startDate = null;
        endDate = null;
        sortField = null;
        sortDirection = null;
        latitude = null;
        longitude = null;
        radius = null;
        limit = null;
        searchQuery = null;
        socialSituations.clear();
        customQuery = null;
    }

    /**
     * Returns a summary of the currently applied filters.
     *
     * @return A summary string.
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        if (userIds.isEmpty()) {
            sb.append("User: ").append(userIds).append("\n");
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
        if (searchQuery != null) {
            sb.append("Search Query: ").append(searchQuery).append("\n");
        }
        if (!socialSituations.isEmpty()) {
            sb.append("Social Situations: ").append(socialSituations.toString()).append("\n");
        }
        if (customQuery != null) {
            sb.append("Custom Query: ").append(customQuery.toString()).append("\n");
        }
        if (limit != null) {
            sb.append("Limit: ").append(limit).append("\n");
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
        if (customQuery != null) {
            return customQuery;
        }

        Query query = collectionReference;

        if (!userIds.isEmpty()) {
            if (userIds.size() == 1) {
                query = query.whereEqualTo("uid", userIds.iterator().next());
            } else {
                query = query.whereIn("uid", new ArrayList<>(userIds));
            }
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
            // https://www.movable-type.co.uk/scripts/latlong.html

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

        if (searchQuery != null && !searchQuery.isBlank()) {
            Filter triggerFilter = Filter.and(
                    Filter.greaterThanOrEqualTo("trigger", searchQuery),
                    Filter.lessThanOrEqualTo("trigger", searchQuery + "\uf8ff")
            );

            Filter reasonFilter = Filter.and(
                    Filter.greaterThanOrEqualTo("reason", searchQuery),
                    Filter.lessThanOrEqualTo("reason", searchQuery + "\uf8ff")
            );

            query = query.where(Filter.or(triggerFilter, reasonFilter));
        }

        if (!socialSituations.isEmpty()) {
            query = query.whereIn("socialSituation", new ArrayList<>(socialSituations));
        }

        if (limit != null) {
            query = query.limit(limit);
        }

        return query;
    }

    /**
     * Merges multiple MoodEventFilters into one.
     * For set fields (emotions, socialSituations) the values are unioned.
     * For scalar fields, the first non-null value is taken (if conflicting, behavior is undefined).
     *
     * @param filters An array of MoodEventFilter instances to merge.
     * @return A new MoodEventFilter representing the merged criteria.
     */
    // Taha used ChatGPT,
    // Here is my current MoodFilters, <insert file>, use the above to create a function that
    //      merges multiple filters.
    public static MoodEventFilter mergeFilters(MoodEventFilter... filters) {
        if (filters == null || filters.length == 0) {
            throw new IllegalArgumentException("At least one filter must be provided.");
        }

        MoodEventFilter merged = new MoodEventFilter(filters[0].collectionReference);

        for (MoodEventFilter f : filters) {
            merged.userIds.addAll(f.userIds);
            merged.emotions.addAll(f.emotions);
            merged.socialSituations.addAll(f.socialSituations);

            if (f.startDate != null) {
                if (merged.startDate == null || f.startDate.after(merged.startDate)) {
                    merged.startDate = f.startDate;
                }
            }

            if (f.endDate != null) {
                if (merged.endDate == null || f.endDate.before(merged.endDate)) {
                    merged.endDate = f.endDate;
                }
            }

            if (f.sortField != null) {
                if (merged.sortField == null) {
                    merged.sortField = f.sortField;
                    merged.sortDirection = f.sortDirection;
                }
            }

            if (f.latitude != null && f.longitude != null && f.radius != null) {
                if (merged.latitude == null) {
                    merged.latitude = f.latitude;
                    merged.longitude = f.longitude;
                    merged.radius = f.radius;
                } else if (f.radius < merged.radius) {
                    merged.latitude = f.latitude;
                    merged.longitude = f.longitude;
                    merged.radius = f.radius;
                }
            }

            if (f.limit != null) {
                if (merged.limit == null || f.limit < merged.limit) {
                    merged.limit = f.limit;
                }
            }

            if (f.searchQuery != null && !f.searchQuery.isBlank()) {
                if (merged.searchQuery == null || merged.searchQuery.isBlank()) {
                    merged.searchQuery = f.searchQuery;
                } else if (!merged.searchQuery.equals(f.searchQuery)) {
                    merged.searchQuery += " " + f.searchQuery;
                }
            }
        }

        return merged;
    }
}

