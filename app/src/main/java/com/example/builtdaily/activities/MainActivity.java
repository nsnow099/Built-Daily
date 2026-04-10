package com.example.builtdaily.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.builtdaily.R;
import com.example.builtdaily.models.Video;
import com.example.builtdaily.repository.YouTubeRepository;
import com.example.builtdaily.utils.UserPreferencesManager;
import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    // text views and buttons for the UI
    private TextView streakValue;
    private TextView streakSupportingText;
    private TextView scheduleSummary;
    private TextView selectedDaySummary;
    private TextView videoSectionTitle;
    private Button completeWorkoutBtn;
    private Button createScheduleBtn;
    private LinearLayout dayPickerContainer;
    private LinearLayout videosContainer;
    private UserPreferencesManager preferencesManager;
    private String selectedDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // helper class to handle shared prefs
        int userId = getSharedPreferences("auth_prefs", MODE_PRIVATE).getInt("logged_in_user_id", -1);
        Log.d("USER_DEBUG", "Current userId = " + userId);
        preferencesManager = new UserPreferencesManager(this, userId);

        // linking all the xml IDs to variables
        streakValue = findViewById(R.id.streakValue);
        streakSupportingText = findViewById(R.id.streakSupportingText);
        scheduleSummary = findViewById(R.id.scheduleSummary);
        selectedDaySummary = findViewById(R.id.selectedDaySummary);
        videoSectionTitle = findViewById(R.id.videoSectionTitle);
        completeWorkoutBtn = findViewById(R.id.completeWorkoutBtn);
        createScheduleBtn = findViewById(R.id.createScheduleBtn);
        dayPickerContainer = findViewById(R.id.dayPickerContainer);
        videosContainer = findViewById(R.id.videosContainer);

        Button profileBtn = findViewById(R.id.profileBtn);
        Button editScheduleBtn = findViewById(R.id.homeEditScheduleBtn);
        Button editPreferencesBtn = findViewById(R.id.homeEditPreferencesBtn);

        // setting up clicks for the different buttons
        profileBtn.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ProfileActivity.class)));
        editScheduleBtn.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, WorkoutScheduleActivity.class)));
        editPreferencesBtn.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, VideoPreferencesActivity.class)));
        createScheduleBtn.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, WorkoutScheduleActivity.class)));
        completeWorkoutBtn.setOnClickListener(v -> markWorkoutCompleted());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // refresh the screen data when coming back to the app
        renderHomeScreen();
    }

    private void renderHomeScreen() {
        // showing the current streak
        int streak = preferencesManager.getStreak();
        streakValue.setText(String.valueOf(streak));
        streakSupportingText.setText(streak == 1
                ? getString(R.string.streak_supporting_singular)
                : getString(R.string.streak_supporting_plural));

        // if there's no schedule yet, show the prompt to create one
        if (!preferencesManager.hasWorkoutSchedule()) {
            scheduleSummary.setText(getString(R.string.no_schedule_message));
            createScheduleBtn.setVisibility(View.VISIBLE);
            completeWorkoutBtn.setVisibility(View.GONE);
            videoSectionTitle.setText(R.string.video_section_placeholder);
            dayPickerContainer.setVisibility(View.GONE);
            selectedDaySummary.setVisibility(View.GONE);
            videosContainer.removeAllViews();
            addEmptyState(getString(R.string.create_schedule_prompt));
            return;
        }

        // if schedule exists, update the summary text
        String scheduleText = getString(
                R.string.schedule_summary_format,
                preferencesManager.getScheduleSummary(),
                preferencesManager.getScheduleDuration()
        );
        scheduleSummary.setText(scheduleText);
        createScheduleBtn.setVisibility(View.GONE);
        completeWorkoutBtn.setVisibility(View.VISIBLE);
        videoSectionTitle.setText(R.string.recommended_videos_title);
        // show the day buttons at the top
        renderDayPicker();
    }

    private void markWorkoutCompleted() {
        // get today's date in string format
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        int previousStreak = preferencesManager.getStreak();
        // save the completion in preferences
        preferencesManager.saveWorkoutCompletion(today);

        // simple check if streak actually went up
        if (preferencesManager.getStreak() == previousStreak) {
            Toast.makeText(this, R.string.workout_already_completed, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.workout_completed, Toast.LENGTH_SHORT).show();
        }

        // update the streak text on the screen
        int streak = preferencesManager.getStreak();
        streakValue.setText(String.valueOf(streak));
        streakSupportingText.setText(streak == 1
                ? getString(R.string.streak_supporting_singular)
                : getString(R.string.streak_supporting_plural));
    }

    private void renderDayPicker() {
        Map<String, String> scheduleMap = preferencesManager.getScheduleMap();
        if (scheduleMap.isEmpty()) {
            dayPickerContainer.setVisibility(View.GONE);
            selectedDaySummary.setVisibility(View.GONE);
            videosContainer.removeAllViews();
            addEmptyState(getString(R.string.no_schedule_message));
            return;
        }

        // clear old buttons before drawing new ones
        dayPickerContainer.removeAllViews();
        dayPickerContainer.setVisibility(View.VISIBLE);
        selectedDaySummary.setVisibility(View.VISIBLE);

        // default to first day if nothing selected
        if (selectedDay == null || !scheduleMap.containsKey(selectedDay)) {
            selectedDay = scheduleMap.keySet().iterator().next();
        }

        // loop through the schedule and make buttons for each day
        for (Map.Entry<String, String> entry : scheduleMap.entrySet()) {
            Button dayButton = createDayButton(getDisplayDayName(entry.getKey()), entry.getKey().equals(selectedDay));
            dayButton.setOnClickListener(v -> {
                selectedDay = entry.getKey();
                renderDayPicker(); // redraw so the highlight moves
            });
            dayPickerContainer.addView(dayButton);
        }

        // display videos for the selected day
        showSelectedDayRecommendations(selectedDay, scheduleMap.get(selectedDay));
    }

    private Button createDayButton(String day, boolean isSelected) {
        // building the button programmatically instead of XML
        Button button = new Button(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.rightMargin = dp(10);
        button.setLayoutParams(params);
        button.setMinWidth(0);
        button.setMinimumWidth(0);
        button.setPadding(dp(18), dp(10), dp(18), dp(10));
        button.setText(day);
        button.setAllCaps(false);
        // change color depending on if it is selected
        if (isSelected) {
            button.setBackgroundTintList(getColorStateList(R.color.teal_primary));
            button.setTextColor(getColor(R.color.white));
        } else {
            button.setBackgroundTintList(getColorStateList(R.color.teal_surface_strong));
            button.setTextColor(getColor(R.color.teal_primary_dark));
        }
        return button;
    }

    private void showSelectedDayRecommendations(String dayKey, String focus) {
        videosContainer.removeAllViews();
        selectedDaySummary.setText(getString(R.string.recommended_for_day_format, getDisplayDayName(dayKey), focus));

        // card for the video list section
        LinearLayout sectionCard = new LinearLayout(this);
        LinearLayout.LayoutParams sectionParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        sectionCard.setLayoutParams(sectionParams);
        sectionCard.setOrientation(LinearLayout.VERTICAL);
        sectionCard.setPadding(dp(18), dp(18), dp(18), dp(18));
        sectionCard.setBackgroundResource(R.drawable.bg_surface_card);
        videosContainer.addView(sectionCard);

        if (preferencesManager.needsRefresh()) {
            addEmptyState(sectionCard, getString(R.string.loading_videos));
            preferencesManager.clearCachedVideos();
            fetchAllVideosForWeek();
        } else {
            List<Video> cachedVideos = preferencesManager.getVideosForDay(dayKey);
            if (cachedVideos.isEmpty()) {
                addEmptyState(sectionCard, getString(R.string.no_videos_found));
            } else {
                for (int i = 0; i < cachedVideos.size(); i++) {
                    sectionCard.addView(createVideoCard(cachedVideos.get(i), i + 1));
                }
            }
        }
    }

    private void fetchAllVideosForWeek() {
        Map<String, String> scheduleMap = preferencesManager.getScheduleMap();
        for (Map.Entry<String, String> entry : scheduleMap.entrySet()) {
            fetchVideosForDay(entry.getKey(), entry.getValue());
        }
    }

    private void fetchVideosForDay(String dayKey, String focus) {
        YouTubeRepository repo = new YouTubeRepository();
        Callback<List<Video>> callback = new Callback<List<Video>>() {
            @Override
            public void onResponse(Call<List<Video>> call, Response<List<Video>> response) {
                List<Video> videos = response.body();
                if (videos != null && !videos.isEmpty()) {
                    Collections.shuffle(videos);
                    int limit = Math.min(videos.size(), 3);
                    List<Video> selected = videos.subList(0, limit);
                    preferencesManager.saveSelectedVideos(selected, dayKey);
                    
                    // if the day we just fetched is the currently selected day, refresh the UI
                    if (dayKey.equals(selectedDay)) {
                        renderDayPicker();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Video>> call, Throwable t) {
                Log.e("API_TEST", "Error fetching videos for " + dayKey + ": " + t.getMessage());
            }
        };

        String duration = preferencesManager.getPreferredDuration();
        boolean noEquipment = preferencesManager.isNoEquipmentEnabled();
        boolean beginner = preferencesManager.isBeginnerFriendlyEnabled();

        switch (focus.toLowerCase(Locale.US)) {
            case "arms":
                repo.searchArmWorkouts(duration, noEquipment, beginner, callback);
                break;
            case "legs":
                repo.searchLegWorkouts(duration, noEquipment, beginner, callback);
                break;
            case "core":
                repo.searchCoreWorkouts(duration, noEquipment, beginner, callback);
                break;
            case "yoga":
                repo.searchYogaWorkouts(duration, noEquipment, beginner, callback);
                break;
            case "cardio":
                repo.searchCardioWorkouts(duration, noEquipment, beginner, callback);
                break;
            case "chest":
                repo.searchChestWorkouts(duration, noEquipment, beginner, callback);
                break;
            case "back":
                repo.searchBackWorkouts(duration, noEquipment, beginner, callback);
                break;
            case "cycling":
                repo.searchCyclingWorkouts(duration, noEquipment, beginner, callback);
                break;
            case "full body":
            default:
                repo.searchFullBodyWorkouts(duration, noEquipment, beginner, callback);
                break;
        }
    }

    private View createVideoCard(Video video, int position) {
        // building the individual video cards programmatically
        MaterialCardView cardView = new MaterialCardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.bottomMargin = dp(16);
        cardView.setLayoutParams(cardParams);
        cardView.setRadius(dp(16));
        cardView.setCardElevation(dp(2));
        cardView.setStrokeWidth(dp(1));
        cardView.setStrokeColor(getColor(R.color.card_stroke));
        cardView.setCardBackgroundColor(getColor(R.color.teal_surface));

        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setPadding(dp(16), dp(16), dp(16), dp(16));

        // loading thumbnail image with Glide
        ImageView thumbnailView = new ImageView(this);
        LinearLayout.LayoutParams thumbnailParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(180)
        );
        thumbnailView.setLayoutParams(thumbnailParams);
        thumbnailView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        thumbnailView.setContentDescription(video.title);
        Glide.with(this)
                .load(video.thumbnailUrl)
                .centerCrop()
                .placeholder(R.drawable.thumbnail_placeholder)
                .error(R.drawable.thumbnail_placeholder)
                .into(thumbnailView);

        // title text
        TextView titleView = new TextView(this);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        titleParams.topMargin = dp(12);
        titleView.setLayoutParams(titleParams);
        titleView.setText(getString(R.string.video_title_format, position, video.title));
        titleView.setTextColor(getColor(R.color.teal_text));
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        titleView.setTypeface(Typeface.DEFAULT_BOLD);

        // duration text
        TextView durationView = new TextView(this);
        LinearLayout.LayoutParams durationParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        durationParams.topMargin = dp(8);
        durationView.setLayoutParams(durationParams);
        String duration = formatDuration(video.duration);
        durationView.setTextColor(getColor(R.color.teal_text_soft));
        durationView.setText(getString(R.string.video_duration_format, duration));

        // link text
        TextView linkView = new TextView(this);
        LinearLayout.LayoutParams linkParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        linkParams.topMargin = dp(8);
        linkView.setLayoutParams(linkParams);
        linkView.setText(getString(R.string.video_link_format, buildVideoUrl(video.videoId)));
        linkView.setTextColor(getColor(R.color.link_blue));
        linkView.setTypeface(Typeface.DEFAULT_BOLD);

        // add everything to the layout
        contentLayout.addView(thumbnailView);
        contentLayout.addView(titleView);
        contentLayout.addView(durationView);
        contentLayout.addView(linkView);
        cardView.addView(contentLayout);

        // click listeners to open the video
        cardView.setClickable(true);
        cardView.setFocusable(true);
        cardView.setOnClickListener(v -> openVideo(video.videoId));
        thumbnailView.setOnClickListener(v -> openVideo(video.videoId));
        return cardView;
    }

    private void addEmptyState(String message) {
        TextView emptyState = new TextView(this);
        emptyState.setText(message);
        emptyState.setTextColor(getColor(R.color.teal_text_soft));
        videosContainer.addView(emptyState);
    }

    private void addEmptyState(LinearLayout container, String message) {
        TextView emptyState = new TextView(this);
        emptyState.setText(message);
        emptyState.setTextColor(getColor(R.color.teal_text_soft));
        container.addView(emptyState);
    }

    // util method to convert dp to pixels because Android likes pixels in code
    private int dp(int value) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                getResources().getDisplayMetrics()
        ));
    }

    private void openVideo(String videoId) {
        // start the video player activity with the youtube URL
        String videoUrl = buildVideoUrl(videoId);
        Intent intent = new Intent(this, VideoPlayerActivity.class);
        intent.putExtra(VideoPlayerActivity.EXTRA_VIDEO_URL, videoUrl);
        startActivity(intent);
    }

    private String buildVideoUrl(String videoId) {
        return "https://www.youtube.com/watch?v=" + videoId;
    }

    private String getDisplayDayName(String dayKey) {
        // maps the keys back to full names
        switch (dayKey) {
            case "Mon":
                return getString(R.string.day_mon);
            case "Tue":
                return getString(R.string.day_tue);
            case "Wed":
                return getString(R.string.day_wed);
            case "Thu":
                return getString(R.string.day_thu);
            case "Fri":
                return getString(R.string.day_fri);
            case "Sat":
                return getString(R.string.day_sat);
            case "Sun":
            default:
                return getString(R.string.day_sun);
        }
    }

    private String formatDuration(String rawDuration) {
        // parses the weird PT ISO 8601 duration string from youtube API
        if (rawDuration == null || rawDuration.isEmpty()) {
            return getString(R.string.duration_unknown);
        }

        int hours = 0;
        int minutes = 0;
        int seconds = 0;
        String duration = rawDuration.replace("PT", "");

        if (duration.contains("H")) {
            String[] split = duration.split("H", 2);
            hours = Integer.parseInt(split[0]);
            duration = split.length > 1 ? split[1] : "";
        }

        if (duration.contains("M")) {
            String[] split = duration.split("M", 2);
            minutes = Integer.parseInt(split[0]);
            duration = split.length > 1 ? split[1] : "";
        }

        if (duration.contains("S")) {
            seconds = Integer.parseInt(duration.replace("S", ""));
        }

        // if there are hours show H:MM:SS otherwise just M:SS
        if (hours > 0) {
            return String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds);
        }

        return String.format(Locale.US, "%d:%02d", minutes, seconds);
    }
}