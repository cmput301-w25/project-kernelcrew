package com.kernelcrew.moodapp;

import static org.junit.Assert.*;

import android.util.Log;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
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
 * A simple test to verify Firebase emulator connectivity.
 * This is a minimal test with no dependencies on complex classes.
 */
@RunWith(RobolectricTestRunner.class)
@Config(
    sdk = 29,
    application = TestApplication.class,
    packageName = "com.kernelcrew.moodapp"
)
@SuppressWarnings("unchecked")
public class SimpleFirebaseTest {
    
    // Test collection name for Firestore operations
    private final String TEST_COLLECTION = "simpleTest";
    
    // Test document ID for Firestore operations
    private final String TEST_DOCUMENT_ID = "testDoc1";
    
    // Your Firebase project ID
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
            // Clear database first to ensure clean state
            clearDatabase();
        } catch (Exception e) {
            Log.e("SimpleFirebaseTest", "Error in setup: " + e.getMessage(), e);
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
            Log.e("SimpleFirebaseTest", "URL Error: " + Objects.requireNonNull(exception.getMessage()), exception);
        }
        
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("DELETE");
            int response = urlConnection.getResponseCode();
            Log.i("SimpleFirebaseTest", "Database clear response code: " + response);
        } catch (IOException exception) {
            Log.e("SimpleFirebaseTest", "IO Error: " + Objects.requireNonNull(exception.getMessage()), exception);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }
    
    @After
    public void tearDown() {
        try {
            clearDatabase();
        } catch (Exception e) {
            Log.e("SimpleFirebaseTest", "Error in tearDown: " + e.getMessage(), e);
        }
    }
    
    /**
     * A simple test that writes to and reads from Firestore emulator.
     * This isolates Firebase testing from any other dependencies.
     */
    @Test
    public void testFirebaseEmulatorConnection() throws ExecutionException, InterruptedException {
        try {
            // Get Firestore instance
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection(TEST_COLLECTION).document(TEST_DOCUMENT_ID);
            
            // Create test data
            Map<String, Object> testData = new HashMap<>();
            testData.put("testField", "testValue");
            testData.put("timestamp", new Timestamp(new Date()));
            
            // Write to Firestore
            Tasks.await(docRef.set(testData));
            Log.i("SimpleFirebaseTest", "Successfully wrote data to Firestore emulator");
            
            // Read from Firestore
            DocumentSnapshot snapshot = Tasks.await(docRef.get());
            
            // Verify data
            assertTrue("Document should exist", snapshot.exists());
            assertEquals("testValue", snapshot.getString("testField"));
            assertNotNull("Timestamp should exist", snapshot.getTimestamp("timestamp"));
            
            Log.i("SimpleFirebaseTest", "Successfully read data from Firestore emulator");
        } catch (Exception e) {
            Log.e("SimpleFirebaseTest", "Test failed with exception: " + e.getMessage(), e);
            throw e;
        }
    }
} 