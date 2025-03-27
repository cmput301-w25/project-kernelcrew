package com.kernelcrew.moodapp.data;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
     * Fetch the username of a specific user.
     *
     * @param uid The user to find the username of.
     * @return The user's username.
     */
    public Task<String> getUsername(@NonNull String uid) {
        return db.collection("users").document(uid).get()
                .onSuccessTask(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        if (username != null) {
                            return Tasks.forResult(username);
                        }
                    } else {
                        return Tasks.forException(new Exception("User document not found"));
                    }
                    return Tasks.forException(null);
                });
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
                String followingUid = doc.getId();
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

    /**
     * Fetch the username for a specific user.
     * @param uid The user ID whose username to fetch.
     * @return A Task containing the username as a String.
     */
    public Task<String> fetchUsername(@NonNull String uid) {
        return db.collection("users")
                .document(uid)
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        String username = document.getString("username");
                        if (username == null) {
                            username = document.getString("name");
                        }
                        if (username == null) {
                            username = "UnknownUser";
                        }
                        return username;
                    } else {
                        throw new Exception("User not found");
                    }
                });
    }
}
