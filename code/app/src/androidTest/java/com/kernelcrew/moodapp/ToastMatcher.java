package com.kernelcrew.moodapp;

//Code from Anthropic, Claude 3.7 Sonnet, "Create a simplified ToastMatcher for Espresso tests", accessed 05-12-2024
//Adapted from: https://stackoverflow.com/a/33387980 (Licensed under CC BY-SA 3.0)

import android.view.WindowManager;
import androidx.test.espresso.Root;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * Custom matcher for finding and verifying Toast messages in Espresso tests.
 * <p>
 * This matcher provides functionality to match against Toast messages displayed
 * in the UI during test execution. It works by identifying window types that 
 * are specifically used for Toast notifications (TYPE_TOAST).
 * <p>
 * Usage example:
 * <pre>
 * onView(withText("Your Toast Message"))
 *     .inRoot(new ToastMatcher())
 *     .check(matches(isDisplayed()));
 * </pre>
 * 
 * @author Anthropic, Claude 3.7 Sonnet
 * @version 1.0
 * @see androidx.test.espresso.Root
 * @see org.hamcrest.TypeSafeMatcher
 */
public class ToastMatcher extends TypeSafeMatcher<Root> {

    /**
     * Describes the matcher for error output.
     * <p>
     * This method is used by the testing framework to generate error messages
     * when a match fails. It appends the description "is toast" to the
     * mismatch description.
     *
     * @param description The description to which the matcher description is appended
     */
    @Override
    public void describeTo(Description description) {
        description.appendText("is toast");
    }

    /**
     * Checks if the current Root object matches a Toast window.
     * <p>
     * The method examines the window layout parameters to determine if the window 
     * is of type TYPE_TOAST, which is specifically used for Toast notifications
     * in Android.
     *
     * @param root The Root object to match against
     * @return true if the Root represents a Toast window, false otherwise
     */
    @Override
    public boolean matchesSafely(Root root) {
        int type = root.getWindowLayoutParams().get().type;
        if (type == WindowManager.LayoutParams.TYPE_TOAST) {
            return true;
        }
        return false;
    }
} 