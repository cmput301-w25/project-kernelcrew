package com.kernelcrew.moodapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.kernelcrew.moodapp.R;
import com.kernelcrew.moodapp.data.User;
import com.kernelcrew.moodapp.data.UserProvider;

import java.util.List;

public class SearchUsers extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView noResultsText;
    private SearchUsersAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_search_users_results_page);

        recyclerView = findViewById(R.id.searchResultsRecyclerView);
        progressBar = findViewById(R.id.searchProgressBar);
        noResultsText = findViewById(R.id.noResultsTextView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SearchUsersAdapter();
        recyclerView.setAdapter(adapter);

        // Get the search query passed via intent extra "search_query"
        String searchQuery = getIntent().getStringExtra("search_query");
        if (searchQuery == null) {
            searchQuery = "";
        }

        // Get the current user id
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : "";

        // Show the loading spinner
        progressBar.setVisibility(View.VISIBLE);
        noResultsText.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);

        // Call the searchUsers method from UserProvider
        UserProvider.getInstance().searchUsers(searchQuery, currentUserId)
                .addOnSuccessListener(new OnSuccessListener<List<User>>() {
                    @Override
                    public void onSuccess(List<User> users) {
                        progressBar.setVisibility(View.GONE);
                        if (users.isEmpty()) {
                            noResultsText.setVisibility(View.VISIBLE);
                        } else {
                            recyclerView.setVisibility(View.VISIBLE);
                            adapter.setUsers(users);
                        }
                    }
                });
    }

    // Inner adapter class for displaying user search results
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
                // Set click listener to navigate to the user's profile
                itemView.setOnClickListener(v -> {
                    // TODO: Implement navigation to user profile
                    // For example:
                    // Intent intent = new Intent(SearchUsers.this, UserProfileActivity.class);
                    // intent.putExtra("username", usernameTextView.getText().toString());
                    // startActivity(intent);
                });
            }

            public void bind(User user) {
                usernameTextView.setText(user.getName());
            }
        }
    }
}
