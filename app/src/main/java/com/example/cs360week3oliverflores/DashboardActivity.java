package com.example.cs360week3oliverflores;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Main screen after login: shows the goal weight and the weight history, and lets the
 * user sort the history, filter it by date range, and see weekly summaries.
 *
 * Data comes from WeightRepository, the current user comes from SessionManager, and
 * the sorting, filtering, and summary logic lives in WeightAnalyzer.
 */
public class DashboardActivity extends AppCompatActivity {

    /** Key used to pass the goal weight to AddWeightActivity in an Intent. */
    public static final String EXTRA_GOAL_WEIGHT = "GOAL_WEIGHT";

    /** Goal weight (lbs) shown until the user changes it. */
    public static final double DEFAULT_GOAL_WEIGHT = 150.0;

    TextView textGoalWeight;
    GridView weightHistoryGrid;
    Button buttonAddData;
    Button buttonSort;
    Button buttonFilter;
    Button buttonSummary;
    WeightRepository repository;
    SessionManager sessionManager;
    WeightEntryAdapter adapter;

    private int currentUserId;
    private double currentGoalWeight = DEFAULT_GOAL_WEIGHT;

    // Every entry for this user, as loaded from the database.
    private List<WeightEntry> allEntries = new ArrayList<>();
    // What the grid is showing right now (allEntries after the filter and sort).
    private List<WeightEntry> displayedEntries = new ArrayList<>();

    private boolean newestFirst = true;
    // Date filter in yyyy-MM-dd form. Both are null when no filter is active.
    private String filterStart = null;
    private String filterEnd = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        repository = new WeightRepository(this);
        sessionManager = new SessionManager(this);

        // Nobody is logged in: send the user to the login screen.
        if (!sessionManager.isLoggedIn()) {
            goToLogin();
            return;
        }
        currentUserId = sessionManager.getUserId();

        setContentView(R.layout.activity_dashboard);

        textGoalWeight = findViewById(R.id.textGoalWeight);
        weightHistoryGrid = findViewById(R.id.weightHistoryGrid);
        buttonAddData = findViewById(R.id.buttonAddData);
        buttonSort = findViewById(R.id.buttonSort);
        buttonFilter = findViewById(R.id.buttonFilter);
        buttonSummary = findViewById(R.id.buttonSummary);

        textGoalWeight.setText("Goal Weight: " + currentGoalWeight + " lbs");

        // One adapter for the life of the screen. Its rows are replaced as the view changes.
        adapter = new WeightEntryAdapter(this, new WeightEntryAdapter.OnDeleteListener() {
            @Override
            public void onDelete(WeightEntry entry) {
                repository.deleteWeight(entry.getId());
                Toast.makeText(DashboardActivity.this, "Weight Entry Deleted", Toast.LENGTH_SHORT).show();
                loadEntries();
            }
        });
        weightHistoryGrid.setAdapter(adapter);

        // The history is loaded in onResume, which always runs right after onCreate.

        buttonAddData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, AddWeightActivity.class);
                intent.putExtra(EXTRA_GOAL_WEIGHT, currentGoalWeight);
                startActivity(intent);
            }
        });

        // Toggle between newest-first and oldest-first.
        buttonSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newestFirst = !newestFirst;
                buttonSort.setText(newestFirst ? "Sort: Newest" : "Sort: Oldest");
                showEntries();
            }
        });

        // Start a date-range filter, or clear the one that is active.
        buttonFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (filterStart != null) {
                    clearDateFilter();
                } else {
                    startDateFilter();
                }
            }
        });

        buttonSummary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showWeeklySummary();
            }
        });

        // Tap the goal weight to change it.
        textGoalWeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGoalWeightDialog();
            }
        });

        // Long-press the goal weight to log out.
        textGoalWeight.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                sessionManager.clear();
                Toast.makeText(DashboardActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
                goToLogin();
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload when returning from the Add Weight screen.
        if (sessionManager.isLoggedIn()) {
            loadEntries();
        }
    }

    /** Opens the login screen and closes this one. */
    private void goToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    // ----- Loading and showing the history -----

    /** Reads this user's entries from the database, then updates the grid. */
    private void loadEntries() {
        allEntries = repository.getWeightEntries(currentUserId);
        showEntries();
    }

    /**
     * Applies the current filter and sort order to the loaded entries and shows
     * the result. This does not touch the database.
     */
    private void showEntries() {
        List<WeightEntry> view;
        if (filterStart != null) {
            view = WeightAnalyzer.filterByDateRange(allEntries, filterStart, filterEnd);
        } else {
            view = new ArrayList<>(allEntries);
        }

        if (newestFirst) {
            WeightAnalyzer.sortNewestFirst(view);
        } else {
            WeightAnalyzer.sortOldestFirst(view);
        }

        displayedEntries = view;
        adapter.setEntries(view);
    }

    // ----- Date filter -----

    /** Asks for a start date, then an end date, then applies the filter. */
    private void startDateFilter() {
        Toast.makeText(this, "Pick the start date", Toast.LENGTH_SHORT).show();
        showDatePicker(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                final String start = DateHelper.fromPicker(year, month, dayOfMonth);
                Toast.makeText(DashboardActivity.this, "Now pick the end date", Toast.LENGTH_SHORT).show();
                showDatePicker(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        applyDateFilter(start, DateHelper.fromPicker(year, month, dayOfMonth));
                    }
                });
            }
        });
    }

    private void applyDateFilter(String start, String end) {
        // Keep the two dates in order, so the message below reads correctly.
        if (start.compareTo(end) > 0) {
            String swap = start;
            start = end;
            end = swap;
        }
        filterStart = start;
        filterEnd = end;
        buttonFilter.setText("Clear Filter");
        showEntries();

        if (displayedEntries.isEmpty()) {
            Toast.makeText(this, "No entries between " + start + " and " + end, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Showing " + displayedEntries.size() + " entries from "
                    + start + " to " + end, Toast.LENGTH_LONG).show();
        }
    }

    private void clearDateFilter() {
        filterStart = null;
        filterEnd = null;
        buttonFilter.setText("Filter Dates");
        showEntries();
    }

    /** Opens a calendar dialog that starts on today and blocks future dates. */
    private void showDatePicker(DatePickerDialog.OnDateSetListener listener) {
        Calendar today = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                listener,
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH));
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }

    // ----- Weekly summary -----

    /** Shows one line per week for the entries currently on screen. */
    private void showWeeklySummary() {
        String message;
        if (displayedEntries.isEmpty()) {
            message = "There are no entries to summarize.";
        } else {
            StringBuilder text = new StringBuilder();
            for (WeekSummary summary : WeightAnalyzer.summarizeByWeek(displayedEntries)) {
                text.append(summary.toString()).append("\n\n");
            }
            message = text.toString().trim();
        }

        new AlertDialog.Builder(this)
                .setTitle("Weekly Summary")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    // ----- Goal weight -----

    private void showGoalWeightDialog() {
        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint(String.valueOf(currentGoalWeight));

        new AlertDialog.Builder(this)
                .setTitle("Update Goal Weight")
                .setMessage("Enter your new target weight:")
                .setView(input)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String value = input.getText().toString().trim();
                        if (!value.isEmpty()) {
                            currentGoalWeight = Double.parseDouble(value);
                            textGoalWeight.setText("Goal Weight: " + currentGoalWeight + " lbs");
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
