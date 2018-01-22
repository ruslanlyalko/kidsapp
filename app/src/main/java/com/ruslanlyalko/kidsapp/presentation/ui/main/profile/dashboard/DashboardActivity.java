package com.ruslanlyalko.kidsapp.presentation.ui.main.profile.dashboard;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ruslanlyalko.kidsapp.R;
import com.ruslanlyalko.kidsapp.common.Constants;
import com.ruslanlyalko.kidsapp.common.DateUtils;
import com.ruslanlyalko.kidsapp.common.ViewUtils;
import com.ruslanlyalko.kidsapp.data.configuration.DefaultConfigurations;
import com.ruslanlyalko.kidsapp.data.models.Expense;
import com.ruslanlyalko.kidsapp.data.models.Report;
import com.ruslanlyalko.kidsapp.data.models.Result;
import com.ruslanlyalko.kidsapp.data.models.User;
import com.ruslanlyalko.kidsapp.presentation.ui.main.profile.dashboard.adapter.UsersSalaryAdapter;
import com.ruslanlyalko.kidsapp.presentation.ui.main.profile.salary.SalaryActivity;
import com.ruslanlyalko.kidsapp.presentation.widget.OnItemClickListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DashboardActivity extends AppCompatActivity implements OnItemClickListener {

    @BindView(R.id.calendar_view) CompactCalendarView mCompactCalendarView;
    @BindView(R.id.text_total) TextView textTotal;
    @BindView(R.id.text_room_total) TextView textRoom;
    @BindView(R.id.text_bday_total) TextView textBday;
    @BindView(R.id.text_mk_total) TextView textMk;
    @BindView(R.id.text_month) TextView textMonth;
    @BindView(R.id.text_cost_total) TextView textCostTotal;
    @BindView(R.id.text_cost_common) TextView textCostCommon;
    @BindView(R.id.text_cost_mk) TextView textCostMk;
    @BindView(R.id.text_salary_total) TextView textSalaryTotal;
    @BindView(R.id.text_stavka_total) TextView textSalaryStavka;
    @BindView(R.id.text_percent_total) TextView textSalaryPercent;
    @BindView(R.id.text_salary_mk_total) TextView textSalaryMk;
    @BindView(R.id.text_birthdays) TextView textBirthdays;
    @BindView(R.id.edit_comment) EditText editComment;
    @BindView(R.id.progress_bar) ProgressBar progressBar;
    @BindView(R.id.progress_bar_cost) ProgressBar progressBarCost;
    @BindView(R.id.progress_bar_salary) ProgressBar progressBarSalary;
    @BindView(R.id.list_users_salary) RecyclerView mListUsersSalary;
    @BindView(R.id.image_expand) ImageView mImageView;
    @BindView(R.id.bar_chart) BarChart mBarChart;
    private UsersSalaryAdapter mUsersSalaryAdapter = new UsersSalaryAdapter(this);
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private List<Report> reportList = new ArrayList<>();
    private List<Expense> mExpenseList = new ArrayList<>();
    private List<User> userList = new ArrayList<>();
    private String yearStr;
    private String monthStr;
    private int incomeTotal;
    private int costTotal;
    private int salaryTotal;
    private String mComment;
    private List<Result> mResults;

    public static Intent getLaunchIntent(final AppCompatActivity launchActivity) {
        return new Intent(launchActivity, DashboardActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        ButterKnife.bind(this);
        initCalendar();
        initBarChart();
        initRecycler();
        textMonth.setText(Constants.MONTH_FULL[Calendar.getInstance().get(Calendar.MONTH)]);
        yearStr = new SimpleDateFormat("yyyy", Locale.US).format(new Date());
        monthStr = new SimpleDateFormat("M", Locale.US).format(new Date());
        loadReports(yearStr, monthStr);
        loadCosts(yearStr, monthStr);
        loadUsers();
        loadComment(yearStr, monthStr);
        loadResults();
    }

    private void initRecycler() {
        mListUsersSalary.setLayoutManager(new LinearLayoutManager(this));
        mListUsersSalary.setAdapter(mUsersSalaryAdapter);
    }

    private void loadResults() {
        mDatabase.getReference(DefaultConfigurations.DB_RESULTS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        List<Result> results = new ArrayList<>();
                        for (DataSnapshot year : dataSnapshot.getChildren()) {
                            for (DataSnapshot month : year.getChildren()) {
                                Result result = month.getValue(Result.class);
                                if (result != null) {
                                    results.add(result);
                                }
                            }
                            updateBarChart(results);
                        }
                    }

                    @Override
                    public void onCancelled(final DatabaseError databaseError) {
                    }
                });
    }

    private void initBarChart() {
        mBarChart.setDrawGridBackground(false);
        mBarChart.getLegend().setEnabled(false);
        mBarChart.getDescription().setEnabled(false);
        mBarChart.getXAxis().setEnabled(true);
        mBarChart.getAxisRight().setEnabled(false);
        mBarChart.getAxisLeft().setAxisLineColor(Color.TRANSPARENT);
        mBarChart.getAxisLeft().enableGridDashedLine(1, 1, 10);
        mBarChart.setMaxVisibleValueCount(150);
        mBarChart.setDrawValueAboveBar(true);
        mBarChart.setDoubleTapToZoomEnabled(false);
        mBarChart.setPinchZoom(false);
        mBarChart.setScaleEnabled(false);
        mBarChart.setTouchEnabled(false);
        mBarChart.setDrawBarShadow(false);
        mBarChart.getAxisLeft().setAxisMinimum(0);
        XAxis xAxis = mBarChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return mResults.get((int) value).getMonth();
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
                if (!DateUtils.isCurrentYear(firstDayOfNewMonth))
                    str = str + "'" + yearSimple;
                textMonth.setText(str);
                yearStr = new SimpleDateFormat("yyyy", Locale.US).format(firstDayOfNewMonth);
                monthStr = new SimpleDateFormat("M", Locale.US).format(firstDayOfNewMonth);
                loadReports(yearStr, monthStr);
                loadCosts(yearStr, monthStr);
                loadUsers();
                loadComment(yearStr, monthStr);
            }
        });
    }

    @OnClick(R.id.panel_action)
    void onExpandClicked() {
        if (mListUsersSalary.getVisibility() == View.VISIBLE) {
            mImageView.setImageResource(R.drawable.ic_action_expand_more);
            ViewUtils.collapse(mListUsersSalary);
        } else {
            ViewUtils.expand(mListUsersSalary);
            mImageView.setImageResource(R.drawable.ic_action_expand_less);
        }
    }

    @OnClick(R.id.button_prev)
    void onPrevClicked() {
        saveCommentToDB(editComment.getText().toString());
        mCompactCalendarView.showPreviousMonth();
    }

    private void saveCommentToDB(String s) {
        if (!s.equals(mComment))
            mDatabase.getReference(DefaultConfigurations.DB_COMMENTS)
                    .child(yearStr)
                    .child(monthStr).setValue(s);
    }

    @OnClick(R.id.button_next)
    void onNextClicked() {
        saveCommentToDB(editComment.getText().toString());
        mCompactCalendarView.showNextMonth();
    }

    private void loadReports(String yearStr, String monthStr) {
        mDatabase.getReference(DefaultConfigurations.DB_REPORTS)
                .child(yearStr)
                .child(monthStr)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        reportList.clear();
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            for (DataSnapshot ds : data.getChildren()) {
                                Report report = ds.getValue(Report.class);
                                if (report != null) {
                                    reportList.add(report);
                                }
                            }
                        }
                        calcIncome();
                    }

                    @Override
                    public void onCancelled(final DatabaseError databaseError) {
                    }
                });
    }

    private void loadCosts(String yearStr, String monthStr) {
        mDatabase.getReference(DefaultConfigurations.DB_EXPENSES)
                .child(yearStr)
                .child(monthStr)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        mExpenseList.clear();
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            Expense expense = data.getValue(Expense.class);
                            if (expense != null) {
                                mExpenseList.add(0, expense);
                            }
                        }
                        calcCostTotal();
                    }

                    @Override
                    public void onCancelled(final DatabaseError databaseError) {
                    }
                });
    }

    private void loadUsers() {
        mDatabase.getReference(DefaultConfigurations.DB_USERS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        userList.clear();
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            User user = data.getValue(User.class);
                            if (user != null) {
                                userList.add(0, user);
                            }
                        }
                        calcSalaryForUsers();
                    }

                    @Override
                    public void onCancelled(final DatabaseError databaseError) {
                    }
                });
    }

    private void loadComment(String yearStr, String monthStr) {
        //editComment.setText("");
        mDatabase.getReference(DefaultConfigurations.DB_COMMENTS)
                .child(yearStr)
                .child(monthStr).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    String s = dataSnapshot.getValue().toString();
                    mComment = s;
                    editComment.setText(s);
                } else {
                    mComment = "";
                    editComment.setText("");
                }
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

    @Override
    public void onBackPressed() {
        // save comments before exit
        saveCommentToDB(editComment.getText().toString());
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        calcIncome();
        calcCostTotal();
    }

    private void calcIncome() {
        int room = 0;
        int bday = 0;
        int mk = 0;
        for (Report rep : reportList) {
            room += rep.totalRoom;
            bday += rep.totalBday;
            mk += rep.totalMk;
        }
        incomeTotal = room + bday + mk;
        textRoom.setText(String.format(getString(R.string.hrn), DateUtils.getIntWithSpace(room)));
        textBday.setText(String.format(getString(R.string.hrn), DateUtils.getIntWithSpace(bday)));
        textMk.setText(String.format(getString(R.string.hrn), DateUtils.getIntWithSpace(mk)));
        String income100Str = DateUtils.getIntWithSpace(incomeTotal);
        String income80Str = DateUtils.getIntWithSpace(incomeTotal * 80 / 100);
        textTotal.setText(String.format(getString(R.string.income), income100Str, income80Str));
        progressBar.setMax(incomeTotal);
        progressBar.setProgress(room);
        progressBar.setSecondaryProgress(room + bday);
        updateNetIncome();
    }

    private void calcCostTotal() {
        int common = 0;
        int mk = 0;
        for (Expense expense : mExpenseList) {
            if (expense.getTitle2().equalsIgnoreCase(getString(R.string.text_cost_common)))
                common += expense.getPrice();
            if (expense.getTitle2().equalsIgnoreCase(getString(R.string.text_cost_mk)))
                mk += expense.getPrice();
        }
        costTotal = common + mk;
        progressBarCost.setMax(costTotal);
        progressBarCost.setProgress(common);
        progressBarCost.setSecondaryProgress(common + mk);
        textCostCommon.setText(String.format(getString(R.string.hrn), DateUtils.getIntWithSpace(common)));
        textCostMk.setText(String.format(getString(R.string.hrn), DateUtils.getIntWithSpace(mk)));
        textCostTotal.setText(String.format(getString(R.string.HRN), DateUtils.getIntWithSpace(costTotal)));
        updateNetIncome();
    }

    private void updateNetIncome() {
        int income80 = (int) (incomeTotal * 0.8);
        int netIncome = income80 - costTotal - salaryTotal;
        setTitle(String.format(getString(R.string.title_activity_dashboard), DateUtils.getIntWithSpace(netIncome)));
        Result result = new Result(incomeTotal, income80, salaryTotal, costTotal, netIncome, yearStr, monthStr);
        if (incomeTotal != 0 && salaryTotal != 0)
            FirebaseDatabase.getInstance()
                    .getReference(DefaultConfigurations.DB_RESULTS)
                    .child(yearStr)
                    .child(monthStr)
                    .setValue(result);
    }

    private void calcSalaryForUsers() {
        String birthdays = "";
        salaryTotal = 0;
        int stavka = 0;
        int percent = 0;
        int mkBirthday = 0;
        int mkArt = 0;
        int mkBirthdayCount = 0;
        int mkBirthdayChildren = 0;
        int mkArtCount = 0;
        int mkArtChildren = 0;
        String usersSalary = "";
        mUsersSalaryAdapter.clearAll();

        for (User user : userList) {
            int uPercentTotal = 0;
            int uStavka = 0;
            int uMkArt = 0;
            int uMkBirth = 0;
            //init hrn
            int userStavka = Constants.SALARY_DEFAULT_STAVKA;
            int userPercent = Constants.SALARY_DEFAULT_PERCENT;
            int userMkBirthday = Constants.SALARY_DEFAULT_MK;
            int userMkBdChild = Constants.SALARY_DEFAULT_MK_CHILD;
            int userMkArtChild = Constants.SALARY_DEFAULT_ART_MK_CHILD;
            boolean isDefault = true;
            for (Report rep : reportList) {
                //required only for Dashboard calc salary
                if (!rep.getUserId().equals(user.getUserId())) continue;
                if (isDefault && user.getMkSpecCalc() && DateUtils.isTodayOrFuture(rep.getDate(), user.getMkSpecCalcDate())) {
                    userStavka = user.getUserStavka();
                    userPercent = user.getUserPercent();
                    userMkBirthday = user.getMkBd();
                    userMkBdChild = user.getMkBdChild();
                    userMkArtChild = user.getMkArtChild();
                    isDefault = false;
                }
                if (DateUtils.future(rep.getDate())) continue;
                // stavka
                uStavka += userStavka;
                // percent
                uPercentTotal += rep.total;
                //Birthdays Mk
                uMkBirth += rep.bMk * userMkBirthday;
                uMkBirth += rep.b30 * userMkBdChild;
                mkBirthdayCount += rep.bMk;
                mkBirthdayChildren += rep.b30;
                // Art MK
                if (rep.mkMy) {
                    uMkArt += (rep.mk1 + rep.mk2) * userMkArtChild;
                    if (rep.mk1 != 0 || rep.mk2 != 0)
                        mkArtCount += 1;
                    mkArtChildren += rep.mk1;
                    mkArtChildren += rep.mk2;
                }
            }
            int uPercent = (uPercentTotal * userPercent / 100);
            stavka += uStavka;
            mkArt += uMkArt;
            mkBirthday += uMkBirth;
            percent += uPercent;
            int uTotal = (uMkArt + uMkBirth + uStavka + uPercent);
            if (uTotal > 0) {
                usersSalary += uTotal + " - " + user.getUserName() + "\n";
                mUsersSalaryAdapter.add(user, uTotal);
            }
            // birthdays list
            if (!user.getUserIsAdmin())
                birthdays += user.getUserBDay() + " - " + user.getUserName() + "\n";
        }
        //mTextExpand.setText(usersSalary);
        textBirthdays.setText(birthdays);
        salaryTotal = stavka + percent + mkBirthday + mkArt;
        textSalaryStavka.setText(String.format(getString(R.string.hrn), DateUtils.getIntWithSpace(stavka)));
        textSalaryPercent.setText(String.format(getString(R.string.hrn), DateUtils.getIntWithSpace(percent)));
        textSalaryMk.setText(String.format(getString(R.string.hrn), DateUtils.getIntWithSpace(mkBirthday + mkArt)));
        textSalaryTotal.setText(String.format(getString(R.string.HRN), DateUtils.getIntWithSpace(salaryTotal)));
        progressBarSalary.setMax(salaryTotal);
        progressBarSalary.setProgress(stavka);
        progressBarSalary.setSecondaryProgress(stavka + percent);
        updateNetIncome();
    }

    private void updateBarChart(List<Result> resultList) {
        mResults = resultList;
        ArrayList<BarEntry> values = new ArrayList<>();
        int[] colors = new int[resultList.size()];
        String yearStr = DateFormat.format("yyyy", Calendar.getInstance()).toString();
        for (int i = 0; i < resultList.size(); i++) {
            Result current = resultList.get(i);
            float val = current.getProfit();
            colors[i] = current.getYear().equals(yearStr)
                    ? ContextCompat.getColor(this, R.color.colorAccent)
                    : ContextCompat.getColor(this, R.color.colorPrimary);
            values.add(new BarEntry(i, val, val));
        }
        BarDataSet set1;
        if (mBarChart.getData() != null &&
                mBarChart.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet) mBarChart.getData().getDataSetByIndex(0);
            set1.setValues(values);
        } else {
            set1 = new BarDataSet(values, "");
            set1.setColors(colors);
            set1.setDrawIcons(true);
            set1.setDrawValues(true);
            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);
            BarData data = new BarData(dataSets);
            data.setBarWidth(0.9f);
            data.setDrawValues(true);
            // data.setValueFormatter(new LabelValueFormatter());
            data.setValueTextColor(Color.BLACK);
            set1.setValueTextSize(10f);
            mBarChart.setData(data);
        }
        mBarChart.getData().notifyDataChanged();
        mBarChart.notifyDataSetChanged();
        mBarChart.invalidate();
    }

    @Override
    public void onItemClicked(final int position) {
        User user = mUsersSalaryAdapter.getItemAtPosition(position);
        startActivity(SalaryActivity.getLaunchIntent(this,
                user.getUserId(),
                user));
    }
}
