package com.example.android.kidsapp;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

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


    TextView textRoom60, textRoom30, textRoom20, textRoom10, textRoomTotal;
    TextView textBday50, textBday30, textBdayTotal, textBdayMk;
    TextView textMk1, textMk2, textMkT1, textMkT2, textMkTotal;

    TextView textDate, textMkName;
    LinearLayout panelDate, panelRoomExpand, panelRoomExpand2, panelRoomExpand3;

    SeekBar seekRoom60, seekRoom30, seekRoom20, seekRoom10;
    SeekBar seekBday50, seekBday30, seekBdayMk;
    SeekBar seekMkT1, seekMkT2, seekMk1, seekMk2;

    EditText inputRoom60, inputRoom30, inputRoom20, inputRoom10;
    EditText inputBday50, inputBday30, inputMk1, inputMk2;
    SwipeLayout swipeLayout, swipeLayout2, swipeLayout3;

    Switch switchMyMk;


    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private String mUId, mUserName;
    private Calendar mDate;
    private String mDateStr, mDateMonth, mDateYear, mDateDay;

    private Report mReport;

    private SimpleDateFormat mSdf = new SimpleDateFormat("d-M-yyyy", Locale.US);
    private boolean isChanged;

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

        initSwipesAndExpandPanels();

        initSeeks();

        loadReport();
    }

    /**
     * Initialize DatePicker
     */
    private void initDatePicker() {

        final DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                setDate(year, monthOfYear, dayOfMonth);
            }
        };

        // Pop up the Date Picker after user clicked on editText
        panelDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(ReportActivity.this, dateSetListener,
                        mDate.get(Calendar.YEAR), mDate.get(Calendar.MONTH),
                        mDate.get(Calendar.DAY_OF_MONTH)).show();


            }
        });
    }


    /**
     * Initialize references for all Views
     */
    private void initRef() {

        panelRoomExpand = (LinearLayout) findViewById(R.id.panel_room_expand);
        panelRoomExpand2 = (LinearLayout) findViewById(R.id.panel_room_expand2);
        panelRoomExpand3 = (LinearLayout) findViewById(R.id.panel_room_expand3);
        textDate = (TextView) findViewById(R.id.text_date);
        panelDate = (LinearLayout) findViewById(R.id.panel_date);
        // Room
        swipeLayout = (SwipeLayout) findViewById(R.id.swipe_layout);

        textRoomTotal = (TextView) findViewById(R.id.text_room_total);
        textRoom60 = (TextView) findViewById(R.id.text_room_60);
        textRoom30 = (TextView) findViewById(R.id.text_room_30);
        textRoom20 = (TextView) findViewById(R.id.text_room_20);
        textRoom10 = (TextView) findViewById(R.id.text_room_10);

        seekRoom60 = (SeekBar) findViewById(R.id.seek_room_60);
        seekRoom30 = (SeekBar) findViewById(R.id.seek_room_30);
        seekRoom20 = (SeekBar) findViewById(R.id.seek_room_20);
        seekRoom10 = (SeekBar) findViewById(R.id.seek_room_10);

        inputRoom60 = (EditText) findViewById(R.id.input_room_60);
        inputRoom30 = (EditText) findViewById(R.id.input_room_30);
        inputRoom20 = (EditText) findViewById(R.id.input_room_20);
        inputRoom10 = (EditText) findViewById(R.id.input_room_10);


        // BirthDay
        swipeLayout2 = (SwipeLayout) findViewById(R.id.swipe_layout2);

        textBdayTotal = (TextView) findViewById(R.id.text_bday_total);
        textBday50 = (TextView) findViewById(R.id.text_bday_50);
        textBday30 = (TextView) findViewById(R.id.text_bday_30);
        textBdayMk = (TextView) findViewById(R.id.text_bday_mk_done);

        seekBday50 = (SeekBar) findViewById(R.id.seek_bday_50);
        seekBday30 = (SeekBar) findViewById(R.id.seek_bday_30);
        seekBdayMk = (SeekBar) findViewById(R.id.seek_bday_mk_done);

        inputBday50 = (EditText) findViewById(R.id.input_bday_50);
        inputBday30 = (EditText) findViewById(R.id.input_bday_30);


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

        inputMk1 = (EditText) findViewById(R.id.input_mk_1);
        inputMk2 = (EditText) findViewById(R.id.input_mk_2);
        switchMyMk = (Switch) findViewById(R.id.switch_my_mk);
    }

    /**
     * Set OnSeekBarChanged and TextChanged Listeners for inputs
     */
    private void initSeeks() {

        seekRoom60.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mReport.r60 = progress;
                updateRoomTotal();
                if(fromUser)
                    closeSoftKeyBoard();

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        seekRoom30.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                mReport.r30 = progress;
                updateRoomTotal();
                if(fromUser)
                    closeSoftKeyBoard();
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

                mReport.r20 = progress;
                updateRoomTotal();
                if(fromUser)
                    closeSoftKeyBoard();

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

                mReport.r10 = progress;
                updateRoomTotal();
                if(fromUser)
                    closeSoftKeyBoard();

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

                mReport.b50 = progress;
                updateBdayTotal();
                if(fromUser)
                    closeSoftKeyBoard();

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBday30.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                mReport.b30 = progress;

                if (mReport.b30 > 0)
                    mReport.bMk = 1;
                else
                    mReport.bMk = 0;
                if (mReport.b30 > 10)
                    mReport.bMk = 2;

                if (mReport.b30 > 20)
                    mReport.bMk = 3;
                seekBdayMk.setProgress(mReport.bMk);

                updateBdayTotal();
                if(fromUser)
                    closeSoftKeyBoard();

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

                mReport.bMk = progress;
                String mkDone = getString(R.string.mk_done) + mReport.bMk;
                textBdayMk.setText(mkDone);

                updateBdayTotal();
                if(fromUser)
                    closeSoftKeyBoard();

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

                mReport.mkt1 = progress;
                updateMkTotal();
                if(fromUser)
                    closeSoftKeyBoard();

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

                mReport.mkt2 = progress;
                updateMkTotal();
                if(fromUser)
                    closeSoftKeyBoard();

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

                mReport.mk1 = progress;
                updateMkTotal();
                if(fromUser)
                    closeSoftKeyBoard();

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

                mReport.mk2 = progress;
                updateMkTotal();
                if(fromUser)
                    closeSoftKeyBoard();

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        inputRoom60.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int value = 0;
                try {
                    value = Integer.parseInt(String.valueOf(s));
                } catch (Exception e) {
                    //eat it
                }

                mReport.r60 = value;
                updateSeekBars();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        inputRoom30.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int value = 0;
                try {
                    value = Integer.parseInt(String.valueOf(s));
                } catch (Exception e) {
                    //eat it
                }

                mReport.r30 = value;
                updateSeekBars();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        inputRoom20.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int value = 0;
                try {
                    value = Integer.parseInt(String.valueOf(s));
                } catch (Exception e) {
                    //eat it
                }

                mReport.r20 = value;
                updateSeekBars();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        inputRoom10.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int value = 0;
                try {
                    value = Integer.parseInt(String.valueOf(s));
                } catch (Exception e) {
                    //eat it
                }

                mReport.r10 = value;
                updateSeekBars();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        inputBday50.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int value = 0;
                try {
                    value = Integer.parseInt(String.valueOf(s));
                } catch (Exception e) {
                    //eat it
                }

                mReport.b50= value;
                updateSeekBars();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        inputBday30.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int value = 0;
                try {
                    value = Integer.parseInt(String.valueOf(s));
                } catch (Exception e) {
                    //eat it
                }

                mReport.b30 = value;
                updateSeekBars();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        inputMk1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int value = 0;
                try {
                    value = Integer.parseInt(String.valueOf(s));
                } catch (Exception e) {
                    //eat it
                }

                mReport.mk1= value;
                updateSeekBars();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        inputMk2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int value = 0;
                try {
                    value = Integer.parseInt(String.valueOf(s));
                } catch (Exception e) {
                    //eat it
                }

                mReport.mk2 = value;
                updateSeekBars();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        switchMyMk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mReport.mkMy = isChecked;
                isChanged = true;
            }
        });
    }

    /**
     * Clear focus on focused View and hide Soft Keyboard
     */
    private void closeSoftKeyBoard() {
        // Clear Focus
        inputRoom60.clearFocus();
        inputRoom30.clearFocus();
        inputRoom20.clearFocus();
        inputRoom10.clearFocus();
        inputBday50.clearFocus();
        inputBday30.clearFocus();
        inputMk1.clearFocus();
        inputMk2.clearFocus();
        // Check if no view has focus:
        View view = ReportActivity.this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * Initialize Swipe panels and Expanded panels
     */
    private void initSwipesAndExpandPanels() {
        swipeLayout.addDrag(SwipeLayout.DragEdge.Left, R.id.swipe_menu);
        swipeLayout.setLeftSwipeEnabled(true);
        swipeLayout.setBottomSwipeEnabled(false);

        swipeLayout2.addDrag(SwipeLayout.DragEdge.Left, R.id.swipe_menu2);
        swipeLayout2.setLeftSwipeEnabled(true);
        swipeLayout2.setBottomSwipeEnabled(false);

        swipeLayout3.addDrag(SwipeLayout.DragEdge.Left, R.id.swipe_menu3);
        swipeLayout3.setLeftSwipeEnabled(true);
        swipeLayout3.setBottomSwipeEnabled(false);

        panelRoomExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swipeLayout.setVisibility((swipeLayout.getVisibility() == View.VISIBLE) ? View.GONE : View.VISIBLE);

                swipeLayout2.setVisibility(View.GONE);
                swipeLayout3.setVisibility(View.GONE);
            }
        });

        panelRoomExpand2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swipeLayout2.setVisibility((swipeLayout2.getVisibility() == View.VISIBLE) ? View.GONE : View.VISIBLE);

                swipeLayout.setVisibility(View.GONE);
                swipeLayout3.setVisibility(View.GONE);

            }
        });
        panelRoomExpand3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swipeLayout3.setVisibility((swipeLayout3.getVisibility() == View.VISIBLE) ? View.GONE : View.VISIBLE);

                swipeLayout2.setVisibility(View.GONE);
                swipeLayout.setVisibility(View.GONE);

            }
        });
    }


    /**
     * If exist load report from DB for selected day, otherwise create new report
     */
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

                        updateSeekBars();
                        updateMkName();

                        isChanged = false;
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

    /**
     * Save current report to DB
     */
    private void saveReport() {

        isChanged = false;
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

    /**
     * Update View with Mk name if exist
     */
    private void updateMkName() {
        if (mReport.getMkName() != null && !mReport.getMkName().isEmpty())
            textMkName.setText(mReport.getMkName());
    }

    /**
     * Update all text views on Room Panel
     * Update Report with new values
     * Update Title of Activity
     */
    void updateRoomTotal() {

        textRoom60.setText("60грн х " + mReport.r60 + " = " + (mReport.r60 * 60) + " ГРН");
        textRoom30.setText("30грн х " + mReport.r30 + " = " + (mReport.r30 * 30) + " ГРН");
        textRoom20.setText("20грн х " + mReport.r20 + " = " + (mReport.r20 * 20) + " ГРН");
        textRoom10.setText("10грн х " + mReport.r10 + " = " + (mReport.r10 * 10) + " ГРН");

        if (!inputRoom60.hasFocus())
            inputRoom60.setText(String.valueOf(mReport.r60));
        if (!inputRoom30.hasFocus())
            inputRoom30.setText(String.valueOf(mReport.r30));
        if (!inputRoom20.hasFocus())
            inputRoom20.setText(String.valueOf(mReport.r20));
        if (!inputRoom10.hasFocus())
            inputRoom10.setText(String.valueOf(mReport.r10));

        mReport.totalRoom = mReport.r60 * 60 + mReport.r30 * 30 + mReport.r20 * 20 + mReport.r10 * 10;

        String total = mReport.totalRoom + " ГРН";
        textRoomTotal.setText(total);

        updateTitle();
    }

    /**
     * Update all text views on BirthDayPanel
     * Update Report with new values
     * Update Title of Activity
     */
    void updateBdayTotal() {

        textBday50.setText("50грн х " + mReport.b50 + " = " + (mReport.b50 * 50) + " ГРН");
        textBday30.setText("30грн х " + mReport.b30 + " = " + (mReport.b30 * 30) + " ГРН");

        String mkDone = getString(R.string.mk_done) + mReport.bMk;
        textBdayMk.setText(mkDone);

        if (!inputBday50.hasFocus())
            inputBday50.setText(String.valueOf(mReport.b50));
        if (!inputBday30.hasFocus())
            inputBday30.setText(String.valueOf(mReport.b30));

        mReport.totalBday = mReport.b50 * 50 + mReport.b30 * 30;

        String total = (mReport.totalBday) + " ГРН";
        textBdayTotal.setText(total);

        updateTitle();
    }

    /**
     * Update all text views on Mk Panel
     * Update Report with new values
     * Update Title of Activity
     */
    void updateMkTotal() {
        int tar1 = 30 + mReport.mkt1 * 10;
        int tar2 = 30 + mReport.mkt2 * 10;

        mReport.totalMk = tar1 * mReport.mk1 + tar2 * mReport.mk2;

        textMkT1.setText("Тариф 1: " + tar1 + " грн x");
        textMkT2.setText("Тариф 2: " + tar2 + " грн x");

        textMk1.setText("  " + mReport.mk1 + " = " + (tar1 * mReport.mk1) + " ГРН");
        textMk2.setText("  " + mReport.mk2 + " = " + (tar2 * mReport.mk2) + " ГРН");

        if (!inputMk1.hasFocus())
            inputMk1.setText(String.valueOf(mReport.mk1));
        if (!inputMk2.hasFocus())
            inputMk2.setText(String.valueOf(mReport.mk2));


        String total = mReport.totalMk + " ГРН";
        textMkTotal.setText(total);
        updateTitle();
    }

    /**
     * Update Title of Activity
     */
    void updateTitle() {

        mReport.total = (mReport.totalRoom + mReport.totalBday + mReport.totalMk);
        setTitle(getResources().getString(R.string.title_activity_report) + " (" + mReport.total + " ГРН)");
        isChanged = true;
    }

    /**
     * Load values from Report to Seek Bars
     */
    void updateSeekBars() {
        seekRoom60.setProgress(mReport.r60);
        seekRoom30.setProgress(mReport.r30);
        seekRoom20.setProgress(mReport.r20);
        seekRoom10.setProgress(mReport.r10);

        seekBday50.setProgress(mReport.b50);
        seekBday30.setProgress(mReport.b30);
        seekBdayMk.setProgress(mReport.bMk);

        seekMk1.setProgress(mReport.mk1);
        seekMk2.setProgress(mReport.mk2);
        seekMkT1.setProgress(mReport.mkt1);
        seekMkT2.setProgress(mReport.mkt2);

        switchMyMk.setChecked(mReport.mkMy);
    }


    /**
     * Set new values of current date
     * @param calendar
     */
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

        textDate.setText(mDateStr);

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
        // Show Dialog if we have not saved data
        if (isChanged) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ReportActivity.this);
            builder.setTitle(R.string.dialog_report_save_before_close_title)
                    .setMessage(R.string.dialog_report_save_before_close_text)
                    .setPositiveButton("ЗБЕРЕГТИ ЗМІНИ", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            saveReport();
                            onBackPressed();

                        }

                    })
                    .setNegativeButton("НЕ ЗБЕРІГАТИ", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                            isChanged = false;
                            onBackPressed();
                        }
                    })
                    .show();
        } else {

            super.onBackPressed();
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        }
    }
}
