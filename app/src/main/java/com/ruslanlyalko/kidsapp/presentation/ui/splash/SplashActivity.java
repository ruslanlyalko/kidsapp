package com.ruslanlyalko.kidsapp.presentation.ui.splash;

import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.ruslanlyalko.kidsapp.presentation.ui.MainActivity;
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
}
