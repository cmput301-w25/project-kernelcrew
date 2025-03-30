package com.kernelcrew.moodapp.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Tasks;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.kernelcrew.moodapp.R;
import com.kernelcrew.moodapp.data.CombinedListener;
import com.kernelcrew.moodapp.data.MoodEvent;
import com.kernelcrew.moodapp.data.MoodEventFilter;
import com.kernelcrew.moodapp.data.MoodEventProvider;
import com.kernelcrew.moodapp.data.User;
import com.kernelcrew.moodapp.data.UserProvider;
import com.kernelcrew.moodapp.ui.components.DefaultFilterBarFragment;
import com.kernelcrew.moodapp.ui.components.FilterBarFragment;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.google.android.gms.tasks.Task;

/**
 * A fragment that displays the user's feed: mood events from people the user is following.
 */
public class HomeFeed extends DefaultFilterBarFragment
        implements FilterBarFragment.OnUserSearchListener, FilterBarFragment.OnFilterChangedListener {

    private FirebaseAuth auth;
    private FirebaseUser user;
    private MoodEventProvider provider;
    private ListenerRegistration registration;
    private FilterBarFragment searchNFilterFragment;
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

        NavigationBarView navigationBar = view.findViewById(R.id.bottom_navigation);
        recyclerView = view.findViewById(R.id.moodRecyclerView);
        navBarController = new BottomNavBarController(navigationBar);

        // Get the FilterBar fragment
        searchNFilterFragment = (FilterBarFragment) getChildFragmentManager().findFragmentById(R.id.filterBarFragment);
        assert searchNFilterFragment != null;
        searchNFilterFragment.setAllowUserSearch(true);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        moodAdapter = new MoodAdapter();
        userAdapter = new UserAdapter();
        recyclerView.setAdapter(moodAdapter);

        if (auth.getCurrentUser() == null) {
            Log.e("HomeFeed", "User not authenticated!");
        }

        searchNFilterFragment.setOnUserSearchListener(this);
        // If the user toggles filters, this method is called
        searchNFilterFragment.setOnFilterChangedListener(this);

        // When a mood is clicked, navigate to mood details.
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

    @Override
    public void onUserSearchResults(List<User> users) {
        recyclerView.setAdapter(userAdapter);
        userAdapter.setUsers(users);
        if (users.isEmpty()) {
            Log.d("HomeFeed", "No user results found.");
        } else {
            Log.d("HomeFeed", "Showing " + users.size() + " user results.");
        }
    }

    @Override
    public void onFilterChanged(MoodEventFilter filter) {
        // Switch back to mood view
        recyclerView.setAdapter(moodAdapter);

        if (user == null) {
            Log.e("HomeFeed", "No user found for feed.");
            return;
        }

        // Fetch the list of users the current user is following.
        UserProvider.getInstance().fetchFollowing(user.getUid())
                .addOnSuccessListener(following -> {
                    // Build list of user IDs, including the current user's UID.
                    List<String> userIds = new ArrayList<>();
                    userIds.add(user.getUid());

                    for (User followedUser : following) {
                        userIds.add(followedUser.getUid());
                    }

                    registration =
                            MoodEventProvider.getInstance().listenToMoodEventsForUsers(userIds, filter, 3, new CombinedListener() {
                                @Override
                                public void onEvent(List<DocumentSnapshot> documents, FirebaseFirestoreException error) {
                                    if (error != null) {
                                        Log.e("HomeFeed", "Error listening to mood events", error);
                                        return;
                                    }

                                    List<MoodEvent> moods = new ArrayList<>();
                                    for (DocumentSnapshot doc : documents) {
                                        MoodEvent mood = doc.toObject(MoodEvent.class);
                                        if (mood != null) {
                                            mood.setId(doc.getId());
                                            moods.add(mood);
                                        }
                                    }

                                    moods.sort((m1, m2) -> m2.getCreated().compareTo(m1.getCreated()));
                                    Log.v("HomeFeed", "Displaying " + String.valueOf(moods.size()) + " Mood Events");
                                    moodAdapter.setMoods(moods);
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("HomeFeed", "Failed to fetch following users", e);
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.registration.remove();
    }

    /**
     * Simple user adapter for search results
     */
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

        static class UserViewHolder extends RecyclerView.ViewHolder {
            private final ShapeableImageView avatar;
            private final android.widget.TextView usernameTextView;

            public UserViewHolder(@NonNull View itemView) {
                super(itemView);
                avatar = itemView.findViewById(R.id.avatarImageView);
                usernameTextView = itemView.findViewById(R.id.usernameTextView);
            }

            public void bind(User user) {
                usernameTextView.setText(user.getName());
            }
        }
    }
}
