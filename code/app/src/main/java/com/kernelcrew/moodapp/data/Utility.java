package com.kernelcrew.moodapp.data;

import android.app.Activity;
import android.view.inputmethod.InputMethodManager;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.List;

/**
 * A utility class for UI related actions.
 */
public class Utility {
    /**
     * Hide the keyboard if it is open
     *
     * @param activity The current activity where the keyboard is active.
     */
    // Taha used the following
    // https://stackoverflow.com/questions/4165414/how-to-hide-soft-keyboard-on-android-after-clicking-outside-edittext
    // Changed the above code due to null value issues.
    public static void hideSoftKeyboard(Activity activity) {
        if (activity == null) {
            return;
        }
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);

        if (inputMethodManager == null) {
            return;
        }

        if (activity.getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    /**
     * Recursively deletes all documents in the given collection.
     *
     * @param collectionRef The collection to clear.
     * @param batchSize The maximum number of documents to delete per batch.
     * @return A Task that completes when the collection is empty.
     */
    public static Task<Void> clearCollection(final CollectionReference collectionRef, final int batchSize) {
        return collectionRef.limit(batchSize).get().continueWithTask(task -> {
            QuerySnapshot snapshot = task.getResult();
            List<DocumentSnapshot> documents = snapshot.getDocuments();

            if (documents.isEmpty()) {
                return Tasks.forResult(null);
            }

            WriteBatch batch = collectionRef.getFirestore().batch();
            for (DocumentSnapshot doc : documents) {
                batch.delete(doc.getReference());
            }

            return batch.commit().continueWithTask(commitTask -> clearCollection(collectionRef, batchSize));
        });
    }

}
