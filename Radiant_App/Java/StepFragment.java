package com.example.knight_radiant_app;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// circle bar code from https://github.com/lopspower/CircularProgressBar
public class StepFragment extends Fragment implements View.OnClickListener {
    SensorManager mSensorManager;
    TextView stepsTaken1;
    Sensor mStepCounter;
    CircularProgressBar circularProgressBar = null;

    int oldTotalSteps;
    int currentSteps;
    int tempSteps;
    boolean running;
    int trashSteps;
    Float totalSteps;
    Float tempFloat;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.step_fragment, container, false);
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 1);
        }
        super.onCreate(savedInstanceState);


        final GestureDetector gesture = new GestureDetector(getActivity(),
                new GestureDetector.SimpleOnGestureListener() {

                    @Override
                    public boolean onDown(MotionEvent e) {
                        return true;
                    }

                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                                           float velocityY) {
                        final int SWIPE_MIN_DISTANCE = 120;
                        final int SWIPE_MAX_OFF_PATH = 250;
                        final int SWIPE_THRESHOLD_VELOCITY = 200;
                        try {
                            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                                return false;
                            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
                                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                                MediaPlayer mPlayer = MediaPlayer.create(getContext(), R.raw.ping);
                                mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                running = false;
                                onPause();
                                mPlayer.start();
//                               //right to left
                            } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
                                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                                MediaPlayer mPlayer = MediaPlayer.create(getContext(), R.raw.startping);
                                mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                running = true;
                                onResume();
                                mPlayer.start();
//                                //left to right
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return super.onFling(e1, e2, velocityX, velocityY);
                    }
                });
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gesture.onTouchEvent(event);
            }
        });


        stepsTaken1 = (TextView) view.findViewById(R.id.tv_stepsTaken1);
        stepsTaken1.setText("temp");
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mStepCounter = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        circularProgressBar = view.findViewById(R.id.yourCircularProgressbar);
// Set Progress
        circularProgressBar.setProgress(0f);
// or with animation
        circularProgressBar.setProgressWithAnimation(0, (long) 1000); // =1s

// Set Progress Max
        circularProgressBar.setProgressMax(200f);

// Set ProgressBar Col
        circularProgressBar.setProgressBarColor(Color.BLACK);
// or with gradient
//        circularProgressBar.setProgressBarColorStart(Color.GRAY);
//        circularProgressBar.setProgressBarColorEnd(ContextCompat.getColor(getContext(), R.color.color2));
//        circularProgressBar.setProgressBarColorDirection(CircularProgressBar.GradientDirection.TOP_TO_BOTTOM);

// Set background ProgressBar Color
        circularProgressBar.setBackgroundProgressBarColor(Color.GRAY);
// or with gradient
//        circularProgressBar.setBackgroundProgressBarColorStart(Color.WHITE);
//        circularProgressBar.setBackgroundProgressBarColorEnd(ContextCompat.getColor(getContext(), R.color.color3));
//        circularProgressBar.setBackgroundProgressBarColorDirection(CircularProgressBar.GradientDirection.BOTTOM_TO_END);

// Set Width
        circularProgressBar.setProgressBarWidth(12f); // in DP
        circularProgressBar.setBackgroundProgressBarWidth(7f); // in DP

// Other
        circularProgressBar.setRoundBorder(true);
        circularProgressBar.setStartAngle(180f);
        circularProgressBar.setProgressDirection(CircularProgressBar.ProgressDirection.TO_RIGHT);

        Button resetButton = (Button) view.findViewById(R.id.reset_step_button);

        resetButton.setOnClickListener((View.OnClickListener) this);


        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


    }

    @NonNull
    private final SensorEventListener mListener = new SensorEventListener() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            if (running) {

                if (totalSteps == null) {
                    Toast.makeText(getContext(), "Swipe left to pause, swipe right to start!", Toast.LENGTH_LONG).show();
                    oldTotalSteps = (int) sensorEvent.values[0];
                } else {
                    loadData();
                }
                totalSteps = sensorEvent.values[0];
                if (trashSteps != 0) {
                    oldTotalSteps = (int) (oldTotalSteps + (totalSteps - trashSteps));
                    trashSteps = 0;
                }

                int currentSteps = totalSteps.intValue() - oldTotalSteps;
                stepsTaken1.setText("" + String.valueOf(currentSteps));
                //save this value right here, currentSteps, save on this line
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formatDateTime = now.format(formatter);
                //save this formatted Date time int he format 2020-11-09 11:44:44
                circularProgressBar.setProgressWithAnimation((float) currentSteps);
            }

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

    @Override
    public void onResume() {
        super.onResume();

        running = true;
        if (mStepCounter != null) {
            mSensorManager.registerListener(mListener, mStepCounter, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Toast.makeText(getContext(), "Count sensor not available!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPause() {
        running = false;
        super.onPause();


        if (mStepCounter != null) {

            mSensorManager.unregisterListener(mListener);
            trashSteps = totalSteps.intValue();
        }
    }

    private void resetSteps(View view) {
        circularProgressBar.setProgress(0f);
        oldTotalSteps = totalSteps.intValue();
        TextView text = view.findViewById(R.id.tv_stepsTaken1);
        int zero = 0;
        stepsTaken1.setText("" + String.valueOf(zero));
        saveData();

    }


    @Override
    public void onClick(View view) {
        saveData();

        resetSteps(view);
    }

    private void saveData() {

        int currentSteps = totalSteps.intValue() - oldTotalSteps;


        Context context = getActivity();
        SharedPreferences sharedPref = context.getSharedPreferences(
                "myPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPref.edit();
        edit.putFloat("key1", currentSteps);
        edit.apply();

    }

    private void loadData() {
        Context context = getActivity();
        SharedPreferences sharedPref = context.getSharedPreferences(
                "myPrefs", Context.MODE_PRIVATE);
        float savedNumber = sharedPref.getFloat("key1", 0f);
        currentSteps = (int) savedNumber;
    }


}
