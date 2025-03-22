package com.kernelcrew.moodapp.data;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserProvider {
    private final FirebaseFirestore db;

    private UserProvider() {
        db = FirebaseFirestore.getInstance();
    }

    private static UserProvider instance;

    /**
     * Get the singleton instance of UserProvider
     * @return Singleton instance of UserProvider
     */
    public static UserProvider getInstance() {
        if (instance == null) {
            instance = new UserProvider();
        }
        return instance;
    }

    /**
     * Fetch all users following a specific user.
     * @param uid The user to look up the followers of.
     * @return All users following this user.
     */
    public Task<List<User>> fetchFollowers(@NonNull String uid) {
        Task<QuerySnapshot> followers =
                db.collection("users").document(uid).collection("followers").get();
        return followers.onSuccessTask(queryDocumentSnapshots -> {
            List<User> followersList = new ArrayList<>();

            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                String followerUid = doc.getId();  // doc ID as the user's UID
                Boolean isFollowingBack = doc.getBoolean("isFollowingBack");
                if (isFollowingBack == null) {
                    isFollowingBack = false;
                }
                // Pass UID as the first argument, and optionally reuse it as 'name' if that's all you have
                followersList.add(new User(followerUid, followerUid, isFollowingBack));
            }

            return Tasks.forResult(followersList);
        });
    }

    /**
     * Fetch all users followed by a specific user.
     * @param uid The user to look up the following of.
     * @return All users followed by this user.
     */
    public Task<List<User>> fetchFollowing(@NonNull String uid) {
        Task<QuerySnapshot> query =
                db.collection("users").document(uid).collection("following").get();
        return query.onSuccessTask(queryDocumentSnapshots -> {
            List<User> followingList = new ArrayList<>();

            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                String followingUid = doc.getId(); // doc ID as the user's UID
                Boolean isFollowed = doc.getBoolean("isFollowed");
                if (isFollowed == null) {
                    isFollowed = false;
                }
                // Same approach here: pass the UID in both fields if that's all you have
                followingList.add(new User(followingUid, followingUid, isFollowed));
            }

            return Tasks.forResult(followingList);
        });
    }

    /**
     * Add a listener for changes made to a specific user.
     * @param uid Id of the user to listen to.
     * @param listener Snapshot listener to attach.
     */
    public void addSnapshotListenerForUser(@NonNull String uid,
                                           @NonNull EventListener<DocumentSnapshot> listener) {
        db.collection("users").document(uid).addSnapshotListener(listener);
    }

    /**
     * Search users by username (case-insensitive, partial-match) while excluding the current user.
     *
     * @param query The search string.
     * @param currentUserId The current user's ID to exclude.
     * @return A Task that returns a List of matching Users.
     */
    public Task<List<User>> searchUsers(String query, @NonNull String currentUserId) {
        final String lowerQuery = query.toLowerCase();
        return db.collection("users").get().onSuccessTask(querySnapshot -> {
            List<User> results = new ArrayList<>();
            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                if (doc.getId().equals(currentUserId)) continue;

                // doc.getString("username") or similar to retrieve the display name
                String username = doc.getString("username");
                if (username != null && username.toLowerCase().contains(lowerQuery)) {
                    // First argument = Firestore doc ID (the user's UID)
                    // Second argument = the username string from Firestore
                    results.add(new User(doc.getId(), username, false));
                }
            }
            return Tasks.forResult(results);
        });
    }
}