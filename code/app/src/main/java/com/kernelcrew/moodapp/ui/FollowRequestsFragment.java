package com.kernelcrew.moodapp.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.kernelcrew.moodapp.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FollowRequestsFragment extends Fragment {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final List<String> requestList = new ArrayList<>();
    private FollowRequestAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.follow_request_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView rv = view.findViewById(R.id.followRequestsRecyclerView);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new FollowRequestAdapter(requestList, this);
        rv.setAdapter(adapter);

        String me = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users")
                .document(me)
                .collection("followRequests")
                .addSnapshotListener((snap, err) -> {
                    requestList.clear();
                    if (snap != null) for (var doc : snap.getDocuments()) requestList.add(doc.getId());
                    adapter.notifyDataSetChanged();
                });
    }

    private void acceptFollowRequest(String requesterUid) {
        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        WriteBatch batch = db.batch();

        // Remove the incoming request
        DocumentReference requestRef = db.collection("users")
                .document(currentUid)
                .collection("followRequests")
                .document(requesterUid);
        batch.delete(requestRef);

        // Add to my followers
        DocumentReference myFollowersRef = db.collection("users")
                .document(currentUid)
                .collection("followers")
                .document(requesterUid);
        batch.set(myFollowersRef, Collections.singletonMap("isFollowingBack", false));

        // Add to requester’s following
        DocumentReference requesterFollowingRef = db.collection("users")
                .document(requesterUid)
                .collection("following")
                .document(currentUid);
        batch.set(requesterFollowingRef, Collections.singletonMap("isFollowed", true));

        // Increment counts on both user docs
        DocumentReference myUserDoc = db.collection("users").document(currentUid);
        DocumentReference requesterUserDoc = db.collection("users").document(requesterUid);
        batch.update(myUserDoc, "followersCount", FieldValue.increment(1));
        batch.update(requesterUserDoc, "followingCount", FieldValue.increment(1));

        // Commit all writes in one atomic transaction
        batch.commit()
                .addOnSuccessListener(unused -> Navigation.findNavController(requireView()).popBackStack())
                .addOnFailureListener(e -> Log.e("RequestFragment", "Failed to accept follow", e));
    }

    private void denyFollowRequest(String requesterUid) {
        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(currentUid)
                .collection("followRequests")
                .document(requesterUid)
                .delete()
                .addOnSuccessListener(unused -> Navigation.findNavController(requireView()).popBackStack())
                .addOnFailureListener(e -> Log.e("RequestFragment", "Failed to deny follow", e));
    }

}
