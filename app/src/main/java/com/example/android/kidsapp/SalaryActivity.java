package com.example.android.kidsapp;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.kidsapp.utils.Constants;
import com.example.android.kidsapp.utils.Report;
import com.example.android.kidsapp.utils.User;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SalaryActivity extends AppCompatActivity {

    ImageButton buttonPrev, buttonNext;
    CompactCalendarView compactCalendarView;
    LinearLayout panelSalary;

    TextView textSalaryStavka, textSalaryPercent, textSalaryArt, textSalaryMk, textSalaryMk2;
    TextView textTotal, textPercent, textStavka, textMk, textMonth;
    ProgressBar progressBar;

    LinearLayout panelDetails;

    TextView textCard, textExpand;
    LinearLayout panelCopy;

    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    List<Report> reportList = new ArrayList<>();
    private User mUser = new User();
    private String mUId;
    private boolean uploaded = false;
    private boolean mIsAdmin = false;
    private LinearLayout panelExpand;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        setContentView(R.layout.activity_salary);

        initRef();

        Bundle bundle = getIntent().getExtras();

        if (bundle == null) {
            if (mAuth.getCurrentUser() != null)
                mUId = mAuth.getCurrentUser().getUid();

        } else {
            mUId = bundle.getString(Constants.EXTRA_UID);
            mIsAdmin = bundle.getBoolean(Constants.EXTRA_IS_ADMIN, false);
        }

        initCalendar();

        initOnClick();

        loadCurrentUser();

        Calendar month = Calendar.getInstance();
        textMonth.setText(Constants.MONTH_FULL[month.get(Calendar.MONTH)]);

        String yearStr = new SimpleDateFormat("yyyy", Locale.US).format(new Date()).toString();
        String monthStr = new SimpleDateFormat("M", Locale.US).format(new Date()).toString();

        loadReports(yearStr, monthStr);
    }

    private void initOnClick() {

        panelCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(textCard.getText().toString(), textCard.getText().toString());
                clipboard.setPrimaryClip(clip);

                Toast.makeText(SalaryActivity.this, getString(R.string.text_copied), Toast.LENGTH_SHORT).show();
            }
        });

        panelDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsAdmin)
                    editSalaryStavkaDialog();
            }
        });

        panelSalary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (panelExpand.getVisibility() != View.VISIBLE) {
                    panelExpand.setVisibility(View.VISIBLE);

                } else {
                    panelExpand.setVisibility(View.GONE);

                }
            }
        });
    }

    private void editSalaryStavkaDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_salary_title);


        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_salary, null, false);
        builder.setView(viewInflated);
        final EditText inputStavka = (EditText) viewInflated.findViewById(R.id.input_salary_stavka);
        final EditText inputPercent = (EditText) viewInflated.findViewById(R.id.input_salary_percent);
        final EditText inputArt = (EditText) viewInflated.findViewById(R.id.input_salary_art);
        final EditText inputMk = (EditText) viewInflated.findViewById(R.id.input_salary_mk);

        inputStavka.setText(mUser.getUserStavka() + "");
        inputPercent.setText(mUser.getUserPercent() + "");
        inputArt.setText(mUser.getUserArt() + "");
        inputMk.setText(mUser.getUserMk() + "");

        builder.setPositiveButton("Ок", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String stavka = inputStavka.getText().toString();
                String percent = inputPercent.getText().toString();
                String art = inputArt.getText().toString();
                String mk = inputMk.getText().toString();

                try {
                    mUser.userStavka = Integer.parseInt(stavka);
                } catch (Exception e) {
                }
                try {
                    mUser.userPercent = Integer.parseInt(percent);
                } catch (Exception e) {
                }
                try {
                    mUser.userArt = Integer.parseInt(art);
                } catch (Exception e) {
                }
                try {
                    mUser.userMk = Integer.parseInt(mk);
                } catch (Exception e) {
                }

                saveCurrentUser();

            }
        });
        builder.setNegativeButton("Відмінити", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

    }


    private void saveCurrentUser() {
        if (uploaded) {
            mDatabase.getReference(Constants.FIREBASE_REF_USERS).child(mUId).setValue(mUser);
        }

    }

    private void loadCurrentUser() {
        mDatabase.getReference(Constants.FIREBASE_REF_USERS).child(mUId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mUser = dataSnapshot.getValue(User.class);

                        textCard.setText(mUser.userCard.toString());

                        textSalaryStavka.setText(mUser.getUserStavka() + " грн/день");
                        textSalaryPercent.setText(mUser.getUserPercent() + " %");
                        textSalaryArt.setText(mUser.getUserArt() + " грн/дитина");
                        textSalaryMk.setText(mUser.getUserMk() + " грн");
                        textSalaryMk2.setText(mUser.getUserMk() * 2 + " грн");

                        uploaded = true;
                        calcSalary();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
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
        calcSalary();
        mDatabase.getReference(Constants.FIREBASE_REF_USER_REPORTS)
                .child(yearStr)
                .child(monthStr)
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
    }


    private void initRef() {

        textExpand = (TextView) findViewById(R.id.text_salary_expand);
        panelExpand = (LinearLayout) findViewById(R.id.panel_expand);
        panelSalary = (LinearLayout) findViewById(R.id.panel_salary);
        panelDetails = (LinearLayout) findViewById(R.id.panel_details);
        buttonNext = (ImageButton) findViewById(R.id.button_next);
        buttonPrev = (ImageButton) findViewById(R.id.button_prev);
        compactCalendarView = (CompactCalendarView) findViewById(R.id.compactcalendar_view);

        textSalaryStavka = (TextView) findViewById(R.id.text_salary_stavka);
        textSalaryPercent = (TextView) findViewById(R.id.text_salary_percent);
        textSalaryArt = (TextView) findViewById(R.id.text_salary_art);
        textSalaryMk = (TextView) findViewById(R.id.text_salary_mk1);
        textSalaryMk2 = (TextView) findViewById(R.id.text_salary_mk2);

        textMonth = (TextView) findViewById(R.id.text_month);
        textTotal = (TextView) findViewById(R.id.text_total);
        textStavka = (TextView) findViewById(R.id.text_stavka_total);
        textPercent = (TextView) findViewById(R.id.text_percent_total);
        textMk = (TextView) findViewById(R.id.text_mk_total);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        panelCopy = (LinearLayout) findViewById(R.id.panel_copy);
        textCard = (TextView) findViewById(R.id.text_card);
    }

    private void calcSalary() {

        int percent = 0;
        int stavka = 0;
        int mk = 0;
        int total1 = 0;
        int mkCount = 0;
        int birthMkCount = 0;
        int childOnArtMk = 0;

        for (Report rep : reportList) {
            total1 += rep.total;
            stavka += mUser.getUserStavka();
            mk += rep.bMk * mUser.getUserMk();
            mk += (rep.mk1 + rep.mk2) * mUser.getUserArt();
            if (rep.mk1 != 0 || rep.mk2 != 0) {
                mkCount += 1;
            }
            childOnArtMk += rep.mk1;
            childOnArtMk += rep.mk2;

            if (rep.bMk != 0) {
                birthMkCount += 1;
            }
        }

        percent = (total1 * mUser.getUserPercent() / 100);

        int total = stavka + percent + mk;

        textStavka.setText(stavka + " грн");
        textPercent.setText(percent + " грн");
        textMk.setText(mk + " грн");
        textTotal.setText(total + " ГРН");

        String text1 = "В цьоу місяці було " + reportList.size() + " робочих днів \n";
        text1 += "Загальна виручка " + total1 + " грн \n\n";
        text1 += "Проведено " + mkCount + " Творчих та Кулінарних МК \n";
        text1 += " На яких було присутньо " + childOnArtMk + " дітей \n\n";
        text1 += "Проведено " + birthMkCount + " МК на Днях Народженнях \n";

        textExpand.setText(text1);

        progressBar.setProgress(total);
    }


    @Override
    protected void onResume() {
        super.onResume();

        calcSalary();
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
