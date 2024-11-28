package com.example.shechaniahinformationsystem;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.text.TextWatcher;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class info extends AppCompatActivity {

    private TextView nameTextView, gradeSectionTextView;
    private TextInputEditText newPasswordEditText, confirmPasswordEditText;
    private Button submitButton;
    private String studentId;
    private OkHttpClient client;

    // TextViews for password requirements
    private TextView requirementUppercase, requirementDigit, requirementSpecialChar, requirementMinLength;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_info);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);

        // Initialize UI elements
        nameTextView = findViewById(R.id.textViewBlankUnderName);
        gradeSectionTextView = findViewById(R.id.textViewBlank);
        newPasswordEditText = findViewById(R.id.newPassword);
        confirmPasswordEditText = findViewById(R.id.confirmPassword);
        submitButton = findViewById(R.id.submit_button);

        // Initialize password requirement TextViews
        requirementUppercase = findViewById(R.id.requirement_uppercase);
        requirementDigit = findViewById(R.id.requirement_digit);
        requirementSpecialChar = findViewById(R.id.requirement_special_char);
        requirementMinLength = findViewById(R.id.requirement_minimum_length);

        // Initialize OkHttpClient
        client = new OkHttpClient();

        // Retrieve student ID from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        studentId = sharedPreferences.getString("student_id", null);

        if (studentId != null) {
            Log.d("info", "Successfully retrieved student ID from SharedPreferences: " + studentId);
            fetchProfileData(studentId);
        } else {
            Log.e("info", "Student ID is not found in SharedPreferences");
            Toast.makeText(this, "Student ID not found", Toast.LENGTH_SHORT).show();
        }

        // Add TextWatcher for password validation
        newPasswordEditText.addTextChangedListener(passwordTextWatcher);

        // Set up button listener for password submission
        submitButton.setOnClickListener(view -> {
            String newPassword = newPasswordEditText.getText().toString();
            String confirmPassword = confirmPasswordEditText.getText().toString();

            if (validatePassword(newPassword, confirmPassword)) {
                updatePassword(studentId, newPassword); // Use studentId directly here
            }
        });
    }

    // TextWatcher for password field to display live password validation
    private final TextWatcher passwordTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String password = s.toString();

            // Show password requirements when user starts typing
            if (!password.isEmpty()) {
                requirementUppercase.setVisibility(View.VISIBLE);
                requirementDigit.setVisibility(View.VISIBLE);
                requirementSpecialChar.setVisibility(View.VISIBLE);
                requirementMinLength.setVisibility(View.VISIBLE);

                // Check for minimum length
                requirementMinLength.setTextColor(password.length() >= 8 ?
                        getResources().getColor(R.color.green) :
                        getResources().getColor(R.color.greyy));

                // Check for uppercase letter
                requirementUppercase.setTextColor(Pattern.compile("[A-Z]").matcher(password).find() ?
                        getResources().getColor(R.color.green) :
                        getResources().getColor(R.color.greyy));

                // Check for digit
                requirementDigit.setTextColor(Pattern.compile("[0-9]").matcher(password).find() ?
                        getResources().getColor(R.color.green) :
                        getResources().getColor(R.color.greyy));

                // Check for special character
                requirementSpecialChar.setTextColor(Pattern.compile("[^a-zA-Z0-9]").matcher(password).find() ?
                        getResources().getColor(R.color.green) :
                        getResources().getColor(R.color.greyy));
            } else {
                // Hide requirements if password field is empty
                requirementUppercase.setVisibility(View.GONE);
                requirementDigit.setVisibility(View.GONE);
                requirementSpecialChar.setVisibility(View.GONE);
                requirementMinLength.setVisibility(View.GONE);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {}
    };

    // Fetch profile data using student ID
    private void fetchProfileData(String studentId) {
        String url = "https://sclc.scarlet2.io/fetchProfile.php?student_id=" + studentId;
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("info", "Failed to fetch profile data: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(info.this, "Error fetching profile data", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String jsonData = response.body().string();
                        Log.d("info", "Profile Response: " + jsonData);

                        JSONObject jsonResponse = new JSONObject(jsonData);
                        boolean success = jsonResponse.getBoolean("success");

                        if (success) {
                            if (jsonResponse.has("name") && jsonResponse.has("section_year_level")) {
                                String name = jsonResponse.getString("name");
                                String sectionYearLevel = jsonResponse.getString("section_year_level");

                                Log.d("info", "Fetched Name: " + name);
                                Log.d("info", "Fetched Section/Year Level: " + sectionYearLevel);

                                runOnUiThread(() -> {
                                    nameTextView.setText(name);
                                    gradeSectionTextView.setText(sectionYearLevel);
                                });
                            } else {
                                Log.e("info", "Required keys 'name' and 'section_year_level' missing from response.");
                                runOnUiThread(() -> Toast.makeText(info.this, "Unexpected response structure.", Toast.LENGTH_SHORT).show());
                            }
                        } else {
                            Log.e("info", "Profile fetch was unsuccessful.");
                            runOnUiThread(() -> Toast.makeText(info.this, jsonResponse.optString("message", "Failed to fetch data"), Toast.LENGTH_SHORT).show());
                        }
                    } catch (JSONException e) {
                        Log.e("info", "JSON Parsing error: " + e.getMessage());
                        runOnUiThread(() -> Toast.makeText(info.this, "Error parsing data. Please try again.", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    Log.e("info", "Response was unsuccessful. Code: " + response.code());
                    runOnUiThread(() -> Toast.makeText(info.this, "Failed to fetch profile data: " + response.code(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    // Validate password requirements
    private boolean validatePassword(String newPassword, String confirmPassword) {
        if (newPassword.length() < 8) {
            Toast.makeText(this, "Password must be at least 8 characters long", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!newPassword.matches(".*[A-Z].*")) {
            Toast.makeText(this, "Password must contain at least one uppercase letter", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!newPassword.matches(".*\\d.*")) {
            Toast.makeText(this, "Password must contain at least one digit", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!newPassword.matches(".*[!@#$%^&*+=?-_].*")) {
            Toast.makeText(this, "Password must contain at least one special character", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    // Update password using student ID
    private void updatePassword(String studentId, String newPassword) {
        String url = "https://sclc.scarlet2.io/updatePassword.php";
        FormBody body = new FormBody.Builder()
                .add("student_id", studentId)  // Use the existing student_id
                .add("new_password", newPassword)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("info", "Failed to reset password: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(info.this, "Error resetting password", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d("info", "Password successfully updated");
                    runOnUiThread(() -> Toast.makeText(info.this, "Password successfully updated", Toast.LENGTH_SHORT).show());
                } else {
                    Log.e("info", "Failed to update password: " + response.code());
                    runOnUiThread(() -> Toast.makeText(info.this, "Failed to update password", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}
