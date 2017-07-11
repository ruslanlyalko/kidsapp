package com.example.android.kidsapp;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.kidsapp.utils.Constants;
import com.example.android.kidsapp.utils.Report;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SalaryActivity extends AppCompatActivity {

    TextView textTotal, textPercent, textStavka, textMk, textMonth;
    TextView textTotal2, textPercent2, textStavka2, textMk2, textMonth2;
    TextView textCard;
    LinearLayout panelCopy;

    ProgressBar progressBar, progressBar2;
    List<Report> reportList, reportList2;
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String mUId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        setContentView(R.layout.activity_salary);

        reportList = new ArrayList<>();
        reportList2 = new ArrayList<>();

        initRef();

        Bundle bundle = getIntent().getExtras();

        if (bundle == null) {
            if (mAuth.getCurrentUser() != null)
                mUId = mAuth.getCurrentUser().getUid();

        } else {
            mUId = bundle.getString(Constants.EXTRA_UID);
        }

        panelCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(textCard.getText().toString(), textCard.getText().toString());
                clipboard.setPrimaryClip(clip);

                Toast.makeText(SalaryActivity.this, getString(R.string.text_copied), Toast.LENGTH_SHORT).show();
            }
        });

        mDatabase.getReference(Constants.FIREBASE_REF_USERS).child(mUId).child("userCard").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                textCard.setText(dataSnapshot.getValue().toString());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Calendar today = Calendar.getInstance();
        mDatabase.getReference(Constants.FIREBASE_REF_USER_REPORTS)
                .child(String.valueOf(today.get(Calendar.YEAR)))
                .child(String.valueOf(today.get(Calendar.MONTH) + 1))
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        Report report = dataSnapshot.child(mUId).getValue(Report.class);
                        if (report != null) {
                            reportList.add(report);
                            calcSalary();
                        }

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

        mDatabase.getReference(Constants.FIREBASE_REF_USER_REPORTS)
                .child(String.valueOf(today.get(Calendar.YEAR)))
                .child(String.valueOf(today.get(Calendar.MONTH))) // TODO KOSTUL'
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        Report report = dataSnapshot.child(mUId).getValue(Report.class);
                        if (report != null) {
                            reportList2.add(report);
                            calcSalary2();
                        }

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

        panelCopy= (LinearLayout) findViewById(R.id.panel_copy);

        textCard = (TextView) findViewById(R.id.text_card);

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

    private void calcSalary() {

        int percent = 0;
        int stavka = 0;
        int mk = 0;

        for (Report rep : reportList) {
            stavka += Constants.SALARY_STAVKA;
            percent += rep.total;
            mk += rep.bMk * Constants.SALARY_BDAY_MK;
            mk += rep.mk1 * Constants.SALARY_ART_MK + rep.mk2 * Constants.SALARY_ART_MK;
        }
        percent = (int) (percent * Constants.SALARY_PERCENT);
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
            stavka += Constants.SALARY_STAVKA;
            percent += rep.total;
            mk += rep.bMk * Constants.SALARY_BDAY_MK;
            mk += rep.mk1 * Constants.SALARY_ART_MK + rep.mk2 * Constants.SALARY_ART_MK;
        }
        percent = (int) (percent * Constants.SALARY_PERCENT);
        int total = stavka + percent + mk;

        textMonth2.setText(Constants.MONTH_FULL[Calendar.getInstance().get(Calendar.MONTH) - 1] + "");

        textStavka2.setText(stavka + " грн");
        textPercent2.setText(percent + " грн");
        textMk2.setText(mk + " грн");
        textTotal2.setText(total + " ГРН");

        progressBar2.setProgress(total);
    }


    @Override
    protected void onResume() {
        super.onResume();

        calcSalary();
        calcSalary2();
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
