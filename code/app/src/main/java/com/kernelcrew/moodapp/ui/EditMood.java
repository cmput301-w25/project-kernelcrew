package com.kernelcrew.moodapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kernelcrew.moodapp.R;

public class EditMood extends Fragment {

    private String moodEventId;
    private FirebaseFirestore db;
    private TextInputEditText editTextMoodState, editTextTrigger, editTextSocialSituation, editTextReason;
    private Button btnSave;

    public EditMood() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            moodEventId = getArguments().getString("moodEventId");
        }
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_mood, container, false);

        editTextMoodState = view.findViewById(R.id.editTextMoodState);
        editTextTrigger = view.findViewById(R.id.editTextTrigger);
        editTextSocialSituation = view.findViewById(R.id.editTextSocialSituation);
        editTextReason = view.findViewById(R.id.editTextReason);
        btnSave = view.findViewById(R.id.btnSave);

        // TODO: Load current mood details from Firestore and populate the fields

        btnSave.setOnClickListener(v -> {
            // TODO: Implement the update logic for Firestore
            Toast.makeText(getContext(), "Mood updated (implement update logic)", Toast.LENGTH_SHORT).show();
        });

        return view;
    }
}
