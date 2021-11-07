package com.example.knight_radiant_app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.widget.Toast;

import com.amplifyframework.AmplifyException;
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.storage.s3.AWSS3StoragePlugin;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

public class BaseActivity extends AppCompatActivity {

    private static String username;
    private LinearLayout weatherButton;
    private LinearLayout hikeButton;
    private LocationManager locationManager;
    private ProfileViewModel model;
    private Bitmap bmp;
    private FloatingActionButton profilePicFab;
    private WeatherViewModel WeatherViewModel;
    private boolean isTablet;
    private double lat = 0;
    private double log = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        Intent intent = getIntent();
        username = intent.getStringExtra("username");

        // Set toolbar title
        model = ViewModelProviders.of(this).get(ProfileViewModel.class);
        if(model.getUserData() != null){
            model.getUserData().observe(this, new Observer<List<User>>() {
                @Override
                public void onChanged(List<User> users) {
                    model.setUserDataViaUsername(username);
                    String firstName = model.getFirstName();
                    String lastName = model.getLastName();
                    if(lastName.endsWith("s")){
                        getSupportActionBar().setTitle(firstName + " " + lastName + "' Homepage");
                    }else{
                        getSupportActionBar().setTitle(firstName + " " + lastName + "'s Homepage");
                    }
                }
            });
        }

        try {
            // Add these lines to add the AWSCognitoAuthPlugin and AWSS3StoragePlugin plugins
            Amplify.addPlugin(new AWSCognitoAuthPlugin());
            Amplify.addPlugin(new AWSS3StoragePlugin());
            Amplify.configure(getApplicationContext());

            uploadDatabase();

            Log.i("MyAmplifyApp", "Initialized Amplify, and uploaded Example to bucket");
        } catch (AmplifyException error) {
            Log.e("MyAmplifyApp", "Could not initialize Amplify", error);
        }

        isTablet = isTablet();

        RequestQueue activityQueue = Volley.newRequestQueue(getApplicationContext());
        if (isTablet) {
            EditText zipCodeET = findViewById(R.id.zipCodeEditText);
            String zipCodeString = zipCodeET.getText().toString();
            if (zipCodeString.length() != 5) {
                Toast.makeText(getApplicationContext(), "Enter a valid 5-digit zip code", Toast.LENGTH_SHORT).show();
                return;
            }
            String zipUrl = "https://www.zipcodeapi.com/rest/" + key + "/info.json/" + zipCodeString + "/radians";


            JsonObjectRequest locRequest = new JsonObjectRequest(zipUrl, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        lat = response.getDouble("lat");
                        log = response.getDouble("lng");
                    } catch (JSONException e) {
                        Toast.makeText(getApplicationContext(), "Enter a real 5-digit zip code", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }

            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            });
            activityQueue.add(locRequest);
            activityQueue.start();
        }

        weatherButton = findViewById(R.id.weather_layout);
        hikeButton = findViewById(R.id.hike_layout);
        profilePicFab = findViewById(R.id.profile_pic_fab);

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            profilePicFab.setEnabled(false);
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE }, 0);
        }

        profilePicFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, 100);
            }
        });

        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET}, 200);
        }

        LinearLayout profileButton = findViewById(R.id.layout_profile);

        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isTablet) {
                    Bundle dataToProfileFrag = new Bundle();
                    dataToProfileFrag.putString("username",username);

                    Fragment profFrag = new ProfileFragment();
                    profFrag.setArguments(dataToProfileFrag);

                    FragmentTransaction fTrans = getSupportFragmentManager().beginTransaction();
                    fTrans.replace(R.id.tablet_fragment_container, profFrag).commit();

                } else {

                    Intent switchToProfile = new Intent(getApplicationContext(), GenericActivity.class);
                    switchToProfile.putExtra("username",username);
                    switchToProfile.putExtra("flag", "profile");

                    startActivity(switchToProfile);
                }
            }
        });

        LinearLayout bmiButton = findViewById(R.id.bmi_layout);
        bmiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isTablet) {

                    Bundle sendBmiInfo = new Bundle();
                    sendBmiInfo.putString("username",username);

                    Fragment bmiFragment = new BMIFragment();
                    bmiFragment.setArguments(sendBmiInfo);

                    FragmentTransaction fTrans = getSupportFragmentManager().beginTransaction();
                    fTrans.replace(R.id.tablet_fragment_container, bmiFragment).commit();

                } else {

                    Intent switchToBMI = new Intent(getApplicationContext(), GenericActivity.class);
                    switchToBMI.putExtra("flag", "bmi");
                    switchToBMI.putExtra("username",username);
                    startActivity(switchToBMI);
                }
            }
        });

        LinearLayout calorieButton = findViewById(R.id.calorie_layout);
        calorieButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isTablet) {
                    Bundle sendCalorieInfo = new Bundle();
                    sendCalorieInfo.putString("username",username);

                    Fragment fragment = new CalorieFragment();
                    fragment.setArguments(sendCalorieInfo);

                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.tablet_fragment_container, fragment);
                    transaction.commit();
                } else {
                    Intent switchToCalorie = new Intent(getApplicationContext(), GenericActivity.class);
                    switchToCalorie.putExtra("flag", "calorie");
                    switchToCalorie.putExtra("username",username);

                    startActivity(switchToCalorie);
                }

            }
        });

        hikeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                LocationData locationData = new LocationData();
                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                @SuppressLint("MissingPermission") Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                RequestQueue hikeQueue = Volley.newRequestQueue(getApplicationContext());
                if (locationGPS != null) { // For a cell phone that can pull the location
                    NetworkUtils.setmLatitude(locationGPS.getLatitude());
                    NetworkUtils.setmLongitude(locationGPS.getLongitude());
                }

                String url = NetworkUtils.buildHikeURLFromString().toString();
                System.out.println(url);

                JsonObjectRequest request = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        HikeData hikeData= new HikeData();
                        hikeData.getCurrentCondition().setHikeJsonData(response.toString());


                        if (isTablet) {
                            Bundle sendHikeInfo = new Bundle();
                            sendHikeInfo.putString("hikeData", response.toString());
                            Fragment hikeFragment = new HikeFragment();
                            hikeFragment.setArguments(sendHikeInfo);

                            FragmentTransaction fTrans = getSupportFragmentManager().beginTransaction();
                            fTrans.replace(R.id.tablet_fragment_container, hikeFragment);
                            fTrans.commit();
                        } else {
//                            Intent switchToHike = new Intent(getApplicationContext(), HikeActivity.class);
                            Intent switchToHike = new Intent(getApplicationContext(), GenericActivity.class);
                            switchToHike.putExtra("flag", "hike");
                            switchToHike.putExtra("hikeData", hikeData.getCurrentCondition().getHikeJsonData());
                            startActivity(switchToHike);
                        }
                    }

                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
                hikeQueue.add(request);
            }
        });


        weatherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LocationData locationData = new LocationData();
                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                @SuppressLint("MissingPermission") Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if (locationGPS != null) {
                    NetworkUtils.setmLatitude(locationGPS.getLatitude());
                    NetworkUtils.setmLongitude(locationGPS.getLongitude());

                } else {
                    Toast.makeText(getApplicationContext(),"Error getting location information - please try again later",Toast.LENGTH_SHORT).show();
                    return;
                }
                List<Address> addresses = null;
                try {
                    if (!isTablet) {
                        addresses = geocoder.getFromLocation(NetworkUtils.getmLatitude(), NetworkUtils.getmLongitude(), 1);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String zipCode, countryCode = "";

                if (isTablet) {
                    EditText zipCodeET = findViewById(R.id.zipCodeEditText);
                    NetworkUtils.setZipCode(zipCodeET.getText().toString());

                } else {
                    zipCode = addresses.get(0).getPostalCode();
                    countryCode = addresses.get(0).getCountryCode();
                    NetworkUtils.setZipCode(zipCode);
                    NetworkUtils.setCountryCode(countryCode);

                }

                RequestQueue weatherQueue = Volley.newRequestQueue(getApplicationContext());
                String url = NetworkUtils.buildWeatherURLFromString().toString();
                JsonObjectRequest request = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (isTablet) {
                            Bundle toWeatherFrag = new Bundle();
                            toWeatherFrag.putString("weatherData", response.toString());
                            Fragment fragment = new WeatherFragment();
                            fragment.setArguments(toWeatherFrag);
                            FragmentTransaction fTrans = getSupportFragmentManager().beginTransaction();
                            fTrans.replace(R.id.tablet_fragment_container, fragment).commit();

                        } else {
                            Intent switchToWeather = new Intent(getApplicationContext(), GenericActivity.class);
                            switchToWeather.putExtra("flag", "weather");
                            switchToWeather.putExtra("weatherData", response.toString());
                            startActivity(switchToWeather);
                        }
                    }

                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });

                weatherQueue.add(request);


            }
        });
        if(!isTablet) {
            LinearLayout stepButton = findViewById(R.id.step_layout);
            stepButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (isTablet) {

                        Bundle sendBmiInfo = new Bundle();


                        Fragment stepFragment = new StepFragment();
                        stepFragment.setArguments(sendBmiInfo);

                        FragmentTransaction fTrans = getSupportFragmentManager().beginTransaction();
                        fTrans.replace(R.id.tablet_fragment_container, stepFragment).commit();

                    } else {

                        Intent switchToStep = new Intent(getApplicationContext(), GenericActivity.class);
                        switchToStep.putExtra("flag", "step");

                        startActivity(switchToStep);
                    }
                }
            });
        }
    }

    private void uploadDatabase() {
        File databaseFile = new File("data/data/com.example.knight_radiant_app/databases/user.db");

        Amplify.Storage.uploadFile(
                "DatabaseKey",
                databaseFile,
                result -> Log.i("MyAmplifyApp", "Successfully uploaded: " + result.getKey()),
                storageFailure -> Log.e("MyAmplifyApp", "Upload failed", storageFailure)
        );
    }

        void loadWeatherData(String location){
        //pass the location in to the view model (this is just a zip code)
        WeatherViewModel.setLocation(location);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 200) {
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                    weatherButton.setEnabled(true);
                    hikeButton.setEnabled(true);
                } else {
                    weatherButton.setEnabled(false);
                    hikeButton.setEnabled(false);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 1) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {

            GenericActivity.profilepic = (Bitmap) data.getExtras().get("data");
            bmp = GenericActivity.profilepic;
            ProfilePic.setPic(bmp);
            profilePicFab.setImageBitmap(bmp);
        }
    }

    boolean isTablet() {
        return getResources().getBoolean(R.bool.isTablet);
    }

}

