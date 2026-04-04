package com.example.builtdaily.network;

import com.example.builtdaily.models.VideoDetailsResponse;
import com.example.builtdaily.models.VideoSearchResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface YouTubeApiService {
    @GET("search")
    Call<VideoSearchResponse> searchVideos(
            @Query("part") String part,
            @Query("q") String query,
            @Query("maxResults") int maxResults,
            @Query("relevanceLanguage") String relevanceLanguage,
            @Query("topicID") String topicID,
            @Query("type") String type,
            @Query("videoDuration") String videoDuration,
            @Query("videoEmbeddable") boolean videoEmbeddable,
            @Query("videoSyndicated") boolean videoSyndicated,
            @Query("key") String apiKey
    );
    @GET("videos")
    Call<VideoDetailsResponse> getVideoDetails(
            @Query("part") String part,
            @Query("id") String ids,
            @Query("key") String apiKey
    );
}
