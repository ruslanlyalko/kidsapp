package com.example.android.kidsapp.utils;

public class Mk {

    public String getKey() {
        return key;
    }

    private String key;
    private String title1;
    private String title2;
    private String largeText;
    private String date;
    private int count;
    private String imageUri;
    private String userId;

    public Mk() {
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Mk( String title1, String title2, String largeText,  String imageUri, String userId) {
        this.key = "";
        this.title1 = title1;
        this.title2 = title2;
        this.largeText = largeText;
        this.date = "";
        this.count = 0;
        this.imageUri = imageUri;
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }


    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
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

    public String getLargeText() {
        return largeText;
    }

    public void setLargeText(String largeText) {
        this.largeText = largeText;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }
}