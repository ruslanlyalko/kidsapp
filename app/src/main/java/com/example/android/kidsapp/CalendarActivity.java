package com.example.android.kidsapp;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.CalendarView;

import com.example.android.kidsapp.utils.Constants;
import com.example.android.kidsapp.utils.Report;
import com.example.android.kidsapp.utils.ReportsAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarActivity extends AppCompatActivity {

    private static final String TAG = CalendarActivity.class.getSimpleName();


    private RecyclerView recyclerView;
    private ReportsAdapter adapter;
    private List<Report> reportList;
    private CalendarView calendarView;


    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private FirebaseStorage mStorage = FirebaseStorage.getInstance();

    private DatabaseReference mDatabaseRefCurrentUser;
    private ValueEventListener mUserListener;
    private String mCurrentDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
        setContentView(R.layout.activity_calendar);

        initRef();

        initRecycle();

        loadData(mCurrentDate);

    }

    @Override
    protected void onResume() {
        super.onResume();


        //loadData(mCurrentDate);
    }

    private void loadData(String date) {

        reportList.clear();
        adapter.notifyDataSetChanged();

        DatabaseReference listOfUsersReports = mDatabase.getReference(Constants.FIREBASE_REF_REPORTS).child(date);

        listOfUsersReports.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //Toast.makeText(CalendarActivity.this, dataSnapshot.getKey(), Toast.LENGTH_SHORT).show();
                Report report = dataSnapshot.getValue(Report.class);

                // TODO if user is not Admin then show only his data

                reportList.add(report);
                adapter.notifyDataSetChanged();
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

        Calendar today = Calendar.getInstance();
        mCurrentDate = today.get(Calendar.DAY_OF_MONTH) + "-" + (today.get(Calendar.MONTH) + 1) + "-" + today.get(Calendar.YEAR);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        calendarView = (CalendarView) findViewById(R.id.calendar_d);

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                mCurrentDate = dayOfMonth+"-"+(month+1)+"-"+year;
                loadData(mCurrentDate);
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
