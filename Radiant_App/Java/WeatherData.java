package com.example.knight_radiant_app;

public class WeatherData {


    private LocationData mLocationData;

    private CurrentCondition mCurrentCondition = new CurrentCondition();


    public class CurrentCondition {

        String weatherDescription = "";
        double tempK;
        double feelsLikeK;
        Integer humidity;
        double tempMinK;
        double tempMaxK;

        public double getTempK() {
            return tempK;
        }

        public void setTempK(double tempK) {
            this.tempK = tempK;
        }

        public double getFeelsLikeK() {
            return feelsLikeK;
        }

        public void setFeelsLikeK(double feelsLikeK) {
            this.feelsLikeK = feelsLikeK;
        }

        public Integer getHumidity() {
            return humidity;
        }

        public void setHumidity(Integer humidity) {
            this.humidity = humidity;
        }

        public double getTempMinK() {
            return tempMinK;
        }

        public void setTempMinK(double tempMinK) {
            this.tempMinK = tempMinK;
        }

        public double getTempMaxK() {
            return tempMaxK;
        }

        public void setTempMaxK(double tempMaxK) {
            this.tempMaxK = tempMaxK;
        }

        public String getWeatherDescription() {
            return weatherDescription;
        }

        public void setWeatherDescription(String weatherDescription) {
            this.weatherDescription = weatherDescription;
        }

    }


    //Setters and Getters
    public void setLocationData(LocationData locationData) {
        mLocationData = locationData;
    }

    public LocationData getLocationData() {
        return mLocationData;
    }

    public void setCurrentCondition(CurrentCondition currentCondition) {
        mCurrentCondition = currentCondition;
    }

    public CurrentCondition getCurrentCondition() {
        return mCurrentCondition;
    }

}
