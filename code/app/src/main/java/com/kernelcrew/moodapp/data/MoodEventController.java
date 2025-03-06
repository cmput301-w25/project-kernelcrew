package com.kernelcrew.moodapp.data;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class MoodEventController {
    private static MoodEventController instance;

    private final CollectionReference collection;

    private MoodEventController() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        collection = db.collection("moodEvents");
    }

    public static MoodEventController getInstance() {
        if (instance == null) {
            synchronized (MoodEventController.class) {
                if (instance == null) {
                    instance = new MoodEventController();
                }
            }
        }

        return instance;
    }

    /**
     * Insert a new mood event into the DB.
     * @param moodEvent Mood event to insert
     * @return Insert task
     */
    public void insertMoodEvent(MoodEvent moodEvent) {
        if (moodEvent == null) {
            throw new IllegalArgumentException("MoodEvent cannot be null");
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            Log.e("Firestore", "User is not authenticated. Cannot insert mood event.");
            return;
        }

        // Ensure the MoodEvent has the correct userId
        moodEvent.setUserId(user.getUid());

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("moodEvents")
                .add(moodEvent)
                .addOnSuccessListener(documentReference ->
                        Log.d("Firestore", "MoodEvent added successfully with ID: " + documentReference.getId()))
                .addOnFailureListener(e ->
                        Log.e("Firestore", "Error adding MoodEvent", e));
    }

}
