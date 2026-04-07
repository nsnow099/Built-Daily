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

import java.util.LinkedHashMap;
import java.util.Map;

public class WorkoutScheduleActivity extends AppCompatActivity {
    private static final String[] DAY_KEYS = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_schedule);

        Spinner durationSpinner = findViewById(R.id.scheduleDurationSpinner);
        Button saveButton = findViewById(R.id.saveScheduleBtn);
        Button cancelButton = findViewById(R.id.cancelScheduleBtn);
        CheckBox[] dayChecks = new CheckBox[]{
                findViewById(R.id.checkboxMon),
                findViewById(R.id.checkboxTue),
                findViewById(R.id.checkboxWed),
                findViewById(R.id.checkboxThu),
                findViewById(R.id.checkboxFri),
                findViewById(R.id.checkboxSat),
                findViewById(R.id.checkboxSun)
        };
        Spinner[] daySpinners = new Spinner[]{
                findViewById(R.id.spinnerMon),
                findViewById(R.id.spinnerTue),
                findViewById(R.id.spinnerWed),
                findViewById(R.id.spinnerThu),
                findViewById(R.id.spinnerFri),
                findViewById(R.id.spinnerSat),
                findViewById(R.id.spinnerSun)
        };

        UserPreferencesManager preferencesManager = new UserPreferencesManager(this);

        ArrayAdapter<CharSequence> focusAdapter = buildAdapter(R.array.workout_focus_options);
        for (Spinner spinner : daySpinners) {
            spinner.setAdapter(focusAdapter);
        }

        ArrayAdapter<CharSequence> durationAdapter = buildAdapter(R.array.workout_duration_options);
        durationSpinner.setAdapter(durationAdapter);

        Map<String, String> savedSchedule = preferencesManager.getScheduleMap();
        for (int i = 0; i < DAY_KEYS.length; i++) {
            String savedWorkout = savedSchedule.get(DAY_KEYS[i]);
            boolean isSelected = savedWorkout != null && !savedWorkout.isEmpty();
            dayChecks[i].setChecked(isSelected);
            daySpinners[i].setEnabled(isSelected);
            if (isSelected) {
                setSpinnerValue(daySpinners[i], savedWorkout);
            }

            Spinner spinner = daySpinners[i];
            dayChecks[i].setOnCheckedChangeListener((buttonView, checked) -> spinner.setEnabled(checked));
        }

        setSpinnerValue(durationSpinner, preferencesManager.getScheduleDuration());

        saveButton.setOnClickListener(v -> {
            String duration = durationSpinner.getSelectedItem().toString();
            LinkedHashMap<String, String> scheduleMap = new LinkedHashMap<>();

            for (int i = 0; i < DAY_KEYS.length; i++) {
                if (dayChecks[i].isChecked()) {
                    scheduleMap.put(DAY_KEYS[i], daySpinners[i].getSelectedItem().toString());
                }
            }

            if (scheduleMap.isEmpty()) {
                Toast.makeText(this, R.string.schedule_day_required, Toast.LENGTH_SHORT).show();
                return;
            }

            preferencesManager.saveWorkoutSchedule(scheduleMap, duration);
            Toast.makeText(this, R.string.schedule_saved_message, Toast.LENGTH_SHORT).show();
            finish();
        });

        cancelButton.setOnClickListener(v -> finish());
    }

    private ArrayAdapter<CharSequence> buildAdapter(int arrayResId) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                arrayResId,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
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
