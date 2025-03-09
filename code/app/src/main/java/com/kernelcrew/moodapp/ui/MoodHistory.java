package com.kernelcrew.moodapp.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.kernelcrew.moodapp.R;
import com.kernelcrew.moodapp.data.MoodEvent;
import com.kernelcrew.moodapp.data.MoodEventController;
import com.kernelcrew.moodapp.data.MoodEventProvider;

import java.util.ArrayList;
import java.util.List;


/**
 * Fragment responsible for displaying a history of user's mood events.
 *
 */
public class MoodHistory extends Fragment implements MoodHistoryAdapter.OnItemClickListener {

    /** RecyclerView for displaying mood history items */
    private RecyclerView recyclerView;

    /** Adapter for binding mood data to the RecyclerView */
    public MoodHistoryAdapter adapter;

    /** Provider for accessing mood events data */
    MoodEventProvider provider;

    /** List to store mood events */
    private List<MoodEvent> moods = new ArrayList<>();


    /**
     * Creates and returns the view hierarchy associated with the fragment.
     * <p>
     * Initializes the UI components including the toolbar and RecyclerView,
     * sets up the adapter, and initiates the data fetching process.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mood_history, container, false);

        provider = MoodEventProvider.getInstance();

        MaterialToolbar toolbar = view.findViewById(R.id.topAppBar);
        recyclerView = view.findViewById(R.id.recyclerViewMoodHistory);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new MoodHistoryAdapter(moods,this);
        recyclerView.setAdapter(adapter);

        // Fetch mood events from Firebase
        fetchMoodEvents();
        toolbar.setNavigationOnClickListener(v -> handleBackButton());

        return view;
    }

    /**
     * Fetches mood events from Firestore database.
     * <p>
     * Sets up a snapshot listener to continuously update the UI when data changes
     * in the Firestore database. Sorted by timestamp in descending order so newest
     * mood events appear first.
     * </p>
     */
    private void fetchMoodEvents() {
        MoodEventController.getInstance().getMoodEvents().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                // Listen for changes in the "moods" collection
                provider.addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.w("MoodHistory", "Listen failed.", error);
                        return;
                    }

                    List<MoodEvent> moodList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        MoodEvent moodFromDB = doc.toObject(MoodEvent.class);
                        moodList.add(moodFromDB);
                    }

                    // Sort moods by timestamp in descending order
                    moodList.sort((mood1, mood2) -> Long.compare(mood2.getTimestamp(), mood1.getTimestamp()));
                    adapter.setMoods(moodList);
                });
                
            } else {
                // Handle the error
                Exception e = task.getException();
                if (e != null) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Handles the navigation back action when the back button is pressed.
     * <p>
     * Uses the NavController to pop the back stack, returning to the previous fragment.
     * </p>
     */
    private void handleBackButton() {
        androidx.navigation.fragment.NavHostFragment.findNavController(this).popBackStack();
    }


    /**
     * Callback method from the {@link MoodHistoryAdapter.OnItemClickListener} interface.
     * <p>
     * Triggered when a user clicks on a mood event in the list. Navigates to the
     * MoodDetails fragment passing the selected mood event ID as an argument.
     * </p>
     *
     * @param moodEventId The ID of the selected mood event
     */
    @Override
    public void onItemClick(String moodEventId) {
        // Navigate to MoodDetails fragment with the moodEventId as an argument
        Bundle args = new Bundle();
        args.putString("moodEventId", moodEventId);

        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        navController.navigate(R.id.action_moodHistory_to_moodDetails, args);
    }
}