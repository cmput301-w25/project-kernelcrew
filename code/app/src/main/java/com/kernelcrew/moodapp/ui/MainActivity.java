package com.kernelcrew.moodapp.ui;

import android.os.Build;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.Manifest;

import com.kernelcrew.moodapp.R;

//Code from Claude AI, Anthropic, "Implement DeleteDialogListener in MainActivity", accessed 03-05-2025
public class MainActivity extends AppCompatActivity implements DeleteDialogFragment.DeleteDialogListener {
    private MapsFragment mapsFragment;

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
        mapsFragment = new MapsFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.map_layout, mapsFragment)
                .commit();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.location_layout, new LocationFragment())
                .commit();
    }

    //The following function is from ChatGPT, OpenAI, "Help me setup the the MainActivity for the map fragment", 2025-03-02
    @Override
    protected void onResume() {
        super.onResume();

        // Ensure MapsFragment is fully attached
        getSupportFragmentManager().executePendingTransactions();
    }

    //The following function is from ChatGPT, OpenAI, "Help me setup the the MainActivity for the location fragment", 2025-03-02
    private void requestPermissions() {

        ActivityResultLauncher<String[]> locationPermissionRequest =
                registerForActivityResult(new ActivityResultContracts
                                .RequestMultiplePermissions(), result -> {

                            Boolean fineLocationGranted = null;
                            Boolean coarseLocationGranted = null;

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                fineLocationGranted = result.getOrDefault(
                                        Manifest.permission.ACCESS_FINE_LOCATION, false);
                                coarseLocationGranted = result.getOrDefault(
                                        Manifest.permission.ACCESS_COARSE_LOCATION,false);
                            }

                            if (fineLocationGranted != null && fineLocationGranted) {
                                // Precise location access granted.
                            } else if (coarseLocationGranted != null && coarseLocationGranted) {
                                // Only approximate location access granted.
                            } else {
                                // No location access granted.
                            }
                        }
                );

        // ...

        // Before you perform the actual permission request, check whether your app
        // already has the permissions, and whether your app needs to show a permission
        // rationale dialog. For more details, see Request permissions.
        locationPermissionRequest.launch(new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    //Code from Claude AI, Anthropic, "Implement delete confirmation callback", accessed 03-05-2025
    @Override
    public void onDeleteConfirmed() {
        // Handle delete confirmation
        // This will be called when user confirms deletion
    }
}
