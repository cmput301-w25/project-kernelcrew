package com.kernelcrew.moodapp.ui.components;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.Query;
import com.kernelcrew.moodapp.R;
import com.kernelcrew.moodapp.data.Emotion;
import com.kernelcrew.moodapp.data.MoodEventFilter;
import com.kernelcrew.moodapp.data.MoodEventProvider;
import com.kernelcrew.moodapp.data.User;
import com.kernelcrew.moodapp.data.UserProvider;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * A fragment that interfaces the filtering options for mood events.
 */
public abstract class FilterBarFragment extends Fragment {
    // ! Constants
    private boolean SHOW_SEARCH_OPTIONS = true;
    private boolean userSearchActive = false;
    private boolean triggerSearchActive = true;
    private boolean reasonSearchActive = true;

    // We'll store an in-progress "user search" text so we can delay queries.
    private final Handler userSearchHandler = new Handler();
    private Runnable userSearchRunnable;

    // Current filters in use
    private MoodEventFilter moodEventFilter;
    private final Set<Emotion> selectedEmotions = new HashSet<>();

    // Getters and Setters of CONSTANTS
    public boolean isSHOW_SEARCH_OPTIONS() {
        return SHOW_SEARCH_OPTIONS;
    }

    public void setSHOW_SEARCH_OPTIONS(boolean SHOW_SEARCH_OPTIONS) {
        this.SHOW_SEARCH_OPTIONS = SHOW_SEARCH_OPTIONS;
    }

    public boolean isUserSearchActive() {
        return userSearchActive;
    }

    public void setUserSearchActive(boolean userSearchActive) {
        this.userSearchActive = userSearchActive;
    }

    public boolean isTriggerSearchActive() {
        return triggerSearchActive;
    }

    public void setTriggerSearchActive(boolean triggerSearchActive) {
        this.triggerSearchActive = triggerSearchActive;
    }

    public boolean isReasonSearchActive() {
        return reasonSearchActive;
    }

    public void setReasonSearchActive(boolean reasonSearchActive) {
        this.reasonSearchActive = reasonSearchActive;
    }

    // Listener for public interface
    private OnFilterChangedListener listener;

    /**
     * Interface to notify when filters are changed.
     */
    public interface OnFilterChangedListener {
        /**
         * Called when the filter has been updated.
         * @param filter The updated filter object.
         */
        void onFilterChanged(MoodEventFilter filter);
    }

    /**
     * Auto-wire the listener from parent Activity or Fragment if available.
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnFilterChangedListener) {
            listener = (OnFilterChangedListener) context;
        } else if (getParentFragment() instanceof OnFilterChangedListener) {
            listener = (OnFilterChangedListener) getParentFragment();
        }
    }

    /**
     * Callback interface for delivering user search results to whoever is listening (e.g., HomeFeed).
     */
    public interface OnUserSearchListener {
        /**
         * Called when new user search results are ready.
         * @param users List of matching users (could be empty, but never null).
         */
        void onUserSearchResults(List<User> users);
    }

    private OnUserSearchListener userSearchListener;

    /**
     * Registers a listener for receiving user search results.
     * @param listener The listener to receive those results.
     */
    public void setOnUserSearchListener(OnUserSearchListener listener) {
        this.userSearchListener = listener;
    }

    // UI Elements
    private TextInputLayout filterSearchLayout;
    private TextInputEditText searchEditText;
    private MaterialButton filterEmotion;
    private MaterialButton filterCountAndEdit;
    private MaterialButton filterTimeRange;
    private MaterialButton filterLocation;
    private MaterialButton searchUser;
    private MaterialButton searchTrigger;
    private MaterialButton searchReason;

    /**
     * Inflates the filter bar layout and initializes filter options.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate fragment's layout
        View view = inflater.inflate(R.layout.layout_filter_bar, container, false);

        // Conditionally hide search row if constant is false
        if (!SHOW_SEARCH_OPTIONS) {
            view.findViewById(R.id.searchRowLayout).setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.searchRowLayout).setVisibility(View.VISIBLE);
        }

        // Initialize Views
        filterSearchLayout = view.findViewById(R.id.filterSearchLayout);
        searchEditText = view.findViewById(R.id.filterSearchEditText);
        filterEmotion = view.findViewById(R.id.filter_emotion);
        filterCountAndEdit = view.findViewById(R.id.filterCountAndEdit);
        filterTimeRange = view.findViewById(R.id.filter_timeRange);
        filterLocation = view.findViewById(R.id.filter_location);
        searchUser = view.findViewById(R.id.searchUser);
        searchTrigger = view.findViewById(R.id.searchTrigger);
        searchReason = view.findViewById(R.id.searchReason);

        // Local Variables
        HorizontalScrollView filterButtonsContainer = view.findViewById(R.id.filterButtonsContainer);

        // Call the abstract setupUI to enforce keyboard-hiding and other UI setups.
        assert getParentFragment() != null;
        setupKeyboardHiding(getParentFragment().getView());

        // -- Event Listeners -----------------
        // Search bar listeners
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (userSearchRunnable != null) {
                    userSearchHandler.removeCallbacks(userSearchRunnable);
                }
                userSearchRunnable = () -> {
                    String typed = s.toString().trim();

                    if (userSearchActive) {
                        performUserSearch(typed);
                    } else {
                        if (triggerSearchActive || reasonSearchActive) {
                            if (typed.isEmpty()) {
                                getMoodEventFilter().setSearchQuery(null);
                            } else {
                                getMoodEventFilter().setSearchQuery(typed);
                            }
                            notifyFilterChanged();
                        }
                    }
                };
                userSearchHandler.postDelayed(userSearchRunnable, 400); // e.g. 400ms delay

            }
            @Override
            public void afterTextChanged(Editable s) {
                if (userSearchRunnable != null) {
                    userSearchHandler.removeCallbacks(userSearchRunnable);
                }
                userSearchRunnable = () -> {
                    String typed = s.toString().trim();

                    if (userSearchActive) {
                        performUserSearch(typed);

                    } else {
                        if (triggerSearchActive || reasonSearchActive) {
                            if (typed.isEmpty()) {
                                getMoodEventFilter().setSearchQuery(null);
                            } else {
                                getMoodEventFilter().setSearchQuery(typed);
                            }
                            notifyFilterChanged();
                        }
                    }
                };
            }
        });

        // Taha used the following resources,
        // https://stackoverflow.com/questions/3205339/android-how-to-make-keyboard-enter-button-say-search-and-handle-its-click
        // https://stackoverflow.com/questions/2004344/how-do-i-handle-imeoptions-done-button-click
        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || (event != null
                            && event.getAction() == KeyEvent.ACTION_DOWN
                            && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                        )) {
                    String typed = v.getText().toString().trim();
                    if (userSearchActive) {
                        performUserSearch(typed);
                        return true;
                    } else if (triggerSearchActive || reasonSearchActive) {
                        getMoodEventFilter().setSearchQuery(typed.isEmpty() ? null : typed);
                        notifyFilterChanged();
                        return true;
                    }
                }
                return false;
            }
        });

        // Whenever the user toggles "Search User":
        searchUser.setOnClickListener(v -> {
            userSearchActive = !userSearchActive;
            searchUser.setChecked(userSearchActive);
            filterSearchLayout.setError(null);
            filterSearchLayout.setErrorEnabled(false);

            if (userSearchActive) {
                // Disable Trigger and Reason
                triggerSearchActive = false;
                reasonSearchActive = false;
                searchTrigger.setChecked(false);
                searchReason.setChecked(false);
                searchTrigger.setEnabled(false);
                searchReason.setEnabled(false);

                filterButtonsContainer.setVisibility(View.GONE);
                filterButtonsContainer.setBackground(null);
            } else {
                // Re-enable Trigger and Reason
                searchTrigger.setEnabled(true);
                searchReason.setEnabled(true);

                filterButtonsContainer.setVisibility(View.VISIBLE);
                filterButtonsContainer.setBackgroundResource(R.drawable.black_border);
            }

            updateSearchLogic();
        });

        // Whenever the user toggles "Search Trigger":
        searchTrigger.setOnClickListener(v -> {
            boolean newVal = !triggerSearchActive;
            searchTrigger.setChecked(newVal);
            triggerSearchActive = newVal;
            filterSearchLayout.setError(null);
            filterSearchLayout.setErrorEnabled(false);

            if (triggerSearchActive) {
                if (userSearchActive) {
                    userSearchActive = false;
                    searchUser.setChecked(false);
                }
                searchUser.setEnabled(false);
            } else {
                if (!reasonSearchActive) {
                    searchUser.setEnabled(true);
                }
            }

            updateSearchLogic();
        });

        // Whenever the user toggles "Search Reason":
        searchReason.setOnClickListener(v -> {
            boolean newVal = !reasonSearchActive;
            searchReason.setChecked(newVal);
            reasonSearchActive = newVal;
            filterSearchLayout.setError(null);
            filterSearchLayout.setErrorEnabled(false);

            if (reasonSearchActive) {
                if (userSearchActive) {
                    userSearchActive = false;
                    searchUser.setChecked(false);
                }
                searchUser.setEnabled(false);
            } else {
                if (!triggerSearchActive) {
                    searchUser.setEnabled(true);
                }
            }

            updateSearchLogic();
        });

        // Filter count and edit popup menu
        filterCountAndEdit.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(requireContext(), filterCountAndEdit);

            MenuItem clearFiltersItem = popup.getMenu().add("Clear All Filters");
            SpannableString redTitle = new SpannableString("Clear All Filters");
            redTitle.setSpan(new ForegroundColorSpan(Color.RED), 0, redTitle.length(), 0);
            clearFiltersItem.setTitle(redTitle);
            popup.getMenu().add("Show Filter Summary");

            popup.setOnMenuItemClickListener(item -> {
                String title = Objects.requireNonNull(item.getTitle()).toString();
                if (title.equals("Clear All Filters")) {
                    getMoodEventFilter().clearFilters();
                    selectedEmotions.clear();
                    searchEditText.setText("");
                    Toast.makeText(requireContext(), "Filters cleared", Toast.LENGTH_SHORT).show();
                } else if (title.equals("Show Filter Summary")) {
                    String summary = getMoodEventFilter().getSummary();
                    Toast.makeText(requireContext(), summary, Toast.LENGTH_LONG).show();
                }
                notifyFilterChanged();
                return true;
            });

            popup.setOnDismissListener(menu -> {
                getMoodEventFilter().setEmotions(selectedEmotions);
                notifyFilterChanged();
                filterCountAndEdit.setChecked(false);
            });

            popup.show();
        });

        // Taha used the following resources,
        // https://stackoverflow.com/questions/21329132/android-custom-dropdown-popup-menu
        // https://stackoverflow.com/questions/13784088/setting-popupmenu-menu-items-programmatically
        // Emotion filter popup menu
        filterEmotion.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(requireContext(), filterEmotion);

            for (Emotion emotion : Emotion.values()) {
                MenuItem item = popup.getMenu().add(emotion.toString());
                item.setCheckable(true);
                item.setChecked(selectedEmotions.contains(emotion));
            }

            // Taha used the following resources,
            // https://stackoverflow.com/questions/29726039/how-to-prevent-popup-menu-from-closing-on-checkbox-click
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    item.setChecked(!item.isChecked());

                    if (item.isChecked()) {
                        selectedEmotions.add(Emotion.fromString(Objects.requireNonNull(item.getTitle()).toString()));
                    } else {
                        selectedEmotions.remove(Emotion.fromString(Objects.requireNonNull(item.getTitle()).toString()));
                    }

                    item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
                    item.setActionView(new View(requireContext()));
                    item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                        @Override
                        public boolean onMenuItemActionExpand(MenuItem item) {
                            return false;
                        }

                        @Override
                        public boolean onMenuItemActionCollapse(MenuItem item) {
                            return false;
                        }
                    });
                    return false;
                }
            });

            popup.setOnDismissListener(menu -> {
                getMoodEventFilter().setEmotions(selectedEmotions);
                notifyFilterChanged();
                filterEmotion.setChecked(false);
            });

            popup.show();
        });

        // Time range filter popup menu
        filterTimeRange.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(requireContext(), filterTimeRange);
            popup.getMenu().add("Today");
            popup.getMenu().add("This Week");
            popup.getMenu().add("This Month");
            popup.getMenu().add("All Time");

            popup.setOnMenuItemClickListener(item -> {
                String selection = item.getTitle().toString();
                Date startDate = null;
                Date endDate = null;
                Calendar calendar = Calendar.getInstance();

                switch (selection) {
                    case "Today":
                        resetTime(calendar);
                        startDate = calendar.getTime();
                        calendar.add(Calendar.DAY_OF_MONTH, 1);
                        calendar.add(Calendar.MILLISECOND, -1);
                        endDate = calendar.getTime();
                        break;
                    case "This Week":
                        // Move to the first day of the week then reset time
                        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
                        resetTime(calendar);
                        startDate = calendar.getTime();
                        calendar.add(Calendar.DAY_OF_WEEK, 7);
                        calendar.add(Calendar.MILLISECOND, -1);
                        endDate = calendar.getTime();
                        break;
                    case "This Month":
                        calendar.set(Calendar.DAY_OF_MONTH, 1);
                        resetTime(calendar);
                        startDate = calendar.getTime();
                        calendar.add(Calendar.MONTH, 1);
                        calendar.add(Calendar.MILLISECOND, -1);
                        endDate = calendar.getTime();
                        break;
                    default:
                        break;
                }

                // Update the filter with the selected date range
                getMoodEventFilter().setDateRange(startDate, endDate);
                notifyFilterChanged();
                return true;
            });

            popup.setOnDismissListener(menu -> {
                notifyFilterChanged();
                filterTimeRange.setChecked(false);
            });

            popup.show();
        });

        // Location filter popup menu
        filterLocation.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(requireContext(), filterLocation);

            popup.getMenu().add("Within 5 km");
            popup.getMenu().add("Within 10 km");

            MenuItem clearLocationItem = popup.getMenu().add("Clear Location Filter");
            SpannableString redTitle = new SpannableString("Clear Location Filter");
            redTitle.setSpan(new ForegroundColorSpan(Color.RED), 0, redTitle.length(), 0);
            clearLocationItem.setTitle(redTitle);

            popup.setOnMenuItemClickListener(item -> {
                Toast.makeText(requireContext(), "Location Stuff not Implemented", Toast.LENGTH_SHORT).show();

                return true;
            });

            popup.setOnDismissListener(menu -> {
                notifyFilterChanged();
                filterLocation.setChecked(false);
            });

            popup.show();
        });

        // ...
        // TODO: Maybe add more filters, I do not know what/how the app will look like in the future
        //          so currently the implementation only has filters for things I could think of.
        // If you want to add a filter, just copy the (one of the preexisting) xml buttons and
        //      and simply change the ID and android:text. Then use the above as a template, all the
        //      buttons should have very similar if not the same pop-up menu logic.
        // ...

        // Display the initial mood list with 0 filters.
        notifyFilterChanged();

        return view;
    }

    /**
     * Updates the search logic whenever a toggle changes.
     * If "Search User" is active, we remove MoodEventFilter's query and do user search.
     * If Trigger/Reason is active, we apply MoodEventFilter searching on reason/trigger.
     * If none are active, we clear results and do nothing.
     */
    private void updateSearchLogic() {
        boolean atLeastOne = userSearchActive || triggerSearchActive || reasonSearchActive;
        if (!atLeastOne) {
            filterSearchLayout.setError("Select a search method!");
            return;
        }

        if (userSearchActive) {
            performUserSearch("");
        } else {
            notifyFilterChanged();
        }
    }

    /**
     * Executes a Firestore search for users based on the typed query,
     * mimicking the existing 'SearchUsers' logic. Once results come back,
     * show them or “No results found.” as needed.
     *
     * @param query The text typed by the user for searching.
     */
    private void performUserSearch(String query) {
        Task<List<User>> q = UserProvider.getInstance().searchUsers(
                query,
                Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()
        );

        q.addOnSuccessListener(users -> {
            if (userSearchListener != null) {
                userSearchListener.onUserSearchResults(users);
            }
        }).addOnFailureListener(e -> {
            Log.e("FilterBarFragment", "User search failed!", e);
            Toast.makeText(getContext(), "Search failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();

            if (userSearchListener != null) {
                userSearchListener.onUserSearchResults(new ArrayList<>());
            }
        });
    }

    /**
     * Abstract method that MUST be implemented by subclasses.
     * Used to set up touch listeners on all views (except EditText)
     * so that touching outside of a text box hides the keyboard.
     * <a href="https://stackoverflow.com/questions/4165414/how-to-hide-soft-keyboard-on-android-after-clicking-outside-edittext">...</a>
     *
     * <pre>
     * {@code
     *     @SuppressLint("ClickableViewAccessibility")
     *     @Override
     *     public void setupKeyboardHiding(View view) {
     *         if (!(view instanceof EditText)) {
     *             view.setOnTouchListener((v, event) -> {
     *                 assert getActivity() != null;
     *                 Utility.hideSoftKeyboard(getActivity());
     *                 return false;
     *             });
     *         }
     *         if (view instanceof ViewGroup) {
     *             ViewGroup group = (ViewGroup) view;
     *             for (int i = 0; i < group.getChildCount(); i++) {
     *                 View child = group.getChildAt(i);
     *                 setupKeyboardHiding(child);
     *             }
     *         }
     *     }
     * }
     * </pre>
     *
     * @param view The root view to set up.
     */
    public abstract void setupKeyboardHiding(View view);

    /**
     * Retrieves the current mood event filter or creates a new one if not initialized.
     *
     * @return The current {@link MoodEventFilter} instance.
     */
    public MoodEventFilter getMoodEventFilter() {
        if (moodEventFilter == null) {
            moodEventFilter = new MoodEventFilter(MoodEventProvider.getInstance());
        }
        return moodEventFilter;
    }

    /**
     * Sets a listener to be notified when the filter changes.
     *
     * @param listener The listener to notify.
     */
    public void setOnFilterChangedListener(OnFilterChangedListener listener) {
        this.listener = listener;
    }

    /**
     * Notifies the listener that the filter has changed.
     */
    private void notifyFilterChanged() {
        getMoodEventFilter().setSortField("created", Query.Direction.DESCENDING);
        filterCountAndEdit.setText(String
                .valueOf(getMoodEventFilter().getSearchQuery() == null
                            ? getMoodEventFilter().count() - 1
                            : getMoodEventFilter().count() - 2
                        )
        );
        if (listener != null) {
            listener.onFilterChanged(getMoodEventFilter());
        } else {
            Log.w("FilterBarFragment", "OnFilterChangedListener is not attached!");
        }
    }

    /**
     * Resets the time of a calendar instance to the start of the day.
     *
     * @param calendar The calendar instance to reset.
     */
    private void resetTime(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    /**
     * Custom Query setter for the Home Page
     * @param query The query built and set according to the correct filters
     */
    public void setCustomQuery(Query query) {
        getMoodEventFilter().setCustomQuery(query);
        notifyFilterChanged();
    }
}
