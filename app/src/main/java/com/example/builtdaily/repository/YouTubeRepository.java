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
    // some static strings for the youtube api
    private static final String part = "snippet";
    private static final int maxResults = 50;
    private static final String relevanceLanguage = "en";
    private static final String type = "video";
    private static final boolean videoEmbeddable = true;
    private static final boolean videoSyndicated = true;
    // stuff to exclude from search so we don't get weird anatomy videos
    private String exclude = " -science -anatomy -explained -research -study -podcast";

    public YouTubeRepository() {
        // init retrofit service
        apiService = RetrofitClient.getInstance().create(YouTubeApiService.class);
    }

    private void searchVideos(String query, String videoDuration, int minSeconds, int maxSeconds, boolean beginnerFriendly, boolean noEquipment, String workout, Callback<List<Video>> callback) {
        // make the search call to youtube
        Call<VideoSearchResponse> call = apiService.searchVideos(part, query+exclude, maxResults, relevanceLanguage, "", type, videoDuration, videoEmbeddable, videoSyndicated, Constants.API_KEY);
        call.enqueue(new retrofit2.Callback<VideoSearchResponse>() {
            @Override
            public void onResponse(Call<VideoSearchResponse> call, Response<VideoSearchResponse> response) {
                Log.d("API_TEST", "api response");
                List<Video> videos = new ArrayList<>();
                List<String> ids = new ArrayList<>();

                if (response.body() != null) {
                    // loop through the results and save basic info
                    for (VideoSearchResponse.Item item : response.body().items) {
                        String title = item.snippet.title;
                        String videoId = item.id.videoId;
                        String thumbnailUrl = item.snippet.thumbnails.medium.url;
                        videos.add(new Video(title, videoId, thumbnailUrl));
                        ids.add(videoId);
                    }
                }
                // now we need to get durations which is a second call
                filterVideos(videos, ids, minSeconds, maxSeconds, beginnerFriendly, noEquipment, workout, callback);
            }

            @Override
            public void onFailure(Call<VideoSearchResponse> call, Throwable t) {
                Log.d("API_TEST", "api failure");
                callback.onFailure(null, t);
            }
        });
    }

    private void filterVideos(List<Video> videos, List<String> ids, int minSeconds, int maxSeconds, boolean beginnerFriendly, boolean noEquipment, String workout, Callback<List<Video>> callback) {
        // join IDs with commas for the api
        String idString = String.join(",", ids);

        // second call to get contentDetails (duration)
        Call<VideoDetailsResponse> call = apiService.getVideoDetails(
                "contentDetails",
                idString,
                Constants.API_KEY
        );

        call.enqueue(new Callback<VideoDetailsResponse>() { 
            @Override
            public void onResponse(Call<VideoDetailsResponse> call, Response<VideoDetailsResponse> response) {

                if (response.body() != null) {
                    // add the duration to our video objects
                    for (int i = 0; i < response.body().items.size(); i++) {
                        videos.get(i).duration = response.body().items.get(i).contentDetails.duration;
                    }
                }

                // filter the list based on what the user wants
                List<Video> filtered = filterLength(videos, minSeconds, maxSeconds);
                if (workout.equals("core")) filtered = filterTitle(filtered, workout);
                if (beginnerFriendly) filtered = filterTitle(filtered, "beginner");
                if (noEquipment) filtered = filterTitle(filtered, "no equipment");
                
                // send the final list back to the activity
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

            // only add if it fits the time range
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
            // check if the title contains the keyword (case insensitive)
            if (v.title != null && v.title.toLowerCase().contains(include.toLowerCase())) {
                filtered.add(v);
            }
        }

        return filtered;
    }

    private int parseDurationToSeconds(String duration) {
        // handles the weird ISO 8601 duration format
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
        // build the string we send to youtube search
        StringBuilder query = new StringBuilder();

        if (beginnerFriendly) query.append("beginner");

        query.append(" ").append(workoutType);

        if (noEquipment) query.append(" no equipment");
        return query.toString();
    }

    // specific search methods for each workout type

    public void searchArmWorkouts( String videoDuration, boolean noEquipment, boolean beginnerFriendly, Callback<List<Video>> callback) {
        Log.d("API_TEST", "searchArmWorkouts called");
        int minSeconds = 300;
        int maxSeconds = 2400;
        switch (videoDuration) {
            case "short":
                maxSeconds = 600;
                videoDuration = "medium";
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
        searchVideos(query, videoDuration, minSeconds, maxSeconds, beginnerFriendly, noEquipment, "arm", callback);
    }

    public void searchLegWorkouts( String videoDuration, boolean noEquipment, boolean beginnerFriendly, Callback<List<Video>> callback) {
        Log.d("API_TEST", "searchLegWorkouts called");
        int minSeconds = 300;
        int maxSeconds = 2400;
        switch (videoDuration) {
            case "short":
                maxSeconds = 600;
                videoDuration = "medium";
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

        String query = buildQuery("leg workout", beginnerFriendly, noEquipment);
        searchVideos(query, videoDuration, minSeconds, maxSeconds, beginnerFriendly, noEquipment, "leg", callback);
    }

    public void searchCoreWorkouts( String videoDuration, boolean noEquipment, boolean beginnerFriendly, Callback<List<Video>> callback) {
        Log.d("API_TEST", "searchCoreWorkouts called");
        int minSeconds = 300;
        int maxSeconds = 2400;
        switch (videoDuration) {
            case "short":
                maxSeconds = 600;
                videoDuration = "medium";
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

        String query = buildQuery("core workout", beginnerFriendly, noEquipment);
        searchVideos(query, videoDuration, minSeconds, maxSeconds, beginnerFriendly, noEquipment, "core", callback);
    }

    public void searchFullBodyWorkouts( String videoDuration, boolean noEquipment, boolean beginnerFriendly, Callback<List<Video>> callback) {
        Log.d("API_TEST", "searchFullBodyWorkouts called");
        int minSeconds = 300;
        int maxSeconds = 2400;
        switch (videoDuration) {
            case "short":
                maxSeconds = 600;
                videoDuration = "medium";
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

        String query = buildQuery("full body workout", beginnerFriendly, noEquipment);
        searchVideos(query, videoDuration, minSeconds, maxSeconds, beginnerFriendly, noEquipment, "full body", callback);
    }

    public void searchYogaWorkouts( String videoDuration, boolean noEquipment, boolean beginnerFriendly, Callback<List<Video>> callback) {
        Log.d("API_TEST", "searchYogaWorkouts called");
        int minSeconds = 300;
        int maxSeconds = 2400;
        switch (videoDuration) {
            case "short":
                maxSeconds = 600;
                videoDuration = "medium";
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

        String query = buildQuery("yoga workout", beginnerFriendly, noEquipment);
        searchVideos(query, videoDuration, minSeconds, maxSeconds, beginnerFriendly, noEquipment, "yoga", callback);
    }

    public void searchCardioWorkouts( String videoDuration, boolean noEquipment, boolean beginnerFriendly, Callback<List<Video>> callback) {
        Log.d("API_TEST", "searchCardioWorkouts called");
        int minSeconds = 300;
        int maxSeconds = 2400;
        switch (videoDuration) {
            case "short":
                maxSeconds = 600;
                videoDuration = "medium";
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

        String query = buildQuery("cardio workout", beginnerFriendly, noEquipment);
        searchVideos(query, videoDuration, minSeconds, maxSeconds, beginnerFriendly, noEquipment, "cardio", callback);
    }

    public void searchChestWorkouts( String videoDuration, boolean noEquipment, boolean beginnerFriendly, Callback<List<Video>> callback) {
        Log.d("API_TEST", "searchChestWorkouts called");
        int minSeconds = 300;
        int maxSeconds = 2400;
        switch (videoDuration) {
            case "short":
                maxSeconds = 600;
                videoDuration = "medium";
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

        String query = buildQuery("chest workout", beginnerFriendly, noEquipment);
        searchVideos(query, videoDuration, minSeconds, maxSeconds, beginnerFriendly, noEquipment, "chest", callback);
    }

    public void searchBackWorkouts( String videoDuration, boolean noEquipment, boolean beginnerFriendly, Callback<List<Video>> callback) {
        Log.d("API_TEST", "searchBackWorkouts called");
        int minSeconds = 300;
        int maxSeconds = 2400;
        switch (videoDuration) {
            case "short":
                maxSeconds = 600;
                videoDuration = "medium";
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

        String query = buildQuery("back workout", beginnerFriendly, noEquipment);
        searchVideos(query, videoDuration, minSeconds, maxSeconds, beginnerFriendly, noEquipment, "back", callback);
    }

    public void searchCyclingWorkouts( String videoDuration, boolean noEquipment, boolean beginnerFriendly, Callback<List<Video>> callback) {
        Log.d("API_TEST", "searchCyclingWorkouts called");
        int minSeconds = 300;
        int maxSeconds = 2400;
        switch (videoDuration) {
            case "short":
                maxSeconds = 600;
                videoDuration = "medium";
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

        String query = buildQuery("cycling workout", beginnerFriendly, noEquipment);
        searchVideos(query, videoDuration, minSeconds, maxSeconds, beginnerFriendly, noEquipment, "cycling", callback);
    }
}
