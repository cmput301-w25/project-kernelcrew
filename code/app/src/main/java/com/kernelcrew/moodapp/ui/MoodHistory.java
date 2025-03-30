package com.kernelcrew.moodapp.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.kernelcrew.moodapp.R;
import com.kernelcrew.moodapp.data.MoodEvent;
import com.kernelcrew.moodapp.data.MoodEventProvider;
import com.kernelcrew.moodapp.ui.components.FilterBarFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment responsible for displaying a history of user's mood events using the new filtering logic.
 */
public class MoodHistory extends Fragment implements MoodHistoryAdapter.OnItemClickListener {

    /** RecyclerView for displaying mood history items */
    private RecyclerView recyclerView;

    NavigationBarView navigationBar;

    private BottomNavBarController navBarController;

    /** Adapter for binding mood data to the RecyclerView */
    public MoodHistoryAdapter adapter;

    /** Provider for accessing mood events data */
    private MoodEventProvider provider;

    /** Firebase ListenerRegistration so we can remove it when needed */
    private ListenerRegistration snapshotListener;

    /** Child fragment that handles search & filter UI */
    private FilterBarFragment searchNFilterFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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

        navigationBar = view.findViewById(R.id.bottom_navigation);
        navBarController = new BottomNavBarController(navigationBar);
        navigationBar.setSelectedItemId(R.id.page_myHistory);

        // Get our filter bar child fragment
        searchNFilterFragment = (FilterBarFragment) getChildFragmentManager()
                .findFragmentById(R.id.moodhistory_filterBarFragment);

        // When FilterBar changes, build the Firestore query and listen for changes
        if (searchNFilterFragment != null) {
            searchNFilterFragment.setOnFilterChangedListener(filter -> {
                filter.setUser(FirebaseAuth.getInstance().getCurrentUser().getUid());

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
                                    mood.setId(doc.getId());
                                    moodList.add(mood);
                                }
                            }

                            // Apply client-side filtering based on search query from reason
                            String searchWord = filter.getSearchQuery().trim().toLowerCase();
                            if (!searchWord.isEmpty()) {
                                List<MoodEvent> filteredList = new ArrayList<>();
                                for (MoodEvent m : moodList) {
                                    if (m.getReason() != null &&
                                            m.getReason().toLowerCase().contains(searchWord)) {
                                        filteredList.add(m);
                                    }
                                }
                                moodList = filteredList;
                            }

                            // Sort newest-first by timestamp
                            moodList.sort((m1, m2) -> Long.compare(m2.getTimestamp(), m1.getTimestamp()));

                            // Update the adapter
                            adapter.setMoods(moodList);
                        });
            });
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navBarController.bind(view);
    }

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
