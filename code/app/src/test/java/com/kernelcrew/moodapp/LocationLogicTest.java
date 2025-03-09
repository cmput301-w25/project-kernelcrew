package com.kernelcrew.moodapp;

// Code from Claude AI, Anthropic, "Create unit tests for location handling logic", accessed 03-05-2025
// Code from Claude AI, Anthropic, "Fix ClassNotFoundException in Android unit tests with Robolectric", accessed 03-06-2024
// Code from Anthropic, Claude 3.7 Sonnet, "Update tests for modern Activity Result API", accessed 05-13-2024
// Code from Anthropic, Claude 3.7 Sonnet, "Add Firestore emulator testing", accessed 05-13-2024

import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

import android.os.Looper;
import android.util.Log;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.core.app.ApplicationProvider;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kernelcrew.moodapp.ui.TestLocationFragment;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;
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
 * Unit tests for the location functionality logic.
 * <p>
 * This test class uses Robolectric to test the TestLocationFragment in isolation,
 * without requiring a device or emulator. It focuses on testing the non-UI logic
 * of the fragment, including the initial state, value handling, and Firestore interactions.
 * <p>
 * To avoid Google Play Services issues in Robolectric tests, this uses TestLocationFragment
 * instead of the real LocationFragment.
 *
 * @author Anthropic, Claude 3.7 Sonnet
 * @version 1.0
 * @see com.kernelcrew.moodapp.ui.TestLocationFragment
 */
@RunWith(RobolectricTestRunner.class)
@Config(
    sdk = 29,
    application = TestApplication.class,
    packageName = "com.kernelcrew.moodapp"
)
@LooperMode(LooperMode.Mode.PAUSED)
@SuppressWarnings("unchecked")
public class LocationLogicTest {

    /**
     * Fragment scenario used to create and manage the LocationFragment instance.
     * <p>
     * FragmentScenario provides a way to test fragments in isolation and control
     * their lifecycle programmatically.
     */
    private FragmentScenario<TestLocationFragment> fragmentScenario;
    
    /**
     * Test collection name for Firestore operations
     */
    private final String TEST_COLLECTION = "moodEvents";
    
    /**
     * Test document ID for Firestore operations
     */
    private final String TEST_DOCUMENT_ID = "testMoodEvent";
    
    /**
     * Your Firebase project ID
     */
    private final String PROJECT_ID = "moodable-a6fde";
    
    /**
     * Emulator connection parameters
     */
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

    /**
     * Sets up the test environment before each test method.
     * <p>
     * This method initializes a FragmentScenario for the TestLocationFragment, which
     * creates a fragment instance in a testing environment.
     */
    @Before
    public void setUp() {
        try {
            // Clear and seed the database before creating the fragment
            clearDatabase();
            seedDatabase();
            
            // Create a FragmentScenario for TestLocationFragment instead of LocationFragment
            fragmentScenario = FragmentScenario.launchInContainer(TestLocationFragment.class);
            
            // Process any pending messages
            shadowOf(Looper.getMainLooper()).idle();
        } catch (Exception e) {
            Log.e("LocationLogicTest", "Error in setup: " + e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Seeds the database with test data
     */
    private void seedDatabase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // Create location data for Edmonton
        Map<String, Object> locationData = new HashMap<>();
        locationData.put("latitude", 53.5461);
        locationData.put("longitude", -113.4938);
        
        // Create a mood event with this location
        Map<String, Object> moodEventData = new HashMap<>();
        moodEventData.put("mood", "happy");
        moodEventData.put("date", new Timestamp(new Date()));
        moodEventData.put("situation", "Testing location");
        moodEventData.put("location", locationData);
        
        // Add to database and wait for completion
        try {
            Tasks.await(db.collection(TEST_COLLECTION).document(TEST_DOCUMENT_ID).set(moodEventData));
            Log.i("LocationLogicTest", "Database seeded successfully");
        } catch (Exception e) {
            Log.e("LocationLogicTest", "Error seeding database: " + e.getMessage(), e);
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
            Log.e("LocationLogicTest", "URL Error: " + Objects.requireNonNull(exception.getMessage()), exception);
        }
        
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("DELETE");
            int response = urlConnection.getResponseCode();
            Log.i("LocationLogicTest", "Database clear response code: " + response);
        } catch (IOException exception) {
            Log.e("LocationLogicTest", "IO Error: " + Objects.requireNonNull(exception.getMessage()), exception);
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
            // Force cleanup of any resources
            clearDatabase();
            if (fragmentScenario != null) {
                fragmentScenario.close();
            }
            System.gc(); // Request garbage collection
        } catch (Exception e) {
            Log.e("LocationLogicTest", "Error in tearDown: " + e.getMessage(), e);
        }
    }

    /**
     * Tests that the initial state of the TestLocationFragment has null location values.
     * <p>
     * This test verifies that the latitude and longitude properties are initially null,
     * which is the expected state before any location is retrieved.
     */
    @Test
    public void testInitialState() {
        fragmentScenario.onFragment(fragment -> {
            // Verify that latitude and longitude are initially null
            assertNull("Initial latitude should be null", fragment.getLatitude());
            assertNull("Initial longitude should be null", fragment.getLongitude());
        });
    }

    /**
     * Tests location handling with sample Vancouver coordinates.
     * <p>
     * Verifies that the TestLocationFragment correctly processes Vancouver's coordinates
     * and makes them available through the getter methods.
     */
    @Test
    public void testVancouverLocation() {
        fragmentScenario.onFragment(fragment -> {
            // Vancouver coordinates
            double vanLatitude = 49.2827;
            double vanLongitude = -123.1207;
            
            // Directly set the location for testing
            fragment.saveMoodEventWithLocation(vanLatitude, vanLongitude);
            
            // Process any pending messages
            shadowOf(Looper.getMainLooper()).idle();
            
            // Verify the stored location values
            assertNotNull("Latitude should not be null", fragment.getLatitude());
            assertNotNull("Longitude should not be null", fragment.getLongitude());
            assertEquals("Latitude should match Vancouver", vanLatitude, fragment.getLatitude(), 0.0001);
            assertEquals("Longitude should match Vancouver", vanLongitude, fragment.getLongitude(), 0.0001);
        });
    }

    /**
     * Tests that location data can be saved to and retrieved from Firestore.
     * <p>
     * This test verifies that:
     * 1. A mood event with location data can be saved to Firestore
     * 2. The data is saved to the correct Firestore collection and document
     * 3. The retrieved data matches what was saved
     */
    @Test
    public void testSaveAndRetrieveLocationToFirestore() throws ExecutionException, InterruptedException {
        // Get access to the Firestore emulator
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection(TEST_COLLECTION).document(TEST_DOCUMENT_ID);
        
        // Create expected location data for Edmonton
        Map<String, Object> locationData = new HashMap<>();
        locationData.put("latitude", 53.5461);
        locationData.put("longitude", -113.4938);
        
        // Create complete mood event data
        Map<String, Object> moodEventData = new HashMap<>();
        moodEventData.put("mood", "happy");
        moodEventData.put("date", new Timestamp(new Date()));
        moodEventData.put("situation", "Testing location");
        moodEventData.put("location", locationData);
        
        // Save data to Firestore emulator and wait for completion
        Tasks.await(docRef.set(moodEventData));
        
        // Retrieve from Firestore and wait for completion
        DocumentSnapshot snapshot = Tasks.await(docRef.get());
        
        // Verify document exists and contains location
        assertTrue("Document should exist", snapshot.exists());
        assertTrue("Document should contain location field", snapshot.contains("location"));
        
        // Get the location data and verify it matches what we saved
        Map<String, Object> retrievedLocation = (Map<String, Object>) snapshot.get("location");
        assertNotNull("Retrieved location should not be null", retrievedLocation);
        assertEquals("Latitude should match", 53.5461, retrievedLocation.get("latitude"));
        assertEquals("Longitude should match", -113.4938, retrievedLocation.get("longitude"));
    }
} 