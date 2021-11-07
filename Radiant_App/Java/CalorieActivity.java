package com.example.knight_radiant_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class CalorieActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calorie);

        Intent intent = getIntent();

        String sex = intent.getStringExtra("sex");
        double weightKg = intent.getDoubleExtra("weightKg",-1);
        double heightCm = intent.getDoubleExtra("heightCm",-1);
        int age = intent.getIntExtra("age",-1);

        Bundle sendToCalorieFrag = new Bundle();
        sendToCalorieFrag.putString("sex",sex);
        sendToCalorieFrag.putDouble("weightKg",weightKg);
        sendToCalorieFrag.putDouble("heightCm",heightCm);
        sendToCalorieFrag.putInt("age",age);

        Fragment calorieFrag = new CalorieFragment();
        calorieFrag.setArguments(sendToCalorieFrag);

        FragmentTransaction fTrans = getSupportFragmentManager().beginTransaction();
        fTrans.replace(R.id.caloriePlaceholder,calorieFrag).commit();

    }

    @Override
    public void onBackPressed() {finish(); }
}
