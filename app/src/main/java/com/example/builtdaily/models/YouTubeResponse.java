package com.example.builtdaily.models;

import java.util.List;

public class YouTubeResponse {
    public List<Item> items;

    public static class Item {
        public Id id;
        public Snippet snippet;
    }

    public static class Id {
        public String videoId;
    }

    public static class Snippet {
        public String title;
        public Thumbnails thumbnails;
    }

    public static class Thumbnails {
        public Thumbnail medium;
    }

    public static class Thumbnail {
        public String url;
    }
}
