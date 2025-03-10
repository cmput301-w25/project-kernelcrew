package com.kernelcrew.moodapp;

import android.graphics.Bitmap;

import com.kernelcrew.moodapp.utils.PhotoUtils;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Check that the PhotoUtils class is correct.
 * The encodeDecode identity test must be an intent test because it relies on the android Bitmap
 * class.
 */
public class PhotoUtilsTest {
    @Test
    public void encodeDecodeIdentity() {
        // Single pixel pink image
        Bitmap image = Bitmap.createBitmap(
                1,
                1,
                Bitmap.Config.ARGB_8888);

        Bitmap decoded = PhotoUtils.decodePhoto(PhotoUtils.compressPhoto(image));
        assertEquals(1, decoded.getWidth());
        assertEquals(1, decoded.getHeight());
    }
}
