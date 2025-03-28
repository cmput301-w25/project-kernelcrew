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

import com.google.android.gms.tasks.Tasks;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kernelcrew.moodapp.R;
import com.kernelcrew.moodapp.data.MoodEvent;
import com.kernelcrew.moodapp.data.MoodEventProvider;

public class CreateMoodEvent extends Fragment {
    private NavController navController;
    private BottomNavBarController navBarController;

    private FirebaseUser currentUser;
    private MoodEventProvider provider;

    private void handleSubmit(MoodEventForm.MoodEventDetails details) {
        details.username = currentUser.getDisplayName();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_mood_event, container, false);

        NavigationBarView navigationBarView = view.findViewById(R.id.bottom_navigation);
        navigationBarView.setSelectedItemId(R.id.page_createMoodEvent);
        navBarController = new BottomNavBarController(navigationBarView);

        return view;
    }

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

        // First set update listener to connect fragments
        locationFragment.setUpdateListener(form);

        // Then set submit callback
        form.onSubmit(this::handleSubmit);

        provider = MoodEventProvider.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;
    }
}