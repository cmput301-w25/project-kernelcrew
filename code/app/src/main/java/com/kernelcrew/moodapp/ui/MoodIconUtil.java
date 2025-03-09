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
