package com.kernelcrew.moodapp.ui;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import com.kernelcrew.moodapp.R;
import com.google.android.material.imageview.ShapeableImageView;
import java.util.ArrayList;
import java.util.List;

public class FollowersFragment extends Fragment {

    private RecyclerView followersRecyclerView;
    private FollowersAdapter adapter;
    private List<User> followersList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_followers, container, false);

        followersRecyclerView = view.findViewById(R.id.followersRecyclerView);
        followersRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Sample data
        followersList = new ArrayList<>();
        followersList.add(new User("Fiona", false));
        followersList.add(new User("George", false));
        followersList.add(new User("Helen", true));
        followersList.add(new User("Ian", true));
        followersList.add(new User("Jane", false));

        adapter = new FollowersAdapter(followersList);
        followersRecyclerView.setAdapter(adapter);

        return view;
    }

    private static class FollowersAdapter extends RecyclerView.Adapter<FollowersAdapter.ViewHolder> {
        private final List<User> users;

        FollowersAdapter(List<User> users) {
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
