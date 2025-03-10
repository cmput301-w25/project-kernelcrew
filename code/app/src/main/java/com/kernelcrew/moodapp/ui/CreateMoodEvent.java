/**
 * //Anthropic, Claude, "Generate source code descriptive comments for 301 rubric", 03-10-2025
 *
 * This fragment manages the creation of new mood events.
 * It coordinates between MoodEventForm and LocationFragment components,
 * handles the form submission, and saves mood events to Firebase.
 */
package com.kernelcrew.moodapp.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainer;
import androidx.fragment.app.FragmentContainerView;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kernelcrew.moodapp.R;
import com.kernelcrew.moodapp.data.MoodEvent;
import com.kernelcrew.moodapp.data.MoodEventProvider;

/**
 * CreateMoodEvent fragment handles the UI and logic for creating new mood events.
 * It integrates the MoodEventForm and LocationFragment to collect all necessary data
 * and submits the completed mood event to Firebase.
 */
public class CreateMoodEvent extends Fragment {
    private NavController navController;
    private BottomNavBarController navBarController;

    private FirebaseUser currentUser;
    private MoodEventProvider provider;

    /**
     * Handles the submission of mood event data from the form.
     * Creates a MoodEvent object and saves it to Firebase.
     *
     * @param details The mood event details collected from the form
     */
    private void handleSubmit(MoodEventForm.MoodEventDetails details) {
        MoodEvent moodEvent = details.toMoodEvent(currentUser.getUid());
        provider.insertMoodEvent(moodEvent)
                .addOnSuccessListener(_result -> {
                    navController.navigate(R.id.homeFeed);
                })
                .addOnFailureListener(error -> {
                    Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("CreateMoodEvent", error.toString());
                });
    }

    /**
     * Inflates the layout for this fragment and initializes the bottom navigation.
     *
     * @param inflater The LayoutInflater object to inflate views
     * @param container The parent view that the fragment UI should be attached to
     * @param savedInstanceState Previous state of the fragment if recreated
     * @return The root View of the fragment layout
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_mood_event, container, false);

        NavigationBarView navigationBarView = view.findViewById(R.id.bottom_navigation);
        navigationBarView.setSelectedItemId(R.id.page_createMoodEvent);
        navBarController = new BottomNavBarController(navigationBarView);

        return view;
    }

    /**
     * Called after the view is created to perform additional setup.
     * Initializes form and location fragments, sets up callbacks, and prepares Firebase.
     *
     * @param view The root view of the fragment
     * @param savedInstanceState Previous state of the fragment if recreated
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navBarController.bind(view);
        navController = Navigation.findNavController(view);

        // Get fragments
        FragmentContainerView formFragmentContainer = view.findViewById(R.id.mood_event_form);
        FragmentContainerView locationFragmentContainer = view.findViewById(R.id.location_fragment);

        // Check nulls first
        assert formFragmentContainer != null;
        assert locationFragmentContainer != null;

        MoodEventForm form = formFragmentContainer.getFragment();
        LocationFragment locationFragment = locationFragmentContainer.getFragment();

        // Check nulls again
        assert form != null;
        assert locationFragment != null;

        // Set the location update listener
        locationFragment.setListener(form);

        // Callback for form submission
        form.onSubmit(this::handleSubmit);

        provider = MoodEventProvider.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;
    }
}
