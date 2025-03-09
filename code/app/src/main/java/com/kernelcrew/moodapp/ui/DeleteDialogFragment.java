package com.kernelcrew.moodapp.ui;

//Code from Claude AI, Anthropic, "Create custom delete confirmation dialog with Material Design in Android", accessed 03-13-2025
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.kernelcrew.moodapp.R;

/**
 * Dialog fragment for confirming mood event deletion
 */
public class DeleteDialogFragment extends DialogFragment {
    private DeleteDialogListener listener;

    /**
     * Interface for handling delete confirmation
     */
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
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        
        // Inflate custom layout
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_delete_dialog, null);

        // Set up button click listeners
        Button keepButton = view.findViewById(R.id.btn_keep);
        Button deleteButton = view.findViewById(R.id.btn_delete);

        keepButton.setOnClickListener(v -> dismiss());
        deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteConfirmed();
            }
            dismiss();
        });

        builder.setView(view);
        return builder.create();
    }
}
