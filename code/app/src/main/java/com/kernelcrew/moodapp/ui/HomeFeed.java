package com.kernelcrew.moodapp.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.kernelcrew.moodapp.R;
import com.kernelcrew.moodapp.data.MoodEvent;
import com.kernelcrew.moodapp.data.MoodEventFilter;
import com.kernelcrew.moodapp.data.MoodEventProvider;
import java.util.ArrayList;
import java.util.List;

public class HomeFeed extends Fragment {
    FirebaseAuth auth;
    FirebaseUser user;
    MoodEventProvider provider;

    TextView homeTextView;
    NavigationBarView navigationBar;
    BottomNavBarController navBarController;
    RecyclerView moodRecyclerView;
    MoodAdapter moodAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_feed, container, false);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        provider = MoodEventProvider.getInstance();

        homeTextView = view.findViewById(R.id.homeTextView);
        navigationBar = view.findViewById(R.id.bottom_navigation);
        moodRecyclerView = view.findViewById(R.id.moodRecyclerView);

        navBarController = new BottomNavBarController(navigationBar);

        homeTextView.setText("Currently signed in as: " + user.getEmail());

        // Setup RecyclerView
        moodRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        moodAdapter = new MoodAdapter();
        moodRecyclerView.setAdapter(moodAdapter);

        if (auth.getCurrentUser() == null) {
            Log.e("Home", "User not authenticated!");
        }

        // Listen for changes in the "moodEvents" collection
        // Create a filter that orders by "created" descending (newest first)
        new MoodEventFilter(provider)
                .setSortField("created", Query.Direction.DESCENDING)
                .buildQuery()
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.w("HomeFeed", "Listen failed.", error);
                        return;
                    }

                    if (snapshots == null) {
                        Log.w("HomeFeed", "No snapshot data received.");
                        return;
                    }

                    List<MoodEvent> moodList = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        MoodEvent mood = doc.toObject(MoodEvent.class);
                        if (mood != null) {
                            mood.setId(doc.getId());
                            moodList.add(mood);
                        }
                    }
                    Log.i("HomeFeed", Integer.toString(moodList.size()));
                    moodAdapter.setMoods(moodList);
                });

        moodAdapter.setOnMoodClickListener(mood -> {
            Bundle args = new Bundle();
            args.putString("moodEventId", mood.getId());
            args.putString("sourceScreen", "home"); // or "filtered"
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.action_homeFeed_to_moodDetails, args);
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navBarController.bind(view);
    }
}
