package com.example.knight_radiant_app;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.JsonObject;

@Entity(tableName = "user_table")
public class User {

    @PrimaryKey
    @NonNull
    public String username;

    @ColumnInfo(name="first_name")
    @NonNull
    public String firstName;

    @ColumnInfo(name="last_name")
    @NonNull
    public String lastName;

    @ColumnInfo(name="age")
    @NonNull
    public int age;

    @ColumnInfo(name="sex")
    @NonNull
    public String sex;

    @ColumnInfo(name="city")
    @NonNull
    public String city;

    @ColumnInfo(name="country")
    @NonNull
    public String country;

    @ColumnInfo(name="height_cm")
    @NonNull
    public double heightCm;

    @ColumnInfo(name="weight_kg")
    @NonNull
    public double weightKg;

    @ColumnInfo(name="prefers_metric")
    @NonNull
    public boolean prefersMetric;

    @ColumnInfo(name="fitness_goals")
    @NonNull
    public String fitnessGoal;

    public User(String username, String firstName, String lastName, int age, String sex,
                String city, String country, double heightCm, double weightKg,
                boolean prefersMetric) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.sex = sex;
        this.city = city;
        this.country = country;
        this.heightCm = heightCm;
        this.weightKg = weightKg;
        this.prefersMetric = prefersMetric;

        this.fitnessGoal = Integer.toString("password".hashCode());
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getSex() { return sex; }
    public void setSex(String sex) { this.sex = sex; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public double getHeightCm() { return heightCm; }
    public void setHeightCm(double heightCm) { this.heightCm = heightCm; }

    public double getWeightKg() { return weightKg; }
    public void setWeightKg(double weightKg) { this.weightKg = weightKg; }

    public boolean isPrefersMetric() { return prefersMetric; }
    public void setPrefersMetric(boolean prefersMetric) { this.prefersMetric = prefersMetric; }

    public String getFitnessGoals() { return fitnessGoal; }
    public void setFitnessGoals(String fitnessGoal) { this.fitnessGoal = fitnessGoal; }

}
