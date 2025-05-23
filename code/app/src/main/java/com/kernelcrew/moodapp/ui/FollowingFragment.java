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
import com.google.firebase.firestore.FirebaseFirestore;
import com.kernelcrew.moodapp.R;
import com.kernelcrew.moodapp.data.FollowProvider;
import com.kernelcrew.moodapp.data.User;
import com.kernelcrew.moodapp.data.UserProvider;
import java.util.ArrayList;
import java.util.List;

public class FollowingFragment extends Fragment {
    private FollowingAdapter adapter;
    private final List<User> followingList = new ArrayList<>();
    private BottomNavBarController navBarController;
    private UserProvider userProvider;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_following, container, false);

        RecyclerView followingRecyclerView = view.findViewById(R.id.followingRecyclerView);
        followingRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new FollowingAdapter(followingList);
        followingRecyclerView.setAdapter(adapter);

        userProvider = UserProvider.getInstance();
        fetchFollowing();

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

    private void fetchFollowing() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            System.out.println("ERROR: No user logged in.");
            return;
        }

        FollowProvider.getInstance()
                .fetchFollowing(currentUser.getUid())
                .addOnSuccessListener(following -> {
                    followingList.clear();
                    followingList.addAll(following);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(error -> {
                    Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("FollowingFragment", error.toString());
                });
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
            String uid = user.getUid();

            if (uid == null || uid.isEmpty()) {
                Log.e("FollowingAdapter", "User UID is null or empty at position " + position);
                return;
            }

            FirebaseFirestore.getInstance().collection("users").document(uid)
                    .get()
                    .addOnSuccessListener(doc -> {
                        String realName = doc.getString("username");
                        holder.usernameTextView.setText((realName != null && !realName.isEmpty()) ? realName : uid);
                    })
                    .addOnFailureListener(e -> holder.usernameTextView.setText(uid));

            holder.avatarImageView.setImageResource(R.drawable.ic_person);

            holder.itemView.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putString("uid", uid);
                Navigation.findNavController(v).navigate(R.id.otherUserProfile, args);
            });
        }


        @Override
        public int getItemCount() {
            return users.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ShapeableImageView avatarImageView;
            TextView usernameTextView;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                avatarImageView = itemView.findViewById(R.id.avatarImageView);
                usernameTextView = itemView.findViewById(R.id.usernameTextView);
            }
        }
    }
}
