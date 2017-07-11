package com.example.android.kidsapp.utils;

public class Cost {

    public String key;
    public String title1;
    public String title2;
    public String date;
    public String userId;
    public String userName;
    public int price;

    public Cost() {
    }

    public Cost(String title1, String title2, String date, String userId, String userName, int price) {
        this.title1 = title1;
        this.title2 = title2;
        this.date = date;
        this.userId = userId;
        this.userName = userName;
        this.price = price;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }


    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTitle1() {
        return title1;
    }

    public void setTitle1(String title1) {
        this.title1 = title1;
    }

    public String getTitle2() {
        return title2;
    }

    public void setTitle2(String title2) {
        this.title2 = title2;
    }

}