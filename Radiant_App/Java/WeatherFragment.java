package com.example.knight_radiant_app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Observer;
import java.util.Scanner;

public class WeatherFragment extends Fragment {

    private DecimalFormat decimalFormat = new DecimalFormat("###.#");
    private final String urlBase = "https://api.openweathermap.org/data/2.5/weather?zip=";
    ImageView weatherpic;
    TextView weatherDescriptionTV;
    TextView currentTempTV;
    TextView feelsLikeTV;
    TextView humidityTV;
    TextView minTempTV;
    TextView maxTempTV;
    private WeatherViewModel WeatherViewModel;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Create the view model

        View view = inflater.inflate(R.layout.weather_fragment, container, false);

        String weatherData = getArguments().getString("weatherData");
        WeatherData weather = new WeatherData();

        try {
            weather = JSONWeatherUtils.getWeatherData(weatherData);
        } catch (JSONException e) {
            e.printStackTrace();

        }


        // fill all of these using the observer
        weatherpic = view.findViewById(R.id.weatherPic);
        currentTempTV = view.findViewById(R.id.temperatureTextView);
        feelsLikeTV = view.findViewById(R.id.feelsLikeTextView);
        humidityTV = view.findViewById(R.id.humidityText);
        minTempTV = view.findViewById(R.id.tempMinTV);
        maxTempTV = view.findViewById(R.id.tempMaxTV);
        weatherDescriptionTV = view.findViewById(R.id.weatherDescriptionTV);
        //Set the observer
        WeatherViewModel = ViewModelProviders.of(this).get(WeatherViewModel.class);
        WeatherViewModel.getData().observe(getViewLifecycleOwner(), nameObserver);
//
//            weatherDescriptionTV.setText(currentCondition.getWeatherDescription());
//
//        if(currentCondition.getWeatherDescription().equals("Clear Sky")) {
//            weatherpic.setImageResource(R.drawable.clear_skies);
//        }else if(currentCondition.getWeatherDescription().equals("Few Clouds")) {
//            weatherpic.setImageResource(R.drawable.partly_cloudy_icon);
//        }else if(currentCondition.getWeatherDescription().equals("Scattered Clouds") || currentCondition.getWeatherDescription().equals("Broken Clouds") || currentCondition.getWeatherDescription().equals("Overcast Clouds")) {
//            weatherpic.setImageResource(R.drawable.cloudy_skies);
//        }else if(currentCondition.getWeatherDescription().equals("Rain") || currentCondition.getWeatherDescription().equals("Shower Rain")){
//            weatherpic.setImageResource(R.drawable.rain_icon);
//        }else if(currentCondition.getWeatherDescription().equals("Thunderstorm")) {
//            weatherpic.setImageResource(R.drawable.thunderstorm_icon);
//        }else if(currentCondition.getWeatherDescription().equals("Snow")){
//            weatherpic.setImageResource(R.drawable.snowstorm_icon);
//        }else{
//            weatherpic.setImageResource(R.drawable.mist_icon);
//        }
//
//        currentTempTV.setText(makeTemperatureString(currentCondition.getTempK()));
//        String one = makeTemperatureString(currentCondition.getFeelsLikeK()) + ")";
//        feelsLikeTV.setText(one);
//        String two = currentCondition.getHumidity() + "%";
//        humidityTV.setText(two);
//        minTempTV.setText(makeTemperatureString(currentCondition.getTempMinK()));
//        maxTempTV.setText(makeTemperatureString(currentCondition.getTempMaxK()));


        return view;
    }


    private String makeTemperatureString(double kelvin) {
        Double cFromK = kelvinToCelcius(kelvin);
        Double fFromK = kelvinToFarenheit(kelvin);

        return (decimalFormat.format(fFromK) + "\u00B0 F / " + decimalFormat.format(cFromK) + "\u00B0 C");
    }

    private double kelvinToCelcius(double kelvin) {
        return (kelvin - 273.15);
    }

    private double kelvinToFarenheit(double kelvin) {
        return kelvinToCelcius(kelvin) * (9.0 / 5.0) + 32.0;
    }

    final androidx.lifecycle.Observer<WeatherData> nameObserver = new androidx.lifecycle.Observer<WeatherData>() {
        @Override
        public void onChanged(@Nullable final WeatherData weatherData) {
            // Update the UI if this data variable changes
            if (weatherData != null) {

                weatherDescriptionTV.setText(weatherData.getCurrentCondition().getWeatherDescription());

                if (weatherData.getCurrentCondition().getWeatherDescription().equals("Clear Sky")) {
                    weatherpic.setImageResource(R.drawable.clear_skies);
                } else if (weatherData.getCurrentCondition().getWeatherDescription().equals("Few Clouds")) {
                    weatherpic.setImageResource(R.drawable.partly_cloudy_icon);
                } else if (weatherData.getCurrentCondition().getWeatherDescription().equals("Scattered Clouds") || weatherData.getCurrentCondition().getWeatherDescription().equals("Broken Clouds") || weatherData.getCurrentCondition().getWeatherDescription().equals("Overcast Clouds")) {
                    weatherpic.setImageResource(R.drawable.cloudy_skies);
                } else if (weatherData.getCurrentCondition().getWeatherDescription().equals("Rain") || weatherData.getCurrentCondition().getWeatherDescription().equals("Shower Rain")) {
                    weatherpic.setImageResource(R.drawable.rain_icon);
                } else if (weatherData.getCurrentCondition().getWeatherDescription().equals("Thunderstorm")) {
                    weatherpic.setImageResource(R.drawable.thunderstorm_icon);
                } else if (weatherData.getCurrentCondition().getWeatherDescription().equals("Snow")) {
                    weatherpic.setImageResource(R.drawable.snowstorm_icon);
                } else {
                    weatherpic.setImageResource(R.drawable.mist_icon);
                }

                currentTempTV.setText(makeTemperatureString(weatherData.getCurrentCondition().getTempK()));
                String one = makeTemperatureString(weatherData.getCurrentCondition().getFeelsLikeK()) + ")";
                feelsLikeTV.setText(one);
                String two = weatherData.getCurrentCondition().getHumidity() + "%";
                humidityTV.setText(two);
                minTempTV.setText(makeTemperatureString(weatherData.getCurrentCondition().getTempMinK()));
                maxTempTV.setText(makeTemperatureString(weatherData.getCurrentCondition().getTempMaxK()));
            }
        }
    };

}
