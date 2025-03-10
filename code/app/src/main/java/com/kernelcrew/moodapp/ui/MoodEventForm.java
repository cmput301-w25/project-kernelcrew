package com.kernelcrew.moodapp.ui;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Picture;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.kernelcrew.moodapp.R;
import com.kernelcrew.moodapp.data.Emotion;
import com.kernelcrew.moodapp.data.MoodEvent;
import com.kernelcrew.moodapp.ui.components.EmotionPickerFragment;
import com.kernelcrew.moodapp.utils.PhotoUtils;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Fragment controller for MoodEventForm.
 * The MoodEventForm manages the fragment_mood_event_form fragment for both the
 * create mood event and edit mood event pages.
 * To listen to submissions, attach an onSubmit listener using the .onSubmit() method.
 * When editing a mood event, update the form state per a mood event using the .bind() method.
 */
public class MoodEventForm extends Fragment {
    private EmotionPickerFragment emotionPickerFragment;
    private TextInputEditText triggerEditText;
    private AutoCompleteTextView situationAutoComplete;
    private TextInputEditText reasonEditText;

    private Bitmap photo;
    private ImageButton photoButton;
    private TextView photoButtonError;

    /**
     * Handler for the image picker submission action.
     */
    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        setImageFromUri(data.getData());
                    }
                }
            });

    /**
     * Open an image picker and update the photoUri and photoButton photo when a selection is made.
     */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    /**
     * Change the photo button image
     * @param imageUri URI of image to set
     */
    private void setImageFromUri(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.requireActivity().getContentResolver(), imageUri);
            photo = bitmap;
            photoButton.setImageBitmap(bitmap);
        } catch (IOException e) {
            Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
            Log.e("MoodEventForm", e.toString());
        }
    }

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
        Bitmap photo;

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
            photo = moodEvent.getPhoto();
        }

        /**
         * Convert the user details to a MoodEvent
         * @param uid The uid of the user owning the mood event
         * @return Mood event from the mood details and uid
         */
        public MoodEvent toMoodEvent(String uid) {
            MoodEvent moodEvent = new MoodEvent(
                    uid,
                    emotion,
                    trigger,
                    socialSituation,
                    reason,
                    "",
                    0.0, // lat
                    0.0 // lng
            );

            moodEvent.setPhoto(photo);

            return moodEvent;
        }
    }

    public void bind(MoodEventDetails details) {
        emotionPickerFragment.setSelected(details.emotion);
        triggerEditText.setText(details.trigger);
        situationAutoComplete.setText(details.socialSituation);
        reasonEditText.setText(details.reason);

        photo = details.photo;
        photoButton.setImageBitmap(details.photo);
    }

    private @Nullable MoodEventDetails validateFields() {
        MoodEventDetails details = new MoodEventDetails();

        emotionPickerFragment.setError(null);
        reasonEditText.setError(null);
        photoButtonError.setText(null);

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

        details.photo = photo;
        if (photo != null && PhotoUtils.compressPhoto(photo).size() > 65536) {
            Log.i("MoodEventForm", "Image too large");
            photoButtonError.setText("Image too large");
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

        photoButton = view.findViewById(R.id.photo_button);
        photoButton.setOnClickListener(_v -> openImagePicker());
        photoButtonError = view.findViewById(R.id.photo_button_error);
    }
}