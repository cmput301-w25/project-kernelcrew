package com.kernelcrew.moodapp.data;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class CheckFollowRequestCount {

    // Method to listen to changes in follow requests for a user
    public static void listenToFollowRequests(String userId, EventListener<QuerySnapshot> listener) {
        // Set up a Firestore snapshot listener to listen to changes in follow requests
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("followRequests")
                .addSnapshotListener(listener); // Returns a listener for updates
    }
}
