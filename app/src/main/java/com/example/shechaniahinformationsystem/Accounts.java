package com.example.shechaniahinformationsystem;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Accounts extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    private RecyclerView gradesRecyclerView;
    private GradesAdapter gradesAdapter;
    private NavigationView navigationView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Spinner dateSpinner;
    private ImageView profileImage;

    private OkHttpClient client;
    private List<Grade> gradesData = new ArrayList<>();
    private String studentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accounts);

        drawerLayout = findViewById(R.id.account);
        navigationView = findViewById(R.id.nav_accounts);
        Toolbar toolbar = findViewById(R.id.toolbarAccount);

        gradesRecyclerView = findViewById(R.id.grades1RecyclerView);
        gradesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        swipeRefreshLayout = findViewById(R.id.refreshLayout);

        dateSpinner = findViewById(R.id.dateSpinner);
        setupYearSpinner();

        setSupportActionBar(toolbar);
        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        client = new OkHttpClient();

        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        studentId = sharedPreferences.getString("student_id", null);

        Log.d("AccountsActivity", "Retrieved Student ID: " + studentId);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            Log.d("AccountsActivity", "Refreshing grades data...");
            if (studentId != null) {
                fetchGrades(getSelectedYearUrl());
            } else {
                Log.e("Accounts", "Student ID is null. Cannot refresh.");
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        gradesAdapter = new GradesAdapter(gradesData);
        gradesRecyclerView.setAdapter(gradesAdapter);

        // Set up click listener for the profile image in the toolbar
        ImageView profileImage = findViewById(R.id.profileImage);
        profileImage.setOnClickListener(v -> {
            // Navigate to Profile activity
            Intent intent = new Intent(Accounts.this, info.class);
            startActivity(intent);
        });
    }

    private void setupYearSpinner() {
        List<String> grades = new ArrayList<>();
        grades.add("Grade 1");
        grades.add("Grade 2");
        grades.add("Grade 3");
        grades.add("Grade 4");
        grades.add("Grade 5");
        grades.add("Grade 6");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, grades);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dateSpinner.setAdapter(adapter);

        dateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedYearLevel = parent.getItemAtPosition(position).toString();
                Log.d("Accounts", "Selected Year Level: " + selectedYearLevel);
                Toast.makeText(Accounts.this, "Year Level selected: " + selectedYearLevel, Toast.LENGTH_SHORT).show();

                gradesData.clear();
                gradesAdapter.notifyDataSetChanged();

                if (studentId != null) {
                    fetchGrades(getSelectedYearUrl());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private String getSelectedYearUrl() {
        String selectedYearLevel = dateSpinner.getSelectedItem().toString();
        return "https://sclc.scarlet2.io/getGrades.php?type=grades&student_id=" + studentId + "&year_level=" + selectedYearLevel;
    }

    private void fetchGrades(String url) {
        runOnUiThread(() -> swipeRefreshLayout.setRefreshing(true));
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("Accounts", "Failed to fetch grades: " + e.getMessage());
                runOnUiThread(() -> swipeRefreshLayout.setRefreshing(false));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String jsonData = response.body().string();
                        Log.d("Accounts", "Grades Response: " + jsonData);

                        JSONObject jsonObject = new JSONObject(jsonData);
                        boolean success = jsonObject.getBoolean("success");

                        if (success) {
                            gradesData.clear();
                            JSONArray jsonArray = jsonObject.getJSONArray("grades");

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject gradeObject = jsonArray.getJSONObject(i);
                                String subject = gradeObject.getString("subject");
                                String g1 = gradeObject.getString("g1");
                                String g2 = gradeObject.getString("g2");
                                String g3 = gradeObject.getString("g3");
                                String g4 = gradeObject.getString("g4");
                                String finalGrade = gradeObject.getString("final_grade");

                                Grade gradeItem = new Grade(subject, g1, g2, g3, g4, finalGrade);
                                gradesData.add(gradeItem);
                            }

                            runOnUiThread(() -> {
                                gradesAdapter.notifyDataSetChanged();
                                if (gradesData.isEmpty()) {
                                    Toast.makeText(Accounts.this, "No grades found for the selected year level.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Log.e("Accounts", "Failed to fetch grades.");
                        }
                    } catch (JSONException e) {
                        Log.e("Accounts", "Error parsing JSON: " + e.getMessage());
                    }
                } else {
                    Log.e("Accounts", "Unsuccessful response: " + response.code());
                }

                runOnUiThread(() -> swipeRefreshLayout.setRefreshing(false));
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Intent intent = null;

        if (id == R.id.nav_accounts) {
            intent = new Intent(Accounts.this, Accounts.class);
        } else if (id == R.id.nav_dashboard) {
            intent = new Intent(Accounts.this, DashboardActivity.class);
        } else if (id == R.id.nav_profile) {
            intent = new Intent(Accounts.this, Profile.class);
        } else if (id == R.id.nav_schedule) {
            intent = new Intent(Accounts.this, Schedule.class);
        } else if (id == R.id.logout) {
            logout();
            return true;
        }

        if (intent != null) {
            startActivity(intent);
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logout() {
        SharedPreferences preferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();

        Toast.makeText(this, "Logged out successfully.", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Accounts.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
