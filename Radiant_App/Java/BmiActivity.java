package com.example.knight_radiant_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;



public class BmiActivity extends AppCompatActivity {

    private double heightCm, weightKg;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmi);
        Toolbar toolbar = findViewById(R.id.toolbar);
//        intent = new Intent(getApplicationContext(), BaseActivity.class);
        getSupportActionBar().setTitle("Your BMI Information");

        Intent intent = getIntent();

        heightCm = intent.getDoubleExtra("heightCm",-1);
        weightKg = intent.getDoubleExtra("weightKg",-1);

        Bundle sendBmiInfo = new Bundle();
        sendBmiInfo.putDouble("heightCm",heightCm);
        sendBmiInfo.putDouble("weightKg",weightKg);

        Fragment bmiFragment = new BMIFragment();
        bmiFragment.setArguments(sendBmiInfo);

        FragmentTransaction fTrans = getSupportFragmentManager().beginTransaction();
        fTrans.replace(R.id.bmiPlaceholder,bmiFragment);
        fTrans.commit();

    }

    @Override
    public void onBackPressed() {
        finish();
    }
}