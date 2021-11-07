package com.example.knight_radiant_app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

public class ProfileFragmentImperial extends Fragment {

    private EditText feetEditText, inchesEditText, lbsEditText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_profile_imperial,container,false);

        feetEditText = view.findViewById(R.id.feetEditText);
        inchesEditText = view.findViewById(R.id.inchesEditText);
        lbsEditText = view.findViewById(R.id.lbsEditText);

        double heightIn = getArguments().getDouble("heightIn");
        double weightLbs = getArguments().getDouble("weightLbs");

        feetEditText.setText(Integer.toString(feetFromInches(heightIn)));
        inchesEditText.setText(Integer.toString(remainingInches(heightIn)));
        lbsEditText.setText(Integer.toString((int) weightLbs));

        return view;
    }

    private int feetFromInches(double heightIn){
        return (int) (heightIn / 12);
    }

    private int remainingInches(double heightIn){
        return (int) (heightIn % 12);
    }

}
