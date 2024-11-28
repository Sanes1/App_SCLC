package com.example.shechaniahinformationsystem;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.view.WindowCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.content.SharedPreferences;

import com.google.android.material.navigation.NavigationView;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    RecyclerView announcementsRecyclerView;
    AnnouncementAdapter announcementAdapter;
    List<Announcement> announcements;
    SwipeRefreshLayout swipeRefreshLayout;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboardactivity);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);

        // Initialize views
        drawerLayout = findViewById(R.id.dashboards);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbarDash);
        announcementsRecyclerView = findViewById(R.id.AnnounceRecyclerView);
        swipeRefreshLayout = findViewById(R.id.refreshLayout);
        progressBar = findViewById(R.id.progress); // Initialize the ProgressBar

        setSupportActionBar(toolbar);
        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        // Initialize the profile ImageView and set an onClickListener
        ImageView profileImage = findViewById(R.id.profileImage);
        profileImage.setOnClickListener(v -> {
            // Navigate to Profile activity
            Intent intent = new Intent(DashboardActivity.this, info.class);
            startActivity(intent);
        });

        // Initialize announcement list and adapter
        announcements = new ArrayList<>();
        announcementAdapter = new AnnouncementAdapter(announcements);
        announcementsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        announcementsRecyclerView.setAdapter(announcementAdapter);

        // Set up swipe refresh
        swipeRefreshLayout.setOnRefreshListener(this::fetchDataFromServer);

        // Initial data fetch
        fetchDataFromServer();
    }
    private void fetchDataFromServer() {
        OkHttpClient client = new OkHttpClient();

        // Show progress bar at the start of data fetching
        runOnUiThread(() -> {
            progressBar.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setRefreshing(true); // Ensure swipe refresh is not spinning
        });

        Request request = new Request.Builder()
                .url("https://sclc.scarlet2.io/fetch_data.php")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("DashboardActivity", "Error fetching data: " + e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(DashboardActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
                    // Hide progress bar in case of failure
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false); // Stop swipe refresh if active
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonData = response.body().string();
                    Log.d("DashboardActivity", "Raw JSON response: " + jsonData);

                    runOnUiThread(() -> {
                        parseJsonData(jsonData);  // Parse and load data on UI thread
                        // Hide progress bar after data is loaded
                        progressBar.setVisibility(View.GONE);
                        swipeRefreshLayout.setRefreshing(false);  // Ensure swipe refresh stops
                    });
                } else {
                    Log.e("DashboardActivity", "Response not successful: " + response.message());
                    runOnUiThread(() -> {
                        Toast.makeText(DashboardActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show();
                        // Hide progress bar if response fails
                        progressBar.setVisibility(View.GONE);
                        swipeRefreshLayout.setRefreshing(false); // Ensure swipe refresh stops
                    });
                }
            }
        });
    }

    private void parseJsonData(String jsonData) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);

            if (jsonObject.has("announcements")) {
                JSONArray announcementsArray = jsonObject.getJSONArray("announcements");

                announcements.clear(); // Clear existing data

                for (int i = 0; i < announcementsArray.length(); i++) {
                    JSONObject announcementJson = announcementsArray.getJSONObject(i);
                    String title = announcementJson.optString("title", "No title");
                    String content = announcementJson.optString("content", "No content");
                    String rawDate = announcementJson.optString("created_at", "");

                    List<AnnouncementContent> contentList = new ArrayList<>();
                    if (announcementJson.has("content_items")) {
                        JSONArray contentItemsArray = announcementJson.getJSONArray("content_items");
                        for (int j = 0; j < contentItemsArray.length(); j++) {
                            String contentItem = contentItemsArray.getString(j);
                            contentList.add(new AnnouncementContent(contentItem));
                        }
                    }

                    // Format the date before adding the announcement
                    String formattedDate = formatDate(rawDate);
                    announcements.add(new Announcement(title, content, formattedDate, contentList));
                }

                announcementAdapter.notifyDataSetChanged();  // Notify adapter for UI update
            }
        } catch (JSONException e) {
            Log.e("DashboardActivity", "Error parsing JSON: " + e.getMessage());
        } finally {
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);  // Ensure progress bar is hidden
                swipeRefreshLayout.setRefreshing(false);  // Stop swipe refresh animation
            });
        }
    }

    // Helper method to format date
    private String formatDate(String rawDate) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()); // Include time
            Date date = inputFormat.parse(rawDate);

            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
            return outputFormat.format(date);
        } catch (ParseException e) {
            Log.e("DashboardActivity", "Date parsing error: " + e.getMessage());
            return rawDate; // Return raw date if parsing fails
        }
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent intent;

        if (item.getItemId() == R.id.nav_dashboard) {
            intent = null;
            // Keep the background and text color for the dashboard
            item.setChecked(true);
        } else {
            // Change color when other items are selected
            item.setChecked(false);

           if (item.getItemId() == R.id.nav_profile) {
                intent = new Intent(DashboardActivity.this, Profile.class);
            } else if (item.getItemId() == R.id.nav_schedule) {
                intent = new Intent(DashboardActivity.this, Schedule.class);
            } else {
                intent = null;
                if (item.getItemId() == R.id.logout) {
                    // Call logout method
                    logout();
                    return true;
                }
            }
        }

        // Delayed transition to the new activity
        if (intent != null) {
            new Handler().postDelayed(() -> {
                startActivity(intent);
                finish(); // Finish the current activity
            }, 1000);
        }

        // Close the navigation drawer after selection
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }


    // Add the logout method
    private void logout() {
        // Clear SharedPreferences (assuming user ID is saved in SharedPreferences)
        SharedPreferences preferences = getSharedPreferences("user_session", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();  // Clear all stored session data
        editor.apply();

        // Redirect to Login Activity
        Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);  // Clear the activity stack
        startActivity(intent);
        finish();  // Close the current activity
    }


    // Announcement class
    public static class Announcement {
        private String title;
        private String content;
        private String createdAt;
        private List<AnnouncementContent> contentList;

        public Announcement(String title, String content, String createdAt, List<AnnouncementContent> contentList) {
            this.title = title;
            this.content = content;
            this.createdAt = createdAt;
            this.contentList = contentList;
        }

        public String getTitle() {
            return title;
        }

        public String getContent() {
            return content;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public List<AnnouncementContent> getContentList() {
            return contentList;
        }
    }


    // AnnouncementContent class
    public static class AnnouncementContent {
        private String text;

        public AnnouncementContent(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }

    // Adapter for RecyclerView (Announcements)
    public static class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.AnnouncementViewHolder> {
        private List<Announcement> announcements;

        public AnnouncementAdapter(List<Announcement> announcements) {
            this.announcements = announcements;
        }

        @NonNull
        @Override
        public AnnouncementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.announcement_item_with_recycler, parent, false);
            return new AnnouncementViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AnnouncementViewHolder holder, int position) {
            Announcement announcement = announcements.get(position);
            holder.titleTextView.setText(announcement.getTitle());
            holder.contentTextView.setText(announcement.getContent());
            holder.createdAtTextView.setText(announcement.getCreatedAt()); // Set the created_at text

            // Set up nested RecyclerView
            AnnouncementContentAdapter contentAdapter = new AnnouncementContentAdapter(announcement.getContentList());
            holder.announcementContentRecyclerView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
            holder.announcementContentRecyclerView.setAdapter(contentAdapter);
        }


        @Override
        public int getItemCount() {
            return announcements.size();
        }

        static class AnnouncementViewHolder extends RecyclerView.ViewHolder {
            TextView titleTextView;
            TextView contentTextView;
            TextView createdAtTextView;
            RecyclerView announcementContentRecyclerView;

            public AnnouncementViewHolder(@NonNull View itemView) {
                super(itemView);
                titleTextView = itemView.findViewById(R.id.announcementTitleTextView);
                contentTextView = itemView.findViewById(R.id.announcementContentTextView);
                createdAtTextView = itemView.findViewById(R.id.announcementCreatedAtTextView);
                announcementContentRecyclerView = itemView.findViewById(R.id.announcementContentRecyclerView);
            }
        }
    }


    public static class AnnouncementContentAdapter extends RecyclerView.Adapter<AnnouncementContentAdapter.ContentViewHolder> {
        private List<AnnouncementContent> contents;

        public AnnouncementContentAdapter(List<AnnouncementContent> contents) {
            this.contents = contents;
        }

        @NonNull
        @Override
        public ContentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.content_item, parent, false);
            return new ContentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ContentViewHolder holder, int position) {
            AnnouncementContent content = contents.get(position);
            holder.contentTextView.setText(content.getText());
        }

        @Override
        public int getItemCount() {
            return contents.size();
        }

        static class ContentViewHolder extends RecyclerView.ViewHolder {
            TextView contentTextView;

            public ContentViewHolder(@NonNull View itemView) {
                super(itemView);
                contentTextView = itemView.findViewById(R.id.contentTextView);
            }
        }
    }
}