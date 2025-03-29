package com.kernelcrew.moodapp.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.kernelcrew.moodapp.R;
import com.kernelcrew.moodapp.data.CombinedListener;
import com.kernelcrew.moodapp.data.FollowProvider;
import com.kernelcrew.moodapp.data.FollowRequestProvider;
import com.kernelcrew.moodapp.data.MoodEvent;
import com.kernelcrew.moodapp.data.MoodEventFilter;
import com.kernelcrew.moodapp.data.MoodEventProvider;
import com.kernelcrew.moodapp.data.User;
import com.kernelcrew.moodapp.data.UserProvider;
import com.kernelcrew.moodapp.ui.components.DefaultFilterBarFragment;
import com.kernelcrew.moodapp.ui.components.FilterBarFragment;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HomeFeed extends DefaultFilterBarFragment implements FilterBarFragment.OnUserSearchListener, FilterBarFragment.OnFilterChangedListener {
    private FirebaseAuth auth;
    private FirebaseUser user;
    private MoodEventProvider provider;

    private ListenerRegistration followingListener;
    private ListenerRegistration moodEventsListener;

    private FilterBarFragment searchNFilterFragment;
    private BottomNavBarController navBarController;
    private RecyclerView recyclerView;
    private MoodAdapter moodAdapter;
    private UserAdapter userAdapter;

    public static List<MoodEvent> currentFilteredList = new ArrayList<>();
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
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        navBarController = new BottomNavBarController(navigationBar);

        // Get our filter and search fragment
        searchNFilterFragment = (FilterBarFragment) getChildFragmentManager().findFragmentById(R.id.filterBarFragment);
        assert searchNFilterFragment != null;
        searchNFilterFragment.setAllowUserSearch(true);

        moodAdapter = new MoodAdapter();
        userAdapter = new UserAdapter();
        recyclerView.setAdapter(moodAdapter);

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
            Log.e("HomeFeed", "User not authenticated!");
        }

        FollowRequestProvider followRequestProvider = new FollowRequestProvider(getContext());

        searchNFilterFragment.setOnUserSearchListener(this);

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
        userAdapter.setUsers(users);
        recyclerView.setAdapter(userAdapter);
        Log.d("HomeFeed", users.isEmpty() ? "No user results found." : "Showing " + users.size() + " user results.");
    }

    @Override
    public void onFilterChanged(MoodEventFilter filter) {
        recyclerView.setAdapter(moodAdapter);
        fetchMoodEvents(filter);
    }

    private void fetchMoodEvents(MoodEventFilter filter) {
        followingListener = FollowProvider.getInstance().listenForFollowing(user.getUid(), (snapshot, error) -> {
            if (error != null) {
                Log.e("HomeFeed", "Error listening to following changes", error);
                return;
            }
            List<String> userIds = new ArrayList<>();

            userIds.add(user.getUid());
            if (snapshot != null && !snapshot.isEmpty()) {
                for (DocumentSnapshot doc : snapshot.getDocuments()) {
                    userIds.add(doc.getId());
                }
            }

            if (moodEventsListener != null) {
                moodEventsListener.remove();
            }

            moodEventsListener = MoodEventProvider.getInstance()
                    .listenToMoodEventsForUsers(userIds, filter, new CombinedListener() {
                        @Override
                        public void onEvent(List<DocumentSnapshot> documents, FirebaseFirestoreException error) {
                            if (error != null) {
                                Log.e("HomeFeed", "Error listening to mood events", error);
                                return;
                            }
                            List<MoodEvent> events = new ArrayList<>();
                            for (DocumentSnapshot doc : documents) {
                                MoodEvent mood = doc.toObject(MoodEvent.class);
                                if (mood != null) {
                                    mood.setId(doc.getId());
                                    events.add(mood);
                                }
                            }
                            // Update your adapter with the combined, sorted events.
                            Log.d("HomeFeed", events.isEmpty() ? "No moodEvent results found." : "Showing " + events.size() + " moodEvent results.");
                            moodAdapter.setMoods(events);
                        }
                    });
        });
    }


    private class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
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

            public UserViewHolder(@NonNull View itemView) {
                super(itemView);
                avatar = itemView.findViewById(R.id.avatarImageView);
                usernameTextView = itemView.findViewById(R.id.usernameTextView);

                // Set a click listener for the entire item view.
                itemView.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        User clickedUser = userList.get(position);

                        Bundle args = new Bundle();
                        args.putString("uid", clickedUser.getUid());
                        NavHostFragment.findNavController(requireParentFragment())
                                .navigate(R.id.otherUserProfile, args);
                    }
                });

            }

            public void bind(User user) {
                usernameTextView.setText(user.getName());
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (followingListener != null) {
            followingListener.remove();
        }
        if (moodEventsListener != null) {
            moodEventsListener.remove();
        }
    }
}