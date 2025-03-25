package com.kernelcrew.moodapp.ui;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kernelcrew.moodapp.R;
import com.kernelcrew.moodapp.data.FollowProvider;
import com.kernelcrew.moodapp.data.User;
import com.kernelcrew.moodapp.data.UserProvider;
import java.util.ArrayList;
import java.util.List;

public class FollowersFragment extends Fragment {
    private FollowersAdapter adapter;
    private final List<User> followersList = new ArrayList<>();
    private BottomNavBarController navBarController;
    private UserProvider userProvider;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_followers, container, false);

        RecyclerView followersRecyclerView = view.findViewById(R.id.followersRecyclerView);
        followersRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new FollowersAdapter(followersList);
        followersRecyclerView.setAdapter(adapter);

        userProvider = UserProvider.getInstance();
        fetchFollowers();

        NavigationBarView navigationBarView = view.findViewById(R.id.bottom_navigation);
        navBarController = new BottomNavBarController(navigationBarView);

        MaterialToolbar topAppBar = view.findViewById(R.id.topAppBar);
        topAppBar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navBarController.bind(view);
    }

    private void fetchFollowers() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            System.out.println("ERROR: No user logged in.");
            return;
        }
        FollowProvider.getInstance()
                .fetchFollowers(currentUser.getUid())
                .addOnSuccessListener(followers -> {
                    followersList.clear();
                    followersList.addAll(followers);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(error -> {
                    Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("FollowersFragment", error.toString());
                });
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
            String uid = user.getName();

            // Fetch the username from Firestore
            FirebaseFirestore.getInstance().collection("users").document(uid)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String realName = doc.getString("username");
                            if (realName == null || realName.isEmpty()) {
                                realName = uid;
                            }
                            holder.usernameTextView.setText(realName);
                        } else {
                            holder.usernameTextView.setText(uid);
                        }
                    })
                    .addOnFailureListener(e -> holder.usernameTextView.setText(uid));

            holder.avatarImageView.setImageResource(R.drawable.ic_person);

            holder.itemView.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                // Pass UID (or if you want, also pass the friendly name)
                bundle.putString("requestType", "follow_request");
                bundle.putString("username", uid); // ideally, pass the friendly name if already fetched
                Navigation.findNavController(v).navigate(R.id.action_followersFragment_to_requestFragment, bundle);
            });
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
}
