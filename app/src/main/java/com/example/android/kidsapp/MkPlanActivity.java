package com.example.android.kidsapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.android.kidsapp.utils.Constants;
import com.example.android.kidsapp.utils.MkPlanAdapter;
import com.example.android.kidsapp.utils.Report;
import com.example.android.kidsapp.utils.Utils;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MkPlanActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    CompactCalendarView compactCalendarView;
    ImageButton buttonPrev, buttonNext;
    TextView textMonth;

    private MkPlanAdapter adapter;
    private List<Report> reportList = new ArrayList<>();
    private Date mCurrentDate;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    //private String mDay, mMonth, mYear;
    private SimpleDateFormat mSdf = new SimpleDateFormat("d-M-yyyy", Locale.US);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mk_plan);
        initRef();

        initRecycler();

        Date today = Calendar.getInstance().getTime();

        showReportsForDate(today);

    }

    private void initRecycler() {
        reportList = new ArrayList<>();
        adapter = new MkPlanAdapter(this, reportList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

    }

    private void initRef() {
        textMonth = (TextView) findViewById(R.id.text_month);
        buttonNext = (ImageButton) findViewById(R.id.button_next);
        buttonPrev = (ImageButton) findViewById(R.id.button_prev);

        compactCalendarView = (CompactCalendarView) findViewById(R.id.calendar_view);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        Calendar month = Calendar.getInstance();
        textMonth.setText(Constants.MONTH_FULL[month.get(Calendar.MONTH)]);

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

                showReportsForDate(firstDayOfNewMonth);

            }
        });
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
                .child(mYear).child(mMonth);

        listOfUsersReports.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Report report = data.getValue(Report.class);

                    // Add to list only current user reports
                    // But if user role - Admin then add all reports
                    if (report != null) {
                        if (Utils.isIsAdmin() || report.getUserId().equals(uId)) {
                            // check if has MK name
                            if (report.getTotalMk() > 0 || (report.getMkRef() != null && !report.getMkRef().isEmpty())) {
                                reportList.add(report);
                                adapter.notifyDataSetChanged();
                            }
                        }
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
