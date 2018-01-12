package com.ruslanlyalko.kidsapp.presentation.ui.calc;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.ruslanlyalko.kidsapp.R;

public class CalcActivity extends AppCompatActivity {

    public static Intent getLaunchIntent(final AppCompatActivity launchActivity) {
        return new Intent(launchActivity, CalcActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calc);
    }
}
