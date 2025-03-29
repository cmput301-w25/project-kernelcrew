package com.kernelcrew.moodapp.ui.components;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
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
import com.kernelcrew.moodapp.data.LocationHandler;
import com.kernelcrew.moodapp.data.MoodEvent;
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
    private boolean userSearchActive = false;
    private boolean reasonSearchActive = false;
    private boolean allowUserSearch = false;

    // UI Elements
    private TextInputLayout filterSearchLayout;
    private TextInputEditText searchEditText;
    private MaterialButton filterEmotion;
    private MaterialButton filterCountAndEdit;
    private MaterialButton filterTimeRange;
    private MaterialButton filterLocation;
    private MaterialButton searchUser;
    private MaterialButton searchReason;

    // Handler for delaying user search queries.
    private final Handler userSearchHandler = new Handler();
    private Runnable userSearchRunnable;

    // Current filters in use
    private MoodEventFilter moodEventFilter;
    private final Set<Emotion> selectedEmotions = new HashSet<>();

    // Listener for public interface
    private OnFilterChangedListener listener;
    private OnUserSearchListener userSearchListener;

    public void setAllowUserSearch(boolean allowUserSearch) {
        this.allowUserSearch = allowUserSearch;
    }

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

    /**
     * Registers a listener for receiving user search results.
     * @param listener The listener to receive those results.
     */
    public void setOnUserSearchListener(OnUserSearchListener listener) {
        this.userSearchListener = listener;
    }

    /**
     * Inflates the filter bar layout and initializes filter options.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate fragment's layout
        View view = inflater.inflate(R.layout.layout_filter_bar, container, false);

        // Initialize Views
        filterSearchLayout = view.findViewById(R.id.filterSearchLayout);
        searchEditText = view.findViewById(R.id.filterSearchEditText);
        filterEmotion = view.findViewById(R.id.filter_emotion);
        filterCountAndEdit = view.findViewById(R.id.filterCountAndEdit);
        filterTimeRange = view.findViewById(R.id.filter_timeRange);
        filterLocation = view.findViewById(R.id.filter_location);
        searchUser = view.findViewById(R.id.searchUser);
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
                // Post a delayed search query to reduce rapid invocations.
                scheduleSearch(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {
                // No additional action needed; onTextChanged handles the search delay.
            }
        });

        searchEditText.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || (event != null
                    && event.getAction() == KeyEvent.ACTION_DOWN
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                handleSearchText(v.getText().toString());
                return true;
            }
            return false;
        });

        // Toggle for "Search User"
        searchUser.setOnClickListener(v -> {
            if (!userSearchActive) {
                userSearchActive = true;
                reasonSearchActive = false;
                searchUser.setChecked(true);
                searchReason.setChecked(false);
                filterSearchLayout.setError(null);
                filterSearchLayout.setErrorEnabled(false);
                filterButtonsContainer.setVisibility(View.GONE);
                filterButtonsContainer.setBackground(null);
                updateSearchLogic();
            }
        });

        // Toggle for "Search Reason"
        searchReason.setChecked(true);
        searchReason.setOnClickListener(v -> {
            if (!reasonSearchActive) {
                reasonSearchActive = true;
                userSearchActive = false;
                searchReason.setChecked(true);
                searchUser.setChecked(false);
                filterSearchLayout.setError(null);
                filterSearchLayout.setErrorEnabled(false);
                filterButtonsContainer.setVisibility(View.VISIBLE);
                filterButtonsContainer.setBackgroundResource(R.drawable.black_border);
                updateSearchLogic();
            }
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
                    getMoodEventFilter().setReasonQuery(null);
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

        // Emotion filter popup menu
        filterEmotion.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(requireContext(), filterEmotion);
            for (Emotion emotion : Emotion.values()) {
                MenuItem item = popup.getMenu().add(emotion.toString());
                item.setCheckable(true);
                item.setChecked(selectedEmotions.contains(emotion));
            }

            popup.setOnMenuItemClickListener(item -> {
                item.setChecked(!item.isChecked());
                Emotion emotion = Emotion.fromString(Objects.requireNonNull(item.getTitle()).toString());
                if (item.isChecked()) {
                    selectedEmotions.add(emotion);
                } else {
                    selectedEmotions.remove(emotion);
                }
                // Prevent the popup from closing immediately.
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
                item.setActionView(new View(requireContext()));
                return false;
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
                String selection = Objects.requireNonNull(item.getTitle()).toString();
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
                String selected = Objects.requireNonNull(item.getTitle()).toString();
                switch (selected) {
                    case "Within 5 km": {
                        Location currentLocation = LocationHandler.getCurrentLocation(requireContext());
                        if (currentLocation != null) {
                            Log.d("FilterBarFrag", "Current Location" + currentLocation.getLatitude() + currentLocation.getLongitude());
                            getMoodEventFilter().setLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 5.0);
                        } else {
                            Toast.makeText(requireContext(), "Unable to retrieve location", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    }
                    case "Within 10 km": {
                        Location currentLocation = LocationHandler.getCurrentLocation(requireContext());
                        if (currentLocation != null) {
                            Log.d("FilterBarFrag", "Current Location" + currentLocation.getLatitude() + currentLocation.getLongitude());
                            getMoodEventFilter().setLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 10.0);
                        } else {
                            Toast.makeText(requireContext(), "Unable to retrieve location", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    }
                    case "Clear Location Filter": {
                        getMoodEventFilter().setLocation(null, null, 0);
                        break;
                    }
                }
                return true;
            });

            popup.setOnDismissListener(menu -> {
                notifyFilterChanged();
                filterLocation.setChecked(false);
            });
            popup.show();
        });

        // ......
        // TODO: Maybe add more filters, I do not know
        //      what/how the app will look like in the future
        // ......

        notifyFilterChanged();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (searchUser != null) {
            searchUser.setVisibility(allowUserSearch ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Updates the search logic whenever a toggle changes.
     * If "Search User" is active, we remove MoodEventFilter's query and do user search.
     * If Reason is active, we apply local searching on reason.
     * If none are active, we clear results and do nothing.
     */
    private void updateSearchLogic() {
        if (!userSearchActive && !reasonSearchActive) {
            filterSearchLayout.setError("Select a search method!");
            return;
        }
        if (userSearchActive) {
            performUserSearch("");
        } else {
            getMoodEventFilter().setReasonQuery(null);
            notifyFilterChanged();
        }
    }

    /**
     * Executes a Firestore search for users based on the typed query.
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
     * See documentation for more details.
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
        filterCountAndEdit.setText(String.valueOf(getMoodEventFilter().count()));
        if (listener != null) {
            listener.onFilterChanged(getMoodEventFilter());
        } else {
            Log.w("FilterBarFragment", "OnFilterChangedListener is not attached!");
        }
    }

    /**
     * Filters a list of MoodEvents locally based on the user’s current search settings.
     * If "reasonSearchActive" is on, we match mood.getReason().
     */
    public List<MoodEvent> applyLocalSearch(List<MoodEvent> allMoods) {
        String query = getMoodEventFilter().getReasonQuery();
        boolean hasQuery = (query != null && !query.trim().isEmpty());
        String lowerQuery = hasQuery ? query.trim().toLowerCase() : null;
        List<MoodEvent> filtered = new ArrayList<>();
        for (MoodEvent event : allMoods) {
            if (!hasQuery || (reasonSearchActive && event.getReason() != null
                    && event.getReason().toLowerCase().contains(lowerQuery))) {
                filtered.add(event);
            }
        }
        return filtered;
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
     * Schedules the search query after a delay to avoid frequent invocations.
     *
     * @param text The current search text.
     */
    private void scheduleSearch(String text) {
        if (userSearchRunnable != null) {
            userSearchHandler.removeCallbacks(userSearchRunnable);
        }
        userSearchRunnable = () -> handleSearchText(text);
        userSearchHandler.postDelayed(userSearchRunnable, 200);
    }

    /**
     * Handles the search text change based on the active search mode.
     *
     * @param text The current search text.
     */
    private void handleSearchText(String text) {
        String typed = text.trim();
        if (userSearchActive) {
            performUserSearch(typed);
        } else if (reasonSearchActive) {
            getMoodEventFilter().setReasonQuery(typed.isEmpty() ? null : typed);
            notifyFilterChanged();
        }
    }
}