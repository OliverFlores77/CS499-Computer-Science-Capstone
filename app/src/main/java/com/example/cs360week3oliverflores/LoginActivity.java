package com.example.cs360week3oliverflores;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Launcher screen: lets a user log in or create an account.
 * After a successful login the user's id is saved in the session so the other
 * screens know who is logged in.
 */
public class LoginActivity extends AppCompatActivity {

    EditText usernameInput;
    EditText passwordInput;
    Button buttonLogin;
    Button buttonCreateAccount;
    WeightRepository repository;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        repository = new WeightRepository(this);
        sessionManager = new SessionManager(this);

        // Arriving at the login screen always starts a fresh session,
        // so an old login can never be left behind.
        sessionManager.clear();

        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonCreateAccount = findViewById(R.id.buttonCreateAccount);

        // Submit action: validate against the database through the repository
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = usernameInput.getText().toString().trim();
                String pass = passwordInput.getText().toString().trim();

                int userId = repository.authenticateUser(user, pass);
                if (userId != WeightRepository.NO_USER) {
                    // Remember who logged in, then open the dashboard.
                    sessionManager.saveUserId(userId);
                    Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                    // Close the login screen so Back from the dashboard exits the app
                    // instead of returning to a stale login screen.
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Registration action: check the input, then save the new account
        buttonCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = usernameInput.getText().toString().trim();
                String pass = passwordInput.getText().toString().trim();

                String error = InputValidator.validateCredentials(user, pass);
                if (error != null) {
                    Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
                    return;
                }

                RegisterResult result = repository.registerUser(user, pass);
                switch (result) {
                    case SUCCESS:
                        Toast.makeText(LoginActivity.this, "Account Created Successfully!", Toast.LENGTH_SHORT).show();
                        break;
                    case USERNAME_TAKEN:
                        Toast.makeText(LoginActivity.this, "That username is already taken", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(LoginActivity.this, "Could not create the account. Please try again.", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
    }
}
