package com.kernelcrew.moodapp.ui.components;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.Query;
import com.kernelcrew.moodapp.R;
import com.kernelcrew.moodapp.data.Emotion;
import com.kernelcrew.moodapp.data.MoodEventFilter;
import com.kernelcrew.moodapp.data.MoodEventProvider;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class FilterBarFragment extends Fragment {
    public interface OnFilterChangedListener {
        void onFilterChanged(MoodEventFilter filter);
    }

    private MoodEventFilter moodEventFilter;
    private OnFilterChangedListener listener;

    private TextInputEditText searchEditText;
    private MaterialButton filterEmotion;
    private MaterialButton filterCountAndEdit;
    private MaterialButton filterTimeRange;
    private MaterialButton filterLocation;
    private Set<Emotion> selectedEmotions = new HashSet<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Inflate your fragment's layout
        View view = inflater.inflate(R.layout.layout_filter_bar, container, false);

        // Initialize Views
        searchEditText          = view.findViewById(R.id.filterSearchEditText);
        filterEmotion           = view.findViewById(R.id.filter_emotion);
        filterCountAndEdit      = view.findViewById(R.id.filterCountAndEdit);
        filterTimeRange         = view.findViewById(R.id.filter_timeRange);
        filterLocation          = view.findViewById(R.id.filter_location);

        // Event Listeners
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void afterTextChanged(Editable s) {
                // TODO: Implement Search
            }
        });

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
            popup.show();
        });

        // Taha used the following resources,
        // https://stackoverflow.com/questions/21329132/android-custom-dropdown-popup-menu
        // https://stackoverflow.com/questions/13784088/setting-popupmenu-menu-items-programmatically
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
                    case "All Time":
                        // Clear any date range filter
                        startDate = null;
                        endDate = null;
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

        // Refresh after creation
        notifyFilterChanged();

        return view;
    }

    public MoodEventFilter getMoodEventFilter() {
        if (moodEventFilter == null) {
            moodEventFilter = new MoodEventFilter(MoodEventProvider.getInstance());
        }
        return moodEventFilter;
    }

    public void setOnFilterChangedListener(OnFilterChangedListener listener) {
        this.listener = listener;
    }

    private void notifyFilterChanged() {
        getMoodEventFilter().setSortField("created", Query.Direction.DESCENDING);
        filterCountAndEdit.setText(String.valueOf(getMoodEventFilter().count() - 1));
        listener.onFilterChanged(getMoodEventFilter());
    }

    private void resetTime(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }
}
