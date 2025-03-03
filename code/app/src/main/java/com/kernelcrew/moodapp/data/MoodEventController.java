package com.kernelcrew.moodapp.data;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class MoodEventController {
    private static MoodEventController instance;

    private final CollectionReference collection;

    private MoodEventController() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        collection = db.collection("moodEvent");
    }

    public static MoodEventController getInstance() {
        if (instance == null) {
            instance = new MoodEventController();
        }

        return instance;
    }

    /**
     * Insert a new mood event into the DB.
     * @param moodEvent Mood event to insert
     * @return Insert task
     */
    public Task<DocumentReference> insertMoodEvent(MoodEvent moodEvent) {
        return collection.add(moodEvent);
    }
}
