package com.example.shechaniahinformationsystem;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

public class ResetPassword extends AppCompatActivity {

    private EditText passwordResetEditText;
    private EditText passwordConfirmEditText;
    private EditText oldPasswordEditText; // New EditText for old password
    private String username; // To store the username
    private String studentId; // To store the student ID
    private String lastUsedPassword; // To store the last used password

    // TextViews for password requirements
    private TextView requirementUppercase;
    private TextView requirementDigit;
    private TextView requirementSpecialChar;
    private TextView requirementMinLength; // New TextView for minimum length requirement

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        passwordResetEditText = findViewById(R.id.newPassword);
        passwordConfirmEditText = findViewById(R.id.confirmPassword);
        oldPasswordEditText = findViewById(R.id.oldPassword); // Initialize the old password EditText
        AppCompatButton submitButton = findViewById(R.id.btn1);

        // TextViews for password requirements
        requirementUppercase = findViewById(R.id.requirement_uppercase);
        requirementDigit = findViewById(R.id.requirement_digit);
        requirementSpecialChar = findViewById(R.id.requirement_special_char);
        requirementMinLength = findViewById(R.id.requirement_minimum_length); // New for minimum length

        // Get the username and student_id from the intent
        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        studentId = intent.getStringExtra("student_id"); // Retrieve the student ID

        // Fetch the last used password from the server
        fetchLastUsedPassword();

        // Add a TextWatcher to the new password input
        passwordResetEditText.addTextChangedListener(passwordTextWatcher);

        // Set the button click listener
        submitButton.setOnClickListener(view -> resetPassword());
    }

    private final TextWatcher passwordTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String password = s.toString();

            // Show the password requirements when the user starts typing
            if (!password.isEmpty()) {
                requirementUppercase.setVisibility(View.VISIBLE);
                requirementDigit.setVisibility(View.VISIBLE);
                requirementSpecialChar.setVisibility(View.VISIBLE);
                requirementMinLength.setVisibility(View.VISIBLE); // Show minimum length requirement

                // Check if the password has at least 8 characters
                requirementMinLength.setTextColor(password.length() >= 8 ? getResources().getColor(R.color.green) : getResources().getColor(R.color.greyy));

                // Check if the password has an uppercase letter
                requirementUppercase.setTextColor(Pattern.compile("[A-Z]").matcher(password).find() ? getResources().getColor(R.color.green) : getResources().getColor(R.color.greyy));

                // Check if the password has a digit
                requirementDigit.setTextColor(Pattern.compile("[0-9]").matcher(password).find() ? getResources().getColor(R.color.green) : getResources().getColor(R.color.greyy));

                // Check if the password has a special character
                requirementSpecialChar.setTextColor(Pattern.compile("[^a-zA-Z0-9]").matcher(password).find() ? getResources().getColor(R.color.green) : getResources().getColor(R.color.greyy));
            } else {
                // Hide the requirements if the user clears the password input
                requirementUppercase.setVisibility(View.GONE);
                requirementDigit.setVisibility(View.GONE);
                requirementSpecialChar.setVisibility(View.GONE);
                requirementMinLength.setVisibility(View.GONE);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {}
    };

    private void fetchLastUsedPassword() {
        new Thread(() -> {
            try {
                URL url = new URL("https://sclc.scarlet2.io/getLastUsedPassword.php?username=" + username);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                // Check the response code
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // Store the last used password
                    lastUsedPassword = response.toString().trim(); // Trim whitespace

                    // Log the last used password for debugging
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Last used password fetched: " + lastUsedPassword, Toast.LENGTH_SHORT).show();
                        Log.d("ResetPassword", "Last used password: " + lastUsedPassword); // Log it
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Error fetching password", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void resetPassword() {
        String newPassword = passwordResetEditText.getText().toString().trim();
        String confirmPassword = passwordConfirmEditText.getText().toString().trim();
        String oldPassword = oldPasswordEditText.getText().toString().trim();

        // Validate input fields
        if (TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword) || TextUtils.isEmpty(oldPassword)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the last used password is fetched
        if (lastUsedPassword == null) {
            Toast.makeText(this, "Old password not fetched. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Log the passwords for debugging
        Log.d("ResetPassword", "Old password entered: " + oldPassword);
        Log.d("ResetPassword", "Last used password: " + lastUsedPassword);

        // Proceed with resetting the password
        updatePasswordOnServer(newPassword, oldPassword);
    }

    private void updatePasswordOnServer(String newPassword, String oldPassword) {
        new Thread(() -> {
            try {
                URL url = new URL("https://sclc.scarlet2.io/verifyAndResetPassword.php"); // New PHP script for verification
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                // Send username, student_id, old password, and new password
                String postData = "username=" + username + "&student_id=" + studentId + "&new_password=" + newPassword + "&old_password=" + oldPassword;
                OutputStream os = connection.getOutputStream();
                os.write(postData.getBytes());
                os.flush();
                os.close();

                int responseCode = connection.getResponseCode();
                runOnUiThread(() -> {
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Error updating password", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
