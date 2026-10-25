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

public class DashboardActivity extends AppCompatActivity {

    TextView textGoalWeight;
    GridView weightHistoryGrid;
    Button buttonAddData;
    DatabaseHelper dbHelper;
    SimpleCursorAdapter adapter;

    private double currentGoalWeight = 150.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        dbHelper = new DatabaseHelper(this);

        textGoalWeight = findViewById(R.id.textGoalWeight);
        weightHistoryGrid = findViewById(R.id.weightHistoryGrid);
        buttonAddData = findViewById(R.id.buttonAddData);

        textGoalWeight.setText("Goal Weight: " + currentGoalWeight + " lbs");

        refreshGridData();

        buttonAddData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, AddWeightActivity.class);
                intent.putExtra("GOAL_WEIGHT", currentGoalWeight);
                startActivity(intent);
            }
        });

        textGoalWeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGoalWeightDialog();
            }
        });

        textGoalWeight.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                finish();
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshGridData();
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

    private void refreshGridData() {
        Cursor cursor = dbHelper.getAllWeights();

        String[] fromColumns = {DatabaseHelper.COL_DATE, DatabaseHelper.COL_WEIGHT_VALUE};
        int[] toViews = {R.id.textRowDate, R.id.textRowWeight};

        // Overriding getView to find and attach the click listener to the specific delete button inside the row
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

                // Use the cursor at this row position to find the unique database entry ID
                Cursor currentCursor = (Cursor) getItem(position);
                final int rowId = currentCursor.getInt(currentCursor.getColumnIndexOrThrow(DatabaseHelper.COL_WEIGHT_ID));

                // Find your row's specific delete button/action item (adjust R.id name if different in your layout)
                View btnDelete = view.findViewById(R.id.buttonDeleteRow);                if (btnDelete != null) {
                    btnDelete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dbHelper.deleteWeight(rowId);
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