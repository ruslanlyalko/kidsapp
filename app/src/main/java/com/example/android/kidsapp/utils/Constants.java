package com.example.android.kidsapp.utils;

import android.graphics.Color;

public class Constants {

    /**
     * Firebase DATABASE
     */
    public static final String FIREBASE_REF_REPORTS = "REPORTS";
    public static final String FIREBASE_REF_USERS = "USERS";
    public static final String FIREBASE_REF_MK = "MK";
    public static final String FIREBASE_REF_COSTS = "COSTS";
    public static final String FIREBASE_REF_COMMENTS= "COMMENTS";
    public static final String FIREBASE_REF_NOTIFICATIONS = "NOTIFICATIONS";
    public static final String FIREBASE_REF_ABOUT = "text_about";


    /**
     * Firebase STORAGE
     */
    public static final String FIREBASE_STORAGE_COST = "COST";
    public static final String FIREBASE_STORAGE_MK = "MK";
    public static final String FIREBASE_STORAGE_REPORT = "REPORT";

    /**
     * Extra constants
     */
    public static final String EXTRA_UID = "extra_uid";
    public static final String EXTRA_USER_NAME = "extra_user_name";
    public static final String EXTRA_DATE = "extra_date";

    /**
     * Other
     */
    public static final int REQUEST_CODE_CAMERA = 9408;
    public static final int REQUEST_CODE_GALLERY = 611;
    public static final int REQUEST_CODE_EDIT = 1605;


    public static final String[] MONTH_FULL = {"СІЧЕНЬ", "ЛЮТИЙ", "БЕРЕЗЕНЬ", "КВІТЕНЬ", "ТРАВЕНЬ"
            , "ЧЕРВЕНЬ", "ЛИПЕНЬ", "СЕРПЕНЬ", "ВЕРЕСЕНЬ", "ЖОВТЕНЬ", "ЛИСТОПАД", "ГРУДЕНЬ"};

    public static final String EXTRA_URI = "extra_uri";
    public static final String EXTRA_ITEM_ID = "extra_mk_id";
    public static final String EXTRA_TITLE2 = "extra_title_2";
    public static final String EXTRA_FOLDER = "extra_folder";
    public static final String EXTRA_NOT_ID = "extra_not_id";
    public static final String EXTRA_ABOUT = "extra_about";


    public static int COST_EDIT_MIN = 5;

    public static int SALARY_DEFAULT_STAVKA = 60;
    public static int SALARY_DEFAULT_PERCENT = 8;
    public static int SALARY_DEFAULT_MK = 50;
    public static int SALARY_DEFAULT_MK_CHILD = 0;
    public static int SALARY_DEFAULT_ART_MK_CHILD = 10;
}
