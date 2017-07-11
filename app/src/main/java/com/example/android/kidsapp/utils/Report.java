package com.example.android.kidsapp.utils;

public class Report {

    public String userId;
    public String userName;
    public String date;
    public String mkRef;
    public String mkName;

    public int total;
    public int totalRoom;
    public int totalBday;
    public int totalMk;

    public int r60;
    public int r40;
    public int r20;
    public int r10;

    public int b50;
    public int b30;
    public int bMk;

    public int mk1;
    public int mk2;
    public int mkt1;
    public int mkt2;

    public Report() {

    }

    public Report(String userId, String userName, String date) {
        this.userId = userId;
        this.userName = userName;
        this.date = date;
        this.mkRef = "";
        this.mkName = "";
    }


    public int getR10() {
        return r10;
    }

    public String getMkRef() {
        return mkRef;
    }

    public String getMkName() {
        return mkName;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserId() {
        return userId;
    }

    public String getDate() {
        return date;
    }

    public int getTotal() {
        return total;
    }

    public int getTotalRoom() {
        return totalRoom;
    }

    public int getTotalBday() {
        return totalBday;
    }

    public int getTotalMk() {
        return totalMk;
    }

    public int getR60() {
        return r60;
    }

    public int getR40() {
        return r40;
    }

    public int getR20() {
        return r20;
    }

    public int getB50() {
        return b50;
    }

    public int getB30() {
        return b30;
    }

    public int getbMk() {
        return bMk;
    }

    public int getMk1() {
        return mk1;
    }

    public int getMk2() {
        return mk2;
    }

    public int getMkt1() {
        return mkt1;
    }

    public int getMkt2() {
        return mkt2;
    }
}
