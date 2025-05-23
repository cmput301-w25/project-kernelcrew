package com.kernelcrew.moodapp.ui;

import static android.app.Activity.RESULT_OK;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import java.util.Calendar;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.kernelcrew.moodapp.R;
import com.kernelcrew.moodapp.data.Emotion;
import com.kernelcrew.moodapp.data.MoodEvent;
import com.kernelcrew.moodapp.data.MoodEventVisibility;
import com.kernelcrew.moodapp.ui.components.EmotionPickerFragment;
import com.kernelcrew.moodapp.ui.components.UploadPhotoFragment;
import com.kernelcrew.moodapp.utils.PhotoUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Fragment controller for MoodEventForm.
 * The MoodEventForm manages the fragment_mood_event_form fragment for both the
 * create mood event and edit mood event pages.
 * To listen to submissions, attach an onSubmit listener using the .onSubmit() method.
 * When editing a mood event, update the form state per a mood event using the .bind() method.
 */
public class MoodEventForm extends Fragment implements LocationUpdateListener {
    private Button addLocation;
    private EmotionPickerFragment emotionPickerFragment;
    private AutoCompleteTextView situationAutoComplete;
    private TextInputEditText reasonEditText;
    private Double currentLatitude = null;
    private Double currentLongitude = null;
    private Bitmap photo;
    private ImageButton photoButton;
    private Button photoResetButton;
    private TextView photoButtonError;
    private MaterialButtonToggleGroup visibilityToggle;

    private LocationFragment locationFragment;

    private TextInputEditText timestampInput;
    private Date selectedDate = new Date();

    // For tests
    private DatePickerDialog datePickerDialog;
    private TimePickerDialog timePickerDialog;

    /**
     * Clear the currently selected photo.
     */
    private void resetPhoto() {
        photo = null;
        photoButton.setImageResource(R.drawable.upload_splash);

        updateResetPhotoVisibility();
        resetPhotoButtonError(); // Clear any error if present
    }

    /**
     * Change the photo button image
     * @param bitmap Bitmap to set
     */
    private void setImageFromBitmap(Bitmap bitmap) {
        Log.i("MoodEventForm", "here");
        photo = bitmap;
        photoButton.setImageBitmap(bitmap);
        updateResetPhotoVisibility();
    }

    /**
     * Update the visibility of the reset photo button based on the current value of photo.
     */
    private void updateResetPhotoVisibility() {
        photoResetButton.setVisibility(photo == null ? INVISIBLE : VISIBLE);

        ViewGroup.LayoutParams layoutParams = photoResetButton.getLayoutParams();
        layoutParams.height = photo == null ? 0 : ViewGroup.LayoutParams.WRAP_CONTENT;
        photoResetButton.setLayoutParams(layoutParams);
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
        String username;
        Emotion emotion;
        String socialSituation;
        String reason;
        Double lat;
        Double lon;
        Bitmap photo;
        MoodEventVisibility visibility;

        Date timestamp;

        /**
         * Empty constructor which initializes everything to null.
         */
        MoodEventDetails() {}

        /**
         * Extract the mood event details from a MoodEvent
         * @param moodEvent MoodEvent to extract details from
         */
        public MoodEventDetails(MoodEvent moodEvent) {
            username = moodEvent.getUsername();
            emotion = moodEvent.getEmotion();
            socialSituation = moodEvent.getSocialSituation();
            reason = moodEvent.getReason();
            lat = moodEvent.getLatitude();
            lon = moodEvent.getLongitude();
            photo = moodEvent.getPhoto();
            visibility = moodEvent.getVisibility();
            timestamp = moodEvent.getCreated();
        }

        /**
         * Convert the user details to a MoodEvent
         * @param uid The uid of the user owning the mood event
         * @return Mood event from the mood details and uid
         */
        public MoodEvent toMoodEvent(String uid) {
            MoodEvent moodEvent = new MoodEvent(
                    uid,
                    username,
                    emotion,
                    socialSituation,
                    reason,
                    lat,
                    lon
            );

            moodEvent.setPhoto(photo);
            moodEvent.setVisibility(visibility);
            moodEvent.setCreated(timestamp);

            return moodEvent;
        }
    }

    public void bind(MoodEventDetails details) {
        emotionPickerFragment.setSelected(details.emotion);
        situationAutoComplete.setText(details.socialSituation);
        reasonEditText.setText(details.reason);
        selectedDate = details.timestamp != null ? details.timestamp : new Date();
        if (timestampInput != null) {
            timestampInput.setText(formatDate(selectedDate));
        }
        if (details.lat != null && details.lon != null) {
            // Update UI to show location is set
            this.currentLatitude = details.lat;
            this.currentLongitude = details.lon;
            if (locationFragment != null) {
                locationFragment.populateMapFromExistingLocation(details.lat, details.lon);
            }
        }

        if (details.photo != null) {
            photo = details.photo;
            photoButton.setImageBitmap(details.photo);
            updateResetPhotoVisibility();
        }

        visibilityToggle.clearChecked();
        switch (details.visibility) {
            case PUBLIC:
                visibilityToggle.check(R.id.visible_public_button);
                break;
            case PRIVATE:
                visibilityToggle.check(R.id.visible_private_button);
                break;
        }
    }

    private void resetPhotoButtonError() {
        photoButtonError.setText(null);

        // Hide the photo button error element
        ViewGroup.LayoutParams layoutParams = photoButtonError.getLayoutParams();
        layoutParams.height = 0;
        photoButtonError.setLayoutParams(layoutParams);
    }

    private @Nullable MoodEventDetails validateFields() {
        MoodEventDetails details = new MoodEventDetails();

        emotionPickerFragment.setError(null);
        reasonEditText.setError(null);
        resetPhotoButtonError();

        details.emotion = emotionPickerFragment.getSelected();
        if (details.emotion == null) {
            emotionPickerFragment.setError("An emotion is required");
            return null;
        }

        details.socialSituation = situationAutoComplete.getText().toString();

        details.reason = reasonEditText.getText().toString();
        if (details.reason.length() > 200) {
            reasonEditText.setError("Reason must be less than 200 characters");
            return null;
        }

        details.photo = photo;
        if (photo != null && PhotoUtils.compressPhoto(photo).size() > 65536) {
            Log.i("MoodEventForm", "Image too large");
            photoButtonError.setText("Image too large");

            ViewGroup.LayoutParams layoutParams = photoButtonError.getLayoutParams();
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            photoButtonError.setLayoutParams(layoutParams);
            return null;
        }

        int checkedButton = visibilityToggle.getCheckedButtonId();
        if (checkedButton == R.id.visible_public_button) {
            details.visibility = MoodEventVisibility.PUBLIC;
        } else if (checkedButton == R.id.visible_private_button) {
            details.visibility = MoodEventVisibility.PRIVATE;
        }

        details.lat = currentLatitude;
        details.lon = currentLongitude;

        details.timestamp = selectedDate;

        return details;
    }

    private void handleSubmit(View _buttonView) {
        MoodEventDetails details = validateFields();
        if (details == null) {
            return;
        }
        Log.d("MoodEventForm", "Submitting form with location: lat=" + details.lat + ", lon=" + details.lon);

        callback.handleSubmit(details);
    }

    public MoodEventForm() {
        super(R.layout.fragment_mood_event_form);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        FragmentContainerView locationFragmentContainer = view.findViewById(R.id.location_fragment);



        if (locationFragmentContainer != null) {
            locationFragment = locationFragmentContainer.getFragment();
            Log.e("MoodEventForm", "LocationFragment attached.");
            if (locationFragment != null) {
                locationFragment.setUpdateListener(this);
            } else {
                Log.e("MoodEventForm", "LocationFragment not attached. Ensure it's specified in the layout.");
            }
        } else {
            Log.e("MoodEventForm", "Location fragment container not found in layout.");
        }
        
        super.onViewCreated(view, savedInstanceState);


        Button submitButton = view.findViewById(R.id.submit_button);
        timestampInput = view.findViewById(R.id.timestamp_input);
        timestampInput.setText(formatDate(selectedDate));
        timestampInput.setOnClickListener(v -> openDateTimePicker());
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

        situationAutoComplete = view.findViewById(R.id.emotion_situation);
        reasonEditText = view.findViewById(R.id.emotion_reason);
        photoButton = view.findViewById(R.id.photo_button);
        photoButton.setOnClickListener(_v -> {
            UploadPhotoFragment uploadPhotoFragment = new UploadPhotoFragment();
            uploadPhotoFragment.setUploadPhotoListener(this::setImageFromBitmap);
            uploadPhotoFragment.show(requireActivity().getSupportFragmentManager(), uploadPhotoFragment.getTag());
        });
        photoResetButton = view.findViewById(R.id.photo_reset_button);
        photoResetButton.setOnClickListener(_v -> resetPhoto());
        photoButtonError = view.findViewById(R.id.photo_button_error);
        visibilityToggle = view.findViewById(R.id.visibility_button);
        updateResetPhotoVisibility();
        


    }
    @Override
    public void onLocationUpdated(Double latitude, Double longitude) {
        Log.d("MoodEventForm", "Location updated: lat=" + latitude + ", lon=" + longitude);
        this.currentLatitude = latitude;
        this.currentLongitude = longitude;
    }

    private void openDateTimePicker() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(selectedDate);

        new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            new TimePickerDialog(getContext(), (view1, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                selectedDate = calendar.getTime();
                timestampInput.setText(formatDate(selectedDate));
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private String formatDate(Date date) {
        return new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault()).format(date);
    }

    /**
     * TEST ONLY. Set the selected date
     * @param selectedDate Set the selected date
     */
    public void setSelectedDate(Date selectedDate) {
        this.selectedDate = selectedDate;
    }
}