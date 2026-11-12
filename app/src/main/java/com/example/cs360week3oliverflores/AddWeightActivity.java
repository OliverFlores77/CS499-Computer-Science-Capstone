package com.example.cs360week3oliverflores;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.Calendar;

/**
 * Screen for logging a weight entry. The user types a weight, picks a date
 * (today by default), and can optionally turn on an SMS alert for reaching the goal.
 * The weight is checked before it is saved, and it is saved under the logged-in user.
 */
public class AddWeightActivity extends AppCompatActivity {

    // Number the goal alert is sent to (5556 is the emulator's own number).
    private static final String SMS_PHONE_NUMBER = "5556";
    private static final String SMS_MESSAGE = "Congratulations! You reached your goal weight!";
    private static final int SMS_PERMISSION_REQUEST_CODE = 101;

    EditText editWeightInput;
    Button buttonPickDate;
    Switch switchSmsPermission;
    Button buttonSaveWeight;
    WeightRepository repository;
    SessionManager sessionManager;

    // The chosen entry date. Month is zero-based (0 = January), as Android's picker uses.
    private int selectedYear;
    private int selectedMonth;
    private int selectedDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        repository = new WeightRepository(this);
        sessionManager = new SessionManager(this);

        // Nobody is logged in: send the user to the login screen.
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_add_weight);

        editWeightInput = findViewById(R.id.editWeightInput);
        buttonPickDate = findViewById(R.id.buttonPickDate);
        switchSmsPermission = findViewById(R.id.switchSmsPermission);
        buttonSaveWeight = findViewById(R.id.buttonSaveWeight);

        // The date defaults to today.
        Calendar today = Calendar.getInstance();
        selectedYear = today.get(Calendar.YEAR);
        selectedMonth = today.get(Calendar.MONTH);
        selectedDay = today.get(Calendar.DAY_OF_MONTH);
        updateDateButton();

        buttonPickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        buttonSaveWeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String weightStr = editWeightInput.getText().toString().trim();

                // Check the input first, so bad input shows a message instead of crashing.
                String error = InputValidator.validateWeight(weightStr);
                if (error != null) {
                    editWeightInput.setError(error);
                    return;
                }

                double weight = Double.parseDouble(weightStr);
                String entryDate = DateHelper.fromPicker(selectedYear, selectedMonth, selectedDay);
                int userId = sessionManager.getUserId();

                if (!repository.addWeight(userId, entryDate, weight)) {
                    Toast.makeText(AddWeightActivity.this, "Could not save the weight entry.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(AddWeightActivity.this, "Weight Logged!", Toast.LENGTH_SHORT).show();

                if (switchSmsPermission.isChecked()) {
                    checkAndSendSmsAlert(weight, repository.getGoalWeight(userId));
                }

                finish();
            }
        });
    }

    /** Shows the calendar dialog, starting on the currently selected date. */
    private void showDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        selectedYear = year;
                        selectedMonth = month;
                        selectedDay = dayOfMonth;
                        updateDateButton();
                    }
                },
                selectedYear, selectedMonth, selectedDay);

        // A weight cannot be logged for a day that has not happened yet.
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }

    /** Shows the selected date on the date button, for example "Date: 2026-06-05". */
    private void updateDateButton() {
        buttonPickDate.setText("Date: " + DateHelper.fromPicker(selectedYear, selectedMonth, selectedDay));
    }

    private void checkAndSendSmsAlert(double weight, double goalWeight) {
        // Compare the entered weight against this user's saved goal
        if (weight <= goalWeight) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST_CODE);
            } else {
                try {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(SMS_PHONE_NUMBER, null, SMS_MESSAGE, null, null);
                    Toast.makeText(this, "Goal notification sent!", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "SMS failed to send.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
