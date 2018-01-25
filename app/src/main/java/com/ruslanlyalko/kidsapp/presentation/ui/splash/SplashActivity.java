package com.ruslanlyalko.kidsapp.presentation.ui.splash;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.ruslanlyalko.kidsapp.presentation.service.MyFirebaseMessagingService;
import com.ruslanlyalko.kidsapp.presentation.ui.main.MainActivity;
import com.ruslanlyalko.kidsapp.presentation.ui.login.LoginActivity;

/**
 * Created by Ruslan Lyalko
 * on 11.11.2017.
 */

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onResume() {
        super.onResume();
        if (FirebaseAuth.getInstance().getCurrentUser() == null)
            startActivity(LoginActivity.getLaunchIntent(this));
        else
            startActivity(MainActivity.getLaunchIntent(this));
        finish();
    }

    public static Intent getLaunchIntent(final Context context) {
        return new Intent(context, SplashActivity.class);
    }
}
