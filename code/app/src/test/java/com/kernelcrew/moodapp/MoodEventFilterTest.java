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
import java.util.List;
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

        when(mockCollectionReference.whereIn(anyString(), anyList())).thenReturn(mockQuery);
        when(mockCollectionReference.whereEqualTo(anyString(), any())).thenReturn(mockQuery);
        when(mockQuery.whereGreaterThanOrEqualTo(anyString(), any(Date.class))).thenReturn(mockQuery);
        when(mockQuery.whereLessThanOrEqualTo(anyString(), any(Date.class))).thenReturn(mockQuery);
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

        verify(mockCollectionReference).whereIn(eq("emotion"), anyList());
        verify(mockQuery).whereGreaterThanOrEqualTo(eq("created"), eq(startDate));
        verify(mockQuery).whereLessThanOrEqualTo(eq("created"), eq(endDate));
        verify(mockQuery).orderBy(eq("created"), eq(Query.Direction.ASCENDING));
        verify(mockCollectionReference).whereEqualTo(eq("uid"), eq("user123"));

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

        verify(mockCollectionReference).whereIn(eq("emotion"), org.mockito.ArgumentMatchers.argThat(list ->
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

        verify(mockQuery).whereGreaterThanOrEqualTo(eq("created"), eq(startDate));
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

        verify(mockQuery).whereGreaterThanOrEqualTo(eq("created"), eq(startDate));
        assertEquals(mockQuery, builtQuery);
    }


    /**
     * Test setting the date range when only an end date is provided.
     * Start date is null so only the end date filter should apply.
     */
    @Test
    public void testSetDateRangeWithOnlyEndDate() {
        Date endDate = new Date(6000L);

        MoodEventFilter filter = new MoodEventFilter(mockCollectionReference)
                .setDateRange(null, endDate);
        Query builtQuery = filter.buildQuery();

        verify(mockQuery).whereLessThanOrEqualTo(eq("created"), eq(endDate));
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
     * Test building a query with only a sort field set.
     */
    @Test
    public void testBuildQueryWithOnlySortField() {
        MoodEventFilter filter = new MoodEventFilter(mockCollectionReference)
                .setSortField("created", Query.Direction.DESCENDING);
        Query builtQuery = filter.buildQuery();

        verify(mockQuery).orderBy(eq("created"), eq(Query.Direction.DESCENDING));
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
     * Test that repeatedly setting the sort field overwrites the previous value.
     */
    @Test
    public void testOverwritingSortField() {
        MoodEventFilter filter = new MoodEventFilter(mockCollectionReference)
                .setSortField("created", Query.Direction.ASCENDING)
                .setSortField("updated", Query.Direction.DESCENDING);

        Query builtQuery = filter.buildQuery();

        verify(mockQuery).orderBy(eq("updated"), eq(Query.Direction.DESCENDING));
        assertEquals(mockQuery, builtQuery);
    }


    /**
     * Test chaining multiple filter methods in various orders.
     */
    @Test
    public void testChainedFiltersOrder() {
        Date startDate = new Date(8000L);
        Date endDate = new Date(9000L);

        MoodEventFilter filter = new MoodEventFilter(mockCollectionReference)
                .setUser("user789")
                .addEmotion(Emotion.HAPPINESS)
                .setDateRange(startDate, endDate)
                .setSortField("created", Query.Direction.ASCENDING)
                .addEmotion(Emotion.SADNESS);

        Query builtQuery = filter.buildQuery();

        verify(mockCollectionReference).whereIn(eq("emotion"), anyList());
        verify(mockCollectionReference).whereEqualTo(eq("uid"), eq("user789"));
        verify(mockQuery).whereGreaterThanOrEqualTo(eq("created"), eq(startDate));
        verify(mockQuery).whereLessThanOrEqualTo(eq("created"), eq(endDate));
        verify(mockQuery).orderBy(eq("created"), eq(Query.Direction.ASCENDING));

        assertEquals(mockQuery, builtQuery);
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
                .addEmotions(new ArrayList<>())
                .setDateRange(null, null)
                .setUser(null);
        Query builtQuery = filter.buildQuery();

        assertEquals(mockCollectionReference, builtQuery);
    }


    /**
     * Test that mixing null and non-null filters results in only non-null filters being applied.
     */
    @Test
    public void testMixingNullAndNonNullFilters() {
        Date startDate = new Date(10000L);
        MoodEventFilter filter = new MoodEventFilter(mockCollectionReference)
                .addEmotion(Emotion.SADNESS)
                .setDateRange(startDate, null)
                .setUser("user999")
                .setSortField("timestamp", Query.Direction.ASCENDING);

        Query builtQuery = filter.buildQuery();

        verify(mockCollectionReference).whereIn(eq("emotion"), anyList());
        verify(mockQuery).whereGreaterThanOrEqualTo(eq("created"), eq(startDate));
        verify(mockCollectionReference).whereEqualTo(eq("uid"), eq("user999"));
        verify(mockQuery).orderBy(eq("timestamp"), eq(Query.Direction.ASCENDING));

        assertEquals(mockQuery, builtQuery);
    }


    /**
     * Test repeated calls to setDateRange with valid parameters.
     * The last call should determine the date range used.
     */
    @Test
    public void testOverwritingDateRange() {
        Date initialStart = new Date(11000L);
        Date initialEnd = new Date(12000L);
        Date newStart = new Date(13000L);
        Date newEnd = new Date(14000L);

        MoodEventFilter filter = new MoodEventFilter(mockCollectionReference)
                .setDateRange(initialStart, initialEnd)
                .setDateRange(newStart, newEnd);

        Query builtQuery = filter.buildQuery();

        verify(mockQuery).whereGreaterThanOrEqualTo(eq("created"), eq(newStart));
        verify(mockQuery).whereLessThanOrEqualTo(eq("created"), eq(newEnd));
        assertEquals(mockQuery, builtQuery);
    }


    /**
     * Test combining multiple sort field calls with other filters.
     * The final sort field should be the one used.
     */
    @Test
    public void testMultipleSortFieldAndOtherFilters() {
        Date startDate = new Date(15000L);
        Date endDate = new Date(16000L);

        MoodEventFilter filter = new MoodEventFilter(mockCollectionReference)
                .addEmotion(Emotion.HAPPINESS)
                .setUser("userABC")
                .setSortField("created", Query.Direction.ASCENDING)
                .setDateRange(startDate, endDate)
                .setSortField("updated", Query.Direction.DESCENDING);

        Query builtQuery = filter.buildQuery();

        verify(mockQuery).orderBy(eq("updated"), eq(Query.Direction.DESCENDING));
        verify(mockCollectionReference).whereIn(eq("emotion"), anyList());
        verify(mockQuery).whereGreaterThanOrEqualTo(eq("created"), eq(startDate));
        verify(mockQuery).whereLessThanOrEqualTo(eq("created"), eq(endDate));
        verify(mockCollectionReference).whereEqualTo(eq("uid"), eq("userABC"));

        assertEquals(mockQuery, builtQuery);
    }


    /**
     * Test that using addEmotions with a non-empty list behaves as expected.
     */
    @Test
    public void testAddEmotionsMethod() {
        List<Emotion> emotionList = new ArrayList<>();
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
     * Test that a combination of all possible filters in various orders is handled correctly.
     */
    @Test
    public void testComplexFilterCombination() {
        Date startDate = new Date(17000L);
        Date endDate = new Date(18000L);

        MoodEventFilter filter = new MoodEventFilter(mockCollectionReference)
                .setUser("complexUser")
                .addEmotion(Emotion.SADNESS)
                .setDateRange(startDate, null)
                .addEmotion(Emotion.HAPPINESS)
                .setSortField("created", Query.Direction.ASCENDING)
                .setDateRange(startDate, endDate);

        Query builtQuery = filter.buildQuery();

        verify(mockCollectionReference).whereIn(eq("emotion"), anyList());
        verify(mockCollectionReference).whereEqualTo(eq("uid"), eq("complexUser"));
        verify(mockQuery).whereGreaterThanOrEqualTo(eq("created"), eq(startDate));
        verify(mockQuery).whereLessThanOrEqualTo(eq("created"), eq(endDate));
        verify(mockQuery).orderBy(eq("created"), eq(Query.Direction.ASCENDING));

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
        List<Emotion> emotionsList = new ArrayList<>();
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


    /**
     * Test that the query building remains stable after a long chain of filter modifications.
     */
    @Test
    public void testLongChainedModifications() {
        MoodEventFilter filter = new MoodEventFilter(mockCollectionReference);

        filter.setUser("userLongChain")
                .addEmotion(Emotion.HAPPINESS)
                .setDateRange(new Date(19000L), new Date(20000L))
                .setSortField("created", Query.Direction.ASCENDING)
                .addEmotion(Emotion.SADNESS)
                .addEmotion(Emotion.ANGER)
                .setDateRange(new Date(19500L), new Date(20500L))
                .setSortField("updated", Query.Direction.DESCENDING)
                .setUser("userFinal");

        Query builtQuery = filter.buildQuery();

        verify(mockQuery).whereGreaterThanOrEqualTo(eq("created"), eq(new Date(19500L)));
        verify(mockQuery).whereLessThanOrEqualTo(eq("created"), eq(new Date(20500L)));
        verify(mockQuery).orderBy(eq("updated"), eq(Query.Direction.DESCENDING));
        verify(mockCollectionReference).whereIn(eq("emotion"), anyList());

        assertEquals(mockQuery, builtQuery);
    }
}