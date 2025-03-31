package com.kernelcrew.moodapp.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.kernelcrew.moodapp.R;

/**
 * Utility class for converting emotion names into map marker icons.
 * <p>
 * This class provides functionality to transform emotion string representations
 * into Google Maps marker icons (BitmapDescriptors) for displaying mood events on maps.
 * It handles the conversion of vector drawables into appropriately sized bitmap markers
 * with proper background and formatting for each distinct emotion.
 * </p>
 *
 * Created by Anthropic, Claude 3.7 Sonnet, "Generate JavaDoc for EmotionIconUtils", accessed 03-30-2025
 */

public class EmotionIconUtils {

    /**
     * Converts an emotion name to its corresponding map marker icon.
     * <p>
     * Takes a string representation of an emotion (matching the enum values
     * in the Emotion class) and returns a properly formatted BitmapDescriptor
     * that can be used as a Google Maps marker icon. Each emotion has a distinct
     * visual representation for easy identification on the map.
     * </p>
     *
     * @param context The Android context used to access resources
     * @param emotion The string name of the emotion (e.g., "Happy", "Sad", "Anger")
     * @return A BitmapDescriptor object representing the emotion as a map marker icon
     */

    public static BitmapDescriptor getEmotionIcon(Context context, String emotion) {
        int resourceId = 0;

        switch (emotion) {
            case "Anger":
                resourceId = R.drawable.ic_anger_color_with_bg;
                break;
            case "Confused":
                resourceId = R.drawable.ic_confused_color_with_bg;
                break;
            case "Disgust":
                resourceId = R.drawable.ic_disgust_color_with_bg;
                break;
            case "Fear":
                resourceId = R.drawable.ic_fear_color_with_bg;
                break;
            case "Happy":
                resourceId = R.drawable.ic_happy_color_with_bg;
                break;
            case "Sad":
                resourceId = R.drawable.ic_sad_color_with_bg;
                break;
            case "Shame":
                resourceId = R.drawable.ic_shame_color_with_bg;
                break;
            case "Surprise":
                resourceId = R.drawable.ic_surprise_color_with_bg;
                break;
        }

        return getBitmapDescriptorFromVector(context, resourceId);


    }

    /**
     * Converts a vector drawable resource into a BitmapDescriptor for map markers.
     * <p>
     * This helper method handles the conversion of vector drawable resources into
     * bitmap-based map marker icons with appropriate sizing. It creates a bitmap with
     * the specified dimensions and renders the vector drawable onto it.
     * </p>
     *
     * @param context The Android context used to access resources
     * @param vectorResId The resource ID of the vector drawable to convert
     * @return A BitmapDescriptor that can be used as a Google Maps marker icon,
     *         or the default marker if the drawable cannot be loaded
     */
    private static BitmapDescriptor getBitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        if (vectorDrawable == null) {
            return BitmapDescriptorFactory.defaultMarker();
        }

        int targetWidth = 80;  // adjust size as needed (original icons may be 96–192px)
        int targetHeight = 80;

        vectorDrawable.setBounds(0, 0, targetWidth, targetHeight);

        Bitmap bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}