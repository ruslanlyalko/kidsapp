package com.example.android.kidsapp;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.kidsapp.utils.Constants;
import com.example.android.kidsapp.utils.Cost;
import com.example.android.kidsapp.utils.Report;
import com.example.android.kidsapp.utils.User;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    TextView textTotal, textRoom, textBday, textMk, textMonth;
    ImageButton buttonPrev, buttonNext;
    CompactCalendarView compactCalendarView;

    TextView textCostTotal, textCostCommon, textCostMk;
    TextView textSalaryTotal, textSalaryStavka, textSalaryPercent, textSalaryMk;

    private List<Cost> costList = new ArrayList<>();
    ProgressBar progressBar, progressBarCost;
    List<Report> reportList = new ArrayList<>();

    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private List<User> userList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        setContentView(R.layout.activity_dashboard);

        initRef();

        initCalendar();

        Calendar month = Calendar.getInstance();
        textMonth.setText(Constants.MONTH_FULL[month.get(Calendar.MONTH)]);

        String yearStr = new SimpleDateFormat("yyyy", Locale.US).format(new Date()).toString();
        String monthStr = new SimpleDateFormat("M", Locale.US).format(new Date()).toString();

        loadReports(yearStr, monthStr);

        loadCosts(yearStr, monthStr);

        loadUsers();

    }

    private void initRef() {

        buttonNext = (ImageButton) findViewById(R.id.button_next);
        buttonPrev = (ImageButton) findViewById(R.id.button_prev);
        compactCalendarView = (CompactCalendarView) findViewById(R.id.compactcalendar_view);

        textMonth = (TextView) findViewById(R.id.text_month);
        textTotal = (TextView) findViewById(R.id.text_total);
        textRoom = (TextView) findViewById(R.id.text_room_total);
        textBday = (TextView) findViewById(R.id.text_bday_total);
        textMk = (TextView) findViewById(R.id.text_mk_total);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);


        progressBarCost = (ProgressBar) findViewById(R.id.progress_bar_cost);
        textCostTotal = (TextView) findViewById(R.id.text_cost_total);
        textCostCommon = (TextView) findViewById(R.id.text_cost_common);
        textCostMk = (TextView) findViewById(R.id.text_cost_mk);

        textSalaryTotal = (TextView) findViewById(R.id.text_salary_total);
        textSalaryStavka = (TextView) findViewById(R.id.text_stavka_total);
        textSalaryPercent = (TextView) findViewById(R.id.text_percent_total);
        textSalaryMk = (TextView) findViewById(R.id.text_salary_mk_total);

    }

    private void initCalendar() {
        compactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                Calendar month = Calendar.getInstance();
                month.setTime(firstDayOfNewMonth);

                String yearSimple = new SimpleDateFormat("yy", Locale.US).format(firstDayOfNewMonth).toString();

                String str = Constants.MONTH_FULL[month.get(Calendar.MONTH)];

                if (firstDayOfNewMonth.getYear() != new Date().getYear())
                    str = str + "'" + yearSimple;

                textMonth.setText(str);

                String yearStr = new SimpleDateFormat("yyyy", Locale.US).format(firstDayOfNewMonth).toString();
                String monthStr = new SimpleDateFormat("M", Locale.US).format(firstDayOfNewMonth).toString();

                loadReports(yearStr, monthStr);
                loadCosts(yearStr, monthStr);
                loadUsers();

            }
        });
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                compactCalendarView.showNextMonth();
            }
        });

        buttonPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                compactCalendarView.showPreviousMonth();
            }
        });
    }

    private void loadReports(String yearStr, String monthStr) {

        reportList.clear();
        calcOborot();
        mDatabase.getReference(Constants.FIREBASE_REF_USER_REPORTS)
                .child(yearStr)
                .child(monthStr)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        for (DataSnapshot ds : dataSnapshot.getChildren()) {

                            Report report = ds.getValue(Report.class);
                            if (report != null) {
                                reportList.add(report);
                                calcOborot();
                            }
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

    private void calcOborot() {

        int room = 0;
        int bday = 0;
        int mk = 0;

        for (Report rep : reportList) {
            room += rep.totalRoom;
            bday += rep.totalBday;
            mk += rep.totalMk;
        }

        int total = room + bday + mk;

        textRoom.setText(room + " грн");
        textBday.setText(bday + " грн");
        textMk.setText(mk + " грн");
        textTotal.setText(total + " ГРН ("+ (total*80/100)+")");

        progressBar.setProgress(total);
    }

    private void loadCosts(String yearStr, String monthStr) {
        costList.clear();
        calcCostTotal();

        mDatabase.getReference(Constants.FIREBASE_REF_COSTS).child(yearStr).child(monthStr)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Cost cost = dataSnapshot.getValue(Cost.class);
                        if (cost != null) {
                            costList.add(0, cost);
                            calcCostTotal();
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

    private void calcCostTotal() {
        int common = 0;
        int mk = 0;

        for (Cost cost : costList) {
            if (cost.getTitle2().equals(getString(R.string.text_cost_common)))
                common += cost.getPrice();
            if (cost.getTitle2().equals(getString(R.string.text_cost_mk)))
                mk += cost.getPrice();
        }

        int total = common + mk;

        progressBarCost.setMax(total);
        progressBarCost.setProgress(common);

        textCostCommon.setText(common + " грн");
        textCostMk.setText(mk + " грн");

        textCostTotal.setText(total + " ГРН");
    }

    private void loadUsers() {

        userList.clear();
        calcSalaryForUsers();

        mDatabase.getReference(Constants.FIREBASE_REF_USERS)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        User user = dataSnapshot.getValue(User.class);
                        if (user != null) {
                            userList.add(0, user);
                            calcSalaryForUsers();
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

    private void calcSalaryForUsers() {

        int total = 0;
        int percent = 0;
        int stavka = 0;
        int mk = 0;

        for (User user : userList) {
            int total1=0;
            for (Report rep : reportList) {

                if (rep.userName.equals(user.userName)) {

                    stavka += user.getUserStavka();
                    total1 += rep.total;
                    mk += rep.bMk * user.getUserMk();
                    mk += (rep.mk1 + rep.mk2) * user.getUserArt();
                }
            }
            percent += total1 * user.getUserPercent() / 100;

        }

        total += stavka + percent + mk;
        textSalaryStavka.setText(stavka + " грн");
        textSalaryPercent.setText(percent + " грн");
        textSalaryMk.setText(mk + " грн");

        textSalaryTotal.setText(total+" ГРН");
    }

    @Override
    protected void onResume() {
        super.onResume();

        calcOborot();
        calcCostTotal();
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
