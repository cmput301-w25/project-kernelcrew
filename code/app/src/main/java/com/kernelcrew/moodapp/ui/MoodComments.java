package com.kernelcrew.moodapp.ui;

import static com.kernelcrew.moodapp.ui.MoodIconUtil.getMoodIconResource;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.kernelcrew.moodapp.R;
import com.kernelcrew.moodapp.data.Comment;
import com.kernelcrew.moodapp.data.CommentProvider;
import com.kernelcrew.moodapp.data.MoodEvent;
import com.kernelcrew.moodapp.data.MoodEventProvider;
import com.kernelcrew.moodapp.data.UserProvider;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * Fragment responsible for displaying the comments on a selected mood.
 *
 */
public class MoodComments extends Fragment implements MoodHistoryAdapter.OnItemClickListener {
    /** RecyclerView for displaying mood comment items */
    private RecyclerView recyclerView;
    private BottomNavBarController navBarController;
    private ImageView imageMoodIcon;
    private TextView tvMoodState, usernameText, dayTimeText, commentCountText;

    /** Adapter for binding mood data to the RecyclerView */
    private MoodEventProvider provider;
    // Add CommentProvider
    private CommentProvider commentProvider;
    private UserProvider userProvider;
    /** List to store mood events */
    private List<MoodEvent> moods = new ArrayList<>();
    /** Registration for Firestore listener */
    private ListenerRegistration snapshotListener;
    private FirebaseFirestore db;
    private String moodEventId;
    private String sourceScreen;
    private String userId;
    private String username;

    // Add these variables for the input fields
    private TextInputLayout searchInputLayout;
    private TextInputEditText searchInput;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve arguments passed from previous screen
        if (getArguments() != null) {
            moodEventId = getArguments().getString("moodEventId");
            sourceScreen = getArguments().getString("sourceScreen", "home");
        }
        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        provider = MoodEventProvider.getInstance();
        // Initialize UserProvider
        userProvider = UserProvider.getInstance();
        // Initialize CommentProvider
        commentProvider = CommentProvider.getInstance();
        // Get current user ID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            userProvider.getUsername(userId)
                    .addOnSuccessListener(result -> {
                        username = result;
                    })
                    .addOnFailureListener(e -> {
                        Log.e("MoodComments", "Error fetching username", e);
                    });
        }
    }

    /**
     * Creates and returns the view hierarchy associated with the fragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mood_comments, container, false);

        provider = MoodEventProvider.getInstance();
        userProvider = UserProvider.getInstance();

        recyclerView = view.findViewById(R.id.commentRecyclerView);
        imageMoodIcon = view.findViewById(R.id.imageMoodIcon);
        tvMoodState = view.findViewById(R.id.tvMoodState);
        dayTimeText = view.findViewById(R.id.dayTimeText);
        usernameText = view.findViewById(R.id.usernameText);
        commentCountText = view.findViewById(R.id.commentCount);

        MaterialToolbar toolbar = view.findViewById(R.id.topAppBar);

        Button viewDetailsButton = view.findViewById(R.id.viewDetailsButton);
        viewDetailsButton.setOnClickListener(v -> {
            onItemClick(moodEventId);
        });

        NavigationBarView navigationBarView = view.findViewById(R.id.bottom_navigation);
        navigationBarView.setSelectedItemId(R.id.page_createMoodEvent);
        navBarController = new BottomNavBarController(navigationBarView);

        // Initialize the comment input components
        searchInputLayout = view.findViewById(R.id.searchInputLayout);
        searchInput = view.findViewById(R.id.searchInput);

        // Set up the send button click listener
        setupCommentSendButton();

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        toolbar.setNavigationOnClickListener(v -> handleBackButton());

        // Fetch mood details from Firestore
        fetchMoodDetails(moodEventId);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navBarController.bind(view);
    }

    /**
     * Sets up the send button click listener to handle comment submission
     */
    private void setupCommentSendButton() {
        searchInputLayout.setEndIconOnClickListener(v -> {
            String commentText = searchInput.getText().toString().trim();
            if (!commentText.isEmpty()) {
                submitComment(commentText);
            }
        });
    }

    /**
     * Submits a new comment to Firestore
     *
     * @param commentText The text content of the comment
     */
    private void submitComment(String commentText) {
        if (userId == null || moodEventId == null) {
            Toast.makeText(getContext(), "Unable to submit comment. User or mood event not found.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a Comment object
        Comment comment = new Comment();
        comment.setUid(userId);
        comment.setUsername(username);
        comment.setMoodEventId(moodEventId);
        comment.setCommentText(commentText);
        comment.setCreated(new Date());

        // Add to DB using CommentProvider
        commentProvider.insertComment(comment)
                .addOnSuccessListener(result -> {
                    searchInput.setText("");
                    refreshComments();
                })
                .addOnFailureListener(error -> {
                    Toast.makeText(getContext(), "Failed to add comment: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("MoodComments", "Error adding comment: " + error);
                });
    }

    private void fetchComments() {
        if (moodEventId == null) {
            return;
        }

        // Create a CommentAdapter
        CommentAdapter commentAdapter = new CommentAdapter();
        recyclerView.setAdapter(commentAdapter);

        // Query comments for this mood event
        commentProvider.getCommentsByMoodEventId(moodEventId)
                .addOnSuccessListener(comments -> {
                    commentAdapter.setComments(comments);
                    commentCountText.setText(String.valueOf(comments.size()));
                })
                .addOnFailureListener(error -> {
                    Log.e("MoodComments", "Error fetching comments: " + error);
                    Toast.makeText(getContext(), "Failed to load comments", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Fetches the mood details document from Firestore using the moodEventId.
     */
    private void fetchMoodDetails(String moodEventId) {
        if (moodEventId == null) return;
        provider.getMoodEvent(moodEventId)
                .addOnSuccessListener(moodEvent -> {
                    if (moodEvent != null) {
                        bindMoodData(moodEvent);
                    } else {
                        Toast.makeText(requireContext(), "Mood event not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to load mood details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        fetchComments();
    }

    /**
     * Binds MoodEvent data to the UI components.
     */
    private void bindMoodData(MoodEvent moodEvent) {
        tvMoodState.setText(moodEvent.getEmotion().toString());
        int moodImageRes = getMoodIconResource(moodEvent.getEmotion().toString());
        imageMoodIcon.setImageResource(moodImageRes);

        SimpleDateFormat sdf = new SimpleDateFormat("EEE ha", Locale.getDefault());
        String formattedDate = sdf.format(moodEvent.getCreated());

        dayTimeText.setText(formattedDate);

        // Retrieve the UID from the mood event
        userId = moodEvent.getUid();
        usernameText.setText(moodEvent.getUsername());

    }

    /**
     * Refreshes the comments list by fetching the latest comments from Firestore
     */
    private void refreshComments() {
        if (moodEventId == null) {
            return;
        }

        // Get the current adapter from the RecyclerView
        CommentAdapter adapter = (CommentAdapter) recyclerView.getAdapter();

        // Query comments for this mood event
        commentProvider.getCommentsByMoodEventId(moodEventId)
                .addOnSuccessListener(comments -> {
                    adapter.setComments(comments);
                    commentCountText.setText(String.valueOf(comments.size()));
                })
                .addOnFailureListener(error -> {
                    Log.e("MoodComments", "Error refreshing comments: " + error);
                    Toast.makeText(getContext(), "Failed to refresh comments", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Handles the navigation back action when the back button is pressed.
     *
     * Uses the NavController to pop the back stack, returning to the previous fragment.
     */
    private void handleBackButton() {
        androidx.navigation.fragment.NavHostFragment.findNavController(this).popBackStack();
    }

    /**
     * Callback method from the {@link MoodHistoryAdapter.OnItemClickListener} interface.
     *
     * Triggered when a user clicks on the mood details button Navigates to the
     * MoodDetails fragment passing the selected mood event ID as an argument.
     *
     * @param moodEventId The ID of the selected mood event
     */
    @Override
    public void onItemClick(String moodEventId) {
        // Navigate to MoodDetails fragment with the moodEventId as an argument
        Bundle args = new Bundle();
        args.putString("moodEventId", moodEventId);
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        navController.navigate(R.id.action_commentsFragment_to_moodDetails, args);
    }
}