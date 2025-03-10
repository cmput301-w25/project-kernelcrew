package com.kernelcrew.moodapp.ui;

import static org.junit.Assert.*;
import static org.mockito.ArgumentCaptor.*;
import static org.mockito.Mockito.*;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kernelcrew.moodapp.data.MoodEvent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Test class for {@link MoodHistoryAdapter}.
 * This class contains unit tests to verify the behavior of the {@link MoodHistoryAdapter},
 * including setting moods, binding data to views, and handling null list initialization.
 */
@RunWith(MockitoJUnitRunner.class)
public class MoodHistoryAdapterTest {

    @Mock
    private MoodHistoryAdapter.OnItemClickListener mockListener;

    @Mock
    private TextView mockTextDate;

    @Mock
    private TextView mockTextMoodEventNumber;

    private MoodHistoryAdapter adapter;
    private List<MoodEvent> testMoods;



    /**
     * Sets up the test environment before each test method is executed.
     */
    @Before
    public void setup() {
        testMoods = createTestMoodEvents();
        adapter = Mockito.spy(new MoodHistoryAdapter(testMoods, mockListener));
        doNothing().when(adapter).notifyDataSetChanged();
    }

    /**
     * Creates a list of test MoodEvent objects for demonstration or testing purposes.
     * This method generates two MoodEvent objects with predefined IDs and creation dates.
     *
     * @return A List of MoodEvent objects containing the two test MoodEvents.
     */
    private List<MoodEvent> createTestMoodEvents() {
        List<MoodEvent> moods = new ArrayList<>();
        Calendar cal = Calendar.getInstance();

        MoodEvent mood1 = new MoodEvent();
        mood1.setId("mood1_id");
        cal.set(2024, Calendar.MARCH, 1);
        mood1.setCreated(cal.getTime());

        MoodEvent mood2 = new MoodEvent();
        mood2.setId("mood2_id");
        cal.set(2024, Calendar.MARCH, 2);
        mood2.setCreated(cal.getTime());

        moods.add(mood1);
        moods.add(mood2);

        return moods;
    }

    /**
     * Represents a mood event with an ID.
     */
    @Test
    public void testSetMoods() {
        List<MoodEvent> newMoods = new ArrayList<>();
        MoodEvent newMood = new MoodEvent();
        newMood.setId("new_mood_id");
        newMoods.add(newMood);

        adapter.setMoods(newMoods);

        assertEquals(1, adapter.getItemCount());

        List<MoodEvent> retrievedItems = adapter.getItems();
        assertEquals(1, retrievedItems.size());
        assertEquals("new_mood_id", retrievedItems.get(0).getId());
    }

    /**
     * Creates a mock MoodViewHolder for testing purposes.
     * This method generates a mocked instance of the MoodViewHolder used within
     * the MoodHistoryAdapter.
     *
     * @return A mocked MoodViewHolder instance, ready for use in unit tests.
     */
    private MoodHistoryAdapter.MoodViewHolder createMockViewHolder() {
        View mockItemView = mock(View.class);  // Mock the itemView
        MoodHistoryAdapter.MoodViewHolder holder = spy(adapter.new MoodViewHolder(mockItemView));

        // Assign mock views
        holder.textDate = mockTextDate;
        holder.textMoodEventNumber = mockTextMoodEventNumber;

        return holder;
    }

    /**
     * Tests the onBindViewHolder method of MoodHistoryAdapter to ensure it correctly sets
     * the date and mood event number in the ViewHolder for a given position.
     */
    @Test
    public void testOnBindViewHolder() {
        MoodHistoryAdapter.MoodViewHolder mockHolder = createMockViewHolder();
        adapter.onBindViewHolder(mockHolder, 0);

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH);
        String expectedDate = dateFormat.format(testMoods.get(0).getCreated());

        ArgumentCaptor<String> dateCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockTextDate).setText(dateCaptor.capture());
        assertEquals(expectedDate, dateCaptor.getValue());

        verify(mockTextMoodEventNumber).setText("Mood Event 1");
    }

    /**
     * Tests the behavior of the MoodHistoryAdapter when it is initialized with a null list.
     */
    @Test
    public void testNullListInitialization() {
        MoodHistoryAdapter nullAdapter = new MoodHistoryAdapter(null, mockListener);
        assertEquals(0, nullAdapter.getItemCount());
    }
}
