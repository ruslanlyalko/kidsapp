package com.example.android.kidsapp;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ProgressBar;
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

    TextView textTotal, textPercent, textStavka, textMk, textMonth;
    TextView textTotal2, textPercent2, textStavka2, textMk2, textMonth2;

    ProgressBar progressBar, progressBar2;
    List<Report> reportList, reportList2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        setContentView(R.layout.activity_dashboard);

        reportList = new ArrayList<>();
        reportList2 = new ArrayList<>();

        initRef();

        reportList.clear();
        reportList2.clear();

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
                        calcSalary();
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

        mDatabase.getReference(Constants.FIREBASE_REF_USERS)
                .child(uId)
                .child(Constants.FIREBASE_REF_REPORTS)
                .child(String.valueOf(today.get(Calendar.YEAR)))
                .child(String.valueOf(today.get(Calendar.MONTH)))// TODO BAG if now if january
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Report report = dataSnapshot.getValue(Report.class);
                        reportList2.add(report);
                        calcSalary2();
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
    }

    private void initRef() {

        textMonth = (TextView) findViewById(R.id.text_month);
        textTotal = (TextView) findViewById(R.id.text_total);
        textStavka = (TextView) findViewById(R.id.text_stavka_total);
        textPercent = (TextView) findViewById(R.id.text_percent_total);
        textMk = (TextView) findViewById(R.id.text_mk_total);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        textMonth2 = (TextView) findViewById(R.id.text_month2);
        textTotal2 = (TextView) findViewById(R.id.text_total2);
        textStavka2 = (TextView) findViewById(R.id.text_stavka_total2);
        textPercent2 = (TextView) findViewById(R.id.text_percent_total2);
        textMk2 = (TextView) findViewById(R.id.text_mk_total2);
        progressBar2 = (ProgressBar) findViewById(R.id.progress_bar2);

    }

    @Override
    protected void onResume() {
        super.onResume();

        calcSalary();
        calcSalary2();

    }

    private void calcSalary() {

        int percent = 0;
        int stavka = 0;
        int mk = 0;

        for (Report rep : reportList) {
            stavka += 60;
            percent += rep.total;
            mk += rep.mk1 * 10 + rep.mk2 * 10;
            mk += rep.bMk * 50;
        }
        percent = (int) (percent * 0.08);
        int total = stavka + percent + mk;

        textMonth.setText(Constants.MONTH_FULL[Calendar.getInstance().get(Calendar.MONTH)] + "");

        textStavka.setText(stavka + " грн");
        textPercent.setText(percent + " грн");
        textMk.setText(mk + " грн");
        textTotal.setText(total + " ГРН");

        progressBar.setProgress(total);
    }

    private void calcSalary2() {

        int percent = 0;
        int stavka = 0;
        int mk = 0;

        for (Report rep : reportList2) {
            stavka += 60;
            percent += rep.total;
            mk += rep.bMk * 50;
            mk += rep.mk1 * 10 + rep.mk2 * 10;
        }
        percent = (int) (percent * 0.08);
        int total = stavka + percent + mk;

        textMonth2.setText(Constants.MONTH_FULL[Calendar.getInstance().get(Calendar.MONTH) - 1] + "");

        textStavka2.setText(stavka + " грн");
        textPercent2.setText(percent + " грн");
        textMk2.setText(mk + " грн");
        textTotal2.setText(total + " ГРН");

        progressBar2.setProgress(total);

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
