package com.ruslanlyalko.kidsapp.data;

public class Utils {

    private static boolean mIsAdmin;

    public static boolean isAdmin() {
        return mIsAdmin;
    }

    public static void setIsAdmin(boolean mIsAdmin) {
        Utils.mIsAdmin = mIsAdmin;
    }
}
