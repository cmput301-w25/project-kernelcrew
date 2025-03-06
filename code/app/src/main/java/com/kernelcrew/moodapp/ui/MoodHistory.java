package com.kernelcrew.moodapp.ui;

import android.os.Bundle;
import android.text.TextUtils;
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
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.kernelcrew.moodapp.R;
import com.kernelcrew.moodapp.data.MoodEventController;
import com.kernelcrew.moodapp.ui.Mood;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class MoodHistory extends Fragment {

    private RecyclerView recyclerView;
    private MoodHistoryAdapter adapter;
    private List<Mood> moods = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mood_history, container, false);
        String sourceScreen = getArguments() != null ? getArguments().getString("sourceScreen", "home") : "home";
        MaterialToolbar toolbar = view.findViewById(R.id.topAppBar);
        recyclerView = view.findViewById(R.id.recyclerViewMoodHistory);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new MoodHistoryAdapter(moods);
        recyclerView.setAdapter(adapter);

        // Fetch mood events from Firebase
        fetchMoodEvents();

        toolbar.setNavigationOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);

            // Navigate based on the sourceScreen
            if ("profile".equals(sourceScreen)) {
                navController.navigate(R.id.myProfile);
            } else if ("home".equals(sourceScreen)) {
                navController.navigate(R.id.homeFeed);
            }
        });

        return view;
    }

    private void fetchMoodEvents() {
        MoodEventController.getInstance().getMoodEvents().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                moods.clear();
                for (DocumentSnapshot document : task.getResult()) {
                    String id = document.getId();
                    String userName = document.getString("userId");
                    String moodText = document.getString("emotion");
                    Timestamp createdTimestamp = document.getTimestamp("created");

                    long timestamp = (createdTimestamp != null) ? createdTimestamp.getSeconds() * 1000 : -1;

                    if (timestamp != -1) {
                        moods.add(new Mood(id, userName, moodText, timestamp));
                    }
                }

                // Sort moods by timestamp in descending order
                Collections.sort(moods, (mood1, mood2) -> Long.compare(mood2.getTimestamp(), mood1.getTimestamp()));

                // Notify adapter of data change
                adapter.notifyDataSetChanged();
            } else {
                // Handle the error
                Exception e = task.getException();
                if (e != null) {
                    e.printStackTrace();
                }
            }
        });
    }
}
