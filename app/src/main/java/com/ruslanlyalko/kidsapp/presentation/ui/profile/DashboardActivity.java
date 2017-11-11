package com.ruslanlyalko.kidsapp.presentation.ui.profile;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ruslanlyalko.kidsapp.R;
import com.ruslanlyalko.kidsapp.common.Constants;
import com.ruslanlyalko.kidsapp.common.DateUtils;
import com.ruslanlyalko.kidsapp.data.configuration.DefaultConfigurations;
import com.ruslanlyalko.kidsapp.data.models.Expense;
import com.ruslanlyalko.kidsapp.data.models.Report;
import com.ruslanlyalko.kidsapp.data.models.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    TextView textTotal, textRoom, textBday, textMk, textMonth;
    ImageButton buttonPrev, buttonNext;
    CompactCalendarView compactCalendarView;

    TextView textCostTotal, textCostCommon, textCostMk;
    TextView textSalaryTotal, textSalaryStavka, textSalaryPercent, textSalaryMk;

    TextView textBirthdays;
    EditText editComment;

    private List<Expense> mExpenseList = new ArrayList<>();
    ProgressBar progressBar, progressBarCost, progressBarSalary;
    List<Report> reportList = new ArrayList<>();

    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private List<User> userList = new ArrayList<>();
    private String yearStr;
    private String monthStr;

    private int netIncome;
    private int incomeTotal;
    private int costTotal;
    private int salaryTotal;
    private String mComment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        initRef();
        initCalendar();
        Calendar month = Calendar.getInstance();
        textMonth.setText(Constants.MONTH_FULL[month.get(Calendar.MONTH)]);
        yearStr = new SimpleDateFormat("yyyy", Locale.US).format(new Date()).toString();
        monthStr = new SimpleDateFormat("M", Locale.US).format(new Date()).toString();
        loadReports(yearStr, monthStr);
        loadCosts(yearStr, monthStr);
        loadUsers();
        loadComment(yearStr, monthStr);
    }

    private void initRef() {
        buttonNext = findViewById(R.id.button_next);
        buttonPrev = findViewById(R.id.button_prev);
        compactCalendarView = findViewById(R.id.calendar_view);
        textMonth = findViewById(R.id.text_month);
        textTotal = findViewById(R.id.text_total);
        textRoom = findViewById(R.id.text_room_total);
        textBday = findViewById(R.id.text_bday_total);
        textMk = findViewById(R.id.text_mk_total);
        progressBar = findViewById(R.id.progress_bar);
        progressBarSalary = findViewById(R.id.progress_bar_salary);
        progressBarCost = findViewById(R.id.progress_bar_cost);
        textCostTotal = findViewById(R.id.text_cost_total);
        textCostCommon = findViewById(R.id.text_cost_common);
        textCostMk = findViewById(R.id.text_cost_mk);
        textSalaryTotal = findViewById(R.id.text_salary_total);
        textSalaryStavka = findViewById(R.id.text_stavka_total);
        textSalaryPercent = findViewById(R.id.text_percent_total);
        textSalaryMk = findViewById(R.id.text_salary_mk_total);
        textBirthdays = findViewById(R.id.text_birthdays);
        editComment = findViewById(R.id.edit_comment);
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
                yearStr = new SimpleDateFormat("yyyy", Locale.US).format(firstDayOfNewMonth).toString();
                monthStr = new SimpleDateFormat("M", Locale.US).format(firstDayOfNewMonth).toString();
                loadReports(yearStr, monthStr);
                loadCosts(yearStr, monthStr);
                loadUsers();
                loadComment(yearStr, monthStr);
            }
        });
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // save comment
                saveCommentToDB(editComment.getText().toString());
                compactCalendarView.showNextMonth();
            }
        });
        buttonPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // save comment
                saveCommentToDB(editComment.getText().toString());
                compactCalendarView.showPreviousMonth();
            }
        });
    }

    private void loadReports(String yearStr, String monthStr) {
        reportList.clear();
        calcIncome();
        mDatabase.getReference(DefaultConfigurations.DB_REPORTS)
                .child(yearStr)
                .child(monthStr)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            Report report = ds.getValue(Report.class);
                            if (report != null) {
                                reportList.add(report);
                                calcIncome();
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
        progressBar.setProgress(room);
        progressBar.setSecondaryProgress(room + bday);
        progressBar.setMax(incomeTotal);
        updateNetIncome();
    }

    private void loadCosts(String yearStr, String monthStr) {
        mExpenseList.clear();
        calcCostTotal();
        mDatabase.getReference(DefaultConfigurations.DB_COSTS).child(yearStr).child(monthStr)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Expense expense = dataSnapshot.getValue(Expense.class);
                        if (expense != null) {
                            mExpenseList.add(0, expense);
                            calcCostTotal();
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

    private void calcCostTotal() {
        int common = 0;
        int mk = 0;
        for (Expense expense : mExpenseList) {
            if (expense.getTitle2().equals(getString(R.string.text_cost_common)))
                common += expense.getPrice();
            if (expense.getTitle2().equals(getString(R.string.text_cost_mk)))
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

    private void loadUsers() {
        userList.clear();
        calcSalaryForUsers();
        mDatabase.getReference(DefaultConfigurations.DB_USERS)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        User user = dataSnapshot.getValue(User.class);
                        if (user != null) {
                            userList.add(0, user);
                            calcSalaryForUsers();
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

    private void calcSalaryForUsers() {
        String birthdays = "";
        salaryTotal = 0;
        int stavka = 0;
        int percent = 0;
        int mkBirthday = 0;
        int mkBirthdayCount = 0;
        int mkBirthdayChildren = 0;
        int mkArt = 0;
        int mkArtCount = 0;
        int mkArtChildren = 0;
        for (User user : userList) {
            int percentTotal = 0;
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
            percent += (percentTotal * userPercent / 100);
            // birthdays list
            if (!user.getUserIsAdmin())
                birthdays += user.getUserBDay() + " - " + user.getUserName() + "\n";
        }
        textBirthdays.setText(birthdays);
        salaryTotal += stavka + percent + mkBirthday + mkArt;
        textSalaryStavka.setText(String.format(getString(R.string.hrn), DateUtils.getIntWithSpace(stavka)));
        textSalaryPercent.setText(String.format(getString(R.string.hrn), DateUtils.getIntWithSpace(percent)));
        textSalaryMk.setText(String.format(getString(R.string.hrn), DateUtils.getIntWithSpace(mkBirthday + mkArt)));
        textSalaryTotal.setText(String.format(getString(R.string.HRN), DateUtils.getIntWithSpace(salaryTotal)));
        progressBarSalary.setProgress(stavka);
        progressBarSalary.setSecondaryProgress(stavka + percent);
        progressBarSalary.setMax(salaryTotal);
        updateNetIncome();
    }

    private void updateNetIncome() {
        netIncome = (int) (incomeTotal * 0.8) - costTotal - salaryTotal;
        setTitle(String.format(getString(R.string.title_activity_dashboard), DateUtils.getIntWithSpace(netIncome)));
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

    private void saveCommentToDB(String s) {
        if (!s.equals(mComment))
            mDatabase.getReference(DefaultConfigurations.DB_COMMENTS)
                    .child(yearStr)
                    .child(monthStr).setValue(s);
    }

    @Override
    protected void onResume() {
        super.onResume();
        calcIncome();
        calcCostTotal();
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
}