package com.example.android.kidsapp.utils;

public class Report {

    public String userId;
    public String userName;
    public String date;
    public String mmRef;

    public int total;
    public int totalRoom;
    public int totalBday;
    public int totalMk;

    public int r60;
    public int r40;
    public int r20;

    public int b50;
    public int b25;
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
        this.mmRef = " ";
    }

    public Report(String userId, String userName, String date, String mmRef, int total, int totalRoom, int totalBday, int totalMk, int r60, int r40, int r20, int b50, int b25, int bMk, int mk1, int mk2, int mkt1, int mkt2) {
        this.userId = userId;
        this.userName = userName;
        this.date = date;
        this.mmRef = mmRef;
        this.total = total;
        this.totalRoom = totalRoom;
        this.totalBday = totalBday;
        this.totalMk = totalMk;
        this.r60 = r60;
        this.r40 = r40;
        this.r20 = r20;
        this.b50 = b50;
        this.b25 = b25;
        this.bMk = bMk;
        this.mk1 = mk1;
        this.mk2 = mk2;
        this.mkt1 = mkt1;
        this.mkt2 = mkt2;
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

    public String getMmRef() {
        return mmRef;
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

    public int getB25() {
        return b25;
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
