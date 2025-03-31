package com.kernelcrew.moodapp;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.firebase.firestore.ListenerRegistration;
import com.kernelcrew.moodapp.data.CombinedListenerComposer;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for {@link CombinedListenerComposer}.
 */
public class CombinedListenerComposerTest {

    /**
     * Test that remove() calls remove on each ListenerRegistration.
     */
    @Test
    public void testRemoveCallsAllRegistrations() {
        ListenerRegistration reg1 = mock(ListenerRegistration.class);
        ListenerRegistration reg2 = mock(ListenerRegistration.class);
        List<ListenerRegistration> mockList = new ArrayList<>();
        mockList.add(reg1);
        mockList.add(reg2);

        CombinedListenerComposer composer = new CombinedListenerComposer(mockList);
        composer.remove();

        // check if the removals worked
        verify(reg1, times(1)).remove();
        verify(reg2, times(1)).remove();
    }

    /**
     * Test that remove() does not crash with an empty list of registrations.
     */
    @Test
    public void testRemoveWithEmptyList() {
        CombinedListenerComposer composer = new CombinedListenerComposer(new ArrayList<>());
        // Just ensuring it doesnt throw any errors
        composer.remove();
    }
}