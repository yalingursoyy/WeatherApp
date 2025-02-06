package com.example.weatherapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final int SETTINGS_REQUEST_CODE = 100;
    private static final int CALENDAR_REQUEST_CODE = 101;
    private static final int LOCATION_REQUEST_CODE = 102;
    private static final String API_KEY = "48195b9011d17159a62fd0c8d9977a6e"; // Replace with your actual API key

    private TextView temperatureTextView;
    private TextView locationTextView;
    private TextView unitTextView;
    private ImageView weatherImageView;
    private RecyclerView forecastRecyclerView;
    private WeatherAdapter weatherAdapter;
    private RequestQueue requestQueue;
    private ImageHelper imageHelper;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        initializeViews();
        setupRecyclerView();
        setupBottomNavigation();

        requestQueue = Volley.newRequestQueue(this);
        imageHelper = ImageHelper.getInstance(this);

        String savedLocation = preferences.getString("location", "Istanbul");
        fetchWeatherData(savedLocation);
    }

    private void initializeViews() {
        temperatureTextView = findViewById(R.id.temperatureTextView);
        locationTextView = findViewById(R.id.locationTextView);
        unitTextView = findViewById(R.id.unitTextView);
        weatherImageView = findViewById(R.id.weatherImageView);
        forecastRecyclerView = findViewById(R.id.forecastRecyclerView);

        String unit = preferences.getString("temperature_unit", "celsius");
        unitTextView.setText(unit.equals("celsius") ? "°C" : "°F");
    }

    private void setupRecyclerView() {
        weatherAdapter = new WeatherAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                this,LinearLayoutManager.HORIZONTAL,false);
        forecastRecyclerView.setLayoutManager(layoutManager);
        forecastRecyclerView.setAdapter(weatherAdapter);
        forecastRecyclerView.setHasFixedSize(true);
    }

    private void setupBottomNavigation() {
        ImageButton settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivityForResult(intent, SETTINGS_REQUEST_CODE);
        });

        ImageButton calendarButton = findViewById(R.id.calendarButton);
        calendarButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, CalendarActivity.class);
            startActivityForResult(intent, CALENDAR_REQUEST_CODE);
        });

        ImageButton windButton = findViewById(R.id.locationButton);
        windButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, LocationActivity.class);
            startActivityForResult(intent, LOCATION_REQUEST_CODE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == SETTINGS_REQUEST_CODE) {
                String unit = preferences.getString("temperature_unit", "celsius");
                unitTextView.setText(unit.equals("celsius") ? "°C" : "°F");

                fetchWeatherData(locationTextView.getText().toString());
            } else if (requestCode == CALENDAR_REQUEST_CODE) {
                long selectedDate = data.getLongExtra("selectedDate", 0);
                if (selectedDate > 0) {
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.setTimeInMillis(selectedDate);

                    Toast.makeText(this, "Date could not be selected because the api i have only shows 5 day range forecast " ,
                            Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == LOCATION_REQUEST_CODE) {
                String newLocation = preferences.getString("location", "Istanbul");
                locationTextView.setText(newLocation);
                fetchWeatherData(newLocation);
            }
        }
    }

    private void fetchWeatherData(String city) {
        try {
            String encodedCity = java.net.URLEncoder.encode(city, "UTF-8");

            String geocodingUrl = String.format(Locale.US,
                    "https://api.openweathermap.org/geo/1.0/direct" +
                            "?q=%s" +
                            "&limit=1" +
                            "&appid=%s",
                    encodedCity, API_KEY);

            JsonArrayRequest geocodingRequest = new JsonArrayRequest(Request.Method.GET, geocodingUrl, null,
                    response -> {
                        try {
                            if (response.length() > 0) {
                                JSONObject location = response.getJSONObject(0);
                                double lat = location.getDouble("lat");
                                double lon = location.getDouble("lon");
                                String retrievedCity = location.getString("name");
                                if (retrievedCity.equalsIgnoreCase(city)) {
                                    fetchWeatherWithCoordinates(lat, lon);
                                } else {
                                    showError("City not found");
                                    resetToDefaultLocation();
                                }
                            } else {
                                showError("City not found");
                                resetToDefaultLocation();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            showError("Error parsing location data");
                            resetToDefaultLocation();
                        }
                    },
                    error -> {
                        Log.e("WeatherAPI", "Geocoding Error: " + error.toString());
                        showError("Error finding city location");
                        resetToDefaultLocation();
                    });

            requestQueue.add(geocodingRequest);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error with city name format");
            resetToDefaultLocation();
        }
    }

    private void resetToDefaultLocation() {
        String defaultLocation = "Istanbul";
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("location", defaultLocation);
        editor.apply();

        locationTextView.setText(defaultLocation);
        fetchWeatherData(defaultLocation);
    }

    private void fetchWeatherWithCoordinates(double lat, double lon) {
        String url = String.format(Locale.US,
                "https://api.openweathermap.org/data/2.5/forecast" +
                        "?lat=%f" +
                        "&lon=%f" +
                        "&units=metric" +
                        "&cnt=4" +
                        "&appid=%s",
                lat, lon, API_KEY);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        updateUI(response);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        showError("Error parsing weather data");
                    }
                },
                error -> showError("Error fetching weather data"));

        requestQueue.add(request);
    }

    private int convertTemperature(double temp) {
        String unit = preferences.getString("temperature_unit", "celsius");
        if (unit.equals("fahrenheit")) {
            return (int) Math.round((temp * 9/5) + 32);
        }
        return (int) Math.round(temp);
    }
    private List<String> generateDayLabels() {
        List<String> dayLabels = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE", Locale.getDefault());

        dayLabels.add("Today");

        for (int i = 1; i < 4; i++) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            dayLabels.add(sdf.format(calendar.getTime()));
        }

        return dayLabels;
    }

    private void updateUI(JSONObject weatherData) throws JSONException {
        JSONArray list = weatherData.getJSONArray("list");
        JSONObject city = weatherData.getJSONObject("city");

        JSONObject currentWeather = list.getJSONObject(0);
        JSONObject currentMain = currentWeather.getJSONObject("main");

        int currentTemp = convertTemperature(currentMain.getDouble("temp"));
        temperatureTextView.setText(String.valueOf(currentTemp));

        locationTextView.setText(preferences.getString("location", "Istanbul"));

        String currentIcon = currentWeather.getJSONArray("weather")
                .getJSONObject(0)
                .getString("icon");
        updateWeatherIcon(currentIcon);

        List<String> dayLabels = generateDayLabels();
        List<WeatherForecast> forecasts = new ArrayList<>();

        for (int i = 0; i < list.length(); i++) {
            JSONObject forecast = list.getJSONObject(i);
            JSONObject main = forecast.getJSONObject("main");
            String icon = forecast.getJSONArray("weather")
                    .getJSONObject(0)
                    .getString("icon");

            String day = dayLabels.get(i);

            forecasts.add(new WeatherForecast(
                    day,
                    convertTemperature(main.getDouble("temp_max")),
                    convertTemperature(main.getDouble("temp_min")),
                    icon
            ));
        }

        weatherAdapter.setForecasts(forecasts);
    }

    private void updateWeatherIcon(String iconCode) {
        String imageUrl = "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";
        imageHelper.getImageLoader().get(imageUrl, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                if (response.getBitmap() != null) {
                    weatherImageView.setImageBitmap(response.getBitmap());
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                showError("Error loading weather icon");
            }
        });
    }

    private void showError(String message) {
        Log.e("WeatherAPI", message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}