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
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.kernelcrew.moodapp.R;
import com.kernelcrew.moodapp.ui.Mood;
import java.util.ArrayList;
import java.util.List;

public class HomeFeed extends Fragment {
    FirebaseAuth auth;
    FirebaseUser user;

    TextView homeTextView;
    NavigationBarView navigationBar;
    BottomNavBarController navBarController;
    RecyclerView moodRecyclerView;
    MoodAdapter moodAdapter;
    FirebaseFirestore firestore;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_feed, container, false);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        homeTextView = view.findViewById(R.id.homeTextView);
        navigationBar = view.findViewById(R.id.bottom_navigation);
        moodRecyclerView = view.findViewById(R.id.moodRecyclerView);

        navBarController = new BottomNavBarController(navigationBar);

        if (user == null) {
            Log.w("HomeFeed", "No user is signed in.");
            // TODO: Optionally navigate back to sign in or show an error
            // Navigation.findNavController(view).popBackStack();
        } else {
            homeTextView.setText("Currently signed in as user: " + user.getDisplayName());
        }

        // Setup RecyclerView
        moodRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        moodAdapter = new MoodAdapter();
        moodRecyclerView.setAdapter(moodAdapter);

        // Initialize Firestore instance
        firestore = FirebaseFirestore.getInstance();

        // Listen for changes in the "moods" collection
        firestore.collection("moods")
                .orderBy("timestamp")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.w("HomeFeed", "Listen failed.", error);
                            return;
                        }
                        List<Mood> moodList = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : snapshots) {
                            Mood mood = doc.toObject(Mood.class);
                            moodList.add(mood);
                        }
                        moodAdapter.setMoods(moodList);
                    }
                });

        moodAdapter.setOnMoodClickListener(new MoodAdapter.OnMoodClickListener() {
            @Override
            public void onViewDetails(Mood mood) {
                // Use mood.getId() (or however you retrieve the moodEventId) and navigate to MoodDetails.
                Bundle args = new Bundle();
                args.putString("moodEventId", mood.getId());
                args.putString("sourceScreen", "home"); // or "filtered"
                NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
                navController.navigate(R.id.action_homeFeed_to_moodDetails, args);
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navBarController.bind(view);
    }
}