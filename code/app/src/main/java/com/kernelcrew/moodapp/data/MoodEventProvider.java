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
import com.google.firebase.firestore.ListenerRegistration;
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

    /**
     * Get a collection of mood events from the DB.
     * @return A collection of mood events
     */
    public Task<QuerySnapshot> getMoodEvents(){
        return collection.get();
    }

    /**
     * Returns the Firestore CollectionReference for filtering purposes.
     * This reference can be then in filtering to build queries or perform Firestore operations,
     * like adding snapshot listeners or inserting and updating documents.
     *
     * @return the CollectionReference instance for the mood events.
     */
    public CollectionReference getCollectionReference() {
        return collection;
    }

    /**
     * Add a snapshot listener to the mood events collection, filtered by the current user's UID.
     * This method returns a ListenerRegistration that can be used to remove the listener.
     *
     * @param listener Snapshot listener to add
     * @return ListenerRegistration that can be used to remove the listener
     */
    public ListenerRegistration addUserFilteredSnapshotListener(@NonNull EventListener<QuerySnapshot> listener) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            // Only listen for mood events belonging to the current user
            return collection.whereEqualTo("uid", user.getUid())
                    .addSnapshotListener(listener);
        } else {
            // If no user is logged in, listen to an empty query
            return collection.limit(0).addSnapshotListener(listener);
        }
    }
}
