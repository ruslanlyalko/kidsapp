package com.example.android.kidsapp.utils;

import android.graphics.Color;

public class Constants {

    /**
     * Firebase DATABASE
     */
    public static final String FIREBASE_REF_USER_REPORTS = "REPORTS";
    public static final String FIREBASE_REF_USERS ="USERS";
    public static final String FIREBASE_REF_MK ="MK";
    /**
     * Firebase STORAGE
     */
    public static final String FIREBASE_STORAGE_PICTURES ="user-pictures";

    /**
     * Extra constants
     */
    public static final String EXTRA_IS_ADMIN ="extra_is_admin";
    public static final String EXTRA_DATE = "extra_date" ;
    public static final String EXTRA_UID = "extra_uid";
    public static final String EXTRA_USER_NAME = "extra_user_name";


    /**
     * Other
     */

    public static final String [] MONTH = {"СІЧ","ЛЮТ","БЕР","КВІ","ТРА","ЧЕР","ЛИП","СЕР","ВЕР","ЖОВ","ЛИС","ГРУ"};
    public static final String [] MONTH_FULL = {"СІЧЕНЬ","ЛЮТИЙ","БЕРЕЗЕНЬ","КВІТЕНЬ","ТРАВЕНЬ"
            ,"ЧЕРВЕНЬ","ЛИПЕНЬ","СЕРПЕНЬ","ВЕРЕСЕНЬ","ЖОВТЕНЬ","ЛИСТОПАД","ГРУДЕНЬ"};


    public static int[] COLORS = {Color.RED, Color.GREEN, Color.BLUE, Color.MAGENTA, Color.GRAY, Color.YELLOW};
}
