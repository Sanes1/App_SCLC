package com.example.shechaniahinformationsystem;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Profile extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    SwipeRefreshLayout swipeRefreshLayout; // Added SwipeRefreshLayout

    // UI elements to display profile data
    EditText editTextText;
    EditText textBirthdate;
    EditText textAddress;
    EditText textNationality;
    EditText textContact;
    EditText editTextFatherName;
    EditText editTextFatherOccupation;
    EditText editTextFatherContactNum;
    EditText editTextMotherName;
    EditText editTextMotherOccupation;
    EditText editTextMotherContactNum;
    EditText editTextEmergencyName;
    EditText editTextEmergencyRelationship;
    EditText editTextEmergencyContactNumber;
    EditText editTextEmergencyHomeAddress;
    EditText textSection;

    private String studentId;
    private ImageView profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Retrieve student_id from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        studentId = sharedPreferences.getString("student_id", null);

        Log.d("ProfileActivity", "Retrieved student_id: " + studentId);

        // Initialize views
        drawerLayout = findViewById(R.id.profile);
        navigationView = findViewById(R.id.nav_profiles);
        toolbar = findViewById(R.id.toolbarProfile);
        swipeRefreshLayout = findViewById(R.id.refreshLayout); // Initialize SwipeRefreshLayout

        // Initialize UI elements
        editTextText = findViewById(R.id.editTextText);
        textBirthdate = findViewById(R.id.textBirthdate);
        textAddress = findViewById(R.id.textAddress);
        textNationality = findViewById(R.id.textNationality);
        textContact = findViewById(R.id.textContact);
        editTextFatherName = findViewById(R.id.editTextFatherName);
        editTextFatherOccupation = findViewById(R.id.editTextfatherOccupations);
        editTextFatherContactNum = findViewById(R.id.editTextfatherContactNum);
        editTextMotherName = findViewById(R.id.editTextMotherName);
        editTextMotherOccupation = findViewById(R.id.editTextMotherOccupation);
        editTextMotherContactNum = findViewById(R.id.editTextMotherContactNum);
        editTextEmergencyName = findViewById(R.id.editTextEmergencyName);
        editTextEmergencyRelationship = findViewById(R.id.editTextEmergencyRelationship);
        editTextEmergencyContactNumber = findViewById(R.id.editTextEmergencyContactNumber);
        editTextEmergencyHomeAddress = findViewById(R.id.editTextEmergencyHomeAddress);
        textSection = findViewById(R.id.textSection);

        setSupportActionBar(toolbar);
        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        // Setup SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(() -> {
            Log.d("ProfileActivity", "Refreshing profile data...");
            if (studentId != null) {
                fetchDataFromServer("https://sclc.scarlet2.io/fetch_profiles.php?type=profiles&student_id=" + studentId);
            } else {
                Toast.makeText(this, "Student ID is null. Please log in again.", Toast.LENGTH_SHORT).show();
                Log.d("ProfileActivity", "Student ID is null. Please log in again.");
                swipeRefreshLayout.setRefreshing(false); // Stop refreshing animation
            }
        });
        ImageView profileImage = findViewById(R.id.profileImage);
        profileImage.setOnClickListener(v -> {
            // Navigate to Profile activity
            Intent intent = new Intent(Profile.this, info.class);
            startActivity(intent);
        });


        // Fetch data from server
        if (studentId != null) {
            fetchDataFromServer("https://sclc.scarlet2.io/fetch_profiles.php?type=profiles&student_id=" + studentId);
        } else {
            Toast.makeText(this, "Student ID is null. Please log in again.", Toast.LENGTH_SHORT).show();
            Log.d("ProfileActivity", "Student ID is null. Please log in again.");
        }
    }

    private void fetchDataFromServer(String url) {
        OkHttpClient client = new OkHttpClient();

        runOnUiThread(() -> {
            swipeRefreshLayout.setRefreshing(true);
        });
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("ProfileActivity", "Error fetching data: " + e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(Profile.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false); // Stop refreshing animation
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    Log.d("ProfileActivity", "Response Data: " + responseData); // Log the raw response
                    try {
                        JSONArray jsonArray = new JSONArray(responseData);
                        runOnUiThread(() -> updateUIWithProfileData(jsonArray));
                    } catch (JSONException e) {
                        Log.e("ProfileActivity", "JSON Parsing error: " + e.getMessage());
                        runOnUiThread(() -> {
                            Toast.makeText(Profile.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                            swipeRefreshLayout.setRefreshing(false); // Stop refreshing animation
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(Profile.this, "Response not successful", Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false); // Stop refreshing animation
                    });
                }
            }
        });
    }

    private void updateUIWithProfileData(JSONArray jsonArray) {
        try {
            if (jsonArray.length() > 0) {
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                try {
                    // Set the student's name in the editTextText
                    editTextText.setText(jsonObject.getString("name"));
                    // Set other profile data
                    textBirthdate.setText(jsonObject.getString("birthdate"));
                    textAddress.setText(jsonObject.getString("address"));
                    textNationality.setText(jsonObject.getString("nationality"));
                    textContact.setText(jsonObject.getString("contact"));
                    editTextFatherName.setText(jsonObject.getString("father_name"));
                    editTextFatherOccupation.setText(jsonObject.getString("father_occupation"));
                    editTextFatherContactNum.setText(jsonObject.getString("father_contact_num"));
                    editTextMotherName.setText(jsonObject.getString("mother_name"));
                    editTextMotherOccupation.setText(jsonObject.getString("mother_occupation"));
                    editTextMotherContactNum.setText(jsonObject.getString("mother_contact_num"));
                    editTextEmergencyName.setText(jsonObject.getString("emergency_name"));
                    editTextEmergencyRelationship.setText(jsonObject.getString("emergency_relationship"));
                    editTextEmergencyContactNumber.setText(jsonObject.getString("emergency_contact_number"));
                    editTextEmergencyHomeAddress.setText(jsonObject.getString("emergency_home_address"));
                    textSection.setText(jsonObject.getString("section_year_level"));

                } catch (JSONException e) {
                    Log.e("ProfileActivity", "Error updating UI: " + e.getMessage());
                    Toast.makeText(this, "Error updating UI: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e("ProfileActivity", "No profiles found for student_id: " + studentId);
                runOnUiThread(() -> {
                    Toast.makeText(Profile.this, "No profile data found", Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false); // Stop refreshing animation
                });
            }
        } catch (JSONException e) {
            Log.e("ProfileActivity", "JSON Error: " + e.getMessage());
            Toast.makeText(this, "Error processing profile data", Toast.LENGTH_SHORT).show();
        }

        swipeRefreshLayout.setRefreshing(false); // Stop refreshing animation
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_profile) {
            // Already in Profile activity, do nothing
        } else if (item.getItemId() == R.id.nav_schedule) {
            startActivity(new Intent(Profile.this, Schedule.class));
        } else if (item.getItemId() == R.id.logout) {
            startActivity(new Intent(Profile.this, MainActivity.class));
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
