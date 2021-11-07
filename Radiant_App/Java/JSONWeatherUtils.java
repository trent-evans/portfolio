package com.example.knight_radiant_app;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

//Declare methods as static. We don't want to create objects of this class.


public class JSONWeatherUtils {
    public static WeatherData getWeatherData(String data) throws JSONException {
        WeatherData weatherData = new WeatherData();

        //Start parsing JSON data

        try {
            JSONObject json = new JSONObject(data); //Must throw JSONException
            JSONObject jsonMain = json.getJSONObject("main");
            String[] weatherStringArray = json.getString("weather").split(",");
            String weatherDescription = "";
            for (int x = 0; x < weatherStringArray.length; x++) {
                weatherStringArray[x] = weatherStringArray[x].replace("\"", "");
                String[] tempArray = weatherStringArray[x].split(":");
                if (tempArray[0].equals("description")) {
                    weatherDescription = capitalizeEachWord(tempArray[1]);
                    break;
                }
            }
            WeatherData.CurrentCondition currentCondition = weatherData.getCurrentCondition();
            currentCondition.setWeatherDescription(weatherDescription);
            currentCondition.setTempK(jsonMain.getDouble("temp"));
            currentCondition.setFeelsLikeK(jsonMain.getDouble("feels_like"));
            currentCondition.setHumidity((int) jsonMain.getDouble("humidity"));
            currentCondition.setTempMinK(jsonMain.getDouble("temp_min"));
            currentCondition.setTempMaxK(jsonMain.getDouble("temp_max"));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return weatherData;
    }

    public static HikeData getHikeData(String data) throws JSONException {
        HikeData hikeData = new HikeData();
            HikeData.CurrentCondition currentCondition = hikeData.getCurrentCondition();
            currentCondition.setHikeJsonData(data);
        ArrayList<String> hikes = new ArrayList<>();
        ArrayList<String> urls = new ArrayList<>();

        JSONArray jsonArray = new JSONArray();
        try {
            JSONObject jsonObject =  new JSONObject(data);
            jsonArray = jsonObject.getJSONArray("trails");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject currentJson = jsonArray.getJSONObject(i);
                String name = (String) currentJson.get("name");
//                    String summary = (String) currentJson.get("summary");
                String location = (String) currentJson.get("location");
                Object length = currentJson.get("length");
                Double latitude = (double) currentJson.get("latitude");
                Double longitude = (double) currentJson.get("longitude");
                //syntax https://www.google.com/maps/search/?api=1&query=<lat>,<lng>
                String mapUrl = "https://www.google.com/maps/search/?api=1&query=" + latitude.toString() + "," + longitude.toString();

                hikes.add(i,name  + "\n Location: " + location + "\n Length in Miles: " + length.toString());
                urls.add(i, mapUrl);

            }
            catch(JSONException d){
                d.printStackTrace();
                System.out.println("item not added");
            }
        }
        currentCondition.setHikes(hikes);
        currentCondition.setUrls(urls);
            return hikeData;


    }



    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private static String capitalizeEachWord(String str) {
        String[] array = str.split(" ");
        String ret = "";
        for (int x = 0; x < array.length; x++) {
            if (x < array.length - 1) {
                ret += capitalize(array[x]) + " ";
            } else {
                ret += capitalize(array[x]);
            }
        }
        return ret;
    }
}