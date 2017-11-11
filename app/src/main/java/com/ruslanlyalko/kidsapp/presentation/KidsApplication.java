package com.ruslanlyalko.kidsapp.presentation;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Ruslan Lyalko
 * on 11.11.2017.
 */

public class KidsApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
