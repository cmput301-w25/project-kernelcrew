package com.kernelcrew.moodapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.kernelcrew.moodapp.R;
import com.google.android.material.imageview.ShapeableImageView;
import java.util.ArrayList;
import java.util.List;

public class FollowingFragment extends Fragment {

    private RecyclerView followingRecyclerView;
    private FollowingAdapter adapter;
    private List<User> followingList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_following, container, false);

        followingRecyclerView = view.findViewById(R.id.followingRecyclerView);
        followingRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Sample data
        followingList = new ArrayList<>();
        followingList.add(new User("Alice", true));
        followingList.add(new User("Bob", false));
        followingList.add(new User("Charlie", true));
        followingList.add(new User("Diana", true));
        followingList.add(new User("Evan", false));

        adapter = new FollowingAdapter(followingList);
        followingRecyclerView.setAdapter(adapter);

        return view;
    }

    private static class FollowingAdapter extends RecyclerView.Adapter<FollowingAdapter.ViewHolder> {
        private final List<User> users;

        FollowingAdapter(List<User> users) {
            this.users = users;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            User user = users.get(position);
            holder.usernameTextView.setText(user.getName());

            holder.avatarImageView.setImageResource(R.drawable.ic_person);
            holder.followCheckBox.setChecked(user.isFollowed());
        }

        @Override
        public int getItemCount() {
            return users.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ShapeableImageView avatarImageView;
            TextView usernameTextView;
            CheckBox followCheckBox;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                avatarImageView = itemView.findViewById(R.id.avatarImageView);
                usernameTextView = itemView.findViewById(R.id.usernameTextView);
                followCheckBox = itemView.findViewById(R.id.followCheckBox);
            }
        }
    }

    private static class User {
        private final String name;
        private final boolean followed;

        User(String name, boolean followed) {
            this.name = name;
            this.followed = followed;
        }

        public String getName() {
            return name;
        }

        public boolean isFollowed() {
            return followed;
        }
    }
}
