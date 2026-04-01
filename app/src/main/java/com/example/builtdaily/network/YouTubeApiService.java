package com.example.builtdaily.network;

import com.example.builtdaily.models.YouTubeResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface YouTubeApiService {
    @GET("search")
    Call<YouTubeResponse> searchVideos(
            @Query("part") String part,
            @Query("q") String query,
            //@Query("eventType") String eventType,
            //@Query("forContentOwner") boolean forContentOwner,
            //@Query("forDeveloper") boolean forDeveloper,
            //@Query("forMine") boolean forMine,
            @Query("maxResults") int maxResults,
            @Query("relevanceLanguage") String relevanceLanguage,
            @Query("topicID") String topicID,
            @Query("type") String type,
            @Query("videoDuration") String videoDuration,
            @Query("videoEmbeddable") boolean videoEmbeddable,
            @Query("videoSyndicated") boolean videoSyndicated,
            @Query("key") String apiKey
    );
}
