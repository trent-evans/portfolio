package com.example.knight_radiant_app;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class  ProfileFragment extends Fragment {

    private EditText firstNameEditText, lastNameEditText, ageEditText,
    cityEditText, countryEditText, feetEditText, inchesEditText, weightEditText;
    TextView heightUnitText, weightUnitText;
    private Switch sexSwitch, measurementSwitch;
    private RadioButton metricRB, imperialRB;
    private FloatingActionButton fab;
    private Button saveChanges;
    private String username;
    private String decimalFormat = "%.1f";
    private ProfileViewModel model;
    private boolean savingChanges = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view =  inflater.inflate(R.layout.profile_fragment,container,false);

        Bundle data = getArguments();
        username = data.getString("username");

        firstNameEditText = view.findViewById(R.id.firstNameEditText);
        lastNameEditText = view.findViewById(R.id.lastNameEditText);
        ageEditText = view.findViewById(R.id.ageEditText);
        cityEditText = view.findViewById(R.id.cityEditText);
        countryEditText = view.findViewById(R.id.countryEditText);
        weightUnitText = view.findViewById(R.id.weightUnitText);
        feetEditText = view.findViewById(R.id.heightEditText_feet_profile);
        inchesEditText = view.findViewById(R.id.heightEditText_inches_profile);
        weightEditText = view.findViewById(R.id.weightEditText_profile);
        sexSwitch = view.findViewById(R.id.sexSwitch);
        saveChanges = view.findViewById(R.id.saveChangesButton);

        model = ViewModelProviders.of(getActivity()).get(ProfileViewModel.class);

        LiveData<List<User>> nullCheck = model.getUserData();

        if(nullCheck == null){ // If there is no user data at all, generate a default user
            // Ideally, this won't have to stay here forever.  But for now it does because otherwise
            // we have no way to test things
            model.createNewUserEntry("blackthorn","Dalinar","Kholin",
                    68,"Male","Kholinar","Alethkar",193.0,90,
                    true, "password");
            nullCheck = model.getUserData();
        }

        if(nullCheck != null) { // If the data is not null, then we have users.  Proceed accordingly
            model.getUserData().observe(getActivity(), new Observer<List<User>>() {
                @Override
                public void onChanged(List<User> users) {

                    if(savingChanges){
                        savingChanges = false;
                        Toast.makeText(getContext(),"Changes saved!",Toast.LENGTH_SHORT).show();
                    }

                    System.out.println("We made it here! Glory");

                    if(!model.setUserDataViaUsername(username)){ // For scenarios when I do dumb things like deleting the old databases
                        model.createNewUserEntry("blackthorn","Dalinar","Kholin",
                                68,"Male","Kholinar","Alethkar",193.0,90,
                                true, "password");
                        model.setUserDataViaUsername(username);
                    }

                    firstNameEditText.setText(model.getFirstName());
                    lastNameEditText.setText(model.getLastName());
                    Integer age = model.getAge();
                    ageEditText.setText(age.toString());
                    cityEditText.setText(model.getCity());
                    countryEditText.setText(model.getCountry());

                    if(model.getSex().toLowerCase().equals("male")){
                        sexSwitch.setChecked(true);
                    }else{
                        sexSwitch.setChecked(false);
                    }

                    weightEditText.setText(String.format(decimalFormat, metricToImperialWeight(model.getWeightKg())));
                    double heightCm = model.getHeightCm();
                    feetEditText.setText(Integer.toString(metricToFeet(heightCm)));
                    inchesEditText.setText(Integer.toString(metricRemainderInches(heightCm)));

                }
            });
        }

        saveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String userSex;
                if(sexSwitch.isChecked()){
                    userSex = "Male";
                }else{
                    userSex = "Female";
                }

                Integer heightFt = Integer.parseInt(feetEditText.getText().toString());
                Integer heightIn = Integer.parseInt(inchesEditText.getText().toString());
                Double weightLb = Double.parseDouble(weightEditText.getText().toString());

                double heightCm = feetAndInchesToCm(heightFt,heightIn);
                double weightKg = imperialToMetricWeight(weightLb);

                model.updateProfileEntry(
                        username, // username
                        firstNameEditText.getText().toString(), // First Name
                        lastNameEditText.getText().toString(), // Last Name
                        Integer.parseInt(ageEditText.getText().toString()), // Age
                        userSex, // Sex
                        cityEditText.getText().toString(), // City
                        countryEditText.getText().toString(), // Country
                        heightCm, // Height
                        weightKg, // Weight/Mass
                        false, // When we were allowing all options, this made sense.  It no longer does.  But updating the room is a pain
                        model.getVerifyUser());
                savingChanges = true;

            }
        });

        return view;
    }

    private int metricToFeet(double heightCm){
        int inches = (int) (heightCm / 2.54);
        return inches / 12;
    }

    private int metricRemainderInches(double heightCm){
        int inches = (int) (heightCm / 2.54);
        return inches % 12;
    }

    private double feetAndInchesToCm(int heightFeet, int heightIn){
        int inchTotal = (heightFeet * 12) + heightIn;
        return (inchTotal * 2.54);
    }


    public double metricToImperialWeight(double weightKg){
        return weightKg * 2.205;
    }

    public double imperialToMetricWeight(double weightLb) { return weightLb / 2.205; }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            GenericActivity.profilepic = (Bitmap) data.getExtras().get("data");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                    fab.setEnabled(true);
                }
            }
        }
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        model = ViewModelProviders.of(requireActivity()).get(ProfileViewModel.class);
    }

}
