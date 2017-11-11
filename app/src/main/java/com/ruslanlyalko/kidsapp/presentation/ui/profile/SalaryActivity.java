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

import butterknife.ButterKnife;

public class SalaryActivity extends AppCompatActivity {

    ImageButton mButtonPrev;
    ImageButton mButtonNext;
    CompactCalendarView mCompactCalendarView;
    LinearLayout mSalaryLayout;

    TextView mTextSalaryStavka;
    TextView mTextSalaryPercent;
    TextView mTextSalaryArt;
    TextView mTextSalaryMk;
    TextView mTextSalaryMkChildren;
    TextView mTextTotal;
    TextView mTextPercent;
    TextView mTextStavka;
    TextView mTextMk;
    TextView mTextMonth;
    ProgressBar mProgressBar;

    TextView mTextCard;
    TextView mTextExpand;
    TextView mTextName;
    LinearLayout mPanelCopy;
    LinearLayout mPanelAction;
    ImageView mImageView;

    List<Report> mReports = new ArrayList<>();
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private User mUser = new User();
    private String mUId;
    private Date mCurrentMonth;

    public static Intent getLaunchIntent(final AppCompatActivity launchActivity, String userId, User user) {
        Intent intent = new Intent(launchActivity, SalaryActivity.class);
        intent.putExtra(Keys.Extras.EXTRA_UID, userId);
        intent.putExtra(Keys.Extras.EXTRA_USER, user);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salary);
        ButterKnife.bind(this);
        parseExtras();
        initRef();
        initCalendar();
        initOnClick();
        Calendar month = Calendar.getInstance();
        mTextMonth.setText(Constants.MONTH_FULL[month.get(Calendar.MONTH)]);
        String yearStr = new SimpleDateFormat("yyyy", Locale.US).format(new Date());
        String monthStr = new SimpleDateFormat("M", Locale.US).format(new Date());
        loadReports(yearStr, monthStr);
        mCurrentMonth = new Date();
        mCurrentMonth.setDate(1);
        updateConditionUI(mCurrentMonth);
    }

    private void parseExtras() {
        Bundle extras;
        if ((extras = getIntent().getExtras()) != null) {
            mUser = (User) extras.getSerializable(Keys.Extras.EXTRA_USER);
            mUId = extras.getString(Keys.Extras.EXTRA_UID);
        }
    }

    private void initOnClick() {
        mPanelCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(mTextCard.getText().toString(), mTextCard.getText().toString());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(SalaryActivity.this, getString(R.string.text_copied), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initCalendar() {
        mCompactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
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
                mTextMonth.setText(str);
                String yearStr = new SimpleDateFormat("yyyy", Locale.US).format(firstDayOfNewMonth);
                String monthStr = new SimpleDateFormat("M", Locale.US).format(firstDayOfNewMonth);
                loadReports(yearStr, monthStr);
                mCurrentMonth = firstDayOfNewMonth;
                updateConditionUI(firstDayOfNewMonth);
            }
        });
        mButtonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCompactCalendarView.showNextMonth();
            }
        });
        mButtonPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCompactCalendarView.showPreviousMonth();
            }
        });
    }

    private void loadReports(String yearStr, String monthStr) {
        mReports.clear();
        calcSalary();
        mDatabase.getReference(DefaultConfigurations.DB_REPORTS)
                .child(yearStr)
                .child(monthStr)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Report report = dataSnapshot.child(mUId).getValue(Report.class);
                        if (report != null) {
                            mReports.add(report);
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
        mTextName = findViewById(R.id.text_name);
        mSalaryLayout = findViewById(R.id.panel_salary);
        mButtonNext = findViewById(R.id.button_next);
        mButtonPrev = findViewById(R.id.button_prev);
        mCompactCalendarView = findViewById(R.id.calendar_view);
        mTextExpand = findViewById(R.id.text_salary_expand);
        mPanelAction = findViewById(R.id.panel_action);
        mImageView = findViewById(R.id.image_expand);
        mPanelAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTextExpand.getVisibility() == View.VISIBLE) {
                    mImageView.setImageResource(R.drawable.ic_action_expand_more);
                    ViewUtils.collapse(mTextExpand);
                } else {
                    ViewUtils.expand(mTextExpand);
                    mImageView.setImageResource(R.drawable.ic_action_expand_less);
                }
            }
        });
        mTextSalaryStavka = findViewById(R.id.text_salary_stavka);
        mTextSalaryPercent = findViewById(R.id.text_salary_percent);
        mTextSalaryArt = findViewById(R.id.text_salary_art);
        mTextSalaryMk = findViewById(R.id.text_salary_mk1);
        mTextSalaryMkChildren = findViewById(R.id.text_salary_mk_children);
        mTextMonth = findViewById(R.id.text_month);
        mTextTotal = findViewById(R.id.text_total);
        mTextStavka = findViewById(R.id.text_stavka_total);
        mTextPercent = findViewById(R.id.text_percent_total);
        mTextMk = findViewById(R.id.text_mk_total);
        mProgressBar = findViewById(R.id.progress_bar);
        mPanelCopy = findViewById(R.id.panel_copy);
        mTextCard = findViewById(R.id.text_card);
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
        updateConditionUI(mCurrentMonth);
    }

    private void updateUI() {
        mTextName.setText(mUser.getUserName());
        mTextCard.setText(mUser.getUserCard());
    }

    private void updateConditionUI(Date firstDayOfNewMonth) {
        if (mUser == null) return;
        String date = new SimpleDateFormat("d-M-yyyy", Locale.US).format(firstDayOfNewMonth);
        boolean isSpecCalc = mUser.getMkSpecCalc() && DateUtils.isTodayOrFuture(date, mUser.getMkSpecCalcDate());
        mTextSalaryStavka.setText(String.format(getString(R.string.hrn_day), String.valueOf(
                isSpecCalc ? mUser.getUserStavka() : Constants.SALARY_DEFAULT_STAVKA)));
        mTextSalaryPercent.setText(String.format(getString(R.string.hrn_percent), String.valueOf(
                isSpecCalc ? mUser.getUserPercent() : Constants.SALARY_DEFAULT_PERCENT)));
        mTextSalaryMk.setText(String.format(getString(R.string.hrn), String.valueOf(
                isSpecCalc ? mUser.getMkBd() : Constants.SALARY_DEFAULT_MK)));
        mTextSalaryMkChildren.setText(String.format(getString(R.string.hrn_child), String.valueOf(
                isSpecCalc ? mUser.getMkBdChild() : Constants.SALARY_DEFAULT_MK_CHILD)));
        mTextSalaryArt.setText(String.format(getString(R.string.hrn_child), String.valueOf(
                isSpecCalc ? mUser.getMkArtChild() : Constants.SALARY_DEFAULT_ART_MK_CHILD)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        calcSalary();
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
                && mReports.size() > 0
                && DateUtils.isTodayOrFuture(mReports.get(0).getDate(), mUser.getMkSpecCalcDate())) {
            userStavka = mUser.getUserStavka();
            userPercent = mUser.getUserPercent();
            userMkBirthday = mUser.getMkBd();
            userMkBdChild = mUser.getMkBdChild();
            userMkArtChild = mUser.getMkArtChild();
        }
        for (Report rep : mReports) {
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
        mTextStavka.setText(String.format(getString(R.string.hrn), DateUtils.getIntWithSpace(stavka)));
        mTextPercent.setText(String.format(getString(R.string.hrn), DateUtils.getIntWithSpace(percent)));
        mTextMk.setText(String.format(getString(R.string.hrn), DateUtils.getIntWithSpace(mkBirthday + mkArt)));
        mTextTotal.setText(String.format(getString(R.string.HRN), DateUtils.getIntWithSpace(total)));
        String text1 = "В цьоу місяці було " + mReports.size() + " робочих днів \n";
        text1 += "Загальна виручка " + percentTotal + " грн \n\n";
        text1 += "Проведено " + mkBirthdayCount + " МК на Днях Народженнях \n";
        text1 += "На яких було присутньо " + mkBirthdayChildren + " дітей \n";
        text1 += "Зароблено " + mkBirthday + " грн\n\n";
        text1 += "Проведено " + mkArtCount + " Творчих та Кулінарних МК \n";
        text1 += "На яких було присутньо " + mkArtChildren + " дітей \n";
        text1 += "Зароблено " + mkArt + " грн\n\n";
        text1 += "Аванс: до 20-го чиса;  ЗП: до 5-го числа\n";
        mTextExpand.setText(text1);
        mProgressBar.setMax(total);
        mProgressBar.setProgress(stavka);
        mProgressBar.setSecondaryProgress(stavka + percent);
    }
}
