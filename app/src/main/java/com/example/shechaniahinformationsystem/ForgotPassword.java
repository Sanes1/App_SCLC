package com.example.shechaniahinformationsystem;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

public class ForgotPassword extends AppCompatActivity {
    private EditText emailEditText;
    private AppCompatButton resetButton;
    private EditText tokenInput;
    private AppCompatButton submitTokenButton;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        emailEditText = findViewById(R.id.forgot1);
        resetButton = findViewById(R.id.btn1);
        tokenInput = findViewById(R.id.token1);
        submitTokenButton = findViewById(R.id.btnreset);

        // Set an input filter to restrict token input to digits only
        tokenInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)}); // Optional: limit length to 6
        tokenInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Show submitTokenButton if tokenInput has text
                if (s.toString().trim().isEmpty()) {
                    submitTokenButton.setVisibility(View.GONE);
                } else {
                    submitTokenButton.setVisibility(View.VISIBLE);
                }
            }
        });

        resetButton.setOnClickListener(view -> sendPasswordResetRequest());

        // Handle token submission
        submitTokenButton.setOnClickListener(view -> {
            String token = tokenInput.getText().toString().trim();
            if (!token.matches("\\d+")) { // Ensure token is numeric
                Toast.makeText(ForgotPassword.this, "Token must be numeric", Toast.LENGTH_SHORT).show();
                return;
            }
            // Proceed with token submission logic
            // ...
        });
    }

    // ... (Your existing ForgotPassword.java code)

    private void sendPasswordResetRequest() {
        String email = emailEditText.getText().toString().trim();

        if (email.isEmpty() || !EMAIL_PATTERN.matcher(email).matches()) {
            Toast.makeText(ForgotPassword.this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                URL url = new URL("https://sclc.scarlet2.io/forgotpassword.php");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String postData = "email=" + email;
                OutputStream os = connection.getOutputStream();
                os.write(postData.getBytes());
                os.flush();
                os.close();

                int responseCode = connection.getResponseCode();
                runOnUiThread(() -> {
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        Toast.makeText(ForgotPassword.this, "Password reset request sent successfully", Toast.LENGTH_SHORT).show();

                        // Get the token from the response (you'll need to modify your PHP script to return the token)
                        String token = connection.getHeaderField("token");

                        // Start ResetPassword activity and pass the email and token
                        Intent intent = new Intent(ForgotPassword.this, ResetPassword.class);
                        intent.putExtra("email", email);
                        intent.putExtra("token", token);
                        startActivity(intent);
                        finish(); // Close ForgotPassword activity
                    } else {
                        Toast.makeText(ForgotPassword.this, "Error sending request", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(ForgotPassword.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}