package com.kernelcrew.moodapp;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.kernelcrew.moodapp.data.CombinedListener;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

/**
 * Unit tests for {@link CombinedListener}.
 */
public class CombinedListenerTest {

    /**
     * Test that the onEvent method can be invoked without error and handles null parameters safely.
     */
    @Test
    public void testOnEventInvocationWithNulls() {
        CombinedListener listener = new CombinedListener() {
            @Override
            public void onEvent(List<DocumentSnapshot> documents, FirebaseFirestoreException error) {
                // nothing (for testing)
            }
        };

        listener.onEvent(null, null);

        assertNotNull("Listener should be instantiated", listener);
    }

    /**
     * Test that the onEvent method can receive non-null documents and a mock error gracefully.
     */
    @Test
    public void testOnEventInvocationWithData() {
        CombinedListener listener = new CombinedListener() {
            @Override
            public void onEvent(List<DocumentSnapshot> documents, FirebaseFirestoreException error) {
                // nothing (for testing)
            }
        };

        List<DocumentSnapshot> mockDocuments = Collections.singletonList(mock(DocumentSnapshot.class));
        FirebaseFirestoreException mockError = mock(FirebaseFirestoreException.class);

        listener.onEvent(mockDocuments, mockError);

        // No specific assertion besides no exception thrown
        assertNotNull("Listener should be instantiated", listener);
    }
}