package com.kernelcrew.moodapp.ui.components;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.kernelcrew.moodapp.R;
import com.kernelcrew.moodapp.data.Emotion;

import java.util.ArrayList;

public class EmotionPickerFragment extends Fragment {
    private final ArrayList<EmotionToggle> toggles;
    private TextView errorView;

    private Emotion selected;
    private OnSelectEmotionListener onSelectEmotionListener;
    private String error;

    private static class EmotionToggle {
        MaterialButton button;
        Emotion emotion;

        public EmotionToggle(MaterialButton button, Emotion emotion) {
            this.button = button;
            this.emotion = emotion;
        }
    }

    public EmotionPickerFragment() {
        super(R.layout.emotion_picker);

        this.toggles = new ArrayList<>();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        class EmotionToggleConfig {
            int id;
            Emotion emotion;

            public EmotionToggleConfig(int id, Emotion emotion) {
                this.id = id;
                this.emotion = emotion;
            }
        }

        EmotionToggleConfig[] toggleConfigs = {
                new EmotionToggleConfig(R.id.toggle_anger, Emotion.ANGER),
                new EmotionToggleConfig(R.id.toggle_confusion, Emotion.CONFUSION),
                new EmotionToggleConfig(R.id.toggle_disgust, Emotion.DISGUST),
                new EmotionToggleConfig(R.id.toggle_fear, Emotion.FEAR),
                new EmotionToggleConfig(R.id.toggle_happy, Emotion.HAPPINESS),
                new EmotionToggleConfig(R.id.toggle_sadness, Emotion.SADNESS),
                new EmotionToggleConfig(R.id.toggle_shame, Emotion.SHAME),
                new EmotionToggleConfig(R.id.toggle_surprise, Emotion.SURPRISE),
        };

        for (var toggleConfig : toggleConfigs) {
            View toggleView = view.findViewById(toggleConfig.id);
            MaterialButton button = toggleView.findViewById(R.id.emotion_picker_button);
            TextView text = toggleView.findViewById(R.id.emotion_picker_text);

            button.setIcon(AppCompatResources.getDrawable(requireContext(), toggleConfig.emotion.getIconRes()));
            text.setText(toggleConfig.emotion.toString());

            EmotionToggle toggle = new EmotionToggle(button, toggleConfig.emotion);
            this.toggles.add(toggle);

            button.setOnClickListener(createToggleClickListener(toggle));
        }

        errorView = view.findViewById(R.id.emotion_picker_error);

        updateToggleTints();
    }

    private void updateToggleTints() {
        for (var toggle : toggles) {
            if (toggle.emotion == selected) {
                toggle.button.setIconTint(AppCompatResources.getColorStateList(requireContext(), toggle.emotion.getColorRes()));
            } else {
                toggle.button.setIconTint(AppCompatResources.getColorStateList(requireContext(), R.color.emotion_neutral));
            }
        }
    }

    private void updateError() {
        errorView.setText(error);
    }

    private View.OnClickListener createToggleClickListener(EmotionToggle toggle) {
        return _view -> {
            setSelected(toggle.emotion);
        };
    }

    public Emotion getSelected() {
        return selected;
    }

    public void setSelected(Emotion selected) {
        if (this.selected != selected && onSelectEmotionListener != null) {
            onSelectEmotionListener.onSelectEmotion(selected);
        }

        this.selected = selected;
        updateToggleTints();
    }

    public void setOnSelectEmotionListener(OnSelectEmotionListener listener) {
        this.onSelectEmotionListener = listener;
    }

    public interface OnSelectEmotionListener {
        void onSelectEmotion(Emotion emotion);
    }

    public String getError() {
        return error;
    }

    /**
     * Start displaying an error. Set to null to hide the error.
     * @param error Error to display or null
     */
    public void setError(@Nullable String error) {
        this.error = error;

        updateError();
    }
}
