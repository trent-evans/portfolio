package com.example.knight_radiant_app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

public class ProfileFragmentMetric extends Fragment {

    private EditText cmEditText, kgEditText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_profile_metric,container,false);

        cmEditText = view.findViewById(R.id.cmEditText);
        kgEditText = view.findViewById(R.id.kgsEditText);

        double heightCm = getArguments().getDouble("heightCm");
        double weightKg = getArguments().getDouble("weightKg");

        cmEditText.setText(Integer.toString((int) heightCm));
        kgEditText.setText(Integer.toString((int) weightKg));

        return view;
    }
}
