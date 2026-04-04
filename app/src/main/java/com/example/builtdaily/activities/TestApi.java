package com.example.builtdaily.activities;

import android.util.Log;

import com.example.builtdaily.models.Video;
import com.example.builtdaily.repository.YouTubeRepository;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TestApi {

    public void runTest() {
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
