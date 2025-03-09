package com.kernelcrew.moodapp.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.google.android.material.textfield.TextInputEditText;
import com.kernelcrew.moodapp.R;
import com.kernelcrew.moodapp.data.Emotion;
import com.kernelcrew.moodapp.data.MoodEvent;
import com.kernelcrew.moodapp.ui.components.EmotionPickerFragment;

public class MoodEventForm extends Fragment {
    private EmotionPickerFragment emotionPickerFragment;
    private TextInputEditText triggerEditText;
    private AutoCompleteTextView situationAutoComplete;
    private TextInputEditText reasonEditText;

    private MoodEventFormSubmitCallback callback;

    /**
     * A callback which is fired when a complete mood event form is submitted.
     */
    public interface MoodEventFormSubmitCallback {
        void handleSubmit(MoodEventDetails details);
    }

    /**
     * Register a on submit callback listener for the submit form action
     * @param callback Callback to register
     */
    public void onSubmit(MoodEventFormSubmitCallback callback) {
        this.callback = callback;
    }

    public static class MoodEventDetails {
        Emotion emotion;
        String trigger;
        String socialSituation;
        String reason;

        /**
         * Empty constructor which initializes everything to null.
         */
        MoodEventDetails() {}

        /**
         * Extract the mood event details from a MoodEvent
         * @param moodEvent MoodEvent to extract details from
         */
        public MoodEventDetails(MoodEvent moodEvent) {
            emotion = moodEvent.getEmotion();
            trigger = moodEvent.getTrigger();
            socialSituation = moodEvent.getSocialSituation();
            reason = moodEvent.getReason();
        }

        /**
         * Convert the user details to a MoodEvent
         * @param uid The uid of the user owning the mood event
         * @return Mood event from the mood details and uid
         */
        public MoodEvent toMoodEvent(String uid) {
            return new MoodEvent(
                    uid,
                    emotion,
                    trigger,
                    socialSituation,
                    reason,
                    "", // photoUrl,
                    0.0, // lat
                    0.0 // lng
            );
        }
    }

    public void bind(MoodEventDetails details) {
        emotionPickerFragment.setSelected(details.emotion);
        triggerEditText.setText(details.trigger);
        situationAutoComplete.setText(details.socialSituation);
        reasonEditText.setText(details.reason);
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

        callback.handleSubmit(details);
    }

    public MoodEventForm() {
        super(R.layout.fragment_mood_event_form);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button submitButton = view.findViewById(R.id.submit_button);
        submitButton.setOnClickListener(this::handleSubmit);

        FragmentContainerView emotionPickerFragmentContainer =
                view.findViewById(R.id.emotion_picker);
        if (emotionPickerFragmentContainer == null) {
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
    }
}