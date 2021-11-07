package com.example.knight_radiant_app;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.View;

public class WeatherActivity extends AppCompatActivity {

    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        Toolbar toolbar = findViewById(R.id.toolbar);
        intent = new Intent(getApplicationContext(), BaseActivity.class);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        String weatherData = intent.getStringExtra("weatherData");


        Bundle toWeatherFrag = new Bundle();
        toWeatherFrag.putString("weatherData",weatherData);

        Fragment fragment = new WeatherFragment();
        fragment.setArguments(toWeatherFrag);
        FragmentTransaction fTrans = getSupportFragmentManager().beginTransaction();
        fTrans.replace(R.id.weatherFrameReplace,fragment).commit();

    }

    @Override
    public void onBackPressed() {
        finish();
    }
}