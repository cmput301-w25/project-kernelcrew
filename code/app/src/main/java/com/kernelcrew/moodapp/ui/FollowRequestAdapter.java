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
    public void onBindViewHolder(@NonNull ViewHolder holder, int pos) {
        String uid = items.get(pos);
        holder.requestMessage.setText(holder.itemView.getContext().getString(R.string.request_follow_message, uid));
        holder.uid.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("uid", uid);
            Navigation.findNavController(v).navigate(R.id.otherUserProfile, args);
        });
        holder.acceptButton.setOnClickListener(v -> fragment.accept(uid));
        holder.denyButton.setOnClickListener(v -> fragment.deny(uid));
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
