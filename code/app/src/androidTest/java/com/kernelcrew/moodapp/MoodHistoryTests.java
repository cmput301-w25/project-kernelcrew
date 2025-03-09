package com.kernelcrew.moodapp;

import static androidx.fragment.app.testing.FragmentScenario.launchInContainer;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.util.Log;
import android.view.View;
import android.widget.ListView;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.android.material.appbar.MaterialToolbar;
import com.kernelcrew.moodapp.data.MoodEvent;
import com.kernelcrew.moodapp.ui.HomeFeed;
import com.kernelcrew.moodapp.ui.MainActivity;
import com.kernelcrew.moodapp.ui.MoodAdapter;
import com.kernelcrew.moodapp.ui.MoodHistory;
import com.kernelcrew.moodapp.ui.MoodHistoryAdapter;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MoodHistoryTests {

    @Test
    public void moodEventsListIsDisplayed() {
        FragmentScenario<MoodHistory> scenario = FragmentScenario.launchInContainer(
                MoodHistory.class,
                null,
                R.style.Theme_MoodApp
        );

        scenario.moveToState(Lifecycle.State.RESUMED);

        // Check if the RecyclerView is displayed
        onView(withId(R.id.recyclerViewMoodHistory)).check(matches(isDisplayed()));
    }

    @Test
    public void moodEventsAreSortedInDescendingOrder() {
        FragmentScenario<MoodHistory> scenario = FragmentScenario.launchInContainer(
                MoodHistory.class,
                null,
                R.style.Theme_MoodApp
        );

        scenario.onFragment(fragment -> {
            RecyclerView recyclerView = fragment.getView().findViewById(R.id.recyclerViewMoodHistory);
            MoodHistoryAdapter adapter = (MoodHistoryAdapter) recyclerView.getAdapter();

            // Ensure adapter is not null
            assertNotNull("Adapter should not be null", adapter);

            List<MoodEvent> moods = adapter.getItems();

            // Ensure there are items to check
            assertTrue("Mood list should not be empty", !moods.isEmpty());

            // Check sorting
            for (int i = 0; i < moods.size() - 1; i++) {
                assertTrue("Moods should be sorted in descending order",
                        moods.get(i).getCreated() >= moods.get(i + 1).getCreated());
            }
        });
    }

    @Test
    public void searchBarCanBeTypedInto() {
        FragmentScenario<MoodHistory> scenario = FragmentScenario.launchInContainer(
                MoodHistory.class,
                null,
                R.style.Theme_MoodApp
        );

        // Type text into search bar
        onView(withId(R.id.searchInput)).perform(typeText("Happy"), closeSoftKeyboard());

        // Verify text is in the search bar
        onView(withId(R.id.searchInput)).check(matches(withText("Happy")));
    }

}
