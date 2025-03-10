/**
 * //Anthropic, Claude, "Generate source code descriptive comments for 301 rubric", 03-10-2025
 *
 * Utility class that maps mood types to their corresponding icons.
 * It provides helper methods to retrieve the appropriate drawable
 * resource for different emotion types, ensuring consistent visual
 * representation of moods throughout the application.
 */
package com.kernelcrew.moodapp.ui;

import com.kernelcrew.moodapp.R;

public class MoodIconUtil {
    public static int getMoodIconResource(String emotion) {
        if (emotion == null) {
            return R.drawable.ic_error_color;
        }
        switch (emotion.toLowerCase()) {
            case "happy":
                return R.drawable.ic_happy_color;
            case "sad":
                return R.drawable.ic_sad_color;
            case "anger":
                return R.drawable.ic_anger_color;
            case "confused":
                return R.drawable.ic_confused_color;
            case "disgust":
                return R.drawable.ic_disgust_color;
            case "fear":
                return R.drawable.ic_fear_color;
            case "shame":
                return R.drawable.ic_shame_color;
            case "surprise":
                return R.drawable.ic_surprise_color;
            default:
                return R.drawable.ic_error_color;
        }
    }
}
