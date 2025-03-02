package com.kernelcrew.moodapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.appbar.MaterialToolbar;
import com.kernelcrew.moodapp.R;

import java.util.ArrayList;

public class MoodHistory extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mood_history, container, false);

        MaterialToolbar toolbar = view.findViewById(R.id.topAppBar);
        ListView listView = view.findViewById(R.id.listViewMoodHistory);

        // Sample MoodEvent data
        ArrayList<MoodEvent> moodEvents = new ArrayList<>();

        addMoodEvent(moodEvents, 2025, 2, 16, 15);
        addMoodEvent(moodEvents, 2025, 2, 15, 14);
        addMoodEvent(moodEvents, 2025, 2, 14, 13);
        addMoodEvent(moodEvents, 2025, 2, 13, 12);
        addMoodEvent(moodEvents, 2025, 2, 12, 11);
        addMoodEvent(moodEvents, 2025, 2, 11, 10);
        addMoodEvent(moodEvents, 2025, 2, 10, 9);
        addMoodEvent(moodEvents, 2025, 2, 9, 8);
        addMoodEvent(moodEvents, 2025, 2, 9, 7);
        addMoodEvent(moodEvents, 2025, 2, 9, 6);
        addMoodEvent(moodEvents, 2025, 2, 9, 5);
        addMoodEvent(moodEvents, 2025, 2, 9, 4);
        addMoodEvent(moodEvents, 2025, 2, 9, 3);
        addMoodEvent(moodEvents, 2025, 2, 9, 2);
        addMoodEvent(moodEvents, 2025, 2, 9, 1);

        // Set up adapter and connect it to the ListView
        MoodEventArrayAdapter adapter = new MoodEventArrayAdapter(requireContext(), moodEvents);
        listView.setAdapter(adapter);

        toolbar.setNavigationOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.homeFeed);
        });

        return view;
    }

    private void addMoodEvent(ArrayList<MoodEvent> moodEvents, int year, int month, int day, int eventNumber) {
        moodEvents.add(new MoodEvent(year, month, day, eventNumber));
    }
}
