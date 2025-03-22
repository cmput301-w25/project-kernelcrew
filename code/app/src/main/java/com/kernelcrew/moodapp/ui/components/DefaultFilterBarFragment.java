package com.kernelcrew.moodapp.ui.components;

import android.annotation.SuppressLint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import com.kernelcrew.moodapp.data.Utility;

public class DefaultFilterBarFragment extends FilterBarFragment {
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void setupKeyboardHiding(View view) {
        if (!(view instanceof EditText)) {
            view.setOnTouchListener((v, event) -> {
                assert getActivity() != null;
                Utility.hideSoftKeyboard(getActivity());

                // Stop the editing of all textboxes
                View currentFocus = getActivity().getCurrentFocus();
                if (currentFocus instanceof EditText) {
                    currentFocus.clearFocus();
                }

                return false;
            });
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                setupKeyboardHiding(child);
            }
        }
    }
}
