package com.kernelcrew.moodapp;

import static org.junit.Assert.assertEquals;
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
import java.util.Date;
import org.junit.Before;
import org.junit.Test;

public class MoodEventFilterTest {

    private CollectionReference mockCollectionReference;
    private Query mockQuery;

    @Before
    public void setUp() {
        // Create a mock CollectionReference.
        mockCollectionReference = mock(CollectionReference.class);

        // Create a separate Query mock to simulate chained calls.
        mockQuery = mock(Query.class);

        // Stub chainable query methods to return our Query mock.
        when(mockCollectionReference.whereIn(anyString(), anyList())).thenReturn(mockQuery);
        when(mockQuery.whereGreaterThanOrEqualTo(anyString(), any(Date.class))).thenReturn(mockQuery);
        when(mockQuery.whereLessThanOrEqualTo(anyString(), any(Date.class))).thenReturn(mockQuery);
        when(mockQuery.orderBy(anyString(), any(Query.Direction.class))).thenReturn(mockQuery);
    }

    @Test
    public void testBuildQueryWithAllFilters() {
        // Prepare test dates.
        Date startDate = new Date(1000L);
        Date endDate = new Date(2000L);

        // Create a filter with an emotion, date range, and sort field.
        MoodEventFilter filter = new MoodEventFilter(mockCollectionReference)
                .addEmotion(Emotion.ANGER)
                .setDateRange(startDate, endDate)
                .setSortField("created", Query.Direction.ASCENDING);

        Query builtQuery = filter.buildQuery();

        // Verify that filtering methods were called with the proper parameters.
        verify(mockCollectionReference).whereIn(eq("emotion"), anyList());
        verify(mockQuery).whereGreaterThanOrEqualTo(eq("created"), eq(startDate));
        verify(mockQuery).whereLessThanOrEqualTo(eq("created"), eq(endDate));
        verify(mockQuery).orderBy(eq("created"), eq(Query.Direction.ASCENDING));

        // The final query should be our mockQuery since the chain calls return it.
        assertEquals(mockQuery, builtQuery);
    }

    @Test
    public void testBuildQueryWithoutFilters() {
        // When no filters are added, buildQuery should return the collection reference unchanged.
        MoodEventFilter filter = new MoodEventFilter(mockCollectionReference);
        Query builtQuery = filter.buildQuery();

        // In this case, the CollectionReference is returned directly as Query.
        assertEquals(mockCollectionReference, builtQuery);
    }
}
