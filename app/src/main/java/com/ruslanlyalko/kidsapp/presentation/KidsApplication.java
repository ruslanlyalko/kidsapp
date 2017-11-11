package com.ruslanlyalko.kidsapp.presentation;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.database.FirebaseDatabase;

import io.fabric.sdk.android.Fabric;

/**
 * Created by Ruslan Lyalko
 * on 11.11.2017.
 */

public class KidsApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
