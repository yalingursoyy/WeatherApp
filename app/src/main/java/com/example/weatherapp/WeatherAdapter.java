package com.example.weatherapp;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

import java.util.ArrayList;
import java.util.List;

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.WeatherViewHolder> {
    private List<WeatherForecast> forecasts;
    private ImageHelper imageHelper;

    public WeatherAdapter() {
        this.forecasts = new ArrayList<>();
    }

    public void setForecasts(List<WeatherForecast> forecasts) {
        this.forecasts = forecasts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WeatherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_weather_forecast, parent, false);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        view.setLayoutParams(params);

        imageHelper = ImageHelper.getInstance(parent.getContext());
        return new WeatherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeatherViewHolder holder, int position) {
        holder.bind(forecasts.get(position));
    }

    @Override
    public int getItemCount() {
        return forecasts.size();
    }

    class WeatherViewHolder extends RecyclerView.ViewHolder {
        private final TextView dayTextView;
        private final TextView tempTextView;
        private final ImageView weatherIcon;

        public WeatherViewHolder(@NonNull View itemView) {
            super(itemView);
            dayTextView = itemView.findViewById(R.id.dayTextView);
            tempTextView = itemView.findViewById(R.id.tempTextView);
            weatherIcon = itemView.findViewById(R.id.weatherIcon);
        }

        public void bind(WeatherForecast forecast) {
            dayTextView.setText(forecast.getDay());
            tempTextView.setText(String.format("%d°/%d°",
                    forecast.getTempHigh(), forecast.getTempLow()));

            String iconCode = forecast.getIcon();
            String imageUrl = "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";
            imageHelper.getImageLoader().get(imageUrl, new ImageLoader.ImageListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }

                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    if (response.getBitmap() != null) {
                        weatherIcon.setImageBitmap(response.getBitmap());
                    }
                }

            });
        }
    }
}