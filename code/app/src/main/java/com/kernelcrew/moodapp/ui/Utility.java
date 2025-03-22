package com.kernelcrew.moodapp.ui;

import android.app.Activity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.util.Objects;

/**
 * A utility class for UI related actions.
 */
public class Utility {
    /**
     * Hide the keyboard if it is open
     *
     * @param activity The current activity where the keyboard is active.
     */
    // Taha used the following
    // https://stackoverflow.com/questions/4165414/how-to-hide-soft-keyboard-on-android-after-clicking-outside-edittext
    // Changed the above code due to null value issues.
    public static void hideSoftKeyboard(Activity activity) {
        if (activity == null) {
            return;
        }
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);

        if (inputMethodManager == null) {
            return;
        }

        if (activity.getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }
}
