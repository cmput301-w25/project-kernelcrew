package com.kernelcrew.moodapp.data;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        if (moodEvent.getId() == null || moodEvent.getId().isEmpty()) {
            String generatedId = collection.document().getId();
            moodEvent.setId(generatedId);
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            throw new IllegalStateException("User must be logged in");
        }
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
     * Returns all mood events which can then be further filtered.
     *
     * @return All (visible) mood events.
     */
    public Query getAll() {
        FirebaseUser user = auth.getCurrentUser();
        return collection.where(Filter.or(
                Filter.and(Filter.equalTo("uid", user.getUid()),
                           Filter.equalTo("visibility", "PRIVATE")),
                Filter.equalTo("visibility", "PUBLIC")));
    }

    /**
     * Listens to mood events for a list of user IDs.
     * For the current user (assumed to be the first element in userIds) all posts are returned.
     * For each followed user, only the 3 most recent posts are returned.
     * The results from all queries are combined and returned via the CombinedListener.
     */
    public ListenerRegistration listenToMoodEventsForUsers(List<String> userIds, MoodEventFilter filter, int followerLimit, CombinedListener listener) {
        if (userIds == null || userIds.isEmpty()) {
            return new ListenerRegistration() {
                @Override
                public void remove() { }
            };
        }

        // Assume first element is the current user's UID.
        String currentUserId = userIds.get(0);
        Map<String, List<DocumentSnapshot>> snapshotsByUser = new HashMap<>();
        List<ListenerRegistration> registrations = new ArrayList<>();

        for (String uid : userIds) {
            Query query = filter.buildQuery().whereEqualTo("uid", uid);
            if (!uid.equals(currentUserId)) {
                query = query.limit(followerLimit);
            }
            ListenerRegistration reg = query.addSnapshotListener((querySnapshot, error) -> {
                if (error != null) {
                    listener.onEvent(null, error);
                    return;
                }
                if (querySnapshot != null) {
                    // Save the latest documents for this uid.
                    snapshotsByUser.put(uid, querySnapshot.getDocuments());

                    // Combine all documents from all users.
                    List<DocumentSnapshot> combined = new ArrayList<>();
                    for (List<DocumentSnapshot> docs : snapshotsByUser.values()) {
                        combined.addAll(docs);
                    }

                    // Sorting the combined by time created aswell
                    combined.sort((d1, d2) -> {
                        Date t1 = d1.getDate("created");
                        Date t2 = d2.getDate("created");
                        if (t1 == null && t2 == null) return 0;
                        if (t1 == null) return 1;
                        if (t2 == null) return -1;
                        return t2.compareTo(t1);
                    });
                    listener.onEvent(combined, null);
                }
            });
            registrations.add(reg);
        }

        return new CombinedListenerComposer(registrations);
    }

    public CollectionReference getCollectionReference() {
        return collection;
    }
}
