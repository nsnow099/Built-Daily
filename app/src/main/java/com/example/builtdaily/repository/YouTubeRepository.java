package com.example.builtdaily.repository;

import android.util.Log;

import com.example.builtdaily.models.Video;
import com.example.builtdaily.models.YouTubeResponse;
import com.example.builtdaily.network.RetrofitClient;
import com.example.builtdaily.network.YouTubeApiService;
import com.example.builtdaily.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class YouTubeRepository {
    private final YouTubeApiService apiService;

    public YouTubeRepository() {
        apiService = RetrofitClient.getInstance().create(YouTubeApiService.class);
    }

    public void searchVideos(String query, String videoDuration, Callback<List<Video>> callback) {
        String part = "snippet";
        int maxResults = 10;
        String relevanceLanguage = "en";
        String type = "video";
        boolean videoEmbeddable = true;
        boolean videoSyndicated = true;

        Log.d("API_TEST", "searchVideos called");

        Call<YouTubeResponse> call = apiService.searchVideos(part, query, maxResults, relevanceLanguage, "", type, videoDuration, videoEmbeddable, videoSyndicated, Constants.API_KEY);
        call.enqueue(new retrofit2.Callback<YouTubeResponse>() {
            @Override
            public void onResponse(Call<YouTubeResponse> call, Response<YouTubeResponse> response) {
                Log.d("API_TEST", "api response");
                List<Video> videos = new ArrayList<>();
                if (response.body() != null) {
                    for (YouTubeResponse.Item item : response.body().items) {
                        String title = item.snippet.title;
                        String videoId = item.id.videoId;
                        String thumbnailUrl = item.snippet.thumbnails.medium.url;
                        videos.add(new Video(title, videoId, thumbnailUrl));
                    }
                }
                callback.onResponse(null, Response.success(videos));
            }

            @Override
            public void onFailure(Call<YouTubeResponse> call, Throwable t) {
                Log.d("API_TEST", "api failure");
                callback.onFailure(null, t);
            }
        });
    }

}
