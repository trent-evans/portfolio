package com.example.knight_radiant_app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class HikeFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private HikeViewModel HikeViewModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.hike_fragment,container,false);

        String hikeData = getArguments().getString("hikeData");
        System.out.println(hikeData);

        HikeData hd = new HikeData();
        hd.getCurrentCondition().setHikeJsonData(hikeData);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_Master);

        layoutManager = new LinearLayoutManager(getActivity());

        HikeViewModel = ViewModelProviders.of(this).get(HikeViewModel.class);
        HikeViewModel.getData().observe(getViewLifecycleOwner(), hikeObserver);

        return view;
    }
    final Observer<HikeData> hikeObserver  = new Observer<HikeData>() {
        @Override
        public void onChanged(@Nullable final HikeData hikeData) {
            // Update the UI if this data variable changes
            if(hikeData!=null) {
                HikeData.CurrentCondition currentCondition = hikeData.getCurrentCondition();
                mRecyclerView.setHasFixedSize(true);
                mRecyclerView.setLayoutManager(layoutManager);
                mAdapter = new MyRVAdapter(currentCondition.hikes,currentCondition.urls);
                mRecyclerView.setAdapter(mAdapter);
            }
        }
    };
}
