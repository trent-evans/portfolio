package com.example.knight_radiant_app;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class NewUserFragment extends Fragment {

    private EditText usernameEditText, firstNameEditText, lastNameEditText, ageEditText,
            cityEditText, countryEditText, feetEditText ,inchEditText, weightEditText, passwordEditText;
    private Switch sexSwitch, measurementSwitch;
    private FloatingActionButton fab;
    private Button createUserButton;
    private String decimalFormat = "%.1f";
    private ProfileViewModel model;
    private boolean savingChanges;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_new_user,container,false);

        savingChanges = false;
        usernameEditText = view.findViewById(R.id.userNameET);
        passwordEditText = view.findViewById(R.id.passwordET);
        firstNameEditText = view.findViewById(R.id.firstNameEditText_newUser);
        lastNameEditText = view.findViewById(R.id.lastNameEditText_newUser);
        ageEditText = view.findViewById(R.id.ageEditText_newUser);
        cityEditText = view.findViewById(R.id.cityEditText_newUser);
        countryEditText = view.findViewById(R.id.countryEditText_newUser);
        inchEditText = view.findViewById(R.id.heightEditText_inches_newUser);
        feetEditText = view.findViewById(R.id.heightEditText_feet_newUser);
        weightEditText = view.findViewById(R.id.weightEditText_newUser);
        sexSwitch = view.findViewById(R.id.sexSwitch_newUser);
        createUserButton = view.findViewById(R.id.createNewUserButton);

        weightEditText.setText("0");
        feetEditText.setText("0");
        inchEditText.setText("0");

        model = ViewModelProviders.of(getActivity()).get(ProfileViewModel.class);
        LiveData<List<User>> nullCheck = model.getUserData();

        if(nullCheck == null){ // Initialize the user data base if there isn't one
            model.createNewUserEntry("blackthorn","Dalinar","Kholin",
                    68,"Male","Kholinar","Alethkar",193.0,90,
                    true, "Maintain");
            nullCheck = model.getUserData();
        }

        if(nullCheck != null){
            model.getUserData().observe(getActivity(), new Observer<List<User>>() {
                @Override
                public void onChanged(List<User> users) {
                    if(savingChanges){
                        savingChanges = false;
                        Toast.makeText(getContext(),"User " + usernameEditText.getText().toString() + " created!",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        createUserButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){

                EditText[] etArray = {usernameEditText,passwordEditText,firstNameEditText,lastNameEditText,
                ageEditText,cityEditText,countryEditText,inchEditText,feetEditText,weightEditText};
                String username = usernameEditText.getText().toString();
                if(allFieldsAreNotFull(etArray)){ // Make sure all the text fields have things in them
                    Toast.makeText(getContext(),"All fields must be filled to proceed",Toast.LENGTH_LONG).show();
                    return;
                }else if(userNameIsTaken(username)){ // Check if the username is already in the database
                    Toast.makeText(getContext(),"The Username " + username + " is already taken\nPlease pick another",Toast.LENGTH_LONG).show();
                    return;
                }else{ // If we pass both of those checks, create the new user

                    String password = passwordEditText.getText().toString();
                    String firstName = firstNameEditText.getText().toString();
                    String lastName = lastNameEditText.getText().toString();
                    int age = Integer.parseInt(ageEditText.getText().toString());
                    String city = cityEditText.getText().toString();
                    String country = countryEditText.getText().toString();
                    String sex;
                    if (sexSwitch.isChecked()) {
                        sex = "Male";
                    } else {
                        sex = "Female";
                    }

                    Integer heightFeet = Integer.parseInt(feetEditText.getText().toString());
                    Integer heightIn  = Integer.parseInt(inchEditText.getText().toString());
                    double heightCm = feetAndInchesToCm(heightFeet,heightIn);

                    Double weightLb = Double.parseDouble(weightEditText.getText().toString());
                    double weightKg = imperialToMetricWeight(weightLb);


                    model.createNewUserEntry(username, firstName, lastName, age, sex, city, country,
                            heightCm, weightKg, false, password);

                    savingChanges = true;
                }
            }
        });


        return view;
    }

    private boolean userNameIsTaken(String username){

        LiveData<List<User>> userData = model.getUserData();

        if(userData != null){ // If the user data is null, then there are no users

            // Is this scalable?  No, and it's generally terrible practice.
            // Were this to be shipped in a production-level app, we'd probably want to pull
            // all the user names into a List/ArrayList, sort, and perform a binary search for
            // the user name.
            // Why not do that here?
            // Because there won't ever be more than 10 or so users, so the time it will take
            // is irrelevant
            // Also laziness
            for(User user : userData.getValue()){
                if(user.getUsername().equals(username)){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean allFieldsAreNotFull(EditText[] list){
        for(int x = 0; x < list.length; x++){
            if(list[x].getText().toString().equals("") || list[x].getText().toString().equals("0")){
                return true;
            }
        }
        return false;
    }

    private double feetAndInchesToCm(int heightFeet, int heightIn){
        int inchTotal = (heightFeet * 12) + heightIn;
        return (inchTotal * 2.54);
    }

    public double imperialToMetricWeight(double weightLb) { return weightLb / 2.205; }
}