package com.kernelcrew.moodapp.ui;

import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;

import com.google.android.material.navigation.NavigationBarView;
import com.kernelcrew.moodapp.R;

/**
 * Controller to manage a bottom nav bar.
 *
 * Construct in the onCreateView callback with a NavigationBarView fetched via findViewById.
 * Call bind(view) in the onViewCreated callback.
 */
public class BottomNavBarController implements NavigationBarView.OnItemSelectedListener {
    private final NavigationBarView navigationBar;
    private View view;

    public BottomNavBarController(NavigationBarView navigationBar) {
        this.navigationBar = navigationBar;
    }

    /**
     * Must be called in the onViewCreated callback because the view needs to have a nav host
     * controller connected to it.
     * @param view View passed to onViewCreated
     */
    public void bind(@NonNull View view) {
        this.view = view;
        this.navigationBar.setOnItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int page = 0;
        int itemId = item.getItemId();
        if (itemId == R.id.page_home) {
            page = R.id.homeFeed;
        } else if (itemId == R.id.page_createMoodEvent) {
            page = R.id.createMoodEvent;
        } else if (itemId == R.id.page_myProfile) {
            page = R.id.myProfile;
        } else {
            return false;
        }

        Navigation.findNavController(this.view).navigate(page);
        return false;
    }
}
