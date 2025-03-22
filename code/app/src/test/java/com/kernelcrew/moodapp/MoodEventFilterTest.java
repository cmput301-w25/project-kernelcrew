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
import com.google.firebase.firestore.Filter;
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

    @Before
    public void setUp() {
        mockCollectionReference = mock(CollectionReference.class);
        mockQuery = mock(Query.class);

        // Stub calls on the collection reference for chaining
        when(mockCollectionReference.whereIn(anyString(), anyList())).thenReturn(mockQuery);
        when(mockCollectionReference.whereEqualTo(anyString(), any())).thenReturn(mockQuery);
        when(mockCollectionReference.whereGreaterThanOrEqualTo(anyString(), any())).thenReturn(mockQuery);
        when(mockCollectionReference.whereLessThanOrEqualTo(anyString(), any())).thenReturn(mockQuery);
        when(mockCollectionReference.orderBy(anyString(), any(Query.Direction.class))).thenReturn(mockQuery);

        // Stub chaining on the query
        when(mockQuery.whereIn(anyString(), anyList())).thenReturn(mockQuery);
        when(mockQuery.whereEqualTo(anyString(), any())).thenReturn(mockQuery);
        when(mockQuery.whereGreaterThanOrEqualTo(anyString(), any())).thenReturn(mockQuery);
        when(mockQuery.whereLessThanOrEqualTo(anyString(), any())).thenReturn(mockQuery);
        when(mockQuery.orderBy(anyString(), any(Query.Direction.class))).thenReturn(mockQuery);
    }

    @Test
    public void testBuildQueryWithAllFilters() {
        Date startDate = new Date(1000L);
        Date endDate = new Date(2000L);

        MoodEventFilter filter = new MoodEventFilter(mockCollectionReference)
                .addEmotions(Emotion.ANGER)
                .setDateRange(startDate, endDate)
                .setSortField("created", Query.Direction.ASCENDING)
                .setUsers("user123");

        Query builtQuery = filter.buildQuery();

        // Verify ordering: user filter first, then emotion, then date, then sort.
        verify(mockCollectionReference).whereEqualTo(eq("uid"), eq("user123"));
        verify(mockQuery).whereIn(eq("emotion"), anyList());
        verify(mockQuery).whereGreaterThanOrEqualTo(eq("created"), eq(startDate));
        verify(mockQuery).whereLessThanOrEqualTo(eq("created"), eq(endDate));
        verify(mockQuery).orderBy(eq("created"), eq(Query.Direction.ASCENDING));

        assertEquals("Expected built query to be the chained mockQuery", mockQuery, builtQuery);
    }

    @Test
    public void testBuildQueryWithoutAnyFilters() {
        MoodEventFilter filter = new MoodEventFilter(mockCollectionReference);
        Query builtQuery = filter.buildQuery();

        // Without any filters, we expect the original collection reference.
        assertEquals("Expected built query to be the base collection reference",
                mockCollectionReference, builtQuery);
    }

    @Test
    public void testBuildQueryWithOnlyEmotion() {
        MoodEventFilter filter = new MoodEventFilter(mockCollectionReference)
                .addEmotions(Emotion.HAPPINESS);
        Query builtQuery = filter.buildQuery();

        verify(mockCollectionReference).whereIn(eq("emotion"), anyList());
        assertEquals(mockQuery, builtQuery);
    }

    @Test
    public void testBuildQueryWithMultipleEmotions() {
        MoodEventFilter filter = new MoodEventFilter(mockCollectionReference)
                .addEmotions(Emotion.SADNESS)
                .addEmotions(Emotion.HAPPINESS)
                .addEmotions(Emotion.ANGER);
        Query builtQuery = filter.buildQuery();

        verify(mockCollectionReference).whereIn(eq("emotion"), anyList());
        assertEquals(mockQuery, builtQuery);
    }

    @Test
    public void testBuildQueryWithDuplicateEmotions() {
        MoodEventFilter filter = new MoodEventFilter(mockCollectionReference)
                .addEmotions(Emotion.SADNESS)
                .addEmotions(Emotion.SADNESS)
                .addEmotions(Emotion.HAPPINESS)
                .addEmotions(Emotion.HAPPINESS);
        Query builtQuery = filter.buildQuery();

        verify(mockCollectionReference).whereIn(eq("emotion"),
                org.mockito.ArgumentMatchers.argThat(list ->
                        list instanceof List && ((List<?>) list).size() == 2
                ));
        assertEquals(mockQuery, builtQuery);
    }

    @Test
    public void testBuildQueryWithOnlyDateRange() {
        Date startDate = new Date(3000L);
        Date endDate = new Date(4000L);

        MoodEventFilter filter = new MoodEventFilter(mockCollectionReference)
                .setDateRange(startDate, endDate);
        Query builtQuery = filter.buildQuery();

        verify(mockCollectionReference).whereGreaterThanOrEqualTo(eq("created"), eq(startDate));
        verify(mockQuery).whereLessThanOrEqualTo(eq("created"), eq(endDate));
        assertEquals(mockQuery, builtQuery);
    }

    @Test
    public void testSetDateRangeWithOnlyStartDate() {
        Date startDate = new Date(5000L);

        MoodEventFilter filter = new MoodEventFilter(mockCollectionReference)
                .setDateRange(startDate, null);
        Query builtQuery = filter.buildQuery();

        verify(mockCollectionReference).whereGreaterThanOrEqualTo(eq("created"), eq(startDate));
        assertEquals(mockQuery, builtQuery);
    }

    @Test
    public void testSetDateRangeWithOnlyEndDate() {
        Date endDate = new Date(6000L);

        MoodEventFilter filter = new MoodEventFilter(mockCollectionReference)
                .setDateRange(null, endDate);
        Query builtQuery = filter.buildQuery();

        verify(mockCollectionReference).whereLessThanOrEqualTo(eq("created"), eq(endDate));
        assertEquals(mockQuery, builtQuery);
    }

    @Test
    public void testSetDateRangeWithNullDates() {
        MoodEventFilter filter = new MoodEventFilter(mockCollectionReference)
                .setDateRange(null, null);
        Query builtQuery = filter.buildQuery();

        assertEquals(mockCollectionReference, builtQuery);
    }

    @Test
    public void testInvalidDateRangeThrowsException() {
        Date startDate = new Date(7000L);
        Date endDate = new Date(6000L);

        MoodEventFilter filter = new MoodEventFilter(mockCollectionReference);
        assertThrows("Expected exception for startDate after endDate",
                IllegalArgumentException.class, () -> {
                    filter.setDateRange(startDate, endDate);
                });
    }

    @Test
    public void testBuildQueryWithOnlyUser() {
        MoodEventFilter filter = new MoodEventFilter(mockCollectionReference)
                .setUsers("user456");
        Query builtQuery = filter.buildQuery();

        verify(mockCollectionReference).whereEqualTo(eq("uid"), eq("user456"));
        assertEquals(mockQuery, builtQuery);
    }

    @Test
    public void testSetSortFieldInvalidEmptyThrowsException() {
        MoodEventFilter filter = new MoodEventFilter(mockCollectionReference);
        assertThrows("Expected exception for empty sort field",
                IllegalArgumentException.class, () -> {
                    filter.setSortField("   ", Query.Direction.ASCENDING);
                });
    }

    @Test
    public void testSetSortFieldInvalidNullDirectionThrowsException() {
        MoodEventFilter filter = new MoodEventFilter(mockCollectionReference);
        assertThrows("Expected exception for null sort direction",
                IllegalArgumentException.class, () -> {
                    filter.setSortField("created", null);
                });
    }

    @Test
    public void testMultipleCallsToAddEmotion() {
        MoodEventFilter filter = new MoodEventFilter(mockCollectionReference)
                .addEmotions(Emotion.ANGER)
                .addEmotions(Emotion.HAPPINESS)
                .addEmotions(Emotion.SADNESS)
                .addEmotions(Emotion.ANGER)
                .addEmotions(Emotion.HAPPINESS);
        Query builtQuery = filter.buildQuery();

        verify(mockCollectionReference).whereIn(eq("emotion"), org.mockito.ArgumentMatchers.argThat(list ->
                list instanceof List && ((List<?>) list).size() == 3
        ));
        assertEquals(mockQuery, builtQuery);
    }

    @Test
    public void testChainingWithoutOptionalFilters() {
        MoodEventFilter filter = new MoodEventFilter(mockCollectionReference)
                .addEmotions(new HashSet<>())
                .setDateRange(null, null)
                .setUsers((String) null);
        Query builtQuery = filter.buildQuery();

        assertEquals(mockCollectionReference, builtQuery);
    }

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

    @Test
    public void testNoOpChaining() {
        MoodEventFilter filter = new MoodEventFilter(mockCollectionReference);
        filter.setDateRange(null, null)
                .setUsers((String) null);
        Query builtQuery = filter.buildQuery();
        assertEquals(mockCollectionReference, builtQuery);
    }

    @Test
    public void testOverwritingUserFilter() {
        MoodEventFilter filter = new MoodEventFilter(mockCollectionReference)
                .setUsers("firstUser")
                .setUsers("secondUser");
        Query builtQuery = filter.buildQuery();

        verify(mockCollectionReference).whereEqualTo(eq("uid"), eq("secondUser"));
        assertEquals(mockQuery, builtQuery);
    }

    @Test
    public void testSequentialEmotionAddition() {
        Set<Emotion> emotionsList = new HashSet<>();
        emotionsList.add(Emotion.HAPPINESS);
        emotionsList.add(Emotion.ANGER);

        MoodEventFilter filter = new MoodEventFilter(mockCollectionReference)
                .addEmotions(Emotion.SADNESS)
                .addEmotions(emotionsList)
                .addEmotions(Emotion.HAPPINESS);
        Query builtQuery = filter.buildQuery();

        verify(mockCollectionReference).whereIn(eq("emotion"), org.mockito.ArgumentMatchers.argThat(list ->
                list instanceof List && ((List<?>) list).size() == 3
        ));
        assertEquals(mockQuery, builtQuery);
    }

    @Test
    public void testMergeFiltersThrowsExceptionOnEmptyArray() {
        assertThrows("Expected exception when merging no filters",
                IllegalArgumentException.class,
                MoodEventFilter::mergeFilters);
    }
}