package com.example.android.kidsapp;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.MenuItem;
import android.view.View;

import com.example.android.kidsapp.utils.Constants;
import com.example.android.kidsapp.utils.Report;
import com.example.android.kidsapp.utils.ReportsAdapter;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    CompactCalendarView compactCalendarView;
    SwipeRefreshLayout swipeRefresh;


    private ReportsAdapter adapter;
    private List<Report> reportList = new ArrayList<>();
    private ArrayList<String> usersList = new ArrayList<>();
    private boolean isAdmin = false;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    //private String mDay, mMonth, mYear;
    private SimpleDateFormat mSdf = new SimpleDateFormat("d-M-yyyy", Locale.US);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
        setContentView(R.layout.activity_calendar);

        initRef();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            isAdmin = bundle.getBoolean(Constants.EXTRA_IS_ADMIN, false);
        }

        initRecycle();

        showReportsOnCalendar();

        Date today = Calendar.getInstance().getTime();
        showReportsForDate(today);

        compactCalendarView.setUseThreeLetterAbbreviation(true);
        compactCalendarView.shouldDrawIndicatorsBelowSelectedDays(true);


        // define a listener to receive callbacks when certain events happen.
        compactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {

                showReportsForDate(dateClicked);
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
            }
        });

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                showReportsOnCalendar();
            }
        });


    }

    private void showReportsOnCalendar() {

        usersList.clear();
        compactCalendarView.removeAllEvents();

        String yearStr = DateFormat.format("yyyy", Calendar.getInstance()).toString();

        mDatabase.getReference(Constants.FIREBASE_REF_USER_REPORTS).child(yearStr)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        for (DataSnapshot datMonth : dataSnapshot.getChildren()) {
                            for (DataSnapshot datDay : datMonth.getChildren()) {
                                Report report = datDay.getValue(Report.class);

                                if (isAdmin || report.getUserId().equals(mAuth.getCurrentUser().getUid())) {

                                    int color = getUserColor(report.getUserId());
                                    long date = getDateLongFromStr(report.getDate());
                                    String uId = report.getUserId();

                                    compactCalendarView.addEvent(
                                            new Event(color, date, uId), true);
                                }
                            }
                        }
                        swipeRefresh.setRefreshing(false);
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

    private long getDateLongFromStr(String dateStr) {
        long dateLong = Calendar.getInstance().getTime().getTime();
        Date date;

        try {
            date = mSdf.parse(dateStr);
            dateLong = date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return dateLong;
    }

    private int getUserColor(String userId) {

        if (!usersList.contains(userId)) {

            usersList.add(userId);
        }

        int index = usersList.indexOf(userId);
        if (index < 6)
            return Constants.COLORS[index];
        else
            return Color.GREEN;
    }

    private void showReportsForDate(Date date) {

        String mDay = DateFormat.format("d", date).toString();
        String mMonth = DateFormat.format("M", date).toString();
        String mYear = DateFormat.format("yyyy", date).toString();

        final String uId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        reportList.clear();
        adapter.notifyDataSetChanged();

        DatabaseReference listOfUsersReports = mDatabase.getReference(Constants.FIREBASE_REF_USER_REPORTS)
                .child(mYear).child(mMonth).child(mDay);

        listOfUsersReports.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Report report = dataSnapshot.getValue(Report.class);

                // Add to list only current user reports
                // But if user role - Admin then add all reports
                if (report != null) {
                    if (isAdmin || report.getUserId().equals(uId)) {
                        reportList.add(report);
                        adapter.notifyDataSetChanged();
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

    private void initRecycle() {

        reportList = new ArrayList<>();
        adapter = new ReportsAdapter(this, reportList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

    }

    private void initRef() {

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        compactCalendarView = (CompactCalendarView) findViewById(R.id.compactcalendar_view);

        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipere_fresh);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        switch (id) {
            case R.id.action_edit: {
                //todo edit zvit
            }
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
    }
}
