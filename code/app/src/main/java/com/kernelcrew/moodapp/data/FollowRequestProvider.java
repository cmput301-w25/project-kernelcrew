package com.kernelcrew.moodapp.data;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.kernelcrew.moodapp.utils.NotificationHelper;

public class FollowRequestProvider {
    private final FirebaseFirestore db;
    private final NotificationHelper notificationHelper;

    public FollowRequestProvider(Context context) {
        this.db = FirebaseFirestore.getInstance();
        this.notificationHelper = new NotificationHelper(context);
    }

    /**
     * Start listening for follow requests (incoming follow requests).
     * This method is called in HomeFeed.
     */
    public ListenerRegistration listenForFollowRequests(String userId) {
        return db.collection("users")
                .document(userId)
                .collection("followRequests")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e("FollowRequestProvider", "Error listening to followRequests", error);
                        return;
                    }
                    if (snapshots != null) {
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                String requesterUid = dc.getDocument().getId();
                                sendFollowRequestNotification(requesterUid);
                            }
                        }
                    }
                });
    }

    /**
     * Start listening for notifications, such as followAccepted.
     */
    public ListenerRegistration listenForFollowAcceptedNotifications(String userId) {
        return db.collection("users")
                .document(userId)
                .collection("notifications")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e("FollowRequestProvider", "Error listening to notifications", error);
                        return;
                    }
                    if (snapshots != null) {
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                String docId = dc.getDocument().getId();
                                String type = dc.getDocument().getString("type");
                                if ("followAccepted".equals(type)) {
                                    String fromUserId = dc.getDocument().getString("fromUserId");
                                    sendFollowAcceptedNotification(fromUserId, docId);
                                }
                            }
                        }
                    }
                });
    }

    /**
     * Send a follow request notification.
     */
    private void sendFollowRequestNotification(String requesterUid) {
        notificationHelper.sendNotification(
                "Follow Request",
                requesterUid + " wants to follow you"
        );
    }

    /**
     * Send a follow accepted notification and optionally delete the notification.
     */
    private void sendFollowAcceptedNotification(String fromUserId, String docId) {
        notificationHelper.sendNotification(
                "Follow Accepted",
                fromUserId + " accepted your follow request"
        );
        // Optionally, delete the notification after processing.
        db.collection("users")
                .document(fromUserId)
                .collection("notifications")
                .document(docId)
                .delete();
    }
}
