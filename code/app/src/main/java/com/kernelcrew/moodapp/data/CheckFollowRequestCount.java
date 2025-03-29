package com.kernelcrew.moodapp.data;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.gms.tasks.Task;

public class CheckFollowRequestCount {

    // Method to check if the current user has follow requests
    public static Task<QuerySnapshot> getFollowRequests(String userId) {
        // Get the follow requests collection for the user
        return FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("followRequests")
                .get(); // This returns a Task<QuerySnapshot>
    }
}
