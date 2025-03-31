package com.kernelcrew.moodapp.ui;

//Code from Claude AI, Anthropic, "Create custom delete confirmation dialog with Material Design in Android", accessed 03-09-2025
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.kernelcrew.moodapp.R;
import com.kernelcrew.moodapp.data.MoodEventProvider;

/**
 * Dialog fragment for confirming mood event deletion
 */
public class DeleteDialogFragment extends DialogFragment {
    private FirebaseFirestore db;
    private DeleteDialogListener listener;

    public interface DeleteDialogListener {
        void onDeleteConfirmed();
    }

    /**
     * Set the delete dialog listener
     * @param listener The listener to be called when delete is confirmed
     */
    public void setDeleteDialogListener(DeleteDialogListener listener) {
        this.listener = listener;
    }
    /**
     * Get the current delete dialog listener
     * @return The current listener or null if none is set
     */
    public DeleteDialogListener getDeleteDialogListener() {
        return this.listener;
    }
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            if (listener == null) {
                listener = (DeleteDialogListener) context;
            }
        } catch (ClassCastException e) {
            throw new ClassCastException(context + " must implement DeleteDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        // Inflate custom layout
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_delete_dialog, null);

        // Get moodEventId from arguments
        String moodEventId = null;
        if (getArguments() != null) {
            moodEventId = getArguments().getString("moodEventId");
        }

        // Use final variable for lambda
        final String finalMoodEventId = moodEventId;

        // Set up button click listeners
        Button keepButton = view.findViewById(R.id.btn_keep);
        Button deleteButton = view.findViewById(R.id.btn_delete);

        keepButton.setOnClickListener(v -> dismiss());

        deleteButton.setOnClickListener(v -> {
            // Check if ID exists before deleting
            if (finalMoodEventId != null && listener != null) {
                MoodEventProvider.getInstance().deleteMoodEvent(finalMoodEventId);
                listener.onDeleteConfirmed();
            }
            dismiss();
        });

        builder.setView(view);
        return builder.create();
    }
}