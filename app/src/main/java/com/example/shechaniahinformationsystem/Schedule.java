package com.example.shechaniahinformationsystem;

import android.content.Intent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
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
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;

// Main Activity for displaying schedule and calendar
public class Schedule extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private RecyclerView scheduleRecyclerView, calendarRecyclerView;
    private ScheduleAdapter scheduleAdapter;
    private CalendarAdapterr calendarAdapterr;
    private ArrayList<ScheduleItemm> scheduleList = new ArrayList<>();
    private ArrayList<CalendarEventt> calendarList = new ArrayList<>();
    private String studentId;
    private Spinner schoolYearSpinner;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String selectedSchoolYear;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        // Initialize views
        drawerLayout = findViewById(R.id.schedules);
        navigationView = findViewById(R.id.scheduleNav);
        toolbar = findViewById(R.id.toolbarSchedule);
        scheduleRecyclerView = findViewById(R.id.scheduleRecyclerView);
        calendarRecyclerView = findViewById(R.id.schoolCalendarRecyclerView);
        swipeRefreshLayout = findViewById(R.id.refreshLayout);
        schoolYearSpinner = findViewById(R.id.yearSpinner);

        // Set up toolbar and navigation
        setSupportActionBar(toolbar);
        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        studentId = sharedPreferences.getString("student_id", null);

        if (studentId == null || studentId.isEmpty()) {
            Toast.makeText(this, "No Student ID found.", Toast.LENGTH_SHORT).show();
        }
        ImageView profileImage = findViewById(R.id.profileImage);
        profileImage.setOnClickListener(v -> {
            // Navigate to Profile activity
            Intent intent = new Intent(Schedule.this, info.class);
            startActivity(intent);
        });

        // Set up RecyclerViews
        scheduleRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        calendarRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        scheduleAdapter = new ScheduleAdapter(scheduleList);
        scheduleRecyclerView.setAdapter(scheduleAdapter);

        calendarAdapterr = new CalendarAdapterr(calendarList);
        calendarRecyclerView.setAdapter(calendarAdapterr);

        // Set up Spinner for School Year
        setupSchoolYearSpinner();

        // Fetch data
        fetchData();

        // Set up swipe-to-refresh
        swipeRefreshLayout.setOnRefreshListener(this::fetchData); // Fetch data on refresh
    }

    // Set up the Spinner for School Year
    private void setupSchoolYearSpinner() {
        // Initialize the ArrayList for school years from 2018 to 2030
        ArrayList<String> schoolYears = new ArrayList<>();

        // Populate the ArrayList with years
        for (int year = 2018; year <= 2030; year++) {
            schoolYears.add(String.valueOf(year));
        }

        // Set up the Spinner adapter with the list of school years
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, schoolYears);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        schoolYearSpinner.setAdapter(adapter);

        // Set a listener to handle school year selection
        schoolYearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedSchoolYear = schoolYears.get(position); // Set the selected year
                fetchData(); // Fetch data again when the school year changes
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });
    }


    private void fetchData() {
        fetchSchedule("https://sclc.scarlet2.io/getSchedule.php?type=schedule&student_id=" + studentId + "&school_year=" + selectedSchoolYear);
        fetchCalendar("https://sclc.scarlet2.io/getCalendar.php?school_year=" + selectedSchoolYear);
    }

    private void fetchSchedule(String url) {
        OkHttpClient client = new OkHttpClient();
        runOnUiThread(() -> swipeRefreshLayout.setRefreshing(true));

        client.newCall(new Request.Builder().url(url).build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Schedule", "Error fetching schedule: " + e.getMessage());
                runOnUiThread(() -> {
                    swipeRefreshLayout.setRefreshing(false); // Stop the refreshing indicator
                    Toast.makeText(Schedule.this, "Failed to fetch schedule", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    Log.e("Schedule", "Response not successful: " + response.message());
                    runOnUiThread(() -> swipeRefreshLayout.setRefreshing(false)); // Stop the refreshing indicator
                    return;
                }

                String jsonData = response.body().string();
                Log.d("Schedule", "Schedule response: " + jsonData);

                runOnUiThread(() -> {
                    try {
                        JSONArray scheduleArray = new JSONArray(jsonData);
                        scheduleList.clear(); // Clear previous data

                        for (int i = 0; i < scheduleArray.length(); i++) {
                            JSONObject classObject = scheduleArray.getJSONObject(i);
                            String subject = classObject.optString("subject", "");
                            String time = classObject.optString("time", "");
                            String days = classObject.optString("days", "");
                            scheduleList.add(new ScheduleItemm(subject, time, days));
                        }

                        // Notify the adapter of data changes
                        scheduleAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        Log.e("Schedule", "Error parsing JSON: " + e.getMessage());
                        Toast.makeText(Schedule.this, "No schedule found", Toast.LENGTH_SHORT).show();
                    } finally {
                        swipeRefreshLayout.setRefreshing(false); // Stop the refreshing indicator
                    }
                });
            }
        });
    }

    private void fetchCalendar(String url) {
        OkHttpClient client = new OkHttpClient();

        client.newCall(new Request.Builder().url(url).build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Schedule", "Error fetching calendar: " + e.getMessage());
                runOnUiThread(() -> {
                    swipeRefreshLayout.setRefreshing(false); // Stop the refreshing indicator
                    Toast.makeText(Schedule.this, "Failed to fetch calendar", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    Log.e("Schedule", "Response not successful: " + response.message());
                    runOnUiThread(() -> swipeRefreshLayout.setRefreshing(false)); // Stop the refreshing indicator
                    return;
                }

                String jsonData = response.body().string();
                Log.d("Schedule", "Calendar response: " + jsonData);

                runOnUiThread(() -> {
                    try {
                        JSONArray calendarArray = new JSONArray(jsonData);
                        calendarList.clear(); // Clear previous data

                        for (int i = 0; i < calendarArray.length(); i++) {
                            JSONObject eventObject = calendarArray.getJSONObject(i);
                            String event = eventObject.optString("event", "");
                            String date = eventObject.optString("date", "");
                            calendarList.add(new CalendarEventt(event, date));
                        }

                        // Notify the adapter of data changes
                        calendarAdapterr.notifyDataSetChanged();
                    } catch (JSONException e) {
                        Log.e("Schedule", "Error parsing JSON: " + e.getMessage());
                        Toast.makeText(Schedule.this, "Error parsing calendar data", Toast.LENGTH_SHORT).show();
                    } finally {
                        swipeRefreshLayout.setRefreshing(false); // Stop the refreshing indicator
                    }
                });
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent intent;

        if (item.getItemId() == R.id.nav_dashboard) {
            intent = new Intent(Schedule.this, DashboardActivity.class);
        } else if (item.getItemId() == R.id.nav_profile) {
            intent = new Intent(Schedule.this, Profile.class);
        } else if (item.getItemId() == R.id.logout) {
            // Call the logout method
            logout();
            return true; // Exit method after logout
        } else {
            intent = null;
        }

        if (intent != null) {
            new Handler().postDelayed(() -> {
                startActivity(intent);
                finish();
            }, 1000);
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    // Add the logout method
    private void logout() {
        SharedPreferences preferences = getSharedPreferences("student_session", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();  // Clear all stored session data
        editor.apply();

        // Redirect to Login Activity
        Intent intent = new Intent(Schedule.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);  // Clear the activity stack
        startActivity(intent);
        finish();  // Close the current activity
    }
}

    // Schedule Adapter class
class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {
    private ArrayList<ScheduleItemm> scheduleItemms;

    public ScheduleAdapter(ArrayList<ScheduleItemm> scheduleItemms) {
        this.scheduleItemms = scheduleItemms;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScheduleItemm item = scheduleItemms.get(position);
        holder.subjectTextView.setText(item.getSubject());
        holder.timeTextView.setText(item.getTime());
        holder.daysTextView.setText(item.getDays());
    }

    @Override
    public int getItemCount() {
        return scheduleItemms.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView subjectTextView;
        public TextView timeTextView;
        public TextView daysTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            subjectTextView = itemView.findViewById(R.id.scheduleSubject);
            timeTextView = itemView.findViewById(R.id.scheduleTime);
            daysTextView = itemView.findViewById(R.id.scheduleDays);
        }
    }
}

// Class representing a single schedule item
class ScheduleItemm {
    private String subject;
    private String time;
    private String days;

    public ScheduleItemm(String subject, String time, String days) {
        this.subject = subject;
        this.time = time;
        this.days = days;
    }

    public String getSubject() {
        return subject;
    }

    public String getTime() {
        return time;
    }

    public String getDays() {
        return days;
    }
}

// Class representing a calendar event
class CalendarEventt {
    private String event;
    private String date;

    public CalendarEventt(String event, String date) {
        this.event = event;
        this.date = date;
    }

    public String getEvent() {
        return event;
    }

    public String getDate() {
        return date;
    }
}

// Calendar Adapter class (you can implement this similarly to ScheduleAdapter)
class CalendarAdapterr extends RecyclerView.Adapter<CalendarAdapterr.ViewHolder> {
    private ArrayList<CalendarEventt> calendarEventts;

    public CalendarAdapterr(ArrayList<CalendarEventt> calendarEventts) {
        this.calendarEventts = calendarEventts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CalendarEventt event = calendarEventts.get(position);
        holder.eventTextView.setText(event.getEvent());

        // Original date string from the event object
        String originalDate = event.getDate();

        // Parse and format the date
        SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()); // Assuming the format in the database is "yyyy-MM-dd"
        SimpleDateFormat desiredFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()); // Desired format "January 08, 2024"

        try {
            // Convert the original date to a Date object
            Date date = originalFormat.parse(originalDate);
            // Format the date into the desired format
            String formattedDate = desiredFormat.format(date);
            // Set the formatted date to the TextView
            holder.dateTextView.setText(formattedDate);
        } catch (ParseException e) {
            e.printStackTrace();
            // If parsing fails, show the original date
            holder.dateTextView.setText(originalDate);
        }
    }

    @Override
    public int getItemCount() {
        return calendarEventts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView eventTextView;
        public TextView dateTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            eventTextView = itemView.findViewById(R.id.calendarEvent);
            dateTextView = itemView.findViewById(R.id.calendarDate);
        }
    }
}
