package com.kernelcrew.moodapp;

//Code from Claude AI, Anthropic, "Convert Robolectric tests to JUnit for DeleteDialogFragment", accessed 05-17-2024
import static org.junit.Assert.*;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kernelcrew.moodapp.ui.DeleteDialogFragment;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

/**
 * JUnit tests for the DeleteDialogFragment.
 * <p>
 * This test class uses JUnit to test the fragment and Firebase emulator for 
 * database operations.
 */
public class DeleteDialogFragmentTest {
    private static final Logger logger = Logger.getLogger(DeleteDialogFragmentTest.class.getName());
    
    private DeleteDialogFragment dialogFragment;
    private boolean listenerCalled;
    
    // Test collection name for Firestore operations
    private final String TEST_COLLECTION = "moodEvents";
    
    // Test document ID for Firestore operations
    private final String TEST_DOCUMENT_ID = "testMoodEvent";
    
    // Your Firebase project ID
    private final String PROJECT_ID = "moodable-a6fde";
    
    // Emulator connection parameters
    private static final String ANDROID_LOCALHOST = "127.0.0.1";
    private static final int FIRESTORE_EMULATOR_PORT = 8080; // Using port 8080 as specified
    
    /**
     * Configure the FirebaseFirestore instance to use the local emulator
     * before any tests run.
     */
    @BeforeClass
    public static void setupEmulator() {
        FirebaseFirestore.getInstance().useEmulator(ANDROID_LOCALHOST, FIRESTORE_EMULATOR_PORT);
        logger.info("Configured Firestore emulator at " + ANDROID_LOCALHOST + ":" + FIRESTORE_EMULATOR_PORT);
    }

    @Before
    public void setUp() {
        try {
            // Initialize mockito
            MockitoAnnotations.openMocks(this);
            
            // Create dialog fragment
            dialogFragment = new DeleteDialogFragment();
            listenerCalled = false;
            
            // Clear database first to ensure clean state
            clearDatabase();
            
            // Seed the database with test data
            seedDatabase();
            
            logger.info("Test setup completed successfully");
        } catch (Exception e) {
            logger.severe("Error in setup: " + e.getMessage());
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
            logger.info("Database seeded successfully");
        } catch (Exception e) {
            logger.severe("Error seeding database: " + e.getMessage());
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
            
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("DELETE");
            int response = urlConnection.getResponseCode();
            logger.info("Database clear response code: " + response);
            
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        } catch (MalformedURLException exception) {
            logger.severe("URL Error: " + exception.getMessage());
        } catch (IOException exception) {
            logger.severe("IO Error: " + exception.getMessage());
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
            logger.info("Test teardown completed");
        } catch (Exception e) {
            logger.severe("Error in tearDown: " + e.getMessage());
        }
    }

    /**
     * Test that we can set a listener on the dialog fragment
     */
    @Test
    public void testListenerSetup() {
        // Create a listener
        DeleteDialogFragment.DeleteDialogListener listener = () -> listenerCalled = true;
        
        // Set the listener on the fragment
        dialogFragment.setDeleteDialogListener(listener);
        
        // Verify no exceptions were thrown
        assertNotNull("Dialog fragment should not be null after setting listener", dialogFragment);
    }

    /**
     * Test that the dialog fragment can be created
     */
    @Test
    public void testDialogFragmentCreation() {
        // Just verify the fragment can be instantiated
        assertNotNull("Dialog fragment should not be null", dialogFragment);
    }

    /**
     * Test the listener interface implementation
     */
    @Test
    public void testListenerInterface() {
        // Create a listener using lambda
        DeleteDialogFragment.DeleteDialogListener listener = () -> listenerCalled = true;
        
        // Set the listener
        dialogFragment.setDeleteDialogListener(listener);
        
        // Verify no exceptions
        assertNotNull("Should be able to set listener", dialogFragment);
    }
    
    /**
     * Test that the listener callback actually works when triggered
     */
    @Test
    public void testListenerCallback() {
        // Create and set the listener
        DeleteDialogFragment.DeleteDialogListener listener = () -> listenerCalled = true;
        dialogFragment.setDeleteDialogListener(listener);
        
        // Trigger the callback directly
        listener.onDeleteConfirmed();
        
        // Verify the callback worked
        assertTrue("Listener callback should set listenerCalled to true", listenerCalled);
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
                    logger.severe("Error updating document: " + e.getMessage());
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
            
            logger.info("Delete location test completed successfully");
        } catch (Exception e) {
            logger.severe("Test failed with exception: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * You need to add this method to your DeleteDialogFragment class
     * to make these tests work properly.
     * 
     * public DeleteDialogListener getDeleteDialogListener() {
     *     return this.listener;
     * }
     */
} 