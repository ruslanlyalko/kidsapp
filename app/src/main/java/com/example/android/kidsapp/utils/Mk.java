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
    private int imageId;

    public Mk() {
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Mk(String key, String title1, String title2, String largeText, int count, String date, int imageId) {
        this.key= key;
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

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }
}