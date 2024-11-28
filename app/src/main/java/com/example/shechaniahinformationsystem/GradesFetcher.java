package com.example.shechaniahinformationsystem;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GradesFetcher {

    private static final String TAG = "GradesFetcher";
    private static final String URL = "https://your-server-url.com/getgrades.php?user_id=";

    public interface GradesCallback {
        void onGradesFetched(List<Grade> grades);
        void onError(String error);
    }

    public static void fetchGrades(int userId, GradesCallback callback) {
        OkHttpClient client = new OkHttpClient();

        String urlWithUserId = URL + userId;

        Request request = new Request.Builder()
                .url(urlWithUserId)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Error fetching grades", e);
                callback.onError("Failed to fetch grades: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonResponse = response.body().string();

                    // Parse the JSON using Gson
                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<Grade>>() {}.getType();
                    List<Grade> grades = gson.fromJson(jsonResponse, listType);

                    // Callback on success
                    callback.onGradesFetched(grades);
                } else {
                    callback.onError("Failed to fetch grades: " + response.message());
                }
            }
        });
    }
}
