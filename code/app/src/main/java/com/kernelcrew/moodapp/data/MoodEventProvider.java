package com.kernelcrew.moodapp.data;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class MoodEventProvider {
    private final CollectionReference collection;
    private final FirebaseAuth auth;

    private MoodEventProvider() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        collection = db.collection("moodEvents");

        auth = FirebaseAuth.getInstance();
    }

    private static MoodEventProvider instance;

    /**
     * Get the singleton instance of the MoodEventProvider.
     * @return Singleton instance
     */
    public static MoodEventProvider getInstance() {
        if (instance == null) {
            instance = new MoodEventProvider();
        }

        return instance;
    }

    /**
     * Insert a new mood event into the DB.
     * @param moodEvent Mood event to insert
     * @return Insert task
     */
    public Task<Void> insertMoodEvent(MoodEvent moodEvent) {
        if (moodEvent == null) {
            throw new IllegalArgumentException("MoodEvent cannot be null");
        }

        FirebaseUser user = auth.getCurrentUser();
        assert user != null;

        // Ensure the MoodEvent has the correct userId
        moodEvent.setUid(user.getUid());

        return collection.document(moodEvent.getId()).set(moodEvent);
    }

    /**
     * Update an existing mood event in DB.
     * @param moodId ID of mood event to update
     * @param moodEvent Mood event details to update with
     * @return Update task
     */
    public Task<Void> updateMoodEvent(String moodId, MoodEvent moodEvent) {
        if (moodId == null) {
            throw new IllegalArgumentException("moodID cannot be null");
        }
        if (moodEvent == null) {
            throw new IllegalArgumentException("moodEvent cannot be null");
        }
        if (!moodId.equals(moodEvent.getId())) {
            throw new IllegalArgumentException("moodEvent.getId() must equal moodId");
        }

        return collection.document(moodEvent.getId()).set(moodEvent);
    }

    /**
     * Get a mood event by id.
     * @param id Id of the mood event to fetch
     * @return Task which should resolve to the mood event (or null if not found)
     */
    public Task<MoodEvent> getMoodEvent(@NonNull String id) {
        return collection.document(id).get().onSuccessTask(doc -> {
            if (!doc.exists()) {
                return Tasks.forResult(null);
            }

            MoodEvent event = doc.toObject(MoodEvent.class);
            return Tasks.forResult(event);
        });
    }

    /**
     * Add a snapshot listener to the mood events collection
     * @param listener Snapshot listener to add
     */
    public void addSnapshotListener(@NonNull EventListener<QuerySnapshot> listener) {
        collection.addSnapshotListener(listener);
    }
}
