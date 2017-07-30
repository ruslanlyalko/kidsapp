package com.example.android.kidsapp.utils;

public class User {


    private String userId;
    private String userName;
    private String userPhone;
    private String userEmail;
    private String userBDay;
    private String userCard;
    private boolean userIsAdmin;
    private String userPositionTitle = "Інструктор, Майстриня";
    private String userFirstDate;
    private String userTimeStart = "10:00";
    private String userTimeEnd = "19:00";

    private int userStavka = 60;
    private int userPercent = 8;
    private int mkBd = 0;
    private int mkBdChild = 8;
    private int mkArtChild = 10;
    private boolean mkSpecCalc = false;
    private String mkSpecCalcDate = "1-8-2017";


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


    public String getMkSpecCalcDate() {
        return mkSpecCalcDate;
    }

    public void setMkSpecCalcDate(String mkSpecCalcDate) {
        this.mkSpecCalcDate = mkSpecCalcDate;
    }

    public boolean getMkSpecCalc() {
        return mkSpecCalc;
    }

    public void setMkSpecCalc(boolean mkSpecCalc) {
        this.mkSpecCalc = mkSpecCalc;
    }

    public int getMkBdChild() {
        return mkBdChild;
    }

    public void setMkBdChild(int mkBdChild) {
        this.mkBdChild = mkBdChild;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public void setUserBDay(String userBDay) {
        this.userBDay = userBDay;
    }

    public void setUserCard(String userCard) {
        this.userCard = userCard;
    }

    public void setUserIsAdmin(boolean userIsAdmin) {
        this.userIsAdmin = userIsAdmin;
    }

    public void setUserPositionTitle(String userPositionTitle) {
        this.userPositionTitle = userPositionTitle;
    }

    public void setUserFirstDate(String userFirstDate) {
        this.userFirstDate = userFirstDate;
    }

    public void setUserTimeStart(String userTimeStart) {
        this.userTimeStart = userTimeStart;
    }

    public void setUserTimeEnd(String userTimeEnd) {
        this.userTimeEnd = userTimeEnd;
    }

    public void setUserStavka(int userStavka) {
        this.userStavka = userStavka;
    }

    public void setUserPercent(int userPercent) {
        this.userPercent = userPercent;
    }

    public void setMkArtChild(int mkArtChild) {
        this.mkArtChild = mkArtChild;
    }

    public void setMkBd(int mkBd) {
        this.mkBd = mkBd;
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

    public int getMkArtChild() {
        return mkArtChild;
    }

    public int getMkBd() {
        return mkBd;
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
