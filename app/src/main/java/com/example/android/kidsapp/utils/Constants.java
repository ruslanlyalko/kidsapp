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
    /**
     * Firebase STORAGE
     */
    public static final String FIREBASE_STORAGE_PICTURES = "user-pictures";
    public static final String FIREBASE_STORAGE_COST = "COST";
    public static final String FIREBASE_STORAGE_MK = "MK";

    /**
     * Extra constants
     */
    public static final String EXTRA_IS_ADMIN = "extra_is_admin";
    public static final String EXTRA_DATE = "extra_date";
    public static final String EXTRA_UID = "extra_uid";
    public static final String EXTRA_USER_NAME = "extra_user_name";


    /**
     * Other
     */
    public static final int REQUEST_CODE_CAMERA = 12344;
    public static final int REQUEST_CODE_GALLERY = 454;


    public static final String[] MONTH = {"СІЧ", "ЛЮТ", "БЕР", "КВІ", "ТРА", "ЧЕР", "ЛИП", "СЕР", "ВЕР", "ЖОВ", "ЛИС", "ГРУ"};
    public static final String[] MONTH_FULL = {"СІЧЕНЬ", "ЛЮТИЙ", "БЕРЕЗЕНЬ", "КВІТЕНЬ", "ТРАВЕНЬ"
            , "ЧЕРВЕНЬ", "ЛИПЕНЬ", "СЕРПЕНЬ", "ВЕРЕСЕНЬ", "ЖОВТЕНЬ", "ЛИСТОПАД", "ГРУДЕНЬ"};
    public static final String FIREBASE_REF_NOTIFICATIONS = "NOTIFICATIONS";
    public static final String EXTRA_URI = "extra_uri";
    public static final String EXTRA_MK_ID = "extra_mk_id";
    public static final String EXTRA_MK_TITLE2 = "extra_title_2";


    public static int[] COLORS = {Color.GREEN, Color.BLUE, Color.RED, Color.MAGENTA, Color.GRAY, Color.YELLOW};


    public static String URI_CARD_MK_T = "https://firebasestorage.googleapis.com/v0/b/kids-3440d.appspot.com/o/mk-cards%2Fmk_t.jpg?alt=media&token=740bb31f-5be0-4c02-9fe4-8e79edf88787";
    public static String URI_CARD_MK_K = "https://firebasestorage.googleapis.com/v0/b/kids-3440d.appspot.com/o/mk-cards%2Fmk_k.jpg?alt=media&token=8e6f4e23-4f19-4a65-8f38-45e175b4faab";


    public static int SALARY_STAVKA = 60;
    public static int SALARY_BDAY_MK = 50;
    public static int SALARY_ART_MK = 10;
    public static double SALARY_PERCENT = 0.08;
    public static int COST_EDIT_MIN = 5;
}
