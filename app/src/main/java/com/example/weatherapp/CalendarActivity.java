package com.example.weatherapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CalendarView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class CalendarActivity extends AppCompatActivity {
    private CalendarView calendarView;
    private Button selectDateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        calendarView = findViewById(R.id.calendarView);
        selectDateButton = findViewById(R.id.selectDateButton);

        Calendar calendar = Calendar.getInstance();
        long now = calendar.getTimeInMillis();
        calendar.add(Calendar.DAY_OF_YEAR, 7);
        long sevenDaysLater = calendar.getTimeInMillis();

        calendarView.setMinDate(now);
        calendarView.setMaxDate(sevenDaysLater);

        final Calendar selectedCalendar = Calendar.getInstance();
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedCalendar.set(year, month, dayOfMonth);
        });

        selectDateButton.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("selectedDate", calendarView.getDate());
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        });
    }
}