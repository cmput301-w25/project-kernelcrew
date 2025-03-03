package com.kernelcrew.moodapp.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kernelcrew.moodapp.R;
import com.kernelcrew.moodapp.data.Emotion;
import com.kernelcrew.moodapp.data.MoodEvent;
import com.kernelcrew.moodapp.data.MoodEventController;
import com.kernelcrew.moodapp.ui.components.EmotionPickerFragment;

public class CreateMoodEvent extends Fragment {
    private NavigationBarView navigationBarView;
    private NavController navController;
    private BottomNavBarController navBarController;

    private EmotionPickerFragment emotionPickerFragment;

    private FirebaseUser currentUser;
    private MoodEventController moodEventController;

    private static class MoodEventDetails {
        Emotion emotion;
    }

    private @Nullable MoodEventDetails validateFields() {
        MoodEventDetails details = new MoodEventDetails();
        details.emotion = emotionPickerFragment.getSelected();
        if (details.emotion == null) {
            emotionPickerFragment.setError("An emotion is required");
            return null;
        }

        return details;
    }

    private void handleSubmit(View _buttonView) {
        MoodEventDetails details = validateFields();
        if (details == null) {
            return;
        }

        MoodEvent moodEvent = new MoodEvent(currentUser.getUid(), details.emotion);

        moodEventController.createMoodEvent(moodEvent);

        navController.navigate(R.id.homeFeed);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_mood_event, container, false);

        navigationBarView = view.findViewById(R.id.bottom_navigation);

        navigationBarView.setSelectedItemId(R.id.page_createMoodEvent);
        navBarController = new BottomNavBarController(navigationBarView);

        Button submitButton = view.findViewById(R.id.submit_button);
        submitButton.setOnClickListener(this::handleSubmit);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navBarController.bind(view);
        navController = Navigation.findNavController(view);

        FragmentContainerView emotionPickerFragmentContainer =
                view.findViewById(R.id.emotion_picker);
        emotionPickerFragment = emotionPickerFragmentContainer.getFragment();
        assert emotionPickerFragment != null;

        moodEventController = MoodEventController.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;
    }
}