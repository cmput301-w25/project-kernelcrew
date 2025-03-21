package com.kernelcrew.moodapp.ui.components;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.kernelcrew.moodapp.R;
import com.kernelcrew.moodapp.data.User;
import com.kernelcrew.moodapp.data.UserProvider;

import java.util.List;

public class SearchUsersFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView noResultsText;
    private SearchUsersAdapter adapter;

    public SearchUsersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search_users_results_page, container, false);

        MaterialToolbar topAppBar = view.findViewById(R.id.topAppBar);
        // Use NavHostFragment.findNavController(this) to handle back navigation
        topAppBar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(SearchUsersFragment.this).navigateUp());

        recyclerView = view.findViewById(R.id.searchResultsRecyclerView);
        progressBar = view.findViewById(R.id.searchProgressBar);
        noResultsText = view.findViewById(R.id.noResultsTextView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SearchUsersAdapter();
        recyclerView.setAdapter(adapter);

        // Retrieve the search query from fragment arguments (set this when navigating to SearchUsersFragment)
        String searchQuery = "";
        Bundle args = getArguments();
        if (args != null) {
            searchQuery = args.getString("search_query", "");
        }

        // Get the current user id
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : "";

        // Show loading spinner
        progressBar.setVisibility(View.VISIBLE);
        noResultsText.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);

        // Perform user search
        UserProvider.getInstance().searchUsers(searchQuery, currentUserId)
                .addOnSuccessListener(users -> {
                    progressBar.setVisibility(View.GONE);
                    if (users.isEmpty()) {
                        noResultsText.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        adapter.setUsers(users);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Search failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        return view;
    }

    // Inner adapter class for displaying search results
    private class SearchUsersAdapter extends RecyclerView.Adapter<SearchUsersAdapter.UserViewHolder> {
        private List<User> users;

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
            User user = users.get(position);
            holder.bind(user);
        }

        @Override
        public int getItemCount() {
            return users == null ? 0 : users.size();
        }

        class UserViewHolder extends RecyclerView.ViewHolder {
            private final TextView usernameTextView;

            public UserViewHolder(@NonNull View itemView) {
                super(itemView);
                usernameTextView = itemView.findViewById(R.id.usernameTextView);

                // Hide the checkbox for search results
                CheckBox followCheckBox = itemView.findViewById(R.id.followCheckBox);
                followCheckBox.setVisibility(View.GONE);

                // Use NavHostFragment.findNavController to navigate when an item is clicked
                itemView.setOnClickListener(v -> {
                    Bundle args = new Bundle();
                    // Passing the username as uid here; adjust if your User model contains a dedicated id field
                    args.putString("uid", usernameTextView.getText().toString());
                    NavHostFragment.findNavController(SearchUsersFragment.this)
                            .navigate(R.id.otherUserProfile, args);
                });
            }

            public void bind(User user) {
                usernameTextView.setText(user.getName());
            }
        }
    }
}
