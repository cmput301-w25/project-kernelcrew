package com.kernelcrew.moodapp;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;

import android.graphics.Bitmap;

import com.kernelcrew.moodapp.data.MoodEvent;
import com.kernelcrew.moodapp.utils.PhotoUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.List;

public class MoodEventTest {
    @Mock
    Bitmap bitmap = Mockito.mock(Bitmap.class);

    List<Integer> fakeByteList = List.of(42);
    MockedStatic<PhotoUtils> photoUtilsMockedStatic;

    @Before
    public void before() {
        photoUtilsMockedStatic = Mockito.mockStatic(PhotoUtils.class, "");

        photoUtilsMockedStatic.when(() -> PhotoUtils.compressPhoto(bitmap)).thenReturn(fakeByteList);
        photoUtilsMockedStatic.when(() -> PhotoUtils.decodePhoto(fakeByteList)).thenReturn(bitmap);
    }

    @After
    public void after() {
        photoUtilsMockedStatic.close();
    }

    @Test
    public void testAddImage() {
        MoodEvent moodEvent = new MoodEvent();
        moodEvent.setPhoto(bitmap);

        List<Integer> photoBytes = moodEvent.getPhotoBytes();
        assertNotNull(photoBytes);
        assertArrayEquals(fakeByteList.toArray(), photoBytes.toArray());

        // Check that compression was called
        photoUtilsMockedStatic.verify(() -> PhotoUtils.compressPhoto(bitmap));
    }

    @Test
    public void testDecodeImage() {
        MoodEvent moodEvent = new MoodEvent();
        moodEvent.setPhotoBytes(fakeByteList);

        assertEquals(bitmap, moodEvent.getPhoto());

        photoUtilsMockedStatic.verify(() -> PhotoUtils.decodePhoto(fakeByteList));
    }

    @Test
    public void testGetNoImage() {
        MoodEvent moodEvent = new MoodEvent();
        assertNull(moodEvent.getPhotoBytes());
    }
    
    
}
