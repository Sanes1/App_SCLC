package com.example.shechaniahinformationsystem;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private EditText usernameEditText;
    private EditText passwordEditText;
    private AppCompatButton loginButton;
    private TextView forgotPasswordTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usernameEditText = findViewById(R.id.username1);
        passwordEditText = findViewById(R.id.password1);
        loginButton = findViewById(R.id.btn1);
        forgotPasswordTextView = findViewById(R.id.textView);

        loginButton.setOnClickListener(view -> loginUser());
        forgotPasswordTextView.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, ForgotPassword.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String usernameInput = usernameEditText.getText().toString().trim();
        String passwordInput = passwordEditText.getText().toString().trim();

        // Input validation
        if (usernameInput.isEmpty() || passwordInput.isEmpty()) {
            Toast.makeText(MainActivity.this, "Username and password cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                URL url = new URL("https://sclc.scarlet2.io/login.php"); // Your API endpoint
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                // Send username and password
                String postData = "login=" + usernameInput + "&password=" + passwordInput;
                OutputStream os = connection.getOutputStream();
                os.write(postData.getBytes());
                os.flush();
                os.close();

                int responseCode = connection.getResponseCode();
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                runOnUiThread(() -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response.toString());

                        if (responseCode == HttpURLConnection.HTTP_OK && jsonResponse.getBoolean("success")) {
                            // Get student_id from response
                            String studentId = jsonResponse.getString("student_id");

                            // Save student_id in SharedPreferences
                            saveStudentId(studentId);

                            Log.d("MainActivity", "Student ID stored: " + studentId); // Log the stored student_id

                            Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
                            startActivity(intent);
                            finish(); // Optional: finish the login activity
                        } else {
                            // Handle specific error messages
                            String message = jsonResponse.getString("message");
                            showErrorMessage(message);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "An error occurred while processing the response.", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                // Handle connection errors and show appropriate error message
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Login failed. Please try again.", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void saveStudentId(String studentId) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("student_id", studentId);  // Save student ID
        editor.apply(); // Apply the changes
    }

    private void showErrorMessage(String message) {
        switch (message.toLowerCase()) {
            case "incorrect username":
                Toast.makeText(MainActivity.this, "Incorrect username", Toast.LENGTH_SHORT).show();
                break;
            case "incorrect password":
                Toast.makeText(MainActivity.this, "Incorrect password", Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(MainActivity.this, "Login failed: " + message, Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
