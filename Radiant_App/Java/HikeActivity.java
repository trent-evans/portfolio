package com.example.knight_radiant_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class HikeActivity extends AppCompatActivity {

    private String hikeData;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hike);
        Toolbar toolbar = findViewById(R.id.toolbar);
//        intent = new Intent(getApplicationContext(), BaseActivity.class);

//        getSupportActionBar().setTitle("Your Local Hikes");

        Intent otherIntent = getIntent();

        hikeData = otherIntent.getStringExtra("hikeData");

        Bundle sendHikeInfo = new Bundle();
        sendHikeInfo.putString("hikeData",hikeData);


        Fragment hikeFragment = new HikeFragment();
        hikeFragment.setArguments(sendHikeInfo);

        FragmentTransaction fTrans = getSupportFragmentManager().beginTransaction();
        fTrans.replace(R.id.hikePlaceholder,hikeFragment);
        fTrans.commit();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}