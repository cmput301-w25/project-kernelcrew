package com.kernelcrew.moodapp.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

// All MapTouchWrapper code is from OpenAI, ChatGPT 4o, "Create custom FrameLayout wrapper to intercept touch events for embedded Google Maps", accessed 03-30-2025
/**
 * A custom FrameLayout that intercepts touch events and notifies a listener when touched.
 *
 * This is particularly useful when embedding interactive views like maps inside scrollable containers,
 * such as ScrollViews. By notifying a listener on touch, it allows the parent to handle
 * scroll disabling or other gesture-related behaviors.
 */
public class MapTouchWrapper extends FrameLayout {

    /**
     * Runnable that is executed when a touch event occurs on the wrapper.
     * Typically used to notify a ScrollView to stop intercepting touch events.
     */
    private Runnable onTouchListener;

    /**
     * Constructs a new MapTouchWrapper with the specified context.
     *
     * @param context the application context
     */
    public MapTouchWrapper(Context context) {
        super(context);
    }

    /**
     * Constructs a new MapTouchWrapper with the specified context and attribute set.
     *
     * @param context the application context
     * @param attrs the attribute set from XML
     */
    public MapTouchWrapper(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Sets a listener that will be executed when the view is touched.
     *
     * @param listener the Runnable to run on touch events
     */
    public void setOnTouchListener(Runnable listener) {
        this.onTouchListener = listener;
    }

    /**
     * Intercepts and dispatches touch events to the view hierarchy.
     * Executes the provided onTouchListener if one is set.
     *
     * @param ev the MotionEvent object containing full information about the event
     * @return true if the event was handled, false otherwise
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (onTouchListener != null) {
            onTouchListener.run();
        }
        return super.dispatchTouchEvent(ev);
    }
}