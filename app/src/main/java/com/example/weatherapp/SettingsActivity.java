package com.example.weatherapp;

import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SettingsActivity extends AppCompatActivity {
    private RadioGroup temperatureUnitRadioGroup;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        temperatureUnitRadioGroup = findViewById(R.id.temperatureUnitRadioGroup);
        String savedUnit = preferences.getString("temperature_unit", "celsius");
        if (savedUnit.equals("fahrenheit")) {
            temperatureUnitRadioGroup.check(R.id.fahrenheitRadioButton);
        } else {
            temperatureUnitRadioGroup.check(R.id.celsiusRadioButton);
        }

        temperatureUnitRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            SharedPreferences.Editor editor = preferences.edit();
            if (checkedId == R.id.celsiusRadioButton) {
                editor.putString("temperature_unit", "celsius");
            } else {
                editor.putString("temperature_unit", "fahrenheit");
            }
            editor.apply();
            setResult(RESULT_OK);
            finish();
        });
    }
}