package com.kernelcrew.moodapp.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.textfield.TextInputEditText;
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
    private TextInputEditText triggerEditText;
    private AutoCompleteTextView situationAutoComplete;
    private TextInputEditText reasonEditText;

    private FirebaseUser currentUser;
    private MoodEventController moodEventController;

    private static class MoodEventDetails {
        Emotion emotion;
        String trigger;
        String socialSituation;
        String reason;
    }

    private @Nullable MoodEventDetails validateFields() {
        MoodEventDetails details = new MoodEventDetails();

        details.emotion = emotionPickerFragment.getSelected();
        if (details.emotion == null) {
            emotionPickerFragment.setError("An emotion is required");
            return null;
        }

        details.trigger = triggerEditText.getText().toString();
        details.socialSituation = situationAutoComplete.getText().toString();

        details.reason = reasonEditText.getText().toString();
        if (details.reason.length() > 20 && details.reason.split(" ").length > 3) {
            reasonEditText.setError("Reason must be less than 20 characters or 3 words");
            return null;
        }

        return details;
    }

    private void handleSubmit(View _buttonView) {
        MoodEventDetails details = validateFields();
        if (details == null) {
            return;
        }

        MoodEvent moodEvent = new MoodEvent(
                currentUser.getUid(),
                details.emotion,
                details.trigger,
                details.socialSituation,
                details.reason,
                "",     // photoUrl
                null,   // latitude
                null    // longitude
        );
        moodEventController.insertMoodEvent(moodEvent);

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
        if (emotionPickerFragmentContainer == null) {
            // Log error and possibly show an error message to the user
            Log.e("CreateMoodEvent", "Emotion picker container not found in layout.");
            return;
        }
        emotionPickerFragment = emotionPickerFragmentContainer.getFragment();
        if (emotionPickerFragment == null) {
            Log.e("CreateMoodEvent", "EmotionPickerFragment not attached. Ensure it's specified in the layout.");
            return;
        }

        triggerEditText = view.findViewById(R.id.emotion_trigger);
        situationAutoComplete = view.findViewById(R.id.emotion_situation);
        reasonEditText = view.findViewById(R.id.emotion_reason);

        moodEventController = MoodEventController.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;
    }
}