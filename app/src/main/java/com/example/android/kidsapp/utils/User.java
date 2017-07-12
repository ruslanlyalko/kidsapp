package com.example.android.kidsapp.utils;

public class User {

    public String userName;
    public String userPhone;
    public String userEmail;
    public String userBDay;
    public String userCard;
    public boolean userIsAdmin;

    public int userStavka = 60;
    public int userPercent = 8;
    public int userArt = 10;
    public int userMk = 50;


    public User(String userName, String userPhone, String userEmail, String userBDay, String userCard, boolean userIsAdmin) {
        this.userName = userName;
        this.userPhone = userPhone;
        this.userEmail = userEmail;
        this.userBDay = userBDay;
        this.userCard = userCard;
        this.userIsAdmin = userIsAdmin;
    }

    public User() {
        // Default constructor required
    }

    public int getUserStavka() {
        return userStavka;
    }

    public int getUserPercent() {
        return userPercent;
    }

    public int getUserArt() {
        return userArt;
    }

    public int getUserMk() {
        return userMk;
    }

    public boolean getUserIsAdmin() {
        return userIsAdmin;
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

    public String getUserCard() {
        return this.userCard;
    }


}
