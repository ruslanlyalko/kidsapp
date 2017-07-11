package com.example.android.kidsapp.utils;

public class Notification {


    private String key;
    private String title1;
    private String title2;
    private String largeText;
    private String date;

    public Notification() {
    }


    public Notification(String key, String title1, String title2, String largeText, String date) {
        this.key = key;
        this.title1 = title1;
        this.title2 = title2;
        this.date = date;
        this.largeText = largeText;
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

    public String getLargeText() {
        return largeText;
    }

    public void setLargeText(String largeText) {
        this.largeText = largeText;
    }

}