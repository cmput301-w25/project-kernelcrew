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
        Task<QuerySnapshot> followers = db.collection("users").document(uid).collection("followers").get();
        return followers.onSuccessTask(queryDocumentSnapshots -> {
            List<User> followersList = new ArrayList<>();

            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                String name = doc.getId();
                Boolean isFollowingBack = doc.getBoolean("isFollowingBack");
                if (isFollowingBack == null) {
                    isFollowingBack = false;
                }
                followersList.add(new User(name, isFollowingBack));
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
        Task<QuerySnapshot> query = db.collection("users").document(uid).collection("following").get();
        return query.onSuccessTask(queryDocumentSnapshots -> {
            List<User> followingList = new ArrayList<>();

            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                String name = doc.getId();
                Boolean isFollowed = doc.getBoolean("isFollowed");
                if (isFollowed == null) {
                    isFollowed = false;
                }

                followingList.add(new User(name, isFollowed));
            }

            return Tasks.forResult(followingList);
        });
    }

    /**
     * Add a listener for changes made to a specific user.
     * @param uid Id of the user to listen to.
     * @param listener Snapshot listener to attach.
     */
    public void addSnapshotListenerForUser(@NonNull String uid, @NonNull EventListener<DocumentSnapshot> listener) {
        db.collection("users").document(uid).addSnapshotListener(listener);
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
