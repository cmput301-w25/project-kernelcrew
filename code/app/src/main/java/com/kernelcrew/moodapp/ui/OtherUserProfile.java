package com.kernelcrew.moodapp.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.kernelcrew.moodapp.R;
import com.kernelcrew.moodapp.data.FollowProvider;
import com.kernelcrew.moodapp.data.MoodEvent;
import com.kernelcrew.moodapp.data.UserProvider;

import java.util.ArrayList;
import java.util.List;

public class OtherUserProfile extends Fragment {

    private static final String TAG = "OtherUserProfile";
    private String uidToLoad;
    private TextView usernameText;
    private TextView emailText;
    private MaterialToolbar toolbar;
    private Button followButton;
    private Button followersButton;
    private Button followingButton;
    private RecyclerView publicMoodsRecyclerView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_other_user_profile, container, false);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        toolbar = view.findViewById(R.id.topAppBarOther);
        usernameText = view.findViewById(R.id.username_text);
        emailText = view.findViewById(R.id.email_text);
        followButton = view.findViewById(R.id.followButton);
        followersButton = view.findViewById(R.id.followers_button);
        followingButton = view.findViewById(R.id.following_button);
        publicMoodsRecyclerView = view.findViewById(R.id.public_moods_recycler_view);

        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack()
        );

        // Retrieve uidToLoad from arguments
        if (getArguments() != null) {
            uidToLoad = getArguments().getString("uid");
            Log.d(TAG, "UID to load: " + uidToLoad);
        }

        // Load user details if uidToLoad is available
        if (uidToLoad != null) {
            UserProvider.getInstance().addSnapshotListenerForUser(uidToLoad, (doc, error) -> {
                if (error != null) {
                    Log.e(TAG, "Error loading user: ", error);
                    usernameText.setText("Error loading user");
                    emailText.setText("");
                    return;
                }
                if (doc != null && doc.exists()) {
                    String username = doc.getString("username");
                    usernameText.setText(username != null ? username : "Unknown User");
                    String email = doc.getString("email");
                    emailText.setText(email != null ? email : "No Email Provided");
                }
            });
        } else {
            usernameText.setText("No user ID provided");
            emailText.setText("");
        }

        // Hide follow button if the current user is viewing their own profile
        if (currentUser != null && uidToLoad != null && uidToLoad.equals(currentUser.getUid())) {
            followButton.setVisibility(View.GONE);
        } else if (currentUser != null && uidToLoad != null) {
            String currentUid = currentUser.getUid();
            FollowProvider provider = FollowProvider.getInstance();

            provider.isFollowing(currentUid, uidToLoad)
                    .addOnSuccessListener(isFollowing -> {
                        if (isFollowing) {
                            followButton.setText("Unfollow");
                            followButton.setOnClickListener(v ->
                                    provider.unfollow(currentUid, uidToLoad)
                                            .addOnSuccessListener(a -> followButton.setText("Follow"))
                                            .addOnFailureListener(e -> Log.e(TAG, "Unfollow failed", e))
                            );
                        } else {
                            provider.hasPendingRequest(uidToLoad, currentUid)
                                    .addOnSuccessListener(isRequested -> {
                                        if (isRequested) {
                                            followButton.setText("Requested");
                                            followButton.setEnabled(false);
                                        } else {
                                            followButton.setText("Follow");
                                            followButton.setEnabled(true);
                                            followButton.setOnClickListener(v -> {
                                                provider.sendRequest(uidToLoad, currentUid)
                                                        .addOnSuccessListener(a -> {
                                                            followButton.setText("Requested");
                                                            followButton.setEnabled(false);
                                                        })
                                                        .addOnFailureListener(e -> Log.e(TAG, "Request failed", e));
                                            });
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Error checking follow status", e));
        }

        if (uidToLoad != null) {
            // Listen for changes in 'followers' subcollection
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uidToLoad)
                    .collection("followers")
                    .addSnapshotListener((snap, e) -> {
                        if (e != null) {
                            Log.e(TAG, "Followers snapshot error", e);
                            return;
                        }
                        int count = (snap == null) ? 0 : snap.size();
                        followersButton.setText("Followers: " + count);
                    });

            // Listen for changes in 'following' subcollection
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uidToLoad)
                    .collection("following")
                    .addSnapshotListener((snap, e) -> {
                        if (e != null) {
                            Log.e(TAG, "Following snapshot error", e);
                            return;
                        }
                        int count = (snap == null) ? 0 : snap.size();
                        followingButton.setText("Following: " + count);
                    });

            // Setup follow button logic and public moods recycler view
            setupFollowButtonLogic();
            setupPublicMoodRecycler(view);
        }

        return view;
    }

    // Helper method for follow button logic.
    // (This is kept as a placeholder since the logic is already handled above.)
    private void setupFollowButtonLogic() {
        // Additional follow button logic can be placed here if needed.
    }

    // Helper method to setup the RecyclerView for public moods.
    private void setupPublicMoodRecycler(View view) {
        MoodAdapter moodAdapter = new MoodAdapter();
        publicMoodsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        publicMoodsRecyclerView.setAdapter(moodAdapter);

        if (uidToLoad != null) {
            Query query = FirebaseFirestore.getInstance()
                    .collection("moodEvents")
                    .whereEqualTo("uid", uidToLoad)
                    .whereEqualTo("visibility", "PUBLIC")
                    .orderBy("created", Query.Direction.DESCENDING);

            query.addSnapshotListener((snapshots, error) -> {
                if (error != null) {
                    Log.e(TAG, "Error loading public moods", error);
                    return;
                }
                if (snapshots != null) {
                    List<MoodEvent> moodList = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        MoodEvent mood = doc.toObject(MoodEvent.class);
                        if (mood != null) {
                            mood.setId(doc.getId());
                            moodList.add(mood);
                        }
                    }
                    moodAdapter.setMoods(moodList);
                    moodAdapter.setOnMoodClickListener(new MoodAdapter.OnMoodClickListener() {
                        @Override
                        public void onViewDetails(MoodEvent mood) {
                            Bundle args = new Bundle();
                            args.putString("moodEventId", mood.getId());
                            args.putString("sourceScreen", "otherUserProfile");
                            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                            navController.navigate(R.id.action_otherUserProfile_to_moodDetails, args);
                        }

                        @Override
                        public void onViewComments(MoodEvent mood) {
                            // Handle view comments action here if needed.
                        }
                    });
                }
            });
        }
    }
}
