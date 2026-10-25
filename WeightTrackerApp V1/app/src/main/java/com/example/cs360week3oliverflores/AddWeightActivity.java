package com.example.cs360week3oliverflores;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class AddWeightActivity extends AppCompatActivity {

    EditText editWeightInput;
    Switch switchSmsPermission;
    Button buttonSaveWeight;
    DatabaseHelper dbHelper;

    private double targetGoalWeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_weight);

        dbHelper = new DatabaseHelper(this);

        // Retrieve intent extra bundle value passed from dashboard
        targetGoalWeight = getIntent().getDoubleExtra("GOAL_WEIGHT", 150.0);

        editWeightInput = findViewById(R.id.editWeightInput);
        switchSmsPermission = findViewById(R.id.switchSmsPermission);
        buttonSaveWeight = findViewById(R.id.buttonSaveWeight);

        buttonSaveWeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String weightStr = editWeightInput.getText().toString().trim();

                if (!weightStr.isEmpty()) {
                    double weight = Double.parseDouble(weightStr);
                    String fakeCurrentDate = "2026-06-13";

                    dbHelper.addWeight(fakeCurrentDate, weight);
                    Toast.makeText(AddWeightActivity.this, "Weight Logged!", Toast.LENGTH_SHORT).show();

                    if (switchSmsPermission.isChecked()) {
                        checkAndSendSmsAlert(weight);
                    }

                    finish();
                }
            }
        });
    }

    private void checkAndSendSmsAlert(double weight) {
        // Compare entered input against current dynamic target goal threshold
        if (weight <= targetGoalWeight) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 101);
            } else {
                try {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage("5556", null, "Congratulations! You reached your goal weight!", null, null);
                    Toast.makeText(this, "Goal notification sent!", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "SMS failed to send.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}