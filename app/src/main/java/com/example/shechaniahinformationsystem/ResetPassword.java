    package com.example.shechaniahinformationsystem;
    
    import android.os.Bundle;
    import android.text.Editable;
    import android.text.TextUtils;
    import android.view.View;
    import android.widget.EditText;
    import android.widget.Toast;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.appcompat.widget.AppCompatButton;
    
    import java.io.OutputStream;
    import java.net.HttpURLConnection;
    import java.net.URL;
    
    public class ResetPassword extends AppCompatActivity {

        private EditText passwordResetEditText;
        private EditText passwordConfirmEditText;
        private AppCompatButton submitButton;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_reset_password);

            passwordResetEditText = findViewById(R.id.passwordReset);
            passwordConfirmEditText = findViewById(R.id.password1);
            submitButton = findViewById(R.id.btn1);

            // Retrieve email and token from Intent
            String email = getIntent().getStringExtra("email");
            String token = getIntent().getStringExtra("token");
        }

        private void resetPassword() {
            String newPassword = passwordResetEditText.getText().toString().trim();
            String confirmPassword = passwordConfirmEditText.getText().toString().trim();

            if (TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword)) {
                Toast.makeText(this, "Both fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // Retrieve email and token from Intent
            String email = getIntent().getStringExtra("email");
            String token = getIntent().getStringExtra("token");

            new Thread(() -> {
                try {
                    URL url = new URL("https://sclc.scarlet2.io/update_password.php"); // Your PHP script URL
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                    String postData = "email=" + email + "&new_password=" + newPassword + "&token=" + token;
                    OutputStream os = connection.getOutputStream();
                    os.write(postData.getBytes());
                    os.flush();
                    os.close();

                    int responseCode = connection.getResponseCode();
                    runOnUiThread(() -> {
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                            finish(); // Close this activity
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
