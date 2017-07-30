package com.example.android.kidsapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.android.kidsapp.utils.Constants;
import com.example.android.kidsapp.utils.Report;
import com.example.android.kidsapp.utils.ReportsAdapter;
import com.example.android.kidsapp.utils.Utils;
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
    ImageButton buttonPrev, buttonNext;
    TextView textMonth;

    private ReportsAdapter adapter;
    private List<Report> reportList = new ArrayList<>();
    private ArrayList<String> usersList = new ArrayList<>();
    private Date mCurrentDate;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    //private String mDay, mMonth, mYear;
    private SimpleDateFormat mSdf = new SimpleDateFormat("d-M-yyyy", Locale.US);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        initRef();

        initRecycle();

        initCalendarView();

        showReportsOnCalendar();

        Date today = Calendar.getInstance().getTime();

        showReportsForDate(today);

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

    private void initCalendarView() {


        compactCalendarView.setUseThreeLetterAbbreviation(true);
        compactCalendarView.shouldDrawIndicatorsBelowSelectedDays(true);
        compactCalendarView.shouldScrollMonth(false);
        compactCalendarView.displayOtherMonthDays(true);

        Calendar month = Calendar.getInstance();
        textMonth.setText(Constants.MONTH_FULL[month.get(Calendar.MONTH)]);

        // define a listener to receive callbacks when certain events happen.
        compactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {

                showReportsForDate(dateClicked);
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
            }
        });
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                showReportsOnCalendar();
                reloadReportsForDate();
            }
        });


    }

    private void showReportsOnCalendar() {

        swipeRefresh.setRefreshing(true);
        usersList.clear();
        compactCalendarView.removeAllEvents();

        String yearStr = DateFormat.format("yyyy", Calendar.getInstance()).toString();

        mDatabase.getReference(Constants.FIREBASE_REF_REPORTS).child(yearStr)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        for (DataSnapshot datMonth : dataSnapshot.getChildren()) {
                            for (DataSnapshot datDay : datMonth.getChildren()) {
                                Report report = datDay.getValue(Report.class);

                                if (Utils.isAdmin() || report.getUserId().equals(mAuth.getCurrentUser().getUid())) {

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

        int [] colors = getResources().getIntArray(R.array.colors);
        if (index < 6)
            return colors[index];
        else
            return Color.GREEN;
    }

    private void reloadReportsForDate() {
        showReportsForDate(mCurrentDate);
    }

    private void showReportsForDate(Date date) {

        mCurrentDate = date;
        String mDay = DateFormat.format("d", date).toString();
        String mMonth = DateFormat.format("M", date).toString();
        String mYear = DateFormat.format("yyyy", date).toString();

        final String uId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        reportList.clear();
        adapter.notifyDataSetChanged();

        DatabaseReference listOfUsersReports = mDatabase.getReference(Constants.FIREBASE_REF_REPORTS)
                .child(mYear).child(mMonth).child(mDay);

        listOfUsersReports.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Report report = dataSnapshot.getValue(Report.class);

                // Add to list only current user reports
                // But if user role - Admin then add all reports
                if (report != null) {
                    if (Utils.isAdmin() || report.getUserId().equals(uId)) {
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

        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipere_fresh);

        textMonth = (TextView) findViewById(R.id.text_month);
        buttonNext = (ImageButton) findViewById(R.id.button_next);
        buttonPrev = (ImageButton) findViewById(R.id.button_prev);

        compactCalendarView = (CompactCalendarView) findViewById(R.id.calendar_view);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        showReportsOnCalendar();
        reloadReportsForDate();
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
    }
}
