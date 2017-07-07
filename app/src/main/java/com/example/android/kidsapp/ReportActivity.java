package com.example.android.kidsapp;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.android.kidsapp.utils.Constants;
import com.example.android.kidsapp.utils.Report;
import com.example.android.kidsapp.utils.SwipeLayout;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.Calendar;

public class ReportActivity extends AppCompatActivity {

    private static final String TAG = ReportActivity.class.getSimpleName();

    Report mReport;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private FirebaseStorage mStorage = FirebaseStorage.getInstance();

    private DatabaseReference mDatabaseRefCurrentUser;
    private ValueEventListener mUserListener;
    private String mDateStr, mActivityTitle, mUId;


    TextView textRoom60, textRoom40, textRoom20, textRoomTotal;
    TextView textBday50, textBday25, textBdayTotal, textBdayMk;
    TextView textMk1, textMk2, textMkT1, textMkT2, textMkTotal;

    SeekBar seekRoom60, seekRoom40, seekRoom20;
    SeekBar seekBday50, seekBday25, seekBdayMk;
    SeekBar seekMkT1, seekMkT2, seekMk1, seekMk2;

    EditText inputRoom60, inputRoom40, inputRoom20;
    SwipeLayout swipeLayout, swipeLayout2, swipeLayout3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
        setContentView(R.layout.activity_report);

        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            String date = bundle.getString("date");
            mDateStr = date;

            String id = bundle.getString("uId");
            mUId = id;


            mActivityTitle = getResources().getString(R.string.title_activity_zvit) + " " + date.replace("201", "1") + "";
        } else {
            mUId = mAuth.getCurrentUser().getUid();
            Calendar today = Calendar.getInstance();
            mDateStr = today.get(Calendar.DAY_OF_MONTH) + "-" + (today.get(Calendar.MONTH) + 1) + "-" + today.get(Calendar.YEAR);

            mActivityTitle = getResources().getString(R.string.title_activity_zvit) + " "
                    + today.get(Calendar.DAY_OF_MONTH) + " "
                    + Constants.MONTH[today.get(Calendar.MONTH)];
        }

        initRef();

        initSwipes();

        loadReport();

        initSeeks();
    }


    private void initRef() {

        // Room
        swipeLayout = (SwipeLayout) findViewById(R.id.swipe_layout);
        textRoomTotal = (TextView) findViewById(R.id.text_room_total);
        textRoom60 = (TextView) findViewById(R.id.text_room_60);
        textRoom40 = (TextView) findViewById(R.id.text_room_40);
        textRoom20 = (TextView) findViewById(R.id.text_room_20);

        seekRoom60 = (SeekBar) findViewById(R.id.seek_room_60);
        seekRoom40 = (SeekBar) findViewById(R.id.seek_room_40);
        seekRoom20 = (SeekBar) findViewById(R.id.seek_room_20);

        inputRoom60 = (EditText) findViewById(R.id.input_room_60);
        inputRoom40 = (EditText) findViewById(R.id.input_room_40);
        inputRoom20 = (EditText) findViewById(R.id.input_room_20);


        // BirthDay
        swipeLayout2 = (SwipeLayout) findViewById(R.id.swipe_layout2);
        textBdayTotal = (TextView) findViewById(R.id.text_bday_total);
        textBday50 = (TextView) findViewById(R.id.text_bday_50);
        textBday25 = (TextView) findViewById(R.id.text_bday_25);
        textBdayMk = (TextView) findViewById(R.id.text_bday_mk_done);

        seekBday50 = (SeekBar) findViewById(R.id.seek_bday_50);
        seekBday25 = (SeekBar) findViewById(R.id.seek_bday_25);
        seekBdayMk = (SeekBar) findViewById(R.id.seek_bday_mk_done);

        // MK
        swipeLayout3 = (SwipeLayout) findViewById(R.id.swipe_layout3);

        seekMk1 = (SeekBar) findViewById(R.id.seek_mk_1);
        seekMk2 = (SeekBar) findViewById(R.id.seek_mk_2);
        seekMkT1 = (SeekBar) findViewById(R.id.seek_mk_t1);
        seekMkT2 = (SeekBar) findViewById(R.id.seek_mk_t2);

        textMkTotal = (TextView) findViewById(R.id.text_mk_total);
        textMk1 = (TextView) findViewById(R.id.text_mk_1);
        textMk2 = (TextView) findViewById(R.id.text_mk_2);
        textMkT1 = (TextView) findViewById(R.id.text_mk_t1);
        textMkT2 = (TextView) findViewById(R.id.text_mk_t2);
    }

    private void initSeeks() {
        seekRoom60.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mReport.r60 = progress;
                    updateRoomTotal();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekRoom40.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mReport.r40 = progress;
                    updateRoomTotal();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekRoom20.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mReport.r20 = progress;
                    updateRoomTotal();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        seekBday50.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mReport.b50 = progress;
                    updateBdayTotal();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBday25.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mReport.b25 = progress;

                    if (mReport.b25 > 0)
                        mReport.bMk = 1;
                    else
                        mReport.bMk = 0;
                    if (mReport.b25 > 10)
                        mReport.bMk = 2;

                    if (mReport.b25 > 20)
                        mReport.bMk = 3;
                    seekBdayMk.setProgress(mReport.bMk);

                    updateBdayTotal();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBdayMk.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mReport.bMk = progress;
                    textBdayMk.setText("Проведено Майстер Класів - " + mReport.bMk);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // MK

        seekMkT1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mReport.mkt1 = progress;
                    updateMkTotal();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekMkT2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mReport.mkt2 = progress;
                    updateMkTotal();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekMk1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mReport.mk1 = progress;
                    updateMkTotal();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekMk2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mReport.mk2 = progress;
                    updateMkTotal();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void initSwipes() {
        swipeLayout2.addDrag(SwipeLayout.DragEdge.Right, R.id.swipe_menu2);
        swipeLayout2.setRightSwipeEnabled(true);
        swipeLayout2.setBottomSwipeEnabled(false);

        swipeLayout3.addDrag(SwipeLayout.DragEdge.Right, R.id.swipe_menu3);
        swipeLayout3.setRightSwipeEnabled(true);
        swipeLayout3.setBottomSwipeEnabled(false);

        swipeLayout.addDrag(SwipeLayout.DragEdge.Right, R.id.swipe_menu);
        swipeLayout.setRightSwipeEnabled(true);
        swipeLayout.setBottomSwipeEnabled(false);
    }


    private void loadReport() {
        final String userName = mAuth.getCurrentUser().getDisplayName();

        mDatabase.getReference(Constants.FIREBASE_REF_REPORTS).child(mDateStr).child(mUId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mReport = dataSnapshot.getValue(Report.class);
                        if (mReport == null) {
                            mReport = new Report(mUId, userName, mDateStr);
                        }
                        updateRoomTotal();
                        updateBdayTotal();
                        updateMkTotal();
                        updateSeekBars();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

    private void saveReport() {
        mDatabase.getReference(Constants.FIREBASE_REF_REPORTS).child(mDateStr).child(mUId)
                .setValue(mReport).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                Snackbar sn = Snackbar.make(textRoom60, "Звіт збережено", Snackbar.LENGTH_LONG)
                        .setAction("ВІДМІНИТИ", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //TODO Implement UNDO for saving report
                            }
                        }).setActionTextColor(Color.YELLOW);
                sn.show();


            }
        })
        ;
        mDatabase.getReference(Constants.FIREBASE_REF_USERS)
                .child(mUId)
                .child(Constants.FIREBASE_REF_REPORTS)
                .child(mDateStr)
                .setValue(mReport);
    }


    void updateRoomTotal() {
        textRoom60.setText("60грн х " + mReport.r60 + " = " + (mReport.r60 * 60) + " ГРН");
        textRoom40.setText("40грн х " + mReport.r40 + " = " + (mReport.r40 * 40) + " ГРН");
        textRoom20.setText("20грн х " + mReport.r20 + " = " + (mReport.r20 * 20) + " ГРН");

        inputRoom60.setText(String.valueOf(mReport.r60));
        inputRoom40.setText(String.valueOf(mReport.r40));
        inputRoom20.setText(String.valueOf(mReport.r20));

        mReport.totalRoom = mReport.r60 * 60 + mReport.r40 * 40 + mReport.r20 * 20;

        textRoomTotal.setText((mReport.totalRoom) + " ГРН");

        updateTitel();
    }

    void updateBdayTotal() {

        textBday50.setText("50грн х " + mReport.b50 + " = " + (mReport.b50 * 50) + " ГРН");
        textBday25.setText("25грн х " + mReport.b25 + " = " + (mReport.b25 * 25) + " ГРН");
        textBdayMk.setText("Проведено Майстер Класів - " + mReport.bMk);

        mReport.totalBday = mReport.b50 * 50 + mReport.b25 * 25;

        textBdayTotal.setText((mReport.totalBday) + " ГРН");

        updateTitel();
    }

    void updateMkTotal() {
        int tar1 = 30 + mReport.mkt1 * 10;
        int tar2 = 30 + mReport.mkt2 * 10;

        mReport.totalMk = tar1 * mReport.mk1 + tar2 * mReport.mk2;

        textMkT1.setText("Тариф 1:  " + tar1 + " грн  x");
        textMkT2.setText("Тариф 2:  " + tar2 + " грн  x");

        textMk1.setText("  " + mReport.mk1 + " = " + (tar1 * mReport.mk1) + " ГРН");
        textMk2.setText("  " + mReport.mk2 + " = " + (tar2 * mReport.mk2) + " ГРН");

        textMkTotal.setText(mReport.totalMk + " ГРН");
        updateTitel();
    }

    void updateTitel() {

        mReport.total = (mReport.totalRoom + mReport.totalBday + mReport.totalMk);
        setTitle(mActivityTitle + " (" + mReport.total + " ГРН)");
    }

    void updateSeekBars() {
        seekRoom60.setProgress(mReport.r60);
        seekRoom40.setProgress(mReport.r40);
        seekRoom20.setProgress(mReport.r20);

        seekBday50.setProgress(mReport.b50);
        seekBday25.setProgress(mReport.b25);
        seekBdayMk.setProgress(mReport.bMk);

        seekMk1.setProgress(mReport.mk1);
        seekMk2.setProgress(mReport.mk2);
        seekMkT1.setProgress(mReport.mkt1);
        seekMkT2.setProgress(mReport.mkt2);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_zvit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            //todo ask if user want to save changes in report (if exist)
            onBackPressed();
            return true;
        }

        switch (id) {
            case R.id.action_add: {
                saveReport();
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
