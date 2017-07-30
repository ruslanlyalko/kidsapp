package com.example.android.kidsapp.utils;


import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {

    private static boolean mIsAdmin;


    public static boolean isAdmin() {
        return mIsAdmin;
    }

    public static void setIsAdmin(boolean mIsAdmin) {
        Utils.mIsAdmin = mIsAdmin;
    }

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

    public static void expand(final View v) {
        v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (int) (targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int) (targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
                } else {
                    v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int) (initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }


}
