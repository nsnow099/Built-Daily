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

        Spinner durationSpinner = findViewById(R.id.preferenceDurationSpinner);
        CheckBox beginnerCheckbox = findViewById(R.id.beginnerFriendlyCheckbox);
        CheckBox noEquipmentCheckbox = findViewById(R.id.noEquipmentCheckbox);
        Button saveButton = findViewById(R.id.savePreferencesBtn);
        Button cancelButton = findViewById(R.id.cancelPreferencesBtn);

        UserPreferencesManager preferencesManager = new UserPreferencesManager(this);

        ArrayAdapter<CharSequence> durationAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.workout_duration_options,
                android.R.layout.simple_spinner_item
        );
        durationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        durationSpinner.setAdapter(durationAdapter);

        setSpinnerValue(durationSpinner, preferencesManager.getPreferredDuration());
        beginnerCheckbox.setChecked(preferencesManager.isBeginnerFriendlyEnabled());
        noEquipmentCheckbox.setChecked(preferencesManager.isNoEquipmentEnabled());

        saveButton.setOnClickListener(v -> {
            String duration = durationSpinner.getSelectedItem().toString();
            boolean beginnerFriendly = beginnerCheckbox.isChecked();
            boolean noEquipment = noEquipmentCheckbox.isChecked();

            preferencesManager.saveVideoPreferences(duration, beginnerFriendly, noEquipment);
            Toast.makeText(this, "Video preferences saved", Toast.LENGTH_SHORT).show();
            finish();
        });

        cancelButton.setOnClickListener(v -> finish());
    }

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
