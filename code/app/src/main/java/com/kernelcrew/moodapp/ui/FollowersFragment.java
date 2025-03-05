package com.kernelcrew.moodapp.ui;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kernelcrew.moodapp.R;
import com.google.android.material.imageview.ShapeableImageView;
import java.util.ArrayList;
import java.util.List;

public class FollowersFragment extends Fragment {

    private RecyclerView followersRecyclerView;
    private FollowersAdapter adapter;
    private List<User> followersList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_followers, container, false);

        followersRecyclerView = view.findViewById(R.id.followersRecyclerView);
        followersRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new FollowersAdapter(followersList);
        followersRecyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        fetchFollowers();

        BottomNavigationView bottomNavigationView = view.findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.page_myProfile);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.page_myProfile) {
                Navigation.findNavController(view).navigate(R.id.myProfile);
                return true;
            }
            return false;
        });

        MaterialToolbar topAppBar = view.findViewById(R.id.topAppBar);
        topAppBar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        return view;
    }

    private void fetchFollowers() {
        db.collection("users").document("testUser").collection("followers")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    followersList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getId();
                        boolean isFollowed = doc.getBoolean("isFollowed") != null && doc.getBoolean("isFollowed");
                        followersList.add(new User(name, isFollowed));
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> System.out.println("TEST_FIRESTORE: Failed to load followers."));
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
