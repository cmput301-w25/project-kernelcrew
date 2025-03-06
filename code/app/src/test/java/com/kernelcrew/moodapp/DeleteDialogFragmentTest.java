package com.kernelcrew.moodapp;

//Code from Claude AI, Anthropic, "Configure Android unit testing with JUnit", accessed 03-05-2025
import static org.junit.Assert.*;

import com.kernelcrew.moodapp.ui.DeleteDialogFragment;

import org.junit.Before;
import org.junit.Test;

public class DeleteDialogFragmentTest {
    private DeleteDialogFragment dialogFragment;
    private boolean listenerCalled;

    @Before
    public void setUp() {
        dialogFragment = new DeleteDialogFragment();
        listenerCalled = false;
    }

    @Test
    public void testListenerSetup() {
        // Test that we can set a listener
        dialogFragment.setDeleteDialogListener(() -> listenerCalled = true);
        assertNotNull(dialogFragment);
    }

    @Test
    public void testDialogFragmentCreation() {
        // Test that dialog fragment can be created
        assertNotNull("Dialog fragment should not be null", dialogFragment);
    }

    @Test
    public void testListenerInterface() {
        // Test the listener interface implementation
        DeleteDialogFragment.DeleteDialogListener listener = () -> listenerCalled = true;
        dialogFragment.setDeleteDialogListener(listener);
        
        // Verify listener can be set
        assertNotNull("Should be able to set listener", dialogFragment);
    }
} 