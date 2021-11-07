package com.example.knight_radiant_app;

import android.app.Application;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.Objects;

public class ProfileViewModel extends AndroidViewModel {
    private ProfileRepository mProfileRepo;

    private User singleUser;
    private LiveData<List<User>> userData;
    private String username;


    public ProfileViewModel(Application application) {
        super(application);
        mProfileRepo = new ProfileRepository(application);
        userData = mProfileRepo.getData();
        if(userData != null){
            List<User> myUsers = userData.getValue();
        }

    }

    // Set username and the single user data
    public boolean setUserDataViaUsername(String username){
        this.username = username;
        // Again, not scalable. Not important here given the size of the app

        List<User> userList = userData.getValue();
        for(User user : userList){
            if(user.username.equals(username)){
                singleUser = user;
                return true;
            }
        }
        return false;
    }

    public boolean verifyUserLogin(String username, String password){
        if(setUserDataViaUsername(username)){
            String passwordHash = Integer.toString(password.hashCode());
            if(getVerifyUser().equals(passwordHash)){
                return true;
            }
        }
        return false;
    }

    // User in Profile Fragment to
    public void createNewUserEntry(String username, String firstName, String lastName, int age,
                              String sex, String city, String country, double heightCm,
                              double weightKg, boolean prefersMetric, String newPassword){

        String newVerify = Integer.toString(newPassword.hashCode());
        User newUser = new User(username, firstName, lastName, age, sex,
                city, country, heightCm, weightKg, prefersMetric);

        mProfileRepo.insert(newUser);
    }

    // Use in the Profile Fragment to update the changes made to the Profile data
    public void updateProfileEntry(String username, String firstName, String lastName, int age,
                                String sex, String city, String country, double heightCm,
                                double weightKg, boolean prefersMetric, String verifyUser){
        User updateUser = new User(username,firstName, lastName, age, sex,
                city, country, heightCm, weightKg, prefersMetric);

        mProfileRepo.insert(updateUser);
    }


    public LiveData<List<User>> getUserData() {
        return userData;
    }

    public boolean singleUserIsNull(){ return singleUser == null; }

    // Getters for each individual field in the single User object.  Nothing thrilling, but important nonetheless
    public String getFirstName() {
        if(singleUser != null) {
            return singleUser.firstName;
        }
        return null;
    }

    public String getLastName() {
        if(singleUser != null) {
            return singleUser.lastName;
        }
        return null;
    }

    public int getAge() {
        if(singleUser != null) {
            return singleUser.age;
        }
        return -1;
    }

    public String getSex() {
        if(singleUser != null) {
            return singleUser.sex;
        }
        return null;
    }

    public String getCity() {
        if(singleUser != null) {
            return singleUser.city;
        }
        return null;
    }

    public String getCountry() {
        if(singleUser != null) {
            return singleUser.country;
        }
        return null;
    }

    public boolean getPrefersMetric() {
        if(singleUser != null) {
            return singleUser.prefersMetric;
        }
        return false;
    }

    public double getHeightCm() {
        if(singleUser != null) {
            return singleUser.heightCm;
        }
        return -1.0;
    }

    public double getWeightKg() {
        if(singleUser != null) {
            return singleUser.weightKg;
        }
        return -1.0;
    }

    public String getVerifyUser() {
        if(singleUser != null) {
            return singleUser.fitnessGoal;
        }
        return null;
    }
}
