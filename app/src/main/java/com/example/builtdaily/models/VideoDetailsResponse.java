package com.example.builtdaily.models;

import java.util.List;

public class VideoDetailsResponse {
    public List<Item> items;

    public static class Item {
        public String id;
        public ContentDetails contentDetails;
    }

    public static class ContentDetails {
        public String duration;
    }
}