package com.kernelcrew.moodapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.kernelcrew.moodapp.R;
import java.util.List;

public class FollowRequestAdapter extends RecyclerView.Adapter<FollowRequestAdapter.ViewHolder> {
    private final List<String> items;
    private final FollowRequestsFragment fragment;

    public FollowRequestAdapter(List<String> items, FollowRequestsFragment fragment) {
        this.items = items;
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_request, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String requestorUid = items.get(position);

        // 1) Fetch the requestor’s user doc
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(requestorUid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        // 2) If user doc found, get their username or fallback to doc ID
                        String userName = doc.getString("username");
                        if (userName == null || userName.isEmpty()) {
                            userName = requestorUid; // fallback to UID if no username
                        }
                        // 3) "X is requesting to follow you"
                        String msg = userName + " is requesting to follow you";
                        holder.requestMessage.setText(msg);
                    } else {
                        // If doc doesn’t exist, fallback
                        holder.requestMessage.setText(requestorUid + " is requesting to follow you");
                    }
                })
                .addOnFailureListener(e -> {
                    // If Firestore read fails, fallback to UID
                    holder.requestMessage.setText(requestorUid + " is requesting to follow you");
                });

        // 4) The small avatar
        holder.uid.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("uid", requestorUid);
            Navigation.findNavController(v).navigate(R.id.otherUserProfile, args);
        });

        // 5) Accept & Deny
        holder.acceptButton.setOnClickListener(v -> fragment.accept(requestorUid));
        holder.denyButton.setOnClickListener(v -> fragment.deny(requestorUid));
    }


    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView uid;
        TextView requestMessage;
        Button acceptButton, denyButton;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            uid = itemView.findViewById(R.id.uid);
            requestMessage = itemView.findViewById(R.id.requestMessage);
            acceptButton = itemView.findViewById(R.id.acceptButton);
            denyButton = itemView.findViewById(R.id.denyButton);
        }
    }
}
