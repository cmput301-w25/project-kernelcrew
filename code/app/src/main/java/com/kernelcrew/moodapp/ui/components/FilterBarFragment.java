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
    // Toggles for which search mode is active
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

    // Add this interface (if you’re not using Java 8 lambdas)
    // Taha used chatgpt, "how do I make my filter class updatable from outside? <Code>"
    public interface FilterUpdater {
        void update(MoodEventFilter filter);
    }

    /**
     * Allows external code to update the current MoodEventFilter. After the update,
     * notifyFilterChanged() is automatically called to refresh the filter state.
     *
     * Usage:
     *   filterBarFragment.updateFilter(new FilterUpdater() {
     *       @Override
     *       public void update(MoodEventFilter filter) {
     *           filter.setLimit(10);
     *       }
     *   });
     *
     * Or, if using Java 8 lambdas:
     *   filterBarFragment.updateFilter(filter -> filter.setLimit(10));
     *
     * @param updater The updater that modifies the MoodEventFilter.
     */
    public void updateFilter(FilterUpdater updater) {
        updater.update(getMoodEventFilter());
        notifyFilterChanged();
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

        HorizontalScrollView filterButtonsContainer = view.findViewById(R.id.filterButtonsContainer);

        // Hide keyboard if user touches outside of EditText.
        assert getParentFragment() != null;
        setupKeyboardHiding(getParentFragment().getView());

        // ----- Event Listeners -----

        // Search bar watchers
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                scheduleSearch(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {
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
            userSearchActive = true;
            searchUser.setChecked(true);

            // If userSearchActive -> disable reasonSearch
            reasonSearchActive = false;
            searchReason.setChecked(false);
            filterButtonsContainer.setVisibility(View.GONE);
            filterButtonsContainer.setBackground(null);

            updateSearchLogic();
        });

        // Toggle for "Search Reason"
        reasonSearchActive = true;
        searchReason.setChecked(reasonSearchActive);

        searchReason.setOnClickListener(v -> {
            reasonSearchActive = true;
            searchReason.setChecked(true);

            userSearchActive = false;
            searchUser.setChecked(false);
            filterButtonsContainer.setVisibility(View.VISIBLE);
            filterButtonsContainer.setBackgroundResource(R.drawable.black_border);
            updateSearchLogic();
        });

        // Filter count & summary
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

        // Emotion filter
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

        // Time range filter
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
                        // Set to the first day of week per device locale.
                        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
                        resetTime(calendar);
                        startDate = calendar.getTime();
                        calendar.add(Calendar.DAY_OF_WEEK, 7);
                        calendar.add(Calendar.MILLISECOND, -1);
                        endDate = calendar.getTime();
                        break;
                    case "This Month":
                        // Set to first day of current month.
                        calendar.set(Calendar.DAY_OF_MONTH, 1);
                        resetTime(calendar);
                        startDate = calendar.getTime();
                        calendar.add(Calendar.MONTH, 1);
                        calendar.add(Calendar.MILLISECOND, -1);
                        endDate = calendar.getTime();
                        break;
                    case "All Time":
                        // No date filtering.
                        startDate = null;
                        endDate = null;
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

        // Location filter
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
                            getMoodEventFilter().setLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 5.0);
                        } else {
                            Toast.makeText(requireContext(), "Unable to retrieve location", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    }
                    case "Within 10 km": {
                        Location currentLocation = LocationHandler.getCurrentLocation(requireContext());
                        if (currentLocation != null) {
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
     * If "Search User" is active, we remove reasonQuery from the filter and do user search.
     * If Reason is active, we rely on local searching by reason.
     */
    private void updateSearchLogic() {
        if (!userSearchActive && !reasonSearchActive) {
            filterSearchLayout.setError("Select a search method!");
            return;
        }
        if (userSearchActive) {
            // Perform user search with whatever is typed
            performUserSearch(searchEditText.getText().toString().trim());
        } else {
            // Clear reason query so local searching doesn't filter everything out
            getMoodEventFilter().setReasonQuery(null);
            notifyFilterChanged();
        }
    }

    /**
     * Executes a Firestore search for users based on the typed query.
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
     */
    public abstract void setupKeyboardHiding(View view);

    /**
     * Retrieves (or creates) the current mood event filter.
     */
    public MoodEventFilter getMoodEventFilter() {
        if (moodEventFilter == null) {
            moodEventFilter = new MoodEventFilter(MoodEventProvider.getInstance());
        }
        return moodEventFilter;
    }

    /**
     * Sets a listener to be notified when the filter changes.
     */
    public void setOnFilterChangedListener(OnFilterChangedListener listener) {
        this.listener = listener;
    }

    /**
     * Notifies the listener that the filter has changed.
     */
    private void notifyFilterChanged() {
        // Always sort by "created" desc by default
        getMoodEventFilter().setSortField("created", Query.Direction.DESCENDING);
        filterCountAndEdit.setText(String.valueOf(getMoodEventFilter().count()));
        if (listener != null) {
            listener.onFilterChanged(getMoodEventFilter());
        } else {
            Log.w("FilterBarFragment", "OnFilterChangedListener is not attached!");
        }
    }

    /**
     * Filters a list of MoodEvents locally based on the user’s current reasonSearchActive state.
     */
    public List<MoodEvent> applyLocalSearch(List<MoodEvent> allMoods) {
        String reasonQ = getMoodEventFilter().getReasonQuery();
        boolean hasQuery = (reasonQ != null && !reasonQ.trim().isEmpty());
        if (!reasonSearchActive || !hasQuery) {
            // If we aren't using reason search or there's no text, we just return them all
            return allMoods;
        }
        String lowerQuery = reasonQ.trim().toLowerCase();
        List<MoodEvent> filtered = new ArrayList<>();
        for (MoodEvent event : allMoods) {
            String reason = event.getReason();
            if (reason != null && reason.toLowerCase().contains(lowerQuery)) {
                filtered.add(event);
            }
        }
        return filtered;
    }

    /**
     * Reset time to start of day
     */
    private void resetTime(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    /**
     * Delay frequent invocations of user searching
     */
    private void scheduleSearch(String text) {
        if (userSearchRunnable != null) {
            userSearchHandler.removeCallbacks(userSearchRunnable);
        }
        userSearchRunnable = () -> handleSearchText(text);
        userSearchHandler.postDelayed(userSearchRunnable, 200);
    }

    /**
     * Decide how to handle typed text given which toggle is active.
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