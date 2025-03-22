package com.kernelcrew.moodapp.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.kernelcrew.moodapp.R;
import com.kernelcrew.moodapp.data.MoodEvent;
import com.kernelcrew.moodapp.data.MoodEventProvider;
import com.kernelcrew.moodapp.ui.components.DefaultFilterBarFragment;
import com.kernelcrew.moodapp.ui.components.FilterBarFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Fragment responsible for displaying a history of user's mood events using the new filtering logic.
 */
public class MoodHistory extends DefaultFilterBarFragment implements MoodHistoryAdapter.OnItemClickListener {

    /** RecyclerView for displaying mood history items */
    private RecyclerView recyclerView;

    /** Adapter for binding mood data to the RecyclerView */
    public MoodHistoryAdapter adapter;

    /** Provider for accessing mood events data */
    private MoodEventProvider provider;

    /** Firebase ListenerRegistration so we can remove it when needed */
    private ListenerRegistration snapshotListener;

    /** Child fragment that handles search & filter UI */
    private FilterBarFragment searchNFilterFragment;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mood_history, container, false);

        // Initialize the provider
        provider = MoodEventProvider.getInstance();

        // Set up the top app bar and the RecyclerView
        MaterialToolbar toolbar = view.findViewById(R.id.topAppBar);
        recyclerView = view.findViewById(R.id.recyclerViewMoodHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Create and set the adapter
        adapter = new MoodHistoryAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        // Handle back button
        toolbar.setNavigationOnClickListener(v -> handleBackButton());

        // Get our filter bar child fragment
        searchNFilterFragment = (FilterBarFragment) getChildFragmentManager()
                .findFragmentById(R.id.moodhistory_filterBarFragment);

        // When FilterBar changes, build the Firestore query and listen for changes
        if (searchNFilterFragment != null) {
            searchNFilterFragment.setOnFilterChangedListener(filter -> {
                 filter.setUsers(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());

                // Remove any existing snapshot listener to avoid duplicates
                if (snapshotListener != null) {
                    snapshotListener.remove();
                    snapshotListener = null;
                }

                // Build the query and attach a snapshot listener
                snapshotListener = filter.buildQuery()
                        .addSnapshotListener((snapshots, error) -> {
                            if (error != null) {
                                Log.w("MoodHistory", "Listen failed.", error);
                                return;
                            }
                            if (snapshots == null) {
                                Log.w("MoodHistory", "No snapshot data received.");
                                return;
                            }

                            // Convert query results into MoodEvent objects
                            List<MoodEvent> moodList = new ArrayList<>();
                            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                                MoodEvent mood = doc.toObject(MoodEvent.class);
                                if (mood != null) {
                                    // Keep track of document ID if you need it
                                    mood.setId(doc.getId());
                                    moodList.add(mood);
                                }
                            }

                            // Update the adapter
                            adapter.setMoods(moodList);
                        });
            });
        }

        return view;
    }

    /**
     * We no longer need onResume/onPause to set/remove a Firestore listener because
     * the FilterBarFragment's callback sets up the listener whenever the user changes filters.
     */
    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Remove any snapshot listener if desired
        if (snapshotListener != null) {
            snapshotListener.remove();
            snapshotListener = null;
        }
    }

    /**
     * Called when a user clicks on a mood event row. Navigates to the mood details screen.
     */
    @Override
    public void onItemClick(String moodEventId) {
        // Navigate to MoodDetails fragment with the moodEventId as an argument
        Bundle args = new Bundle();
        args.putString("moodEventId", moodEventId);
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        navController.navigate(R.id.action_moodHistory_to_moodDetails, args);
    }

    /**
     * Handles the navigation back action.
     */
    private void handleBackButton() {
        Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).popBackStack();
    }
}