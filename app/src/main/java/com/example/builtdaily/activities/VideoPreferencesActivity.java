package com.example.builtdaily.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.builtdaily.R;
import com.example.builtdaily.utils.UserPreferencesManager;

public class VideoPreferencesActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_preferences);

        // link the spinners and checkboxes from the layout
        Spinner durationSpinner = findViewById(R.id.preferenceDurationSpinner);
        CheckBox beginnerCheckbox = findViewById(R.id.beginnerFriendlyCheckbox);
        CheckBox noEquipmentCheckbox = findViewById(R.id.noEquipmentCheckbox);
        Button saveButton = findViewById(R.id.savePreferencesBtn);
        Button cancelButton = findViewById(R.id.cancelPreferencesBtn);

        // manager class for the shared preferences
        int userId = getSharedPreferences("auth_prefs", MODE_PRIVATE).getInt("logged_in_user_id", -1);
        UserPreferencesManager preferencesManager = new UserPreferencesManager(this, userId);

        // setting up the spinner with the duration options
        ArrayAdapter<CharSequence> durationAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.workout_duration_options,
                android.R.layout.simple_spinner_item
        );
        durationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        durationSpinner.setAdapter(durationAdapter);

        // load the saved preferences so they show up when the screen opens
        setSpinnerValue(durationSpinner, preferencesManager.getPreferredDuration());
        beginnerCheckbox.setChecked(preferencesManager.isBeginnerFriendlyEnabled());
        noEquipmentCheckbox.setChecked(preferencesManager.isNoEquipmentEnabled());

        saveButton.setOnClickListener(v -> {
            // get the values the user selected
            String duration = durationSpinner.getSelectedItem().toString();
            boolean beginnerFriendly = beginnerCheckbox.isChecked();
            boolean noEquipment = noEquipmentCheckbox.isChecked();

            // save everything to shared prefs
            preferencesManager.saveVideoPreferences(duration, beginnerFriendly, noEquipment);
            Toast.makeText(this, "Video preferences saved", Toast.LENGTH_SHORT).show();
            finish(); // go back to the profile/home screen
        });

        // just close the screen if they hit cancel
        cancelButton.setOnClickListener(v -> finish());
    }

    // helper to set the spinner value from a string
    private void setSpinnerValue(Spinner spinner, String value) {
        if (value == null || value.isEmpty()) {
            return;
        }

        for (int i = 0; i < spinner.getCount(); i++) {
            if (value.equalsIgnoreCase(spinner.getItemAtPosition(i).toString())) {
                spinner.setSelection(i);
                return;
            }
        }
    }
}