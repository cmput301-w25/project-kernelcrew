package com.kernelcrew.moodapp.data;

import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.List;

public interface CombinedListener {
    /**
     * Called when one or more underlying snapshot listeners return data.
     * @param documents Combined list of DocumentSnapshots from all queries; may be empty.
     * @param error An error if any listener encountered one, otherwise null.
     */
    void onEvent(List<DocumentSnapshot> documents, FirebaseFirestoreException error);
}
