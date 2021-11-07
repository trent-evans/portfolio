package com.example.knight_radiant_app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.text.DecimalFormat;
import java.util.List;

public class BMIFragment extends Fragment {

    private String username;
    ProfileViewModel model;
    TextView bmiTextView;

    double calculateBMI(double weight, double height){
        return weight / Math.pow((height * .01),2) ;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.bmi_fragment,container,false);
        Bundle data = getArguments();
        bmiTextView = view.findViewById(R.id.bmiTextView);
        username = data.getString("username");

        model = ViewModelProviders.of(getActivity()).get(ProfileViewModel.class);
        LiveData<List<User>> nullCheck = model.getUserData();

        if(nullCheck == null){ // this should never be a problem, but let's leave it here just to be safe
            Toast.makeText(getContext(),"No profile information found.  Please set your user information",Toast.LENGTH_SHORT).show();

        }else{
            model.getUserData().observe(getActivity(), new Observer<List<User>>() {
                @Override
                public void onChanged(List<User> users) {
                    model.setUserDataViaUsername(username);

                    double weightTemp = model.getWeightKg();
                    double heightTemp = model.getHeightCm();
                    double bmi = calculateBMI(weightTemp, heightTemp);
                    DecimalFormat bmiFormat = new DecimalFormat("#.00");

                    bmiTextView.setText("Your BMI = " + bmiFormat.format(bmi));
                }
            });


        }
        return view;
    }

}
