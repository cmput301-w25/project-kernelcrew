package com.kernelcrew.moodapp.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.kernelcrew.moodapp.R;
import com.kernelcrew.moodapp.data.FollowRequestProvider;
import com.kernelcrew.moodapp.data.MoodEvent;
import com.kernelcrew.moodapp.data.MoodEventProvider;
import com.kernelcrew.moodapp.data.User;
import com.kernelcrew.moodapp.data.UserProvider;
import com.kernelcrew.moodapp.ui.components.FilterBarFragment;

import java.util.ArrayList;
import java.util.List;

public class HomeFeed extends Fragment {
    FirebaseAuth auth;
    FirebaseUser user;
    MoodEventProvider provider;

    FilterBarFragment searchNFilterFragment;
    NavigationBarView navigationBar;
    BottomNavBarController navBarController;
    RecyclerView moodRecyclerView;
    MoodAdapter moodAdapter;
    UserAdapter userAdapter; // new adapter for user search results

    public static List<MoodEvent> currentFilteredList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_feed, container, false);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        provider = MoodEventProvider.getInstance();

        navigationBar = view.findViewById(R.id.bottom_navigation);
        moodRecyclerView = view.findViewById(R.id.moodRecyclerView);

        navBarController = new BottomNavBarController(navigationBar);

        searchNFilterFragment = (FilterBarFragment) getChildFragmentManager().findFragmentById(R.id.filterBarFragment);

        moodRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        moodAdapter = new MoodAdapter();
        userAdapter = new UserAdapter(new ArrayList<>()); // initialize the new user adapter

        // Initially show mood events
        moodRecyclerView.setAdapter(moodAdapter);

        if (user != null) {
            String myUid = user.getUid();

            // Initialize FollowRequestProvider
            FollowRequestProvider followRequestProvider = new FollowRequestProvider(getContext());

            // Listen for follow requests
            followRequestProvider.listenForFollowRequests(myUid);

            // Listen for follow accepted notifications
            followRequestProvider.listenForFollowAcceptedNotifications(myUid);
        }

        if (auth.getCurrentUser() == null) {
            Log.e("Home", "User not authenticated!");
        }

        FollowRequestProvider followRequestProvider = new FollowRequestProvider(getContext());

        // Listener for filter changes
        if (searchNFilterFragment != null) {
            searchNFilterFragment.setOnFilterChangedListener(filter -> {
                // Check the search type from the filter.
                // (Assuming FilterBarFragment's filter provides getSearchType() and getSearchQuery())
                if ("USERS".equalsIgnoreCase(filter.getSearchType())) {
                    // Perform user search using UserProvider
                    String query = filter.getSearchQuery();
                    UserProvider.getInstance().searchUsers(query)
                            .addOnSuccessListener(users -> {
                                // Switch to the user adapter and update the list
                                userAdapter.setUsers(users);
                                moodRecyclerView.setAdapter(userAdapter);
                            })
                            .addOnFailureListener(e -> {
                                Log.w("HomeFeed", "User search failed.", e);
                            });
                } else {
                    // Otherwise, perform mood event search as before
                    moodRecyclerView.setAdapter(moodAdapter); // ensure mood adapter is active
                    filter.buildQuery()
                            .addSnapshotListener((snapshots, error) -> {
                                if (error != null) {
                                    Log.w("HomeFeed", "Listen failed.", error);
                                    return;
                                }
                                if (snapshots == null) {
                                    Log.w("HomeFeed", "No snapshot data received.");
                                    return;
                                }
                                List<MoodEvent> moodList = new ArrayList<>();
                                for (DocumentSnapshot doc : snapshots.getDocuments()) {
                                    MoodEvent mood = doc.toObject(MoodEvent.class);
                                    if (mood != null) {
                                        mood.setId(doc.getId());
                                        moodList.add(mood);
                                    }
                                }
                                currentFilteredList = moodList;
                                moodAdapter.setMoods(moodList);
                            });
                }
            });
        }

        moodAdapter.setOnMoodClickListener(new MoodAdapter.OnMoodClickListener() {
            @Override
            public void onViewDetails(MoodEvent mood) {
                Bundle args = new Bundle();
                args.putString("moodEventId", mood.getId());
                args.putString("sourceScreen", "home");
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                navController.navigate(R.id.action_homeFeed_to_moodDetails, args);
            }

            @Override
            public void onViewComments(MoodEvent mood) {
                Bundle args = new Bundle();
                args.putString("moodEventId", mood.getId());
                args.putString("sourceScreen", "home");
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                navController.navigate(R.id.action_homeFeed_to_moodComments, args);
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navBarController.bind(view);
    }

    // Inner class for the user search results adapter
    private class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
        private List<User> users;

        UserAdapter(List<User> users) {
            this.users = users;
        }

        public void setUsers(List<User> users) {
            this.users = users;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
            return new UserViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
            User userItem = users.get(position);
            holder.usernameTextView.setText(userItem.getName());
            // Optionally set avatar or other fields if needed.
        }

        @Override
        public int getItemCount() {
            return users.size();
        }

        class UserViewHolder extends RecyclerView.ViewHolder {
            TextView usernameTextView;
            ImageView avatarImageView;

            UserViewHolder(@NonNull View itemView) {
                super(itemView);
                usernameTextView = itemView.findViewById(R.id.usernameTextView);
                avatarImageView = itemView.findViewById(R.id.avatarImageView);
            }
        }
    }
}
