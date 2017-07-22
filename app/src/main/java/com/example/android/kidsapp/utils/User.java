package com.example.android.kidsapp.utils;

public class User {


    public String userId;
    public String userName;
    public String userPhone;
    public String userEmail;
    public String userBDay;
    public String userCard;
    public boolean userIsAdmin;
    public String userPositionTitle = "Інструктор, Майстриня";
    public String userFirstDate;
    public String userTimeStart = "10:00";
    public String userTimeEnd = "19:00";

    public int userStavka = 60;
    public int userPercent = 8;
    public int userArt = 10;
    public int userMk = 50;


    public User(String userId, String userName, String userPhone, String userEmail, String userBDay, String userCard, boolean userIsAdmin) {
        this.userId = userId;
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

    public String getUserId() {
        return userId;
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


    public String getUserPositionTitle() {
        return userPositionTitle;
    }

    public String getUserFirstDate() {
        return userFirstDate;
    }

    public String getUserTimeStart() {
        return userTimeStart;
    }

    public String getUserTimeEnd() {
        return userTimeEnd;
    }
}
