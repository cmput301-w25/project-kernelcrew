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

import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kernelcrew.moodapp.R;
import com.kernelcrew.moodapp.data.MoodEvent;
import com.kernelcrew.moodapp.data.MoodEventProvider;
import com.kernelcrew.moodapp.ui.components.FilterBarFragment;
import com.kernelcrew.moodapp.utils.NotificationHelper;

import java.util.ArrayList;
import java.util.List;

public class HomeFeed extends Fragment {
    FirebaseAuth auth;
    FirebaseUser user;
    MoodEventProvider provider;

    FilterBarFragment searchNFilterFragment;
    NavigationBarView navigationBar;
    BottomNavBarController navBarController;
    RecyclerView moodRecyclerView;
    MoodAdapter moodAdapter;

    public static List<MoodEvent> currentFilteredList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_feed, container, false);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        provider = MoodEventProvider.getInstance();

        navigationBar = view.findViewById(R.id.bottom_navigation);
        moodRecyclerView = view.findViewById(R.id.moodRecyclerView);

        navBarController = new BottomNavBarController(navigationBar);

        if (user != null) {
            String myUid = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Listener for followRequests (for incoming requests)
            db.collection("users")
                    .document(myUid)
                    .collection("followRequests")
                    .addSnapshotListener((snapshots, error) -> {
                        if (error != null) {
                            Log.e("HomeFeed", "Error listening to followRequests", error);
                            return;
                        }
                        if (snapshots != null) {
                            for (DocumentChange dc : snapshots.getDocumentChanges()) {
                                if (dc.getType() == DocumentChange.Type.ADDED) {
                                    String requesterUid = dc.getDocument().getId();
                                    FirebaseFirestore.getInstance()
                                            .collection("users")
                                            .document(requesterUid)
                                            .get()
                                            .addOnSuccessListener(doc -> {
                                                String userName = requesterUid; // fallback
                                                if (doc.exists()) {
                                                    String fetchedName = doc.getString("username");
                                                    if (fetchedName != null && !fetchedName.isEmpty()) {
                                                        userName = fetchedName;
                                                    }
                                                }
                                                NotificationHelper notificationHelper = new NotificationHelper(getContext());
                                                notificationHelper.sendNotification(
                                                        "Follow Request",
                                                        userName + " wants to follow you"
                                                );
                                            })
                                            .addOnFailureListener(e -> {
                                                NotificationHelper notificationHelper = new NotificationHelper(getContext());
                                                notificationHelper.sendNotification(
                                                        "Follow Request",
                                                        requesterUid + " wants to follow you"
                                                );
                                            });
                                }
                            }
                        }
                    });

            // Listener for notifications (to catch "followAccepted" events)
            db.collection("users")
                    .document(myUid)
                    .collection("notifications")
                    .addSnapshotListener((snapshots, error) -> {
                        if (error != null) {
                            Log.e("HomeFeed", "Error listening to notifications", error);
                            return;
                        }
                        if (snapshots != null) {
                            for (DocumentChange dc : snapshots.getDocumentChanges()) {
                                if (dc.getType() == DocumentChange.Type.ADDED) {
                                    String docId = dc.getDocument().getId();
                                    String type = dc.getDocument().getString("type");
                                    if ("followAccepted".equals(type)) {
                                        // "fromUserId" is the user who accepted the follow request.
                                        String fromUserId = dc.getDocument().getString("fromUserId");
                                        if (fromUserId != null) {
                                            db.collection("users")
                                                    .document(fromUserId)
                                                    .get()
                                                    .addOnSuccessListener(doc -> {
                                                        String userName = fromUserId; // fallback
                                                        if (doc.exists()) {
                                                            String fetchedName = doc.getString("username");
                                                            if (fetchedName != null && !fetchedName.isEmpty()) {
                                                                userName = fetchedName;
                                                            }
                                                        }
                                                        NotificationHelper helper = new NotificationHelper(getContext());
                                                        helper.sendNotification(
                                                                "Follow Accepted",
                                                                userName + " accepted your follow request"
                                                        );
                                                        // Optionally delete the notification doc after processing
                                                        db.collection("users")
                                                                .document(myUid)
                                                                .collection("notifications")
                                                                .document(docId)
                                                                .delete();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        NotificationHelper helper = new NotificationHelper(getContext());
                                                        helper.sendNotification(
                                                                "Follow Accepted",
                                                                fromUserId + " accepted your follow request"
                                                        );
                                                    });
                                        }
                                    }
                                }
                            }
                        }
                    });
        }

        searchNFilterFragment = (FilterBarFragment) getChildFragmentManager().findFragmentById(R.id.filterBarFragment);

        moodRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        moodAdapter = new MoodAdapter();

        moodRecyclerView.setAdapter(moodAdapter);

        if (auth.getCurrentUser() == null) {
            Log.e("Home", "User not authenticated!");
        }

        // Listener for moodEvents collection
        if (searchNFilterFragment != null) {
            searchNFilterFragment.setOnFilterChangedListener(filter -> {
                filter.buildQuery()
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
                            currentFilteredList = moodList;
                            moodAdapter.setMoods(moodList);
                        });
            });
        }

        moodAdapter.setOnMoodClickListener(new MoodAdapter.OnMoodClickListener() {
            @Override
            public void onViewDetails(MoodEvent mood) {
                Bundle args = new Bundle();
                args.putString("moodEventId", mood.getId());
                args.putString("sourceScreen", "home");
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                navController.navigate(R.id.action_homeFeed_to_moodDetails, args);
            }

            @Override
            public void onViewComments(MoodEvent mood) {
                Bundle args = new Bundle();
                args.putString("moodEventId", mood.getId());
                args.putString("sourceScreen", "home");
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                navController.navigate(R.id.action_homeFeed_to_moodComments, args);
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
