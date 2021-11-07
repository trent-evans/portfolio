package com.example.knight_radiant_app;

import android.app.Application;

import android.os.AsyncTask;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;

public class WeatherViewModel extends AndroidViewModel {
    private MutableLiveData<WeatherData> jsonData;
    private WeatherRepository mWeatherRepository;

    public WeatherViewModel(Application application){
        super(application);
        mWeatherRepository = new WeatherRepository(application);
        jsonData = mWeatherRepository.getData();
    }
    public void setLocation(String location){
        mWeatherRepository.setLocation(location);
    }

    public LiveData<WeatherData> getData(){
        return jsonData;
    }


}
