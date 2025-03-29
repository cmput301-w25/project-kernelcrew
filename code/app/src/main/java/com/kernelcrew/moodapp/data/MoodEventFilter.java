package com.kernelcrew.moodapp.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A class for filtering mood events through Firestore. It is used to build
 * queries based on various filters.
 */
public class MoodEventFilter implements Cloneable {
    private static final double EARTH_RADIUS_KM = 6371.0;

    private final Query allMoodEvents;
    private final FilterCriteria criteria = new FilterCriteria();
    private String reasonQuery;

    private static class FilterCriteria implements Cloneable {
        Set<Emotion> emotions = new HashSet<>();
        Set<String> socialSituations = new HashSet<>();
        String userId;
        DateRange dateRange;
        Sorting sorting;
        LocationFilter location;
        Integer limit;

        @NonNull
        @Override
        protected Object clone() throws CloneNotSupportedException {
            FilterCriteria cloned = (FilterCriteria) super.clone();

            cloned.emotions = new HashSet<>(this.emotions);
            cloned.socialSituations = new HashSet<>(this.socialSituations);
            cloned.userId = this.userId;

            if (this.dateRange != null) {
                cloned.dateRange = (DateRange) this.dateRange.clone();
            }
            if (this.sorting != null) {
                cloned.sorting = (Sorting) this.sorting.clone();
            }
            if (this.location != null) {
                cloned.location = (LocationFilter) this.location.clone();
            }

            return cloned;
        }
    }

    private static class DateRange implements Cloneable {
        Date start;
        Date end;

        @NonNull
        @Override
        protected Object clone() throws CloneNotSupportedException {
            DateRange cloned = (DateRange) super.clone();

            if (this.start != null) {
                cloned.start = (Date) this.start.clone();
            }
            if (this.end != null) {
                cloned.end = (Date) this.end.clone();
            }

            return cloned;
        }
    }

    private static class Sorting implements Cloneable {
        String field;
        Query.Direction direction;

        @NonNull
        @Override
        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
    }

    private static class LocationFilter implements Cloneable {
        Double latitude;
        Double longitude;
        Double radius;

        @NonNull
        @Override
        protected Object clone() throws CloneNotSupportedException {
            LocationFilter cloned = (LocationFilter) super.clone();

            cloned.latitude = this.latitude;
            cloned.longitude = this.longitude;
            cloned.radius = this.radius;

            return cloned;
        }
    }

    @NonNull
    @Override
    public Object clone() throws CloneNotSupportedException {
        MoodEventFilter cloned = (MoodEventFilter) super.clone();

        cloned.criteria.clone();

        return cloned;
    }

    // Constructors
    /**
     * Constructor accepting a CollectionReference directly.
     * This is useful if you have the Firestore collection from your provider.
     *
     * @param allMoodEvents All mood events query
     */
    public MoodEventFilter(Query allMoodEvents) {
        this.allMoodEvents = allMoodEvents;
    }

    public MoodEventFilter(MoodEventProvider provider) {
        this.allMoodEvents = provider.getAll();
    }

    // Helper method to check if a string is valid (non-null and non-blank)
    private static boolean isValidString(String s) {
        return s != null && !s.isBlank();
    }

    // Emotion filters
    public MoodEventFilter addEmotion(Emotion emotion) {
        criteria.emotions.add(emotion);
        return this;
    }

    public MoodEventFilter addEmotions(Set<Emotion> emotions) {
        criteria.emotions.addAll(emotions);
        return this;
    }

    public MoodEventFilter setEmotions(Set<Emotion> emotions) {
        criteria.emotions.clear();
        criteria.emotions.addAll(emotions);
        return this;
    }

    // Location filter
    public MoodEventFilter setLocation(Double latitude, Double longitude, double radius) {
        if (latitude == null || longitude == null || radius <= 0) {
            criteria.location = null;
        } else {
            LocationFilter loc = new LocationFilter();
            loc.latitude = latitude;
            loc.longitude = longitude;
            loc.radius = radius;
            criteria.location = loc;
        }
        return this;
    }

    // Date range filter
    public MoodEventFilter setDateRange(@Nullable Date startDate, @Nullable Date endDate) {
        if (startDate != null && endDate != null && startDate.after(endDate)) {
            throw new IllegalArgumentException("Start date must not be after end date.");
        }
        if (criteria.dateRange == null) {
            criteria.dateRange = new DateRange();
        }

        criteria.dateRange.start = startDate;
        criteria.dateRange.end = endDate;

        return this;
    }

    // Sorting
    public MoodEventFilter setSortField(String field, Query.Direction direction) {
        if (!isValidString(field)) {
            throw new IllegalArgumentException("Sort field must be a non-empty string.");
        }
        if (direction == null) {
            throw new IllegalArgumentException("Sort direction must not be null.");
        }
        if (criteria.sorting == null) {
            criteria.sorting = new Sorting();
        }
        criteria.sorting.field = field;
        criteria.sorting.direction = direction;
        return this;
    }

    // User filters
    public MoodEventFilter setUser(String userId) {
        if (isValidString(userId)) {
            this.criteria.userId = userId;
        }
        return this;
    }

    // Limit filter
    public MoodEventFilter setLimit(int limit) {
        criteria.limit = limit;
        return this;
    }

    // Social situation filters
    public MoodEventFilter addSocialSituation(String socialSituation) {
        if (isValidString(socialSituation)) {
            criteria.socialSituations.add(socialSituation);
        }
        return this;
    }

    public MoodEventFilter addSocialSituations(Set<String> socialSituations) {
        if (socialSituations != null) {
            criteria.socialSituations.addAll(socialSituations);
        }
        return this;
    }

    public MoodEventFilter setSocialSituations(Set<String> socialSituations) {
        criteria.socialSituations.clear();
        if (socialSituations != null) {
            criteria.socialSituations.addAll(socialSituations);
        }
        return this;
    }

    public MoodEventFilter setReasonQuery(String query) {
        reasonQuery = isValidString(query) ? query.trim().toLowerCase() : null;
        return this;
    }

    // Getters
    public Set<Emotion> getEmotions() {
        return new HashSet<>(criteria.emotions);
    }

    public Date getStartDate() {
        return criteria.dateRange != null ? criteria.dateRange.start : null;
    }

    public Date getEndDate() {
        return criteria.dateRange != null ? criteria.dateRange.end : null;
    }

    public String getReasonQuery() {
        return reasonQuery;
    }

    /**
     * Counts the number of applied filters.
     * 
     * @return The number of active filters.
     */
    public int count() {
        int count = 0;
        if (!criteria.emotions.isEmpty()) count++;
        if (criteria.dateRange != null && (criteria.dateRange.start != null || criteria.dateRange.end != null)) count++;
        if (criteria.sorting != null) count++;
        if (criteria.location != null) count++;
        if (!criteria.socialSituations.isEmpty()) count++;
        if (!isValidString(criteria.userId)) count++;
        if (criteria.limit != null) count++;
        if (!isValidString(reasonQuery)) count++;
        return count;
    }

    /**
     * Clears all applied filters.
     */
    public void clearFilters() {
        criteria.userId = null;
        criteria.emotions.clear();
        criteria.socialSituations.clear();
        criteria.dateRange = null;
        criteria.sorting = null;
        criteria.location = null;
        criteria.limit = null;
        reasonQuery = null;
    }

    /**
     * Returns a summary of the currently applied filters.
     * 
     * @return A summary string.
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        if (criteria.userId != null) {
            sb.append("User ID: ").append(criteria.userId).append("\n");
        }
        if (!criteria.emotions.isEmpty()) {
            sb.append("Emotions: ").append(criteria.emotions).append("\n");
        }
        if (criteria.dateRange != null) {
            if (criteria.dateRange.start != null) {
                sb.append("Start Date: ").append(criteria.dateRange.start).append("\n");
            }
            if (criteria.dateRange.end != null) {
                sb.append("End Date: ").append(criteria.dateRange.end).append("\n");
            }
        }
        if (criteria.sorting != null) {
            sb.append("Sort: ").append(criteria.sorting.field)
                    .append(" (").append(criteria.sorting.direction).append(")\n");
        }
        if (criteria.location != null) {
            sb.append("Location: (").append(criteria.location.latitude).append(", ")
                    .append(criteria.location.longitude).append(") within ")
                    .append(criteria.location.radius).append(" km\n");
        }
        if (!criteria.socialSituations.isEmpty()) {
            sb.append("Social Situations: ").append(criteria.socialSituations).append("\n");
        }
        if (criteria.limit != null) {
            sb.append("Limit: ").append(criteria.limit).append("\n");
        }
        if (reasonQuery != null) {
            sb.append("Reason Query: ").append(reasonQuery).append("\n");
        }
        if (sb.length() == 0) {
            sb.append("No filters applied.");
        }
        return sb.toString();
    }

    /**
     * Builds a Firestore Query using the applied filters.
     * 
     * @return A Query with filtering and sorting applied.
     */
    public Query buildQuery() {
        // Start with the collection reference
        Query query = allMoodEvents;

        if (isValidString(criteria.userId)) {
            query = query.whereEqualTo("uid", criteria.userId);
        }

        if (!criteria.emotions.isEmpty()) {
            query = query.whereIn("emotion", new ArrayList<>(criteria.emotions));
        }

        if (criteria.dateRange != null) {
            if (criteria.dateRange.start != null) {
                query = query.whereGreaterThanOrEqualTo("created", criteria.dateRange.start);
            }
            if (criteria.dateRange.end != null) {
                query = query.whereLessThanOrEqualTo("created", criteria.dateRange.end);
            }
        }

        if (criteria.sorting != null) {
            query = query.orderBy(criteria.sorting.field, criteria.sorting.direction);
        }

        if (criteria.location != null) {
            double latDelta = Math.toDegrees(criteria.location.radius / EARTH_RADIUS_KM);
            double lonDelta = Math.toDegrees(criteria.location.radius / (EARTH_RADIUS_KM *
                    Math.cos(Math.toRadians(criteria.location.latitude))));

            double minLat = criteria.location.latitude - latDelta;
            double maxLat = criteria.location.latitude + latDelta;
            double minLon = criteria.location.longitude - lonDelta;
            double maxLon = criteria.location.longitude + lonDelta;

            query = query.whereGreaterThanOrEqualTo("latitude", minLat)
                    .whereLessThanOrEqualTo("latitude", maxLat)
                    .whereGreaterThanOrEqualTo("longitude", minLon)
                    .whereLessThanOrEqualTo("longitude", maxLon);
        }

        if (!criteria.socialSituations.isEmpty()) {
            query = query.whereIn("socialSituation", new ArrayList<>(criteria.socialSituations));
        }

        if (criteria.limit != null) {
            query = query.limit(criteria.limit);
        }

        return query;
    }

    // ------------------------------------- Filtering Lists ----
    /**
     * Filters a list of mood events.
     *
     * @param events The events to filter.
     * @return A filtered List containing only those with the correct filters.
     */
    public List<MoodEvent> applyReasonFilter(List<MoodEvent> events) {
        if (reasonQuery == null || reasonQuery.isEmpty()) {
            return events;
        }
        List<MoodEvent> filtered = new ArrayList<>();
        for (MoodEvent event : events) {
            String reason = event.getReason();
            if (reason != null && reason.toLowerCase().contains(reasonQuery)) {
                filtered.add(event);
            }
        }
        return filtered;
    }
}
