package com.kernelcrew.moodapp.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.kernelcrew.moodapp.R;
import com.kernelcrew.moodapp.data.MoodEvent;
import com.kernelcrew.moodapp.data.MoodEventFilter;
import com.kernelcrew.moodapp.data.MoodEventProvider;
import com.kernelcrew.moodapp.data.User;
import com.kernelcrew.moodapp.ui.components.DefaultFilterBarFragment;
import com.kernelcrew.moodapp.ui.components.FilterBarFragment;

import java.util.ArrayList;
import java.util.List;

public class HomeFeed extends DefaultFilterBarFragment implements FilterBarFragment.OnUserSearchListener {
    private FirebaseAuth auth;
    private FirebaseUser user;
    private MoodEventProvider provider;

    private FilterBarFragment searchNFilterFragment;
    private NavigationBarView navigationBar;
    private BottomNavBarController navBarController;
    private RecyclerView recyclerView;
    private MoodAdapter moodAdapter;
    private UserAdapter userAdapter;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_feed, container, false);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        provider = MoodEventProvider.getInstance();

        navigationBar = view.findViewById(R.id.bottom_navigation);
        recyclerView = view.findViewById(R.id.moodRecyclerView);

        navBarController = new BottomNavBarController(navigationBar);

        searchNFilterFragment = (FilterBarFragment) getChildFragmentManager().findFragmentById(R.id.filterBarFragment);
        assert searchNFilterFragment != null;
        searchNFilterFragment.setAllowUserSearch(true);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        moodAdapter = new MoodAdapter();
        userAdapter = new UserAdapter();

        recyclerView.setAdapter(moodAdapter);

        if (auth.getCurrentUser() == null) {
            Log.e("Home", "User not authenticated!");
        }

        searchNFilterFragment.setOnUserSearchListener(this);
        searchNFilterFragment.setOnFilterChangedListener(filter -> {
            MoodEventProvider.getInstance().getMoodEvents().addOnSuccessListener(querySnapshot -> {
                if (querySnapshot == null) {
                    Log.w("HomeFeed", "No snapshot data received.");
                    return;
                }

                List<MoodEvent> allMoods = new ArrayList<>();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    MoodEvent mood = doc.toObject(MoodEvent.class);
                    if (mood != null) {
                        mood.setId(doc.getId());
                        allMoods.add(mood);
                    }
                }

                List<MoodEvent> filtered = searchNFilterFragment.applyLocalSearch(filter, allMoods);

                recyclerView.setAdapter(moodAdapter);
                moodAdapter.setMoods(filtered);
            });
        });

        moodAdapter.setOnMoodClickListener(mood -> {
            Bundle args = new Bundle();
            args.putString("moodEventId", mood.getId());
            args.putString("sourceScreen", "home"); // or "filtered"
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.action_homeFeed_to_moodDetails, args);
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navBarController.bind(view);
    }

    @Override
    public void onUserSearchResults(List<User> users) {
        // Switch our RecyclerView to show user items instead of mood items
        recyclerView.setAdapter(userAdapter);
        userAdapter.setUsers(users);

        // If you want to do other UI changes if empty, check users.isEmpty()
        if (users.isEmpty()) {
            Log.d("HomeFeed", "No user results found.");
            // Possibly show a "No results" text, etc.
        } else {
            Log.d("HomeFeed", "Showing " + users.size() + " user results.");
        }
    }

    private static class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
        private final List<User> userList = new ArrayList<>();

        @SuppressLint("NotifyDataSetChanged")
        public void setUsers(List<User> newUsers) {
            userList.clear();
            userList.addAll(newUsers);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_user, parent, false);
            return new UserViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
            User user = userList.get(position);
            holder.bind(user);
        }

        @Override
        public int getItemCount() {
            return userList.size();
        }

        class UserViewHolder extends RecyclerView.ViewHolder {
            private final ShapeableImageView avatar;
            private final TextView usernameTextView;
            private final CheckBox followCheckBox;

            public UserViewHolder(@NonNull View itemView) {
                super(itemView);
                avatar = itemView.findViewById(R.id.avatarImageView);
                usernameTextView = itemView.findViewById(R.id.usernameTextView);
                followCheckBox = itemView.findViewById(R.id.followCheckBox);
            }

            public void bind(User user) {
                usernameTextView.setText(user.getName());
                followCheckBox.setChecked(user.isFollowed());
            }
        }
    }
}