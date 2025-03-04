package com.kernelcrew.moodapp.ui;

import android.os.Bundle;
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
import com.kernelcrew.moodapp.R;
import com.kernelcrew.moodapp.ui.Mood;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MoodHistory extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mood_history, container, false);

        MaterialToolbar toolbar = view.findViewById(R.id.topAppBar);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewMoodHistory);

        List<Mood> moods = new ArrayList<>();
        addMood(moods, "4", "David", "Angry", 1709232000000L);
        addMood(moods, "1", "Alice", "Happy", 1709491200000L);
        addMood(moods, "2", "Bob", "Sad", 1709404800000L);
        addMood(moods, "3", "Charlie", "Excited", 1709318400000L);

        Collections.sort(moods, new Comparator<Mood>() {
            @Override
            public int compare(Mood mood1, Mood mood2) {
                return Long.compare(mood2.getTimestamp(), mood1.getTimestamp()); // reversed order
            }
        });


        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        MoodHistoryAdapter adapter = new MoodHistoryAdapter(moods);
        recyclerView.setAdapter(adapter);

        toolbar.setNavigationOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.homeFeed);
        });

        return view;
    }

    private void addMood(List<Mood> moods, String id, String userName, String moodText, long timestamp) {
        moods.add(new Mood(id, userName, moodText, timestamp));
    }
}
