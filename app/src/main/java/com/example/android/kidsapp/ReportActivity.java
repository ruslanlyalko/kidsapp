package com.example.android.kidsapp;

import android.app.DatePickerDialog;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.kidsapp.utils.Constants;
import com.example.android.kidsapp.utils.Report;
import com.example.android.kidsapp.utils.SwipeLayout;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ReportActivity extends AppCompatActivity {


    TextView textRoom60, textRoom40, textRoom20, textRoom10, textRoomTotal;
    TextView textBday50, textBday25, textBdayTotal, textBdayMk;
    TextView textMk1, textMk2, textMkT1, textMkT2, textMkTotal;
    EditText inputDate;
    TextView textMkName;


    SeekBar seekRoom60, seekRoom40, seekRoom20, seekRoom10;
    SeekBar seekBday50, seekBday25, seekBdayMk;
    SeekBar seekMkT1, seekMkT2, seekMk1, seekMk2;

    EditText inputRoom60, inputRoom40, inputRoom20, inputRoom10;
    SwipeLayout swipeLayout, swipeLayout2, swipeLayout3;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private String mUId, mUserName;
    private Calendar mDate;
    private String mDateStr, mDateMonth, mDateYear, mDateDay;

    private Report mReport;

    private SimpleDateFormat mSdf = new SimpleDateFormat("d-M-yyyy", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
        setContentView(R.layout.activity_report);

        initRef();

        Bundle bundle = getIntent().getExtras();

        // If there are next extras then we need to operate with them
        if (bundle == null) {

            if (mAuth.getCurrentUser() != null)
                mUId = mAuth.getCurrentUser().getUid();

            if (mAuth.getCurrentUser() != null)
                mUserName = mAuth.getCurrentUser().getDisplayName();

            setDate(Calendar.getInstance());

        } else {

            mUId = bundle.getString(Constants.EXTRA_UID);

            mUserName = bundle.getString(Constants.EXTRA_USER_NAME);

            setDate(bundle.getString(Constants.EXTRA_DATE));
        }


        initDatePicker();

        initSwipes();

        initSeeks();

        loadReport();
    }


    private void initDatePicker() {

        final DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                setDate(year, monthOfYear, dayOfMonth);
            }
        };

        // Pop up the Date Picker after user clicked on editText
        inputDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(ReportActivity.this, dateSetListener,
                        mDate.get(Calendar.YEAR), mDate.get(Calendar.MONTH),
                        mDate.get(Calendar.DAY_OF_MONTH)).show();


            }
        });
    }


    private void initRef() {

        inputDate = (EditText) findViewById(R.id.input_date);
        // Room
        swipeLayout = (SwipeLayout) findViewById(R.id.swipe_layout);

        textRoomTotal = (TextView) findViewById(R.id.text_room_total);
        textRoom60 = (TextView) findViewById(R.id.text_room_60);
        textRoom40 = (TextView) findViewById(R.id.text_room_40);
        textRoom20 = (TextView) findViewById(R.id.text_room_20);
        textRoom10 = (TextView) findViewById(R.id.text_room_10);

        seekRoom60 = (SeekBar) findViewById(R.id.seek_room_60);
        seekRoom40 = (SeekBar) findViewById(R.id.seek_room_40);
        seekRoom20 = (SeekBar) findViewById(R.id.seek_room_20);
        seekRoom10 = (SeekBar) findViewById(R.id.seek_room_10);

        inputRoom60 = (EditText) findViewById(R.id.input_room_60);
        inputRoom40 = (EditText) findViewById(R.id.input_room_40);
        inputRoom20 = (EditText) findViewById(R.id.input_room_20);
        inputRoom10 = (EditText) findViewById(R.id.input_room_10);


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

        textMkName = (TextView) findViewById(R.id.text_mk_name);

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
        seekRoom10.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mReport.r10 = progress;
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
                    String mkDone = getString(R.string.mk_done) + mReport.bMk;
                    textBdayMk.setText(mkDone);
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

        mDatabase.getReference(Constants.FIREBASE_REF_USER_REPORTS)
                .child(mDateYear).child(mDateMonth).child(mDateDay).child(mUId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mReport = dataSnapshot.getValue(Report.class);
                        if (mReport == null) {
                            mReport = new Report(mUId, mUserName, mDateStr);
                        }
                        updateRoomTotal();
                        updateBdayTotal();
                        updateMkTotal();
                        updateSeekBars();
                        updateMkName();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

    private void saveReport() {

        mDatabase.getReference(Constants.FIREBASE_REF_USER_REPORTS)
                .child(mDateYear).child(mDateMonth).child(mDateDay)
                .child(mUId)
                .setValue(mReport).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Snackbar.make(textRoom60, getString(R.string.toast_report_saved), Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void updateMkName() {
        if (mReport.getMkName() != null && !mReport.getMkName().isEmpty())
            textMkName.setText(mReport.getMkName());
    }

    void updateRoomTotal() {
        textRoom60.setText("60грн х " + mReport.r60 + " = " + (mReport.r60 * 60) + " ГРН");
        textRoom40.setText("40грн х " + mReport.r40 + " = " + (mReport.r40 * 40) + " ГРН");
        textRoom20.setText("20грн х " + mReport.r20 + " = " + (mReport.r20 * 20) + " ГРН");
        textRoom10.setText("10грн х " + mReport.r10 + " = " + (mReport.r10 * 10) + " ГРН");

        inputRoom60.setText(String.valueOf(mReport.r60));
        inputRoom40.setText(String.valueOf(mReport.r40));
        inputRoom20.setText(String.valueOf(mReport.r20));
        inputRoom10.setText(String.valueOf(mReport.r10));

        mReport.totalRoom = mReport.r60 * 60 + mReport.r40 * 40 + mReport.r20 * 20 + mReport.r10 * 10;

        String total = mReport.totalRoom + " ГРН";
        textRoomTotal.setText(total);

        updateTitle();
    }

    void updateBdayTotal() {

        textBday50.setText("50грн х " + mReport.b50 + " = " + (mReport.b50 * 50) + " ГРН");
        textBday25.setText("25грн х " + mReport.b25 + " = " + (mReport.b25 * 25) + " ГРН");

        String mkDone = getString(R.string.mk_done) + mReport.bMk;
        textBdayMk.setText(mkDone);

        mReport.totalBday = mReport.b50 * 50 + mReport.b25 * 25;

        String total = (mReport.totalBday) + " ГРН";
        textBdayTotal.setText(total);

        updateTitle();
    }

    void updateMkTotal() {
        int tar1 = 30 + mReport.mkt1 * 10;
        int tar2 = 30 + mReport.mkt2 * 10;

        mReport.totalMk = tar1 * mReport.mk1 + tar2 * mReport.mk2;

        textMkT1.setText("Тариф 1:  " + tar1 + " грн  x");
        textMkT2.setText("Тариф 2:  " + tar2 + " грн  x");

        textMk1.setText("  " + mReport.mk1 + " = " + (tar1 * mReport.mk1) + " ГРН");
        textMk2.setText("  " + mReport.mk2 + " = " + (tar2 * mReport.mk2) + " ГРН");

        String total = mReport.totalMk + " ГРН";
        textMkTotal.setText(total);
        updateTitle();
    }

    void updateTitle() {

        mReport.total = (mReport.totalRoom + mReport.totalBday + mReport.totalMk);
        setTitle(getResources().getString(R.string.title_activity_report) + " (" + mReport.total + " ГРН)");
    }

    void updateSeekBars() {
        seekRoom60.setProgress(mReport.r60);
        seekRoom40.setProgress(mReport.r40);
        seekRoom20.setProgress(mReport.r20);
        seekRoom10.setProgress(mReport.r10);

        seekBday50.setProgress(mReport.b50);
        seekBday25.setProgress(mReport.b25);
        seekBdayMk.setProgress(mReport.bMk);

        seekMk1.setProgress(mReport.mk1);
        seekMk2.setProgress(mReport.mk2);
        seekMkT1.setProgress(mReport.mkt1);
        seekMkT2.setProgress(mReport.mkt2);
    }


    private void setDate(Calendar calendar) {

        mDate = calendar;
        mDateStr = mSdf.format(mDate.getTime());

        fillDateStr(mDateStr);
    }

    private void setDate(String dateStr) {
        mDateStr = dateStr;
        mDate = getDateFromStr(mDateStr);

        fillDateStr(mDateStr);
    }

    private void setDate(int year, int monthOfYear, int dayOfMonth) {
        mDate.set(Calendar.YEAR, year);
        mDate.set(Calendar.MONTH, monthOfYear);
        mDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        mDateStr = mSdf.format(mDate.getTime());

        fillDateStr(mDateStr);
    }

    private void fillDateStr(String dateStr) {
        int first = dateStr.indexOf('-');
        int last = dateStr.lastIndexOf('-');

        mDateDay = dateStr.substring(0, first);
        mDateMonth = dateStr.substring(first + 1, last);
        mDateYear = dateStr.substring(last + 1);

        inputDate.setText(mDateStr);

        if (mReport != null)
            mReport.date = mDateStr;
    }

    private Calendar getDateFromStr(String dateStr) {

        Calendar date = Calendar.getInstance();

        try {
            date.setTime(mSdf.parse(dateStr));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
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
