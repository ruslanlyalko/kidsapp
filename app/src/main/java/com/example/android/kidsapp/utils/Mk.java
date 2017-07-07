package com.example.android.kidsapp.utils;

public class Mk {
    private String title1;
    private String title2;
    private String largeText;
    private String date;
    private String count;
    private int imageId;

    public Mk() {
    }

    public Mk(String title1, String title2, String largeText, String date, String count, int imageId) {
        this.title1 = title1;
        this.title2 = title2;
        this.date = date;
        this.count = count;
        this.largeText = largeText;
        this.imageId = imageId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
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

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }
}