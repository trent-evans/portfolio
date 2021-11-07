package com.example.knight_radiant_app;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class NetworkUtils {
    private static String urlBaseWeather = "https://api.openweathermap.org/data/2.5/weather?zip=";
    private static String weather = "ea0c23cbfbdeebca2a2d769f425c84ed";
    private static String urlBaseHike = "https://www.hikingproject.com/data/get-trails?lat=";
    private static String zipCode, countryCode = "";

    public static Double getmLatitude() {
        return mLatitude;
    }

    public static Double getmLongitude() {
        return mLongitude;
    }

    private static Double mLatitude;
    private static Double mLongitude;


    public static void setZipCode(String zip) {
        zipCode = zip;
    }

    public static void setCountryCode(String code) {
        countryCode = code;
    }

    public static URL buildWeatherURLFromString() {
        URL myURL = null;
        try {
            myURL = new URL(urlBaseWeather + zipCode + "," + countryCode + "&appid=" + weather);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return myURL;
    }

    public static URL buildHikeURLFromString() {
        URL myURL = null;
        try {
            myURL = new URL(urlBaseHike + mLatitude + "&lon=" + mLongitude + "&maxDistance=10&key=200916387-c2f49bee4910f5478c9456524ade7c81");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return myURL;
    }

    public static URL tabletHikeUrl() {
        return null;
    }

    public static String getDataFromURL(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream inputStream = urlConnection.getInputStream();

            //The scanner trick: search for the next "beginning" of the input stream
            //No need to user BufferedReader
            Scanner scanner = new Scanner(inputStream);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }

        } finally {
            urlConnection.disconnect();
        }
    }

    public static void setmLatitude(double mLatitude) {
        NetworkUtils.mLatitude = mLatitude;
    }

    public static void setmLongitude(double mLongitude) {
        NetworkUtils.mLongitude = mLongitude;
    }
}
