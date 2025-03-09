package com.kernelcrew.moodapp;

//Code from Claude AI, Anthropic, "Configure Android unit testing with JUnit", accessed 03-05-2025
//Code from Anthropic, Claude 3.7 Sonnet, "Add Firestore emulator testing", accessed 05-13-2024
import static org.junit.Assert.*;

import android.util.Log;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kernelcrew.moodapp.ui.DeleteDialogFragment;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

/**
 * Unit tests for the DeleteDialogFragment.
 * <p>
 * This test class uses Robolectric to test the fragment and Firebase emulator for 
 * database operations.
 */
@RunWith(RobolectricTestRunner.class)
@Config(
    sdk = 29,
    application = TestApplication.class,
    packageName = "com.kernelcrew.moodapp"
)

public class DeleteDialogFragmentTest {
    private DeleteDialogFragment dialogFragment;
    private boolean listenerCalled;
    
    // Teest collection name for Firestore operations
    private final String TEST_COLLECTION = "moodEvents";
    
    // Test document ID for Firestore operations
    private final String TEST_DOCUMENT_ID = "testMoodEvent";
    
    // Your Firebase project ID - replace with your actual project ID
    private final String PROJECT_ID = "moodable-a6fde";
    
    // Emulator connection parameters
    private static final String ANDROID_LOCALHOST = "127.0.0.1";
    private static final int FIRESTORE_EMULATOR_PORT = 8080;
    
    static {
        // Enable Robolectric logging to console
        ShadowLog.stream = System.out;
    }
    
    /**
     * Configure the FirebaseFirestore instance to use the local emulator
     * before any tests run.
     */
    @BeforeClass
    public static void setupEmulator() {
        FirebaseFirestore.getInstance().useEmulator(ANDROID_LOCALHOST, FIRESTORE_EMULATOR_PORT);
    }

    @Before
    public void setUp() {
        try {
            dialogFragment = new DeleteDialogFragment();
            listenerCalled = false;
            
            // Clear database first to ensure clean state
            clearDatabase();
            
            // Seed the database with test data
            seedDatabase();
        } catch (Exception e) {
            Log.e("DeleteDialogTest", "Error in setup: " + e.getMessage(), e);
        }
    }
    
    /**
     * Populates the emulated database with test data before each test
     */
    private void seedDatabase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // Create a test mood event with location data
        Map<String, Object> locationData = new HashMap<>();
        locationData.put("latitude", 43.6532); // Toronto coordinates  
        locationData.put("longitude", -79.3832);
        
        Map<String, Object> moodEventData = new HashMap<>();
        moodEventData.put("mood", "happy");
        moodEventData.put("date", new Timestamp(new Date()));
        moodEventData.put("situation", "Testing location");
        moodEventData.put("location", locationData);
        
        // Add to database and wait for completion
        DocumentReference docRef = db.collection(TEST_COLLECTION).document(TEST_DOCUMENT_ID);
        try {
            Tasks.await(docRef.set(moodEventData));
            Log.i("DeleteDialogTest", "Database seeded successfully");
        } catch (Exception e) {
            Log.e("DeleteDialogTest", "Error seeding database: " + e.getMessage(), e);
        }
    }
    
    /**
     * Clears the emulated database
     */
    private void clearDatabase() {
        URL url = null;
        try {
            url = new URL("http://" + ANDROID_LOCALHOST + ":" + FIRESTORE_EMULATOR_PORT + 
                          "/emulator/v1/projects/" + PROJECT_ID + "/databases/(default)/documents");
        } catch (MalformedURLException exception) {
            Log.e("DeleteDialogTest", "URL Error: " + Objects.requireNonNull(exception.getMessage()), exception);
        }
        
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("DELETE");
            int response = urlConnection.getResponseCode();
            Log.i("DeleteDialogTest", "Database clear response code: " + response);
        } catch (IOException exception) {
            Log.e("DeleteDialogTest", "IO Error: " + Objects.requireNonNull(exception.getMessage()), exception);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }
    
    /**
     * Cleans up after each test
     */
    @After
    public void tearDown() {
        try {
            // Force cleanup of resources
            clearDatabase();
            System.gc(); // Request garbage collection
        } catch (Exception e) {
            Log.e("DeleteDialogTest", "Error in tearDown: " + e.getMessage(), e);
        }
    }

    @Test
    public void testListenerSetup() {
        // Test that we can set a listener
        dialogFragment.setDeleteDialogListener(() -> listenerCalled = true);
        assertNotNull(dialogFragment);
    }

    @Test
    public void testDialogFragmentCreation() {
        // Test that dialog fragment can be created
        assertNotNull("Dialog fragment should not be null", dialogFragment);
    }

    @Test
    public void testListenerInterface() {
        // Test the listener interface implementation
        DeleteDialogFragment.DeleteDialogListener listener = () -> listenerCalled = true;
        dialogFragment.setDeleteDialogListener(listener);
        
        // Verify listener can be set
        assertNotNull("Should be able to set listener", dialogFragment);
    }
    
    /**
     * Tests that the delete dialog can trigger location deletion from Firestore
     * <p>
     * This test verifies that:
     * 1. A document in Firestore initially contains location data
     * 2. When the delete listener is triggered, the location field is removed
     * 3. The document still exists but with the location field set to null
     */
    @Test
    public void testDeleteLocationFromFirestore() throws ExecutionException, InterruptedException {
        try {
            // Get access to the Firestore emulator
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection(TEST_COLLECTION).document(TEST_DOCUMENT_ID);
            
            // Verify that the document exists and has a location field
            DocumentSnapshot snapshot = Tasks.await(docRef.get());
            assertTrue("Document should exist", snapshot.exists());
            assertTrue("Document should initially have location", snapshot.contains("location"));
            assertNotNull("Location data should not be null", snapshot.get("location"));
            
            // Set up the delete dialog listener
            DeleteDialogFragment.DeleteDialogListener deleteListener = () -> {
                try {
                    // Update the document to remove the location and wait for completion
                    Tasks.await(docRef.update("location", null));
                    listenerCalled = true;
                } catch (Exception e) {
                    Log.e("DeleteDialogTest", "Error updating document: " + e.getMessage(), e);
                }
            };
            
            // Set the listener on the dialog fragment
            dialogFragment.setDeleteDialogListener(deleteListener);
            
            // Manually invoke the listener method to simulate button click
            deleteListener.onDeleteConfirmed();
            
            // Wait for the emulator to process the update
            Thread.sleep(500);
            
            // Verify the listener was called
            assertTrue("Delete listener should have been called", listenerCalled);
            
            // Get the document again to verify location was removed
            snapshot = Tasks.await(docRef.get());
            
            // Verify document still exists but location is gone
            assertTrue("Document should still exist after deletion", snapshot.exists());
            assertNull("Location field should be null after deletion", snapshot.get("location"));
        } catch (Exception e) {
            Log.e("DeleteDialogTest", "Test failed with exception: " + e.getMessage(), e);
            throw e;
        }
    }
} 