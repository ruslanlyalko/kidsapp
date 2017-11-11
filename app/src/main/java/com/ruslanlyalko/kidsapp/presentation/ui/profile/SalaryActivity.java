package com.ruslanlyalko.kidsapp.presentation.ui.profile;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ruslanlyalko.kidsapp.R;
import com.ruslanlyalko.kidsapp.common.Constants;
import com.ruslanlyalko.kidsapp.common.DateUtils;
import com.ruslanlyalko.kidsapp.common.Keys;
import com.ruslanlyalko.kidsapp.common.ViewUtils;
import com.ruslanlyalko.kidsapp.data.Utils;
import com.ruslanlyalko.kidsapp.data.configuration.DefaultConfigurations;
import com.ruslanlyalko.kidsapp.data.models.Report;
import com.ruslanlyalko.kidsapp.data.models.User;

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

    TextView textSalaryStavka, textSalaryPercent, textSalaryArt, textSalaryMk, textSalaryMkChildren;
    TextView textTotal, textPercent, textStavka, textMk, textMonth;
    ProgressBar progressBar;

    TextView textCard, textExpand, textName;
    LinearLayout panelCopy, panelAction;

    ImageView imageView;
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    List<Report> reportList = new ArrayList<>();
    private User mUser = new User();
    private String mUId;
    private Date currentMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salary);
        initRef();
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            if (mAuth.getCurrentUser() != null)
                mUId = mAuth.getCurrentUser().getUid();
        } else {
            mUId = bundle.getString(Keys.Extras.EXTRA_UID);
        }
        initCalendar();
        initOnClick();
        loadCurrentUser();
        Calendar month = Calendar.getInstance();
        textMonth.setText(Constants.MONTH_FULL[month.get(Calendar.MONTH)]);
        String yearStr = new SimpleDateFormat("yyyy", Locale.US).format(new Date());
        String monthStr = new SimpleDateFormat("M", Locale.US).format(new Date());
        loadReports(yearStr, monthStr);
        currentMonth = new Date();
        currentMonth.setDate(1);
        updateConditionUI(currentMonth);
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
    }

    private void loadCurrentUser() {
        mDatabase.getReference(DefaultConfigurations.DB_USERS).child(mUId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mUser = dataSnapshot.getValue(User.class);
                        updateUI();
                        calcSalary();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    private void updateUI() {
        textName.setText(mUser.getUserName());
        textCard.setText(mUser.getUserCard());
    }

    private void updateConditionUI(Date firstDayOfNewMonth) {
        if (mUser == null) return;
        String date = new SimpleDateFormat("d-M-yyyy", Locale.US).format(firstDayOfNewMonth);
        boolean isSpecCalc = mUser.getMkSpecCalc() && DateUtils.isTodayOrFuture(date, mUser.getMkSpecCalcDate());
        textSalaryStavka.setText(String.format(getString(R.string.hrn_day), String.valueOf(
                isSpecCalc ? mUser.getUserStavka() : Constants.SALARY_DEFAULT_STAVKA)));
        textSalaryPercent.setText(String.format(getString(R.string.hrn_percent), String.valueOf(
                isSpecCalc ? mUser.getUserPercent() : Constants.SALARY_DEFAULT_PERCENT)));
        textSalaryMk.setText(String.format(getString(R.string.hrn), String.valueOf(
                isSpecCalc ? mUser.getMkBd() : Constants.SALARY_DEFAULT_MK)));
        textSalaryMkChildren.setText(String.format(getString(R.string.hrn_child), String.valueOf(
                isSpecCalc ? mUser.getMkBdChild() : Constants.SALARY_DEFAULT_MK_CHILD)));
        textSalaryArt.setText(String.format(getString(R.string.hrn_child), String.valueOf(
                isSpecCalc ? mUser.getMkArtChild() : Constants.SALARY_DEFAULT_ART_MK_CHILD)));
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
                String yearSimple = new SimpleDateFormat("yy", Locale.US).format(firstDayOfNewMonth);
                String str = Constants.MONTH_FULL[month.get(Calendar.MONTH)];
                if (firstDayOfNewMonth.getYear() != new Date().getYear())
                    str = str + "'" + yearSimple;
                textMonth.setText(str);
                String yearStr = new SimpleDateFormat("yyyy", Locale.US).format(firstDayOfNewMonth);
                String monthStr = new SimpleDateFormat("M", Locale.US).format(firstDayOfNewMonth);
                loadReports(yearStr, monthStr);
                currentMonth = firstDayOfNewMonth;
                updateConditionUI(firstDayOfNewMonth);
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
        mDatabase.getReference(DefaultConfigurations.DB_REPORTS)
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
        textName = findViewById(R.id.text_name);
        panelSalary = findViewById(R.id.panel_salary);
        buttonNext = findViewById(R.id.button_next);
        buttonPrev = findViewById(R.id.button_prev);
        compactCalendarView = findViewById(R.id.calendar_view);
        textExpand = findViewById(R.id.text_salary_expand);
        panelAction = findViewById(R.id.panel_action);
        imageView = findViewById(R.id.image_expand);
        panelAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (textExpand.getVisibility() == View.VISIBLE) {
                    //textExpand.setVisibility(View.GONE);
                    imageView.setImageResource(R.drawable.ic_action_expand_more);
                    ViewUtils.collapse(textExpand);
                } else {
                    ViewUtils.expand(textExpand);
                    //textExpand.setVisibility(View.VISIBLE);
                    imageView.setImageResource(R.drawable.ic_action_expand_less);
                }
            }
        });
        textSalaryStavka = findViewById(R.id.text_salary_stavka);
        textSalaryPercent = findViewById(R.id.text_salary_percent);
        textSalaryArt = findViewById(R.id.text_salary_art);
        textSalaryMk = findViewById(R.id.text_salary_mk1);
        textSalaryMkChildren = findViewById(R.id.text_salary_mk_children);
        textMonth = findViewById(R.id.text_month);
        textTotal = findViewById(R.id.text_total);
        textStavka = findViewById(R.id.text_stavka_total);
        textPercent = findViewById(R.id.text_percent_total);
        textMk = findViewById(R.id.text_mk_total);
        progressBar = findViewById(R.id.progress_bar);
        panelCopy = findViewById(R.id.panel_copy);
        textCard = findViewById(R.id.text_card);
    }

    private void calcSalary() {
        //init hrn
        int userStavka = Constants.SALARY_DEFAULT_STAVKA;
        int userPercent = Constants.SALARY_DEFAULT_PERCENT;
        int userMkBirthday = Constants.SALARY_DEFAULT_MK;
        int userMkBdChild = Constants.SALARY_DEFAULT_MK_CHILD;
        int userMkArtChild = Constants.SALARY_DEFAULT_ART_MK_CHILD;
        //check what exactly we have
        int stavka = 0;
        int percentTotal = 0;
        int percent;
        int mkBirthday = 0;
        int mkBirthdayCount = 0;
        int mkBirthdayChildren = 0;
        int mkArt = 0;
        int mkArtCount = 0;
        int mkArtChildren = 0;
        if (mUser.getMkSpecCalc()
                && reportList.size() > 0
                && DateUtils.isTodayOrFuture(reportList.get(0).getDate(), mUser.getMkSpecCalcDate())) {
            userStavka = mUser.getUserStavka();
            userPercent = mUser.getUserPercent();
            userMkBirthday = mUser.getMkBd();
            userMkBdChild = mUser.getMkBdChild();
            userMkArtChild = mUser.getMkArtChild();
        }
        for (Report rep : reportList) {
            if (DateUtils.future(rep.getDate())) continue;
            // stavka
            stavka += userStavka;
            // percent
            percentTotal += rep.total;
            //Birthdays Mk
            mkBirthday += rep.bMk * userMkBirthday;
            mkBirthday += rep.b30 * userMkBdChild;
            mkBirthdayCount += rep.bMk;
            mkBirthdayChildren += rep.b30;
            // Art MK
            if (rep.mkMy) {
                mkArt += (rep.mk1 + rep.mk2) * userMkArtChild;
                if (rep.mk1 != 0 || rep.mk2 != 0)
                    mkArtCount += 1;
                mkArtChildren += rep.mk1;
                mkArtChildren += rep.mk2;
            }
        }
        percent = (percentTotal * userPercent / 100);
        int total = stavka + percent + mkBirthday + mkArt;
        textStavka.setText(String.format(getString(R.string.hrn), DateUtils.getIntWithSpace(stavka)));
        textPercent.setText(String.format(getString(R.string.hrn), DateUtils.getIntWithSpace(percent)));
        textMk.setText(String.format(getString(R.string.hrn), DateUtils.getIntWithSpace(mkBirthday + mkArt)));
        textTotal.setText(String.format(getString(R.string.HRN), DateUtils.getIntWithSpace(total)));
        String text1 = "В цьоу місяці було " + reportList.size() + " робочих днів \n";
        text1 += "Загальна виручка " + percentTotal + " грн \n\n";
        text1 += "Проведено " + mkBirthdayCount + " МК на Днях Народженнях \n";
        text1 += "На яких було присутньо " + mkBirthdayChildren + " дітей \n";
        text1 += "Зароблено " + mkBirthday + " грн\n\n";
        text1 += "Проведено " + mkArtCount + " Творчих та Кулінарних МК \n";
        text1 += "На яких було присутньо " + mkArtChildren + " дітей \n";
        text1 += "Зароблено " + mkArt + " грн\n\n";
        text1 += "Аванс: до 20-го чиса;  ЗП: до 5-го числа\n";
        textExpand.setText(text1);
        progressBar.setMax(total);
        progressBar.setProgress(stavka);
        progressBar.setSecondaryProgress(stavka + percent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        calcSalary();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        if (Utils.isAdmin())
            getMenuInflater().inflate(R.menu.menu_salary, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        if (id == R.id.action_edit) {
            Intent intent = new Intent(this, SalaryEditActivity.class);
            intent.putExtra(Keys.Extras.EXTRA_UID, mUId);
            startActivityForResult(intent, 0);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        updateUI();
        updateConditionUI(currentMonth);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
