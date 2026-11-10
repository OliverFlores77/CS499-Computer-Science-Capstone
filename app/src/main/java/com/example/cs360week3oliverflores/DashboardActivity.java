package com.example.cs360week3oliverflores;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Main screen after login: shows the goal weight and the weight history,
 * and links to the Add Weight screen.
 * Data comes from WeightRepository; the current user comes from SessionManager.
 */
public class DashboardActivity extends AppCompatActivity {

    /** Key used to pass the goal weight to AddWeightActivity in an Intent. */
    public static final String EXTRA_GOAL_WEIGHT = "GOAL_WEIGHT";

    /** Goal weight (lbs) shown until the user changes it. */
    public static final double DEFAULT_GOAL_WEIGHT = 150.0;

    TextView textGoalWeight;
    GridView weightHistoryGrid;
    Button buttonAddData;
    WeightRepository repository;
    SessionManager sessionManager;
    SimpleCursorAdapter adapter;

    private int currentUserId;
    private double currentGoalWeight = DEFAULT_GOAL_WEIGHT;

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

        textGoalWeight.setText("Goal Weight: " + currentGoalWeight + " lbs");

        refreshGridData();

        buttonAddData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, AddWeightActivity.class);
                intent.putExtra(EXTRA_GOAL_WEIGHT, currentGoalWeight);
                startActivity(intent);
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
            refreshGridData();
        }
    }

    /** Opens the login screen and closes this one. */
    private void goToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

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

    /** Loads the current user's weight history into the grid. */
    private void refreshGridData() {
        Cursor cursor = repository.getWeights(currentUserId);

        String[] fromColumns = {DatabaseHelper.COL_DATE, DatabaseHelper.COL_WEIGHT_VALUE};
        int[] toViews = {R.id.textRowDate, R.id.textRowWeight};

        // The adapter is extended so each row's delete button can be wired up in getView.
        adapter = new SimpleCursorAdapter(
                this,
                R.layout.grid_item_weight,
                cursor,
                fromColumns,
                toViews,
                0
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                // Use the cursor at this row position to find the unique database entry ID.
                Cursor currentCursor = (Cursor) getItem(position);
                final int rowId = currentCursor.getInt(
                        currentCursor.getColumnIndexOrThrow(DatabaseHelper.COL_WEIGHT_ID));

                // Attach the delete action to this row's delete button.
                View btnDelete = view.findViewById(R.id.buttonDeleteRow);
                if (btnDelete != null) {
                    btnDelete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            repository.deleteWeight(rowId);
                            Toast.makeText(DashboardActivity.this, "Weight Entry Deleted", Toast.LENGTH_SHORT).show();
                            refreshGridData();
                        }
                    });
                }

                return view;
            }
        };

        weightHistoryGrid.setAdapter(adapter);
    }
}
