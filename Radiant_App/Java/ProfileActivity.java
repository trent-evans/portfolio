package com.example.knight_radiant_app;


import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class ProfileActivity extends AppCompatActivity {

    private Fragment profileFrag;
    private int profile_resource_id;
    public static Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intent = new Intent(getApplicationContext(), BaseActivity.class);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Edit Your Profile");

        // Read in all the information
        Intent otherIntent = getIntent();
        Bundle extras = otherIntent.getExtras();
        profile_resource_id = extras.getInt("RESOURCE_ID");

        String firstName = otherIntent.getStringExtra("firstName");
        String lastName = otherIntent.getStringExtra("lastName");
        int age = otherIntent.getIntExtra("age",-1);
        String sex = otherIntent.getStringExtra("sex");
        String city = otherIntent.getStringExtra("city");
        String country = otherIntent.getStringExtra("country");
        double heightCm = otherIntent.getDoubleExtra("heightCm",-1);
        double weightKg = otherIntent.getDoubleExtra("weightKg",-1);
        boolean prefersMetric = otherIntent.getBooleanExtra("prefersMetric",true);

        // Prepare, replace, and generate Profile Fragment

        Bundle dataToProfileFrag = new Bundle();
        dataToProfileFrag.putString("firstName",firstName);
        dataToProfileFrag.putString("lastName",lastName);
        dataToProfileFrag.putInt("age",age);
        dataToProfileFrag.putString("sex",sex);
        dataToProfileFrag.putString("city",city);
        dataToProfileFrag.putString("country",country);
        dataToProfileFrag.putBoolean("prefersMetric",prefersMetric);
        dataToProfileFrag.putDouble("heightCm",heightCm);
        dataToProfileFrag.putDouble("weightKg",weightKg);
        dataToProfileFrag.putInt("RESOURCE_ID",profile_resource_id);

        profileFrag = new ProfileFragment();
        profileFrag.setArguments(dataToProfileFrag);

        FragmentTransaction fTrans = getSupportFragmentManager().beginTransaction();
        fTrans.replace(R.id.profileFragmentPlaceholder, profileFrag).commit();

        // Handle the save changes button
//        saveChanges = findViewById(R.id.saveChangesButton);
//        saveChanges.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View view){
//                // TODO: Implement
//                Toast.makeText(ProfileAcitivty.this,"TODO: Implement",Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }

}