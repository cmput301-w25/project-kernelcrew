package com.kernelcrew.moodapp.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.kernelcrew.moodapp.R;

public class EmotionIconUtils {

    public static BitmapDescriptor getEmotionIcon(Context context, String emotion) {
        int resourceId;

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
            default:
                resourceId = R.drawable.ic_error_color;
                break;
        }

        return getBitmapDescriptorFromVector(context, resourceId);
    }

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