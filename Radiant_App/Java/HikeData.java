package com.example.knight_radiant_app;

import org.json.JSONException;

import java.util.ArrayList;

public class HikeData {

    private CurrentCondition mCurrentCondition = new CurrentCondition();

    public class CurrentCondition {
        String hikeJsonData;
        ArrayList<String> hikes;
        ArrayList<String> urls;


        public ArrayList<String> getHikes() {
            return hikes;
        }

        public void setHikes(ArrayList<String> hikes) {
            this.hikes = hikes;
        }

        public ArrayList<String> getUrls() {
            return urls;
        }

        public void setUrls(ArrayList<String> urls) {
            this.urls = urls;
        }


        public String getHikeJsonData() {
            return hikeJsonData;
        }

        public void setHikeJsonData(String hikeJsonData) {
            this.hikeJsonData = hikeJsonData;
        }
    }
    public CurrentCondition getCurrentCondition() {
        return mCurrentCondition;
    }

}
