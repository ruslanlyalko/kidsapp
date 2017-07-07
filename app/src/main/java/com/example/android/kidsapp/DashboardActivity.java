package com.example.android.kidsapp;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.android.kidsapp.utils.Constants;
import com.example.android.kidsapp.utils.Report;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    TextView textSalary;
    Button buttonSalary;

    List<Report> reportList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        setContentView(R.layout.activity_dashboard);

        reportList = new ArrayList<>();

        textSalary = (TextView) findViewById(R.id.text_salary);
        buttonSalary = (Button) findViewById(R.id.button_salary);

        reportList.clear();

        String uId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // Delete item from DB
        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();

        Calendar today = Calendar.getInstance();

        mDatabase.getReference(Constants.FIREBASE_REF_USERS)
                .child(uId)
                .child(Constants.FIREBASE_REF_REPORTS)
                .child(String.valueOf(today.get(Calendar.YEAR)))
                .child(String.valueOf(today.get(Calendar.MONTH) + 1))
                .addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Report report = dataSnapshot.getValue(Report.class);
                reportList.add(report);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        buttonSalary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                calcSalary();
            }
        });

    }

    private void calcSalary() {

        int percent = 0;
        int stavka = 0;
        int mk = 0;

        for (Report rep : reportList) {
            stavka += 60;
            percent += rep.total ;
            mk += rep.mk1 * 10 + rep.mk2 * 10;
        }
        percent = (int)(percent *0.08);

        textSalary.setText("Stavka: " + stavka+"; Procent: "+percent+"; Mk: "+mk);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
    }
}
