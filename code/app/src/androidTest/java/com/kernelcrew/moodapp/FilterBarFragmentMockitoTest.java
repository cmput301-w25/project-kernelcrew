// File: FilterBarFragmentMockitoTest.java
package com.kernelcrew.moodapp;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.material.button.MaterialButton;
import com.kernelcrew.moodapp.FirebaseEmulatorMixin;
import com.kernelcrew.moodapp.data.Emotion;
import com.kernelcrew.moodapp.data.MoodEventFilter;
import com.kernelcrew.moodapp.ui.components.FilterBarFragment;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashSet;

@RunWith(AndroidJUnit4.class)
public class FilterBarFragmentMockitoTest extends FirebaseEmulatorMixin {

    /**
     * Custom FragmentFactory that sets a given listener on the FilterBarFragment
     * before onCreateView is invoked.
     */
    public static class TestFilterBarFragmentFactory extends FragmentFactory {
        private final FilterBarFragment.OnFilterChangedListener listener;
        public TestFilterBarFragmentFactory(FilterBarFragment.OnFilterChangedListener listener) {
            this.listener = listener;
        }
        @Override
        public Fragment instantiate(ClassLoader classLoader, String className) {
            if (className.equals(FilterBarFragment.class.getName())) {
                FilterBarFragment fragment = new FilterBarFragment();
                fragment.setOnFilterChangedListener(listener);
                return fragment;
            }
            return super.instantiate(classLoader, className);
        }
    }

    /**
     * Helper method to invoke the private notifyFilterChanged() method via reflection.
     */
    private void invokeNotifyFilterChanged(FilterBarFragment fragment) {
        try {
            Method method = FilterBarFragment.class.getDeclaredMethod("notifyFilterChanged");
            method.setAccessible(true);
            method.invoke(fragment);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke notifyFilterChanged", e);
        }
    }

    /**
     * Verifies that when a filter is changed, the OnFilterChangedListener is called
     * with a MoodEventFilter containing the expected values.
     */
    @Test
    public void testOnFilterChangedListenerIsCalled() {
        // Create a mock listener.
        FilterBarFragment.OnFilterChangedListener mockListener = mock(FilterBarFragment.OnFilterChangedListener.class);
        // Supply the listener early using our custom FragmentFactory.
        TestFilterBarFragmentFactory factory = new TestFilterBarFragmentFactory(mockListener);
        FragmentScenario<FilterBarFragment> scenario = FragmentScenario.launchInContainer(
                FilterBarFragment.class,
                null,
                com.google.android.material.R.style.Theme_MaterialComponents,
                factory
        );

        scenario.onFragment(fragment -> {
            // Modify the filter – for example, set a user.
            MoodEventFilter filter = fragment.getMoodEventFilter();
            filter.setUser("mockUser");

            // Optionally, explicitly invoke notifyFilterChanged() to update the state.
            invokeNotifyFilterChanged(fragment);

            // Capture the argument passed to onFilterChanged() using Mockito's ArgumentCaptor.
            ArgumentCaptor<MoodEventFilter> captor = ArgumentCaptor.forClass(MoodEventFilter.class);
            verify(mockListener, atLeastOnce()).onFilterChanged(captor.capture());
            MoodEventFilter capturedFilter = captor.getValue();

            assertNotNull("The filter passed to the listener should not be null", capturedFilter);
            assertTrue("Filter should contain user set as mockUser",
                    capturedFilter.getSummary().contains("User: mockUser"));
        });
    }

    /**
     * Verifies that after applying several filters, the filter count button is updated correctly.
     */
    @Test
    public void testFilterCountButtonUpdate() {
        // For this test, a dummy listener is sufficient.
        FilterBarFragment.OnFilterChangedListener dummyListener = filter -> { /* no-op */ };
        TestFilterBarFragmentFactory factory = new TestFilterBarFragmentFactory(dummyListener);
        FragmentScenario<FilterBarFragment> scenario = FragmentScenario.launchInContainer(
                FilterBarFragment.class,
                null,
                com.google.android.material.R.style.Theme_MaterialComponents,
                factory
        );

        scenario.onFragment(fragment -> {
            // Set some filter values.
            MoodEventFilter filter = fragment.getMoodEventFilter();
            filter.setUser("userTest");
            Emotion sampleEmotion = Emotion.values()[0];
            HashSet<Emotion> emotions = new HashSet<>();
            emotions.add(sampleEmotion);
            filter.setEmotions(emotions);
            filter.setDateRange(new Date(5000), new Date(10000));

            // Invoke notifyFilterChanged() to update the UI.
            invokeNotifyFilterChanged(fragment);

            // Verify that the button displaying the filter count is updated.
            View view = fragment.getView();
            assertNotNull("Fragment view should not be null", view);
            MaterialButton countButton = view.findViewById(R.id.filterCountAndEdit);
            int expectedCount = fragment.getMoodEventFilter().count() - 1;
            assertEquals("Filter count button text should match the expected count",
                    String.valueOf(expectedCount), countButton.getText().toString());
        });
    }
}