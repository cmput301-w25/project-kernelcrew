package com.kernelcrew.moodapp.data;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class CommentProvider {
    private final CollectionReference collection;
    private final FirebaseAuth auth;

    private CommentProvider() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        collection = db.collection("comments");

        auth = FirebaseAuth.getInstance();
    }

    private static CommentProvider instance;

    /**
     * Get the singleton instance of the CommentProvider.
     * @return Singleton instance
     */
    public static CommentProvider getInstance() {
        if (instance == null) {
            instance = new CommentProvider();
        }

        return instance;
    }

    public Task<Void> insertComment(Comment comment) {
        DocumentReference docRef = collection.document();
        comment.setId(docRef.getId());
        return docRef.set(comment);
    }

    public Task<List<Comment>> getCommentsByMoodEventId(String moodEventId) {
        return collection
                .whereEqualTo("moodEventId", moodEventId)
                .orderBy("created", Query.Direction.DESCENDING)
                .get()
                .continueWith(task -> {
                    List<Comment> comments = new ArrayList<>();
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (DocumentSnapshot doc : task.getResult()) {
                            Comment comment = doc.toObject(Comment.class);
                            if (comment != null) {
                                comments.add(comment);
                            }
                        }
                    } else {
                        // Handle the error
                        Exception exception = task.getException();
                        if (exception != null) {
                            exception.printStackTrace();
                            Log.e("CommentProvider", "Error fetching comments: " + exception.getMessage());
                        }
                    }
                    return comments;
                });
    }

    /**
     * Add a snapshot listener to the comments collection
     * @param listener Snapshot listener to add
     */
    public void addSnapshotListener(@NonNull EventListener<QuerySnapshot> listener) {
        collection.addSnapshotListener(listener);
    }

    /**
     * Get a collection of comments from the DB.
     * @return A collection of comments
     */
    public Task<QuerySnapshot> getComments(){
        return collection.get();
    }

    /**
     * Returns the Firestore CollectionReference for filtering purposes.
     * This reference can be then in filtering to build queries or perform Firestore operations,
     * like adding snapshot listeners or inserting and updating documents.
     *
     * @return the CollectionReference instance for the comments.
     */
    public CollectionReference getCollectionReference() {
        return collection;
    }

    /**
     * Add a snapshot listener to the comments collection, filtered by the current user's UID.
     * This method returns a ListenerRegistration that can be used to remove the listener.
     *
     * @param listener Snapshot listener to add
     * @return ListenerRegistration that can be used to remove the listener
     */
    public ListenerRegistration addUserFilteredSnapshotListener(@NonNull EventListener<QuerySnapshot> listener) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            // Only listen for comments belonging to the current user
            return collection.whereEqualTo("uid", user.getUid())
                    .addSnapshotListener(listener);
        } else {
            // If no user is logged in, listen to an empty query
            return collection.limit(0).addSnapshotListener(listener);
        }
    }
}
