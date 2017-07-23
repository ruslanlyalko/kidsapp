package com.example.android.kidsapp.utils;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.SimpleTimeZone;

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
}
