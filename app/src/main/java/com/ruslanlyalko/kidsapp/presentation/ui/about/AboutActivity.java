package com.ruslanlyalko.kidsapp.presentation.ui.about;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.ruslanlyalko.kidsapp.R;
import com.ruslanlyalko.kidsapp.common.Keys;
import com.ruslanlyalko.kidsapp.data.Utils;
import com.ruslanlyalko.kidsapp.data.configuration.DefaultConfigurations;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        TextView textVersion = findViewById(R.id.text_version);
        TextView textAbout = findViewById(R.id.text_about);
        final EditText editAbout = findViewById(R.id.edit_about);
        FloatingActionButton fab = findViewById(R.id.fab);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            textAbout.setText(bundle.getString(Keys.Extras.EXTRA_ABOUT, getString(R.string.text_about_company)));
            editAbout.setText(bundle.getString(Keys.Extras.EXTRA_ABOUT, getString(R.string.text_about_company)));
        }
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String version = getString(R.string.dialog_about_message) + "" + pInfo.versionName;
        textVersion.setText(version);
        fab.setVisibility(Utils.isAdmin() ? View.VISIBLE : View.GONE);
        if (Utils.isAdmin()) {
            textAbout.setVisibility(View.GONE);
            editAbout.setVisibility(View.VISIBLE);
        }
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Utils.isAdmin())
                    FirebaseDatabase.getInstance().getReference(DefaultConfigurations.DB_ABOUT)
                            .setValue(editAbout.getText().toString().trim())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(AboutActivity.this, R.string.mk_updated, Toast.LENGTH_SHORT).show();
                                }
                            });
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}