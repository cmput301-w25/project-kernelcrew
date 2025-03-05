package com.kernelcrew.moodapp.data;

import android.widget.Button;

import androidx.annotation.NonNull;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;


public class UserController {

    private final FirebaseFirestore db;

    public UserController() {
        db = FirebaseFirestore.getInstance();
    }

    public void fetchFollowers(@NonNull String userId, List<com.kernelcrew.moodapp.data.User> followersList,
                               androidx.recyclerview.widget.RecyclerView.Adapter<?> adapter) {
        db.collection("users").document(userId).collection("followers")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    followersList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getId();
                        boolean isFollowingBack = doc.getBoolean("isFollowingBack") != null && doc.getBoolean("isFollowingBack");
                        followersList.add(new com.kernelcrew.moodapp.data.User(name, isFollowingBack));
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> System.out.println("ERROR: Failed to load followers."));
    }


    public void fetchFollowing(@NonNull String userId, List<com.kernelcrew.moodapp.data.User> followingList,
                               androidx.recyclerview.widget.RecyclerView.Adapter<?> adapter) {
        db.collection("users").document(userId).collection("following")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    followingList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getId();
                        boolean isFollowed = doc.getBoolean("isFollowed") != null && doc.getBoolean("isFollowed");
                        followingList.add(new com.kernelcrew.moodapp.data.User(name, isFollowed));
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> System.out.println("ERROR: Failed to load following."));
    }


    public void listenForUserUpdates(@NonNull String userId, Button followersButton, Button followingButton) {
        db.collection("users").document(userId)
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null) {
                        followersButton.setText("Followers: 0");
                        followingButton.setText("Following: 0");
                        return;
                    }
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        Long followersCount = documentSnapshot.getLong("followersCount");
                        Long followingCount = documentSnapshot.getLong("followingCount");

                        followersButton.setText("Followers: " + (followersCount != null ? followersCount : 0));
                        followingButton.setText("Following: " + (followingCount != null ? followingCount : 0));
                    }
                });
    }
}
