// MapTouchWrapper.java
package com.kernelcrew.moodapp.ui;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class MapTouchWrapper extends FrameLayout {
    private Runnable onTouchListener;

    public MapTouchWrapper(Context context) {
        super(context);
    }

    public MapTouchWrapper(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOnTouchListener(Runnable listener) {
        this.onTouchListener = listener;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (onTouchListener != null) {
            onTouchListener.run();
        }
        return super.dispatchTouchEvent(ev);
    }
}