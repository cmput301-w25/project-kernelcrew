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

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kernelcrew.moodapp.R;
import com.kernelcrew.moodapp.data.FollowProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Collections;

public class FollowRequestsFragment extends Fragment {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final List<String> requestList = new ArrayList<>();
    private FollowRequestAdapter adapter;
    private BottomNavBarController navBarController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.follow_request_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialToolbar toolbar = view.findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.myProfile)
        );

        RecyclerView rv = view.findViewById(R.id.followRequestsRecyclerView);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new FollowRequestAdapter(requestList, this);
        rv.setAdapter(adapter);

        BottomNavigationView bottomNav = view.findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.page_myProfile);
        navBarController = new BottomNavBarController(bottomNav);
        navBarController.bind(view);

        // Firestore snapshot listener
        String me = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users")
                .document(me)
                .collection("followRequests")
                .addSnapshotListener((snap, err) -> {
                    requestList.clear();
                    if (snap != null) {
                        for (var doc : snap.getDocuments()) {
                            requestList.add(doc.getId());
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    public void accept(String requesterUid) {
        String me = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FollowProvider.getInstance()
                .acceptRequest(me, requesterUid)
                .addOnSuccessListener(unused ->
                        Navigation.findNavController(requireView()).navigate(R.id.myProfile))
                .addOnFailureListener(e -> Log.e("FollowRequestsFragment", "Accept failed", e));
    }

    public void deny(String requesterUid) {
        String me = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FollowProvider.getInstance()
                .deleteRequest(me, requesterUid)
                .addOnSuccessListener(unused ->
                        Navigation.findNavController(requireView()).navigate(R.id.myProfile))
                .addOnFailureListener(e -> Log.e("FollowRequestsFragment", "Deny failed", e));
    }
}
