package com.example.builtdaily.repository;

import android.util.Log;

import com.example.builtdaily.models.Video;
import com.example.builtdaily.models.VideoSearchResponse;
import com.example.builtdaily.models.VideoDetailsResponse;
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
    private static final String part = "snippet";
    private static final int maxResults = 50;
    private static final String relevanceLanguage = "en";
    private static final String type = "video";
    private static final boolean videoEmbeddable = true;
    private static final boolean videoSyndicated = true;
    private String exclude = " -science -anatomy -explained -research -study -podcast";

    public YouTubeRepository() {
        apiService = RetrofitClient.getInstance().create(YouTubeApiService.class);
    }

    private void searchVideos(String query, String videoDuration, int minSeconds, int maxSeconds, boolean beginnerFriendly, boolean noEquipment, Callback<List<Video>> callback) {
        Log.d("API_TEST", "searchVideos called");

        Call<VideoSearchResponse> call = apiService.searchVideos(part, query+exclude, maxResults, relevanceLanguage, "", type, videoDuration, videoEmbeddable, videoSyndicated, Constants.API_KEY);
        call.enqueue(new retrofit2.Callback<VideoSearchResponse>() {
            @Override
            public void onResponse(Call<VideoSearchResponse> call, Response<VideoSearchResponse> response) {
                Log.d("API_TEST", "api response");
                List<Video> videos = new ArrayList<>();
                List<String> ids = new ArrayList<>();

                if (response.body() != null) {
                    for (VideoSearchResponse.Item item : response.body().items) {
                        String title = item.snippet.title;
                        String videoId = item.id.videoId;
                        String thumbnailUrl = item.snippet.thumbnails.medium.url;
                        videos.add(new Video(title, videoId, thumbnailUrl));
                        ids.add(videoId);
                    }
                }
                filterVideos(videos, ids, minSeconds, maxSeconds, beginnerFriendly, noEquipment, callback);
            }

            @Override
            public void onFailure(Call<VideoSearchResponse> call, Throwable t) {
                Log.d("API_TEST", "api failure");
                callback.onFailure(null, t);
            }
        });
    }

    private void filterVideos(List<Video> videos, List<String> ids, int minSeconds, int maxSeconds, boolean beginnerFriendly, boolean noEquipment, Callback<List<Video>> callback) {
        String idString = String.join(",", ids);

        Call<VideoDetailsResponse> call = apiService.getVideoDetails(
                "contentDetails",
                idString,
                Constants.API_KEY
        );

        call.enqueue(new Callback<VideoDetailsResponse>() { //another call to get the videos durations
            @Override
            public void onResponse(Call<VideoDetailsResponse> call, Response<VideoDetailsResponse> response) {

                if (response.body() != null) {
                    for (int i = 0; i < response.body().items.size(); i++) {
                        videos.get(i).duration = response.body().items.get(i).contentDetails.duration;
                    }
                }

                List<Video> filtered = filterLength(videos, minSeconds, maxSeconds);
                if (beginnerFriendly) filtered = filterTitle(filtered, "beginner");
                if (noEquipment) filtered = filterTitle(filtered, "no equipment");
                callback.onResponse(null, Response.success(filtered));
            }

            @Override
            public void onFailure(Call<VideoDetailsResponse> call, Throwable t) {
                callback.onFailure(null, t);
            }
        });
    }

    private List<Video> filterLength(List<Video> videos, int minSeconds, int maxSeconds) {
        List<Video> filtered = new ArrayList<>();

        for (Video v : videos) {
            int seconds = parseDurationToSeconds(v.duration);

            if (seconds >= minSeconds && seconds <= maxSeconds) {
                filtered.add(v);
            }
        }

        return filtered;
    }

    private List<Video> filterTitle(List<Video> videos, String include) {
        List<Video> filtered = new ArrayList<>();

        if (include == null || include.isEmpty()) {
            return videos; // nothing to filter
        }

        for (Video v : videos) {
            if (v.title != null && v.title.toLowerCase().contains(include.toLowerCase())) {
                filtered.add(v);
            }
        }

        return filtered;
    }

    private int parseDurationToSeconds(String duration) {
        int hours = 0, minutes = 0, seconds = 0;

        duration = duration.replace("PT", "");

        if (duration.contains("H")) {
            String[] split = duration.split("H");
            hours = Integer.parseInt(split[0]);
            duration = split[1];
        }

        if (duration.contains("M")) {
            String[] split = duration.split("M");
            minutes = Integer.parseInt(split[0]);
            duration = split.length > 1 ? split[1] : "";
        }

        if (duration.contains("S")) {
            seconds = Integer.parseInt(duration.replace("S", ""));
        }

        return hours * 3600 + minutes * 60 + seconds;
    }

    private String buildQuery(String workoutType, boolean beginnerFriendly, boolean noEquipment) {
        StringBuilder query = new StringBuilder();

        if (beginnerFriendly) query.append("beginner");

        query.append(" ").append(workoutType);

        if (noEquipment) query.append(" no equipment");
        return query.toString();
    }

    public void searchArmWorkouts( String videoDuration, boolean noEquipment, boolean beginnerFriendly, Callback<List<Video>> callback) {
        Log.d("API_TEST", "searchArmWorkouts called");
        int minSeconds = 300;
        int maxSeconds = 2400;
        switch (videoDuration) {
            case "short":
                maxSeconds = 600;
                videoDuration = "medium"; //5-10 mins still falls within medium duration for the api param
                break;
            case "medium":
                minSeconds = 601;
                maxSeconds = 1500;
                break;
            case "long":
                minSeconds = 1501;
                break;
            default:
                videoDuration = "any";
        }

        String query = buildQuery("arm workout", beginnerFriendly, noEquipment);
        searchVideos(query, videoDuration, minSeconds, maxSeconds, beginnerFriendly, noEquipment, callback);
    }

}
