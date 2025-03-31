package com.kernelcrew.moodapp.ui;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kernelcrew.moodapp.R;
import com.kernelcrew.moodapp.data.FollowRequestProvider;


public class MainActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FollowRequestProvider followRequestProvider;
    private boolean listenersAttached = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Enable Firestore offline persistence
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        FirebaseFirestore.getInstance().setFirestoreSettings(settings);


        auth = FirebaseAuth.getInstance();
        followRequestProvider = new FollowRequestProvider(this);

        auth.addAuthStateListener(firebaseAuth -> {
            FirebaseUser newUser = firebaseAuth.getCurrentUser();

            if (newUser != null && (currentUser == null || !newUser.getUid().equals(currentUser.getUid()))) {
                currentUser = newUser;
                followRequestProvider.listenForFollowRequests(currentUser.getUid());
                followRequestProvider.listenForFollowAcceptedNotifications(currentUser.getUid());

                listenersAttached = true;
            }
        });
    }
}
