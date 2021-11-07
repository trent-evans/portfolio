package com.example.knight_radiant_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;

public class GenericActivity extends AppCompatActivity {

    public static Bitmap profilepic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generic);

        Intent intentData = getIntent();
        String fragmentFlag = intentData.getStringExtra("flag");

        if(fragmentFlag.equals("profile")){

            String username = intentData.getStringExtra("username");

            Bundle dataToProfileFrag = new Bundle();
            dataToProfileFrag.putString("username",username);

            Fragment profileFrag = new ProfileFragment();
            profileFrag.setArguments(dataToProfileFrag);

            FragmentTransaction fTrans = getSupportFragmentManager().beginTransaction();
            fTrans.replace(R.id.genericFragmentPlaceholder, profileFrag).commit();

        }else if(fragmentFlag.equals("bmi")){

            String username = intentData.getStringExtra("username");

            Bundle dataToBMIFrag = new Bundle();
            dataToBMIFrag.putString("username",username);

            Fragment bmiFrag = new BMIFragment();
            bmiFrag.setArguments(dataToBMIFrag);

            FragmentTransaction fTrans = getSupportFragmentManager().beginTransaction();
            fTrans.replace(R.id.genericFragmentPlaceholder,bmiFrag).commit();

        }else if(fragmentFlag.equals("calorie")){

            String username = intentData.getStringExtra("username");

            Bundle sendToCalorieFrag = new Bundle();
            sendToCalorieFrag.putString("username",username);

            Fragment calorieFrag = new CalorieFragment();
            calorieFrag.setArguments(sendToCalorieFrag);

            FragmentTransaction fTrans = getSupportFragmentManager().beginTransaction();
            fTrans.replace(R.id.genericFragmentPlaceholder,calorieFrag).commit();

        }else if(fragmentFlag.equals("hike")){

            String hikeData = intentData.getStringExtra("hikeData");

            Bundle sendHikeInfo = new Bundle();
            sendHikeInfo.putString("hikeData",hikeData);

            Fragment hikeFragment = new HikeFragment();
            hikeFragment.setArguments(sendHikeInfo);

            FragmentTransaction fTrans = getSupportFragmentManager().beginTransaction();
            fTrans.replace(R.id.genericFragmentPlaceholder,hikeFragment).commit();

        }else if(fragmentFlag.equals("weather")){

            String weatherData = intentData.getStringExtra("weatherData");

            Bundle toWeatherFrag = new Bundle();
            toWeatherFrag.putString("weatherData",weatherData);

            Fragment fragment = new WeatherFragment();
            fragment.setArguments(toWeatherFrag);
            FragmentTransaction fTrans = getSupportFragmentManager().beginTransaction();
            fTrans.replace(R.id.genericFragmentPlaceholder,fragment).commit();
        }else if(fragmentFlag.equals("new_user")){

            Fragment newUserFrag = new NewUserFragment();
            FragmentTransaction fTrans = getSupportFragmentManager().beginTransaction();
            fTrans.replace(R.id.genericFragmentPlaceholder,newUserFrag).commit();

        }else if(fragmentFlag.equals("step")){

            String  user = intentData.getStringExtra("name");
            Bundle sendStepInfo = new Bundle();
            sendStepInfo.putString("name",user);

            Fragment newStepFrag = new StepFragment();

            FragmentTransaction fTrans = getSupportFragmentManager().beginTransaction();
            fTrans.replace(R.id.genericFragmentPlaceholder,newStepFrag).commit();
        }
    }


    @Override
    public void onBackPressed(){
        finish();
    }
}