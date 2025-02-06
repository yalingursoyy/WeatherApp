package com.example.weatherapp;

public class WeatherForecast {
    private String day;
    private int tempHigh;
    private int tempLow;
    private String icon;

    public WeatherForecast(String day, int tempHigh, int tempLow, String icon) {
        this.day = day;
        this.tempHigh = tempHigh;
        this.tempLow = tempLow;
        this.icon = icon;
    }

    public String getDay() {
        return day;
    }

    public int getTempHigh() {
        return tempHigh;
    }

    public int getTempLow() {
        return tempLow;
    }

    public String getIcon() {
        return icon;
    }
}