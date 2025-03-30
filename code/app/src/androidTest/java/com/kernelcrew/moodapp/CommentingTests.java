package com.kernelcrew.moodapp;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;

import android.view.View;

import androidx.fragment.app.FragmentContainerView;
import androidx.navigation.NavController;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.kernelcrew.moodapp.data.Comment;
import com.kernelcrew.moodapp.data.CommentProvider;
import com.kernelcrew.moodapp.data.Emotion;
import com.kernelcrew.moodapp.data.MoodEvent;
import com.kernelcrew.moodapp.data.MoodEventProvider;
import com.kernelcrew.moodapp.data.MoodEventVisibility;
import com.kernelcrew.moodapp.data.UserProvider;
import com.kernelcrew.moodapp.ui.MainActivity;
import com.kernelcrew.moodapp.ui.components.EmotionPickerFragment;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class CommentingTests extends FirebaseEmulatorMixin {

    private static final String TEST_MOOD_EVENT_ID = "test_mood_event_id";
    private static final String TEST_USER_ID = "test_user_id";
    private static final String TEST_USERNAME = "TestUser";
    private static final String TEST_COMMENT_TEXT = "This is a test comment";

    @Mock
    private MoodEventProvider moodEventProvider;

    @Mock
    private CommentProvider commentProvider;

    @Mock
    private UserProvider userProvider;

    @Mock
    private FirebaseAuth firebaseAuth;

    @Mock
    private FirebaseUser firebaseUser;

    @Mock
    private NavController navController;

    private AutoCloseable mocks;
    private List<Comment> testCommentList = new ArrayList<>();


    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @BeforeClass
    public static void signupUser() throws ExecutionException, InterruptedException {
        staticCreateUser();
    }

    @Test
    public void createNewMood() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        onView(withId(R.id.moodRecyclerView))
                .perform(actionOnItemAtPosition(0,
                        clickOnChildViewWithId(R.id.commentLayout)));
    }

    public static ViewAction clickOnChildViewWithId(final int id) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return null;
            }

            @Override
            public String getDescription() {
                return "Click on a child view with specified ID.";
            }

            @Override
            public void perform(UiController uiController, View view) {
                View child = view.findViewById(id);
                if (child != null) {
                    child.performClick();
                }
            }
        };
    }

//    /**
//     * Test loading initial comments from the database
//     */
//    @Test
//    public void testLoadInitialComments() {
//        // Prepare test data: 3 comments in the database
//        List<Comment> initialComments = createTestComments(3);
//        testCommentList.addAll(initialComments);
//
//        // Launch fragment
//        launchFragmentWithMoodEventId();
//
//        // Verify the RecyclerView is displayed
//        onView(withId(R.id.commentRecyclerView))
//                .check(matches(isDisplayed()));
//
//        // Verify comment count is displayed correctly (should be "3")
//        onView(withId(R.id.commentCount))
//                .check(matches(withText("3")));
//
//        // Verify first comment is displayed
//        onView(withId(R.id.commentRecyclerView))
//                .check(matches(hasDescendant(withText(initialComments.get(0).getCommentText()))));
//    }
//
//    /**
//     * Test adding a new comment
//     */
//    @Test
//    public void testAddComment() throws InterruptedException {
//        // Launch fragment with empty comment list
//        launchFragmentWithMoodEventId();
//
//        // Type the comment text
//        onView(withId(R.id.searchInput))
//                .perform(typeText(TEST_COMMENT_TEXT), closeSoftKeyboard());
//
//        // Setup comment list to be updated when sendComment is called
//        final CountDownLatch latch = new CountDownLatch(1);
//        doAnswer(new Answer<Task<Void>>() {
//            @Override
//            public Task<Void> answer(InvocationOnMock invocation) {
//                Comment newComment = invocation.getArgument(0);
//
//                // Add comment to our test list
//                testCommentList.add(newComment);
//
//                // Create a task that will complete after we manually update the UI
//                Task<Void> task = Tasks.forResult(null);
//
//                // Signal the latch to continue test execution
//                latch.countDown();
//
//                return task;
//            }
//        }).when(commentProvider).insertComment(any(Comment.class));
//
//        // Click the send button (end icon of TextInputLayout)
//        onView(withId(R.id.searchInputLayout))
//                .perform(clickEndIcon());
//
//        // Wait for the mock's async operation to complete
//        latch.await(2, TimeUnit.SECONDS);
//
//        // Wait for UI to update
//        Thread.sleep(500);
//
//        // Now verify the updated comment count
//        onView(withId(R.id.commentCount))
//                .check(matches(withText("1")));
//
//        // Verify comment text is displayed in the RecyclerView
//        onView(withId(R.id.commentRecyclerView))
//                .check(matches(hasDescendant(withText(TEST_COMMENT_TEXT))));
//    }
//
//    /**
//     * Helper method to launch fragment with arguments
//     */
//    private FragmentScenario<MoodComments> launchFragmentWithMoodEventId() {
//        Bundle args = new Bundle();
//        args.putString("moodEventId", TEST_MOOD_EVENT_ID);
//        args.putString("sourceScreen", "home");
//
//        FragmentScenario<MoodComments> scenario = FragmentScenario.launchInContainer(
//                MoodComments.class,
//                args,
//                R.style.Theme_MoodApp,
//                (FragmentFactory) null
//        );
//
//        // Set up navigation controller
//        scenario.onFragment(fragment -> {
//            Navigation.setViewNavController(fragment.requireView(), navController);
//        });
//
//        return scenario;
//    }
//
//    /**
//     * Helper method to create test comments
//     */
//    private List<Comment> createTestComments(int count) {
//        List<Comment> comments = new ArrayList<>();
//        for (int i = 0; i < count; i++) {
//            Comment comment = new Comment();
//            comment.setUid(TEST_USER_ID);
//            comment.setUsername(TEST_USERNAME);
//            comment.setMoodEventId(TEST_MOOD_EVENT_ID);
//            comment.setCommentText("Test comment " + i);
//            comment.setCreated(new Date());
//            comments.add(comment);
//        }
//        return comments;
//    }
//
//    /**
//     * Helper method to mock singleton classes
//     */
//    private void mockSingletons() {
//        try {
//            // Mock MoodEventProvider singleton
//            java.lang.reflect.Field moodEventField = MoodEventProvider.class.getDeclaredField("instance");
//            moodEventField.setAccessible(true);
//            moodEventField.set(null, moodEventProvider);
//
//            // Mock CommentProvider singleton
//            java.lang.reflect.Field commentField = CommentProvider.class.getDeclaredField("instance");
//            commentField.setAccessible(true);
//            commentField.set(null, commentProvider);
//
//            // Mock UserProvider singleton
//            java.lang.reflect.Field userField = UserProvider.class.getDeclaredField("instance");
//            userField.setAccessible(true);
//            userField.set(null, userProvider);
//
//            // Mock FirebaseAuth
//            java.lang.reflect.Field authField = FirebaseAuth.class.getDeclaredField("INSTANCE");
//            authField.setAccessible(true);
//            authField.set(null, firebaseAuth);
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to mock singleton", e);
//        }
//    }
//
//    /**
//     * Helper method to set up mock Firebase Auth
//     */
//    private void mockFirebaseAuth() {
//        when(firebaseAuth.getCurrentUser()).thenReturn(firebaseUser);
//        when(firebaseUser.getUid()).thenReturn(TEST_USER_ID);
//    }
//
//    /**
//     * Helper method to set up mock MoodEvent
//     */
//    private void setupMockMoodEvent() {
//        MoodEvent testMoodEvent = new MoodEvent();
//        testMoodEvent.setId(TEST_MOOD_EVENT_ID);
//        testMoodEvent.setEmotion(Emotion.HAPPINESS.toString());
//        testMoodEvent.setCreated(new Date());
//        testMoodEvent.setId(TEST_USER_ID);
//        testMoodEvent.setUsername(TEST_USERNAME);
//
//        Task<MoodEvent> moodEventTask = Tasks.forResult(testMoodEvent);
//        when(moodEventProvider.getMoodEvent(TEST_MOOD_EVENT_ID)).thenReturn(moodEventTask);
//    }
//
//    /**
//     * Helper method to set up mock CommentProvider
//     */
//    private void setupMockCommentProvider() {
//        // Setup the getCommentsByMoodEventId to return our test list
//        when(commentProvider.getCommentsByMoodEventId(TEST_MOOD_EVENT_ID)).thenAnswer(
//                new Answer<Task<List<Comment>>>() {
//                    @Override
//                    public Task<List<Comment>> answer(InvocationOnMock invocation) {
//                        // Return a copy of the current test comment list
//                        return Tasks.forResult(new ArrayList<>(testCommentList));
//                    }
//                }
//        );
//    }
//
//    /**
//     * Custom ViewAction to click the end icon of a TextInputLayout
//     */
//    private static ViewAction clickEndIcon() {
//        return new ViewAction() {
//            @Override
//            public Matcher<View> getConstraints() {
//                return ViewMatchers.isAssignableFrom(TextInputLayout.class);
//            }
//
//            @Override
//            public String getDescription() {
//                return "Click on the end icon of TextInputLayout";
//            }
//
//            @Override
//            public void perform(UiController uiController, View view) {
//                TextInputLayout textInputLayout = (TextInputLayout) view;
//                // Find the end icon view and click it
//                View endIconView = textInputLayout.findViewById(com.google.android.material.R.id.text_input_end_icon);
//                endIconView.performClick();
//            }
//        };
//    }
}