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
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.kernelcrew.moodapp.R;
import com.kernelcrew.moodapp.data.Emotion;
import com.kernelcrew.moodapp.data.MoodEvent;
import com.kernelcrew.moodapp.data.MoodEventController;
import com.kernelcrew.moodapp.data.MoodEventProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MoodHistory extends Fragment implements MoodHistoryAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private MoodHistoryAdapter adapter;
    MoodEventProvider provider;
    private List<MoodEvent> moods = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mood_history, container, false);

        provider = MoodEventProvider.getInstance();

        String sourceScreen = getArguments() != null ? getArguments().getString("sourceScreen", "home") : "home";
        MaterialToolbar toolbar = view.findViewById(R.id.topAppBar);
        recyclerView = view.findViewById(R.id.recyclerViewMoodHistory);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new MoodHistoryAdapter(moods, this);
        recyclerView.setAdapter(adapter);

        // Fetch mood events from Firebase
        fetchMoodEvents();

        toolbar.setNavigationOnClickListener(v -> handleBackButton());

        return view;
    }

    private void fetchMoodEvents() {
        MoodEventController.getInstance().getMoodEvents().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                moods.clear();
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

    private void handleBackButton() {
        androidx.navigation.fragment.NavHostFragment.findNavController(this).popBackStack();
    }

    @Override
    public void onItemClick(String moodEventId) {
        // Navigate to MoodDetails fragment with the moodEventId as an argument
        Bundle args = new Bundle();
        args.putString("moodEventId", moodEventId);

        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        navController.navigate(R.id.action_moodHistory_to_moodDetails, args);
    }
}