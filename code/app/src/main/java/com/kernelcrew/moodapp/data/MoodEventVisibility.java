package com.kernelcrew.moodapp.data;

/**
 * The visibility of a mood event.
 * Controls who is allowed to read the mood event.
 */
public enum MoodEventVisibility {
    /**
     * Anyone following the author can view this mood event.
     */
    PUBLIC,

    /**
     * Only the author can view this mood event.
     */
    PRIVATE,
}
