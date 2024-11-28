package com.example.shechaniahinformationsystem;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import com.google.android.material.textfield.TextInputEditText;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

public class SignUp extends AppCompatActivity {
    private EditText usernameEditText;
    private TextInputEditText passwordEditText;
    private AppCompatButton registerButton;

    private TextView requirementUppercase;
    private TextView requirementDigit;
    private TextView requirementSpecialChar;
    private TextView requirementMinLength;  // New TextView for minimum length

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        usernameEditText = findViewById(R.id.username2);
        passwordEditText = findViewById(R.id.password2);
        registerButton = findViewById(R.id.btn1);

        requirementUppercase = findViewById(R.id.requirement_uppercase);
        requirementDigit = findViewById(R.id.requirement_digit);
        requirementSpecialChar = findViewById(R.id.requirement_special_char);
        requirementMinLength = findViewById(R.id.requirement_min_length);  // Initialize the new TextView

        // Add a TextWatcher to the password input
        passwordEditText.addTextChangedListener(passwordTextWatcher);

        registerButton.setOnClickListener(view -> registerUser());
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
                requirementMinLength.setVisibility(View.VISIBLE);  // Show minimum length requirement

                // Check if the password has at least 8 characters
                if (password.length() >= 8) {
                    requirementMinLength.setTextColor(getResources().getColor(R.color.green)); // Satisfied
                } else {
                    requirementMinLength.setTextColor(getResources().getColor(R.color.greyy)); // Not satisfied
                }

                // Check if the password has an uppercase letter
                if (Pattern.compile("[A-Z]").matcher(password).find()) {
                    requirementUppercase.setTextColor(getResources().getColor(R.color.green)); // Satisfied
                } else {
                    requirementUppercase.setTextColor(getResources().getColor(R.color.greyy)); // Not satisfied
                }

                // Check if the password has a digit
                if (Pattern.compile("[0-9]").matcher(password).find()) {
                    requirementDigit.setTextColor(getResources().getColor(R.color.green)); // Satisfied
                } else {
                    requirementDigit.setTextColor(getResources().getColor(R.color.greyy)); // Not satisfied
                }

                // Check if the password has a special character
                if (Pattern.compile("[^a-zA-Z0-9]").matcher(password).find()) {
                    requirementSpecialChar.setTextColor(getResources().getColor(R.color.green)); // Satisfied
                } else {
                    requirementSpecialChar.setTextColor(getResources().getColor(R.color.greyy)); // Not satisfied
                }

            } else {
                // Hide the requirements if the user clears the password input
                requirementUppercase.setVisibility(View.GONE);
                requirementDigit.setVisibility(View.GONE);
                requirementSpecialChar.setVisibility(View.GONE);
                requirementMinLength.setVisibility(View.GONE);  // Hide minimum length requirement
            }
        }

        @Override
        public void afterTextChanged(Editable s) {}
    };

    private void registerUser() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(SignUp.this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isPasswordValid(password)) {
            Toast.makeText(SignUp.this, "Password must meet all requirements.", Toast.LENGTH_LONG).show();
            return;
        }

        // Start the registration process in a background thread
        new Thread(() -> {
            try {
                URL url = new URL("https://sclc.scarlet2.io/registration.php");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String postData = "username=" + username + "&password=" + password;
                OutputStream os = connection.getOutputStream();
                os.write(postData.getBytes());
                os.flush();
                os.close();

                // Read the server's response
                StringBuilder response = new StringBuilder();
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                String serverResponse = response.toString();
                runOnUiThread(() -> {
                    if (serverResponse.equals("success")) {
                        Toast.makeText(SignUp.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SignUp.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(SignUp.this, "Registration Failed: " + serverResponse, Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(SignUp.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private boolean isPasswordValid(String password) {
        // Check if the password meets the following criteria:
        return password.length() >= 8 &&  // Minimum length 8 characters
                Pattern.compile("[A-Z]").matcher(password).find() &&  // At least one uppercase letter
                Pattern.compile("[0-9]").matcher(password).find() &&  // At least one digit
                Pattern.compile("[^a-zA-Z0-9]").matcher(password).find();  // At least one special character
    }
}
