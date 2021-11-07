package com.example.knight_radiant_app;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class HikeViewModel extends AndroidViewModel {
    private MutableLiveData<HikeData> hikeJsonData;
    private HikeRepository mHikeRepository;

    public HikeViewModel(Application application){
        super(application);
        mHikeRepository = new HikeRepository(application);
        hikeJsonData = mHikeRepository.getData();
    }
    public void setLocation(String location){
        mHikeRepository.setLocation(location);
    }

    public LiveData<HikeData> getData(){
        return hikeJsonData;
    }


}
