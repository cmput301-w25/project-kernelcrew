package com.kernelcrew.moodapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.Query;
import com.kernelcrew.moodapp.data.Emotion;
import com.kernelcrew.moodapp.data.MoodEventFilter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class MoodEventFilterTest {

    private CollectionReference mockCollectionReference;
    private Query mockQuery;

    /**
     * Setup the mock CollectionReference and Query before each test.
     */
    @Before
    public void setUp() {
        mockCollectionReference = mock(CollectionReference.class);
        mockQuery = mock(Query.class);

        // Make sure that ANY filter call on the collection returns the same mockQuery for chaining:
        when(mockCollectionReference.whereIn(anyString(), anyList())).thenReturn(mockQuery);
        when(mockCollectionReference.whereEqualTo(anyString(), any())).thenReturn(mockQuery);
        when(mockCollectionReference.whereGreaterThanOrEqualTo(anyString(), any())).thenReturn(mockQuery);
        when(mockCollectionReference.whereLessThanOrEqualTo(anyString(), any())).thenReturn(mockQuery);
        when(mockCollectionReference.orderBy(anyString(), any(Query.Direction.class))).thenReturn(mockQuery);

        // Also stub calls on mockQuery so subsequent filters can chain:
        when(mockQuery.whereIn(anyString(), anyList())).thenReturn(mockQuery);
        when(mockQuery.whereEqualTo(anyString(), any())).thenReturn(mockQuery);
        when(mockQuery.whereGreaterThanOrEqualTo(anyString(), any())).thenReturn(mockQuery);
        when(mockQuery.whereLessThanOrEqualTo(anyString(), any())).thenReturn(mockQuery);
        when(mockQuery.orderBy(anyString(), any(Query.Direction.class))).thenReturn(mockQuery);
    }

    /**
     * Test building a query when all filters are set.
     */
    @Test
    public void testBuildQueryWithAllFilters() {
        Date startDate = new Date(1000L);
        Date endDate = new Date(2000L);

        MoodEventFilter filter = new MoodEventFilter(mockCollectionReference)
                .addEmotion(Emotion.ANGER)
                .setDateRange(startDate, endDate)
                .setSortField("created", Query.Direction.ASCENDING)
                .setUser("user123");

        Query builtQuery = filter.buildQuery();

        // If your code calls user filter FIRST, then emotion filter, then startDate, then endDate, then sort,
        // we can reorder the verifies accordingly:
        verify(mockCollectionReference).whereEqualTo(eq("uid"), eq("user123")); // user filter first
        verify(mockQuery).whereIn(eq("emotion"), anyList());                    // emotion second
        verify(mockQuery).whereGreaterThanOrEqualTo(eq("created"), eq(startDate));
        verify(mockQuery).whereLessThanOrEqualTo(eq("created"), eq(endDate));
        verify(mockQuery).orderBy(eq("created"), eq(Query.Direction.ASCENDING));

        assertEquals(mockQuery, builtQuery);
    }


    /**
     * Test building a query when no filters are applied.
     * This should return the original CollectionReference.
     */
    @Test
    public void testBuildQueryWithoutAnyFilters() {
        MoodEventFilter filter = new MoodEventFilter(mockCollectionReference);
        Query builtQuery = filter.buildQuery();

        assertEquals(mockCollectionReference, builtQuery);
    }

    /**
     * Test building a query when only one emotion filter is added.
     */
    @Test
    public void testBuildQueryWithOnlyEmotion() {
        MoodEventFilter filter = new MoodEventFilter(mockCollectionReference)
                .addEmotion(Emotion.HAPPINESS);
        Query builtQuery = filter.buildQuery();

        verify(mockCollectionReference).whereIn(eq("emotion"), anyList());
        assertEquals(mockQuery, builtQuery);
    }

    /**
     * Test adding multiple different emotions.
     */
    @Test
    public void testBuildQueryWithMultipleEmotions() {
        MoodEventFilter filter = new MoodEventFilter(mockCollectionReference)
                .addEmotion(Emotion.SADNESS)
                .addEmotion(Emotion.HAPPINESS)
                .addEmotion(Emotion.ANGER);
        Query builtQuery = filter.buildQuery();

        verify(mockCollectionReference).whereIn(eq("emotion"), anyList());
        assertEquals(mockQuery, builtQuery);
    }

    /**
     * Test that duplicate emotions are removed.
     */
    @Test
    public void testBuildQueryWithDuplicateEmotions() {
        MoodEventFilter filter = new MoodEventFilter(mockCollectionReference)
                .addEmotion(Emotion.SADNESS)
                .addEmotion(Emotion.SADNESS)
                .addEmotion(Emotion.HAPPINESS)
                .addEmotion(Emotion.HAPPINESS);

        Query builtQuery = filter.buildQuery();

        verify(mockCollectionReference).whereIn(eq("emotion"),
                org.mockito.ArgumentMatchers.argThat(list ->
                        list instanceof List && ((List<?>) list).size() == 2
                ));
        assertEquals(mockQuery, builtQuery);
    }

    /**
     * Test building a query with only a date range filter (both start and end dates).
     */
    @Test
    public void testBuildQueryWithOnlyDateRange() {
        Date startDate = new Date(3000L);
        Date endDate = new Date(4000L);

        MoodEventFilter filter = new MoodEventFilter(mockCollectionReference)
                .setDateRange(startDate, endDate);
        Query builtQuery = filter.buildQuery();

        // If your code calls startDate => query.whereGreaterThanOrEqualTo(...),
        // then endDate => query.whereLessThanOrEqualTo(...):
        verify(mockCollectionReference).whereGreaterThanOrEqualTo(eq("created"), eq(startDate));
        verify(mockQuery).whereLessThanOrEqualTo(eq("created"), eq(endDate));

        assertEquals(mockQuery, builtQuery);
    }

    /**
     * Test setting the date range when only a start date is provided.
     * End date is null so only the start date filter should apply.
     */
    @Test
    public void testSetDateRangeWithOnlyStartDate() {
        Date startDate = new Date(5000L);

        MoodEventFilter filter = new MoodEventFilter(mockCollectionReference)
                .setDateRange(startDate, null);
        Query builtQuery = filter.buildQuery();

        verify(mockCollectionReference).whereGreaterThanOrEqualTo(eq("created"), eq(startDate));
        assertEquals(mockQuery, builtQuery);
    }

    /**
     * Test setting the date range when only an end date is provided.
     */
    @Test
    public void testSetDateRangeWithOnlyEndDate() {
        Date endDate = new Date(6000L);

        MoodEventFilter filter = new MoodEventFilter(mockCollectionReference)
                .setDateRange(null, endDate);
        Query builtQuery = filter.buildQuery();

        verify(mockCollectionReference).whereLessThanOrEqualTo(eq("created"), eq(endDate));
        assertEquals(mockQuery, builtQuery);
    }


    /**
     * Test setting the date range with both dates as null.
     * No date filters should be applied.
     */
    @Test
    public void testSetDateRangeWithNullDates() {
        MoodEventFilter filter = new MoodEventFilter(mockCollectionReference)
                .setDateRange(null, null);
        Query builtQuery = filter.buildQuery();

        assertEquals(mockCollectionReference, builtQuery);
    }


    /**
     * Test that an invalid date range (startDate after endDate) throws an exception.
     */
    @Test
    public void testInvalidDateRangeThrowsException() {
        Date startDate = new Date(7000L);
        Date endDate = new Date(6000L);

        MoodEventFilter filter = new MoodEventFilter(mockCollectionReference);
        assertThrows(IllegalArgumentException.class, () -> {
            filter.setDateRange(startDate, endDate);
        });
    }


    /**
     * Test building a query with only a user filter.
     */
    @Test
    public void testBuildQueryWithOnlyUser() {
        MoodEventFilter filter = new MoodEventFilter(mockCollectionReference)
                .setUser("user456");
        Query builtQuery = filter.buildQuery();

        verify(mockCollectionReference).whereEqualTo(eq("uid"), eq("user456"));
        assertEquals(mockQuery, builtQuery);
    }

    /**
     * Test that setting an empty sort field throws an exception.
     */
    @Test
    public void testSetSortFieldInvalidEmptyThrowsException() {
        MoodEventFilter filter = new MoodEventFilter(mockCollectionReference);
        assertThrows(IllegalArgumentException.class, () -> {
            filter.setSortField("   ", Query.Direction.ASCENDING);
        });
    }


    /**
     * Test that setting a null sort direction throws an exception.
     */
    @Test
    public void testSetSortFieldInvalidNullDirectionThrowsException() {
        MoodEventFilter filter = new MoodEventFilter(mockCollectionReference);
        assertThrows(IllegalArgumentException.class, () -> {
            filter.setSortField("created", null);
        });
    }

    /**
     * Test adding emotions via multiple calls to addEmotion.
     */
    @Test
    public void testMultipleCallsToAddEmotion() {
        MoodEventFilter filter = new MoodEventFilter(mockCollectionReference)
                .addEmotion(Emotion.ANGER)
                .addEmotion(Emotion.HAPPINESS)
                .addEmotion(Emotion.SADNESS)
                .addEmotion(Emotion.ANGER)
                .addEmotion(Emotion.HAPPINESS);

        Query builtQuery = filter.buildQuery();

        verify(mockCollectionReference).whereIn(eq("emotion"), org.mockito.ArgumentMatchers.argThat(list ->
                list instanceof List && ((List<?>) list).size() == 3
        ));
        assertEquals(mockQuery, builtQuery);
    }


    /**
     * Test that chaining methods without setting any optional filters returns the base collection.
     */
    @Test
    public void testChainingWithoutOptionalFilters() {
        MoodEventFilter filter = new MoodEventFilter(mockCollectionReference)
                .addEmotions(new HashSet<>())
                .setDateRange(null, null)
                .setUser(null);
        Query builtQuery = filter.buildQuery();

        assertEquals(mockCollectionReference, builtQuery);
    }

    /**
     * Test that using addEmotions with a non-empty list behaves as expected.
     */
    @Test
    public void testAddEmotionsMethod() {
        Set<Emotion> emotionList = new HashSet<>();
        emotionList.add(Emotion.ANGER);
        emotionList.add(Emotion.HAPPINESS);
        emotionList.add(Emotion.ANGER);

        MoodEventFilter filter = new MoodEventFilter(mockCollectionReference)
                .addEmotions(emotionList);
        Query builtQuery = filter.buildQuery();

        verify(mockCollectionReference).whereIn(eq("emotion"), org.mockito.ArgumentMatchers.argThat(list ->
                list instanceof List && ((List<?>) list).size() == 2
        ));
        assertEquals(mockQuery, builtQuery);
    }

    /**
     * Test that chaining a series of filter calls without setting any filters
     * results in the original collection reference.
     */
    @Test
    public void testNoOpChaining() {
        MoodEventFilter filter = new MoodEventFilter(mockCollectionReference);

        filter.setDateRange(null, null)
                .setUser(null);

        Query builtQuery = filter.buildQuery();
        assertEquals(mockCollectionReference, builtQuery);
    }


    /**
     * Test that setting a user filter multiple times uses the latest value.
     */
    @Test
    public void testOverwritingUserFilter() {
        MoodEventFilter filter = new MoodEventFilter(mockCollectionReference)
                .setUser("firstUser")
                .setUser("secondUser");

        Query builtQuery = filter.buildQuery();
        verify(mockCollectionReference).whereEqualTo(eq("uid"), eq("secondUser"));
        assertEquals(mockQuery, builtQuery);
    }


    /**
     * Test multiple sequential calls to addEmotion and addEmotions.
     */
    @Test
    public void testSequentialEmotionAddition() {
        Set<Emotion> emotionsList = new HashSet<>();
        emotionsList.add(Emotion.HAPPINESS);
        emotionsList.add(Emotion.ANGER);

        MoodEventFilter filter = new MoodEventFilter(mockCollectionReference)
                .addEmotion(Emotion.SADNESS)
                .addEmotions(emotionsList)
                .addEmotion(Emotion.HAPPINESS);

        Query builtQuery = filter.buildQuery();

        verify(mockCollectionReference).whereIn(eq("emotion"), org.mockito.ArgumentMatchers.argThat(list ->
                list instanceof List && ((List<?>) list).size() == 3
        ));
        assertEquals(mockQuery, builtQuery);
    }

}