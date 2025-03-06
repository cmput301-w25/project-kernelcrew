package com.kernelcrew.moodapp;

//Code from Claude AI, Anthropic, "Create Espresso UI tests for Android dialog interactions", accessed 03-05-2025
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.kernelcrew.moodapp.ui.DeleteDialogFragment;
import com.kernelcrew.moodapp.ui.MainActivity;

/**
 * UI tests for delete dialog functionality
 */
@RunWith(AndroidJUnit4.class)
public class DeleteDialogUITest {
    
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = 
            new ActivityScenarioRule<>(MainActivity.class);

    private DeleteDialogFragment dialogFragment;

    @Before
    public void setUp() {
        dialogFragment = new DeleteDialogFragment();
        final ActivityScenario<MainActivity> scenario = activityRule.getScenario();
        scenario.onActivity(new ActivityScenario.ActivityAction<MainActivity>() {
            @Override
            public void perform(MainActivity activity) {
                dialogFragment.show(activity.getSupportFragmentManager(), "dialog");
            }
        });
    }

    /**
     * Tests delete dialog appears with correct elements
     * US 01.06.01
     */
    @Test
    public void testDeleteDialogDisplay() {
        // Verify dialog elements
        onView(withText("Delete this mood event?"))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        onView(withText("Are you sure you want to delete this mood event? It cannot be recovered after deletion."))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        onView(withText("No, Keep It"))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        onView(withText("Yes, Delete It"))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    /**
     * Tests delete confirmation flow
     * US 01.06.01
     */
    @Test
    public void testDeleteConfirmation() {
        // First verify dialog is showing
        onView(withText("Delete this mood event?"))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        // Click delete confirmation button
        onView(withText("Yes, Delete It"))
                .inRoot(isDialog())
                .perform(click());

        // Verify dialog is dismissed
        onView(withText("Delete this mood event?"))
                .check(doesNotExist());
    }

    /**
     * Tests delete cancellation flow
     * US 01.06.01
     */
    @Test
    public void testDeleteCancellation() {
        // First verify dialog is showing
        onView(withText("Delete this mood event?"))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        // Click keep button
        onView(withText("No, Keep It"))
                .inRoot(isDialog())
                .perform(click());

        // Verify dialog is dismissed
        onView(withText("Delete this mood event?"))
                .check(doesNotExist());
    }
}