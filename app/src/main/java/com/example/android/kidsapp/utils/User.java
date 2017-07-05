package com.example.android.kidsapp.utils;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class User {
    public String userName;
    public String userPhone;
    public String userEmail;
    public String userBDay;

    public User(String userName, String userPhone, String userEmail, String bDay) {
        this.userName = userName;
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


    public String getUserPhone() {
        return userPhone;
    }

    public String getUserName() {
        return this.userName;
    }

    public String getUserEmail() {
        return this.userEmail;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("userFirstName", userName);
        result.put("userPhone", userPhone);
        result.put("userEmail", userEmail);
        result.put("userBDay", userBDay);

        return result;
    }

}
