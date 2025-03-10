package com.kernelcrew.moodapp.ui;

/**
 * Fragment responsible for handling location functionality in the MoodApp.
 * <p>
 * This fragment provides functionality to request and handle location permissions,
 * retrieve current device location using the FusedLocationProviderClient, and
 * save mood events with or without location data depending on permission status.
 * <p>
 * The fragment handles different scenarios:
 * - Location permissions granted: retrieves location and saves mood with location
 * - Location permissions denied: saves mood without location data
 * - Location services disabled: alerts user and saves mood without location
 *
 * @author OpenAI, ChatGPT (base implementation)
 * @author Anthropic, Claude 3.7 Sonnet (enhancements and fixes)
 * @version 1.0
 */

public interface LocationUpdateListener {
    void onLocationUpdated(Double latitude, Double longitude);
}
