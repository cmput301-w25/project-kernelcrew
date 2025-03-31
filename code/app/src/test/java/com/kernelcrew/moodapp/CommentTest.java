package com.kernelcrew.moodapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.kernelcrew.moodapp.data.Comment;

import org.junit.Test;

import java.util.Date;

/**
 * Unit tests for {@link Comment}.
 */
public class CommentTest {

    /**
     * Test basic constructor and field assignments.
     */
    @Test
    public void testCommentConstructor() {
        Comment comment = new Comment("placeholed", "Hello", "moodId", "World!");
        assertNotNull("Comment should be created", comment);
        assertEquals("Hello", comment.getUsername());
        assertEquals("moodId", comment.getMoodEventId());
        assertEquals("World!", comment.getCommentText());
        assertNotNull("Created date should be assigned", comment.getCreated());
    }

    /**
     * Test setter and getter for ID.
     */
    @Test
    public void testSetId() {
        Comment comment = new Comment();
        comment.setId("moodId");
        assertEquals("moodId", comment.getId());
    }

    /**
     * Test timestamp retrieval from the Date object.
     */
    @Test
    public void testGetTimestamp() {
        Comment comment = new Comment();
        Date now = new Date();
        comment.setCreated(now);

        long expected = now.getTime();
        assertEquals(expected, comment.getTimestamp());
    }
}