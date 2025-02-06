package com.example.weatherapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class LocationActivity extends AppCompatActivity {
    private EditText locationInput;
    private Button saveButton;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        locationInput = findViewById(R.id.locationInput);
        saveButton = findViewById(R.id.saveButton);

        String savedLocation = preferences.getString("location", "Istanbul");
        locationInput.setText(savedLocation);

        saveButton.setOnClickListener(v -> {
            String newLocation = locationInput.getText().toString().trim();
            if (!newLocation.isEmpty()) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("location", newLocation);
                editor.apply();
                setResult(RESULT_OK);
                finish();
            }
        });
    }
}