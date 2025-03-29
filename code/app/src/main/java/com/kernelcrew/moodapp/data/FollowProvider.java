package com.kernelcrew.moodapp.data;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FollowProvider {
    private static FollowProvider instance;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private FollowProvider() {}

    public static FollowProvider getInstance() {
        if (instance == null) {
            instance = new FollowProvider();
        }
        return instance;
    }

    // Send a follow request
    public Task<Void> sendRequest(String targetUid, String requesterUid) {
        return db.collection("users")
                .document(targetUid)
                .collection("followRequests")
                .document(requesterUid)
                .set(Collections.emptyMap());
    }

    // Delete (deny or cancel) a follow request
    public Task<Void> deleteRequest(String targetUid, String requesterUid) {
        return db.collection("users")
                .document(targetUid)
                .collection("followRequests")
                .document(requesterUid)
                .delete();
    }

    // Accept a follow request
    public Task<Void> acceptRequest(String targetUid, String requesterUid) {
        return deleteRequest(targetUid, requesterUid)
                .addOnSuccessListener(unused -> {
                    db.collection("users").document(targetUid)
                            .collection("followers")
                            .document(requesterUid)
                            .set(Collections.emptyMap());
                    db.collection("users").document(requesterUid)
                            .collection("following")
                            .document(targetUid)
                            .set(Collections.emptyMap());
                });
    }

    // Fetch all followers for a user
    public Task<List<User>> fetchFollowers(String userUid) {
        return db.collection("users")
                .document(userUid)
                .collection("followers")
                .get()
                .onSuccessTask(snapshot -> {
                    List<User> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        list.add(new User(doc.getId(), doc.getString("username"),false));
                    }
                    return Tasks.forResult(list);
                });
    }

    public Task<List<User>> fetchFollowing(String userUid) {
        return db.collection("users")
                .document(userUid)
                .collection("following")
                .get()
                .onSuccessTask(snapshot -> {
                    List<User> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        list.add(new User(doc.getId(), doc.getString("username"), false));
                    }
                    return Tasks.forResult(list);
                });
    }

    // Unfollow someone
    public Task<Void> unfollow(String userUid, String followedUid) {
        Task<Void> deleteFollowing = db.collection("users")
                .document(userUid)
                .collection("following")
                .document(followedUid)
                .delete();

        Task<Void> deleteFollower = db.collection("users")
                .document(followedUid)
                .collection("followers")
                .document(userUid)
                .delete();

        return Tasks.whenAllComplete(deleteFollowing, deleteFollower)
                .continueWith(task -> null);
    }

    /** Returns true if userUid is already following targetUid */
    public Task<Boolean> isFollowing(String userUid, String targetUid) {
        return db.collection("users")
                .document(userUid)
                .collection("following")
                .document(targetUid)
                .get()
                .continueWith(task -> task.getResult().exists());
    }

    /** Returns true if requesterUid has a pending follow request to targetUid */
    public Task<Boolean> hasPendingRequest(String targetUid, String requesterUid) {
        return db.collection("users")
                .document(targetUid)
                .collection("followRequests")
                .document(requesterUid)
                .get()
                .continueWith(task -> task.getResult().exists());
    }

}
