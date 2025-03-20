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

    private CommentProvider() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        collection = db.collection("comments");
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
                        Exception exception = task.getException();
                        if (exception != null) {
                            Log.e("CommentProvider", "Error fetching comments: " + exception.getMessage());
                        }
                    }
                    return comments;
                });
    }
}
