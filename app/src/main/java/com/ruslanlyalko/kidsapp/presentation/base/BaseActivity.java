package com.ruslanlyalko.kidsapp.presentation.base;

import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

/**
 * Created by Ruslan Lyalko
 * on 28.01.2018.
 */

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else
            return super.onOptionsItemSelected(item);
    }
}
