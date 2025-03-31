package com.kernelcrew.moodapp.data;

import com.google.firebase.firestore.ListenerRegistration;
import java.util.List;

public class CombinedListenerComposer implements ListenerRegistration {
    private final List<ListenerRegistration> registrations;

    public CombinedListenerComposer(List<ListenerRegistration> registrations) {
        this.registrations = registrations;
    }

    @Override
    public void remove() {
        for (ListenerRegistration reg : registrations) {
            reg.remove();
        }
    }
}