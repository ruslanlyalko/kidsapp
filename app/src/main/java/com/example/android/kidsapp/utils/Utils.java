package com.example.android.kidsapp.utils;


public class Utils {

    private static boolean mIsAdmin;

    public static boolean isIsAdmin() {
        return mIsAdmin;
    }

    public static void setIsAdmin(boolean mIsAdmin) {
        Utils.mIsAdmin = mIsAdmin;
    }
}
