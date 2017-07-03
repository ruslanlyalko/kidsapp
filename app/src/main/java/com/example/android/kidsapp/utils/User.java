package com.example.android.kidsapp.utils;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class User {
    public String userFirstName;
    public String userSecondName;
    public String userLastName;
    public String userPhone;
    public String userEmail;
    public String userBDay;



    public User(String userFirstName, String userSecondName, String userLastName, String userPhone, String userEmail, String bDay) {
        this.userFirstName = userFirstName;
        this.userSecondName = userSecondName;
        this.userLastName = userLastName;
        this.userPhone = userPhone;
        this.userEmail = userEmail;
        this.userBDay = bDay;
    }


    public User() {
        // Default constructor required
    }

    public String getUserBDay() {
        return userBDay;
    }

    public String getUserSecondName() {
        return userSecondName;
    }

    public String getUserLastName() {
        return userLastName;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public String getUserFirstName() {
        return this.userFirstName;
    }

    public String getUserEmail() {
        return this.userEmail;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("userFirstName", userFirstName);
        result.put("userSecondName", userSecondName);
        result.put("userLastName", userLastName);
        result.put("userPhone", userPhone);
        result.put("userEmail", userEmail);
        result.put("userBDay", userBDay);

        return result;
    }

}
