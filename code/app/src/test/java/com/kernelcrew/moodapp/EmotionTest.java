package com.kernelcrew.moodapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.kernelcrew.moodapp.data.Emotion;

import org.junit.Test;

/**
 * Unit tests for {@link Emotion}.
 */
public class EmotionTest {

    /**
     * Test that each enum has a readable toString and consistent fromString lookup.
     */
    @Test
    public void testToStringAndFromString() {
        for (Emotion e : Emotion.values()) {
            String name = e.toString();
            Emotion from = Emotion.fromString(name);
            assertEquals("Should match the same emotion", e, from);
        }
    }

    /**
     * Test fromString with unknown input yields null.
     */
    @Test
    public void testFromStringUnknown() {
        assertNull("Unknown emotion string should return null",
                Emotion.fromString("NonExistentEmotion"));
    }
}
