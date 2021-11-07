package com.example.knight_radiant_app;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.MutableLiveData;

import org.json.JSONException;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;

public class HikeRepository {

    private final MutableLiveData<HikeData> hikeJsonData =
            new MutableLiveData<>();
    private String mLocation;

    HikeRepository(Application application){
        loadData();
    }

    public void setLocation(String location){
        mLocation = location;
        loadData();
    }

    public MutableLiveData<HikeData> getData() {
        System.out.println(hikeJsonData.toString());
        return hikeJsonData;
    }

    private void loadData(){
        new LoadTask(this).execute(mLocation);
    }

    private static class LoadTask extends AsyncTask<String,Void,String>{
        private WeakReference<HikeRepository> mHikeRepositoryReference;

        LoadTask(HikeRepository context)
        {
            mHikeRepositoryReference = new WeakReference<>(context);
        }

        @Override
        protected String doInBackground(String... strings) {
            URL hikeDataURL = NetworkUtils.buildHikeURLFromString();
            String retrievedJsonData = "";
            if(hikeDataURL!=null) {
                try {
                    retrievedJsonData = NetworkUtils.getDataFromURL(hikeDataURL);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return retrievedJsonData;
        }

        @Override
        protected void onPostExecute(String s) {
            if(s!=null) {
                try {
                    HikeRepository ref = mHikeRepositoryReference.get();
                    ref.hikeJsonData.setValue(JSONWeatherUtils.getHikeData(s));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    };
}
