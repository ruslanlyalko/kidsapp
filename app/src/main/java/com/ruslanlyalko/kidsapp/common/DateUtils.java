package com.ruslanlyalko.kidsapp.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Ruslan Lyalko
 * on 11.11.2017.
 */

public class DateUtils {

    public static String getCurrentTimeStamp() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
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
            Date date = new SimpleDateFormat("d-M-yyyy", Locale.US).parse(dateStr);
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
            Date date = new SimpleDateFormat("d-M-yyyy", Locale.US).parse(dateStr);
            date.setHours(1);
            return date.getTime() > today.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isTodayOrFuture(String firstDate, String secondDate) {
        try {
            Date dateFirst = new SimpleDateFormat("d-M-yyyy", Locale.US).parse(firstDate);
            Date dateSecond = new SimpleDateFormat("d-M-yyyy", Locale.US).parse(secondDate);
            return dateFirst.getTime() >= dateSecond.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false; //todo
    }

    public static String getIntWithSpace(int data) {
        String resultStr = data + "";
        if (resultStr.length() > 3)
            resultStr = resultStr.substring(0, resultStr.length() - 3) + " "
                    + resultStr.substring(resultStr.length() - 3, resultStr.length());
        return resultStr;
    }

    public static boolean isCurrentYear(final Date date) {
        return  date.getYear() == new Date().getYear();
    }
}
