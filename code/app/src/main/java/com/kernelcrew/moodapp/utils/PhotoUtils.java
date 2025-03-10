package com.kernelcrew.moodapp.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class PhotoUtils {
    /**
     * Compress a Bitmap photo into a WEBP encoded byte array.
     * The byte array is returned as a List&lt;Integer&gt; to be compatible with Firestore
     * serialization.
     * @param photo Photo to complete
     * @return Encoded byte array
     */
    public static List<Integer> compressPhoto(@NonNull Bitmap photo) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.WEBP, 0, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();

        ArrayList<Integer> byteList = new ArrayList<>();
        byteList.ensureCapacity(byteArray.length);
        for (byte b : byteArray) {
            byteList.add((int)b);
        }
        return byteList;
    }

    /**
     * Decode a WEBP-encoded image into a bitmap.
     * Takes the byte list as a List&lt;Integer&gt; to be compatible with Firestore.
     * @param byteList List of bytes encoding the WEBP image
     * @return Decoded image as a Bitmap
     */
    public static Bitmap decodePhoto(@NonNull List<Integer> byteList) {
        byte[] byteArray = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); i++) {
            byteArray[i] = byteList.get(i).byteValue();
        }

        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }
}
