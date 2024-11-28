package com.example.shechaniahinformationsystem;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ForgotPassword extends AppCompatActivity {
    private EditText emailEditText;
    private AppCompatButton resetButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        emailEditText = findViewById(R.id.forgot1);
        resetButton = findViewById(R.id.btn1);

        resetButton.setOnClickListener(view -> sendPasswordResetRequest());
    }

    private void sendPasswordResetRequest() {
        String username = emailEditText.getText().toString().trim();

        if (username.isEmpty()) {
            Toast.makeText(ForgotPassword.this, "Please enter a valid username", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                URL url = new URL("https://sclc.scarlet2.io/forgotpassword.php?username=" + username);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                Log.d("ForgotPassword", "Response Code: " + responseCode);

                // Read the response
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Log the full response for debugging
                String jsonResponse = response.toString();
                Log.d("ForgotPassword", "Response: " + jsonResponse);

                // Check if the response is valid JSON
                if (jsonResponse.startsWith("{") && jsonResponse.endsWith("}")) {
                    JSONObject jsonObject = new JSONObject(jsonResponse);
                    boolean success = jsonObject.getBoolean("success");

                    runOnUiThread(() -> {
                        if (success) {
                            // Get the student_id from the response
                            String studentId = null;
                            try {
                                studentId = jsonObject.getString("student_id");
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                            // Navigate to ResetPassword activity with username and student ID
                            Intent intent = new Intent(ForgotPassword.this, ResetPassword.class);
                            intent.putExtra("username", username);
                            intent.putExtra("student_id", studentId);
                            startActivity(intent);
                            finish();
                        } else {
                            String message = null;
                            try {
                                message = jsonObject.getString("message");
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                            Toast.makeText(ForgotPassword.this, message, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(ForgotPassword.this, "Invalid response from server.", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                Log.e("ForgotPassword", "Error: " + e.getMessage(), e);
                runOnUiThread(() -> Toast.makeText(ForgotPassword.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
