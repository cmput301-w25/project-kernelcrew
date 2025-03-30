package com.kernelcrew.moodapp.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kernelcrew.moodapp.R;
import com.kernelcrew.moodapp.data.MoodEvent;
import com.kernelcrew.moodapp.data.MoodEventProvider;

public class EditMoodEvent extends Fragment {
    private String moodEventId;

    private BottomNavBarController navBarController;

    private FirebaseUser currentUser;
    private MoodEventProvider provider;

    public EditMoodEvent() {}

    private void handleSubmit(MoodEventForm.MoodEventDetails details) {
        MoodEvent moodEvent = details.toMoodEvent(currentUser.getUid());
        moodEvent.setId(moodEventId);
        provider.updateMoodEvent(moodEventId, moodEvent)
                .addOnSuccessListener(_result -> {
                    Bundle args = new Bundle();
                    args.putString("sourceScreen", "editMoodScreen");
                    Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).popBackStack();
                })
                .addOnFailureListener(error -> {
                    Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("EditMoodEvent", error.toString());
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_mood_event, container, false);

        NavigationBarView navigationBarView = view.findViewById(R.id.bottom_navigation);
        navigationBarView.setSelected(false);
        navBarController = new BottomNavBarController(navigationBarView);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navBarController.bind(view);

        // Get fragment using child fragment manager instead of container's getFragment()
        MoodEventForm form = (MoodEventForm) getChildFragmentManager().findFragmentById(R.id.mood_event_form);
        assert form != null;

        moodEventId = getArguments().getString("moodEventId");

        provider = MoodEventProvider.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;

        provider.getMoodEvent(moodEventId)
                .addOnSuccessListener(moodEvent ->
                        form.bind(new MoodEventForm.MoodEventDetails(moodEvent)));

        MaterialButton submitButton = view.findViewById(R.id.editMood_submitButton);
        submitButton.setOnClickListener(v -> {
            MoodEventForm.MoodEventDetails details = form.validateFields();
            if (details != null) {
                handleSubmit(details);
            }
        });
    }
}