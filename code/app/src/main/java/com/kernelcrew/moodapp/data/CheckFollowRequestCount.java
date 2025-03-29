package com.kernelcrew.moodapp.data;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * This class provides functionality to listen for changes in a user's follow requests
 * in the Firestore database.
 */
public class CheckFollowRequestCount {

    /**
     * Listens to changes in the follow requests collection for a specific user.
     * This method adds a snapshot listener to the follow requests collection and triggers
     * the provided listener whenever there is a change in the collection (e.g., a new follow
     * request is added or removed).
     *
     * @param userId The unique ID of the user whose follow requests are being monitored.
     * @param listener The listener that will be triggered whenever there is a change
     *                 in the follow requests collection.
     */
    public static void listenToFollowRequests(String userId, EventListener<QuerySnapshot> listener) {
        // Set up a Firestore snapshot listener to listen to changes in follow requests
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("followRequests")
                .addSnapshotListener(listener); // Returns a listener for updates
    }
}
