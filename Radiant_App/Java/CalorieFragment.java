package com.example.knight_radiant_app;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import java.util.List;

public class CalorieFragment extends Fragment {


    private String activityLevelSelection;
    private String goalSelection;
    private String username;
    private int activityIndex;
    private int goalIndex;
    private String sex;
    private double weightKg, heightCm;
    private int age;
    ProfileViewModel model;
    double[] activityModifierArr = new double[]{1.2, 1.375, 1.55, 1.725, 1.9};
    Spinner goalSpinner, activityLevelSpinner;

    /**
     * Harris bendict formula for calculating BMR
     * http://www.checkyourhealth.org/eat-healthy/cal_calculator.php
     *
     * @param sex
     * @param bodyWeightLbs
     * @param heightInches
     * @param age
     * @return
     */
    double calculateBMR(String sex, double bodyWeightLbs, double heightInches, int age) {
        double BMR;
        if (sex == "male") {
            BMR = 66 + (6.3 * bodyWeightLbs) + (12.9 * heightInches) - (6.8 * age);
        } else {
            BMR = 655 + (4.3 * bodyWeightLbs) + (4.7 * heightInches) - (4.7 * age);
        }
        return BMR;
    }

    /**
     * https://jcdfitness.com/2010/03/the-perfect-caloric-surplus/#:~:text=In%20general%2C%20somewhere%20between%20100%2D300%20calories%20above%20maintenance%20intake,pound%20of%20muscle%20you%20gain.
     *
     * @param BMR
     * @param actvitiyModifier
     * @param goal
     * @return
     */
    int calculateCalories(double BMR, double actvitiyModifier, int goal) {
        int maintenance = (int) (BMR * actvitiyModifier);
        if (goal == 1) { // Maintenance
            if(maintenance < 1200){
                return 1200;
            }
            return maintenance;
        } else if (goal == 0) { // Loss
            int deficit = maintenance - 500;
            if (deficit < 1200) {
                return 1200;
            } else {
                return deficit;
            }
        } else { // Gainz
            int surplus = maintenance + 150;
            if(surplus<1200){
                return 1200;    
            }
            return surplus;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.calorie_fragment, container, false);
        Bundle data = getArguments();

        username = data.getString("username");
        model = ViewModelProviders.of(getActivity()).get(ProfileViewModel.class);

        LiveData<List<User>> nullCheck = model.getUserData();

        if(nullCheck == null){
            Toast.makeText(getContext(),"No profile data found for username: " + "blackthorn", Toast.LENGTH_SHORT);
        }else{
            model.getUserData().observe(getActivity(), new Observer<List<User>>() {
                @Override
                public void onChanged(List<User> users) {
                    model.setUserDataViaUsername(username);

                    sex = model.getSex();
                    weightKg = model.getWeightKg();
                    heightCm = model.getHeightCm();
                    age = model.getAge();


                }
            });
        }

        //Adult male: 66 + (6.3 x body weight in lbs.) + (12.9 x height in inches) - (6.8 x age in years) = BMR
        //Adult female: 655 + (4.3 x weight in lbs.) + (4.7 x height in inches) - (4.7 x age in years) = BMR

        activityLevelSpinner = view.findViewById(R.id.activity_spinner);
// Create an ArrayAdapter using the string array and a default spinner layout
        Context context = getActivity();
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                R.array.activity_level_Array, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        activityLevelSpinner.setAdapter(adapter);
// Set the listener
        activityLevelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> adapterView, View view, int position, long id) {

                activityLevelSelection = (String) adapterView.getItemAtPosition(position);
                activityIndex = position;

                System.out.println(activityLevelSelection);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> adapterView) {
                // TODO Auto-generated method stub
            }
        });


        goalSpinner = view.findViewById(R.id.goal_spinner);

        ArrayAdapter<CharSequence> goalAdapter = ArrayAdapter.createFromResource(context,
                R.array.weight_goal_array, android.R.layout.simple_spinner_item);
        goalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        goalSpinner.setAdapter(goalAdapter);
// Set the listener
        goalSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> adapterView, View view, int position, long id) {

                goalSelection = (String) adapterView.getItemAtPosition(position);
                goalIndex = position;
                System.out.println(goalSelection);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> adapterView) {
                // TODO Auto-generated method stub
            }
        });


        Button button = (Button) view.findViewById(R.id.calcCalsButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double BMR = calculateBMR(sex, weightMtoI(weightKg), heightMtoI(heightCm), age);
                int calorieAmount = calculateCalories(BMR, activityModifierArr[activityIndex], goalIndex);
                String cals = String.valueOf(calorieAmount);
                TextView placeholder = (TextView) getView().findViewById(R.id.calories);
                placeholder.setText(cals);
            }
        });


        return view;
    }

    double weightMtoI(double weightKg){
        return weightKg*2.205;
    }

    double heightMtoI(double heightCm){
        return heightCm/2.54;
    }

}
