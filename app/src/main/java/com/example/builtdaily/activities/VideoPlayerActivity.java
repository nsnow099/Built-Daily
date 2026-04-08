package com.example.builtdaily.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.builtdaily.R;

public class VideoPlayerActivity extends AppCompatActivity {
    // string for the intent extra so we can get the video url
    public static final String EXTRA_VIDEO_URL = "extra_video_url";

    private WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        // find the webview and close button in xml
        webView = findViewById(R.id.videoWebView);
        Button closeButton = findViewById(R.id.closeVideoBtn);

        // setup webview settings so youtube actually works
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true); // youtube needs javascript
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);

        // set the clients
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());

        // get the url from the intent
        String videoUrl = getIntent().getStringExtra(EXTRA_VIDEO_URL);
        if (videoUrl != null && !videoUrl.isEmpty()) {
            // load the youtube page
            webView.loadUrl(videoUrl);
        }

        // just finish the activity to go back to main screen
        closeButton.setOnClickListener(v -> finish());
    }

    @Override
    public void onBackPressed() {
        // if webview can go back (like to previous video) do that instead of closing
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        // clean up the webview when activity is destroyed
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}
