package com.example.builtdaily.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.builtdaily.R;
import com.example.builtdaily.models.Video;
import com.example.builtdaily.repository.YouTubeRepository;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    Button logoutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logoutBtn = findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(v -> {
            getSharedPreferences("auth_prefs", MODE_PRIVATE)
                    .edit()
                    .remove("logged_in_user")
                    .apply();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        });

        YouTubeRepository repo = new YouTubeRepository();
        repo.searchHIITWorkouts("medium", false, false, new Callback<List<Video>>() {
            @Override
            public void onResponse(Call<List<Video>> call, Response<List<Video>> response) {
                List<Video> videos = response.body();

                if (videos != null) {
                    for (Video video : videos) {
                        Log.d("API_TEST", "Title: " + video.title);
                        Log.d("API_TEST", "ID: " + video.videoId);
                        Log.d("API_TEST", "Thumbnail: " + video.thumbnailUrl);
                        Log.d("API_TEST", "Length: " + video.duration);
                    }
                } else {
                    Log.d("API_TEST", "No videos returned");
                }
            }

            @Override
            public void onFailure(Call<List<Video>> call, Throwable t) {
                Log.e("API_TEST", "Error: " + t.getMessage());
            }
        });
    }
}