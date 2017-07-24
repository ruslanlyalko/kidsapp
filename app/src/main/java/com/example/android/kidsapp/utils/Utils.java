package com.example.android.kidsapp.utils;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {

    private static boolean mIsAdmin;

    public static boolean isIsAdmin() {
        return mIsAdmin;
    }

    public static void setIsAdmin(boolean mIsAdmin) {
        Utils.mIsAdmin = mIsAdmin;
    }

    public static String getCurrentTimeStamp() {

        return new SimpleDateFormat("yyyyMMdd_hhmmss", Locale.US).format(new Date()).toString();

    }

    public static String getFirstLetters(String displayName) {

        int ind = displayName.indexOf(' ');

        return displayName.substring(0, 1) + displayName.substring(ind + 1, ind + 2);
    }

    public static boolean todayOrFuture(String dateStr) {

        Date today = new Date();

        today.setHours(0);
        today.setMinutes(0);
        today.setSeconds(0);

        try {
            Date date = new SimpleDateFormat("d-M-yyyy").parse(dateStr);
            date.setHours(1);
            return date.getTime() >= today.getTime();

        } catch (ParseException e) {
            e.printStackTrace();
        }

        //todo write logic here
        return false;
    }

    public static boolean future(String dateStr) {
        Date today = new Date();

        today.setHours(23);
        today.setMinutes(59);
        today.setSeconds(59);

        try {
            Date date = new SimpleDateFormat("d-M-yyyy").parse(dateStr);
            date.setHours(1);
            return date.getTime() > today.getTime();

        } catch (ParseException e) {
            e.printStackTrace();
        }

        //todo write logic here
        return false;
    }
}
