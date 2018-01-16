package com.ruslanlyalko.kidsapp.presentation.ui.main.expenses;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.UploadTask;
import com.ruslanlyalko.kidsapp.R;
import com.ruslanlyalko.kidsapp.common.Constants;
import com.ruslanlyalko.kidsapp.common.DateUtils;
import com.ruslanlyalko.kidsapp.data.FirebaseUtils;
import com.ruslanlyalko.kidsapp.data.configuration.DefaultConfigurations;
import com.ruslanlyalko.kidsapp.data.models.Expense;
import com.ruslanlyalko.kidsapp.presentation.ui.main.expenses.adapter.ExpensesAdapter;
import com.ruslanlyalko.kidsapp.presentation.ui.main.expenses.adapter.OnExpenseClickListener;
import com.ruslanlyalko.kidsapp.presentation.widget.PhotoPreviewActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ExpensesActivity extends AppCompatActivity implements OnExpenseClickListener {

    @BindView(R.id.text_cost_total) TextSwitcher mTotalSwitcher;
    CompactCalendarView mCompactCalendarView;

    Button buttonDeleteAll;
    RecyclerView mExpensesList;
    TextView textMonth;
    TextView textUserName;
    TextView textCommon;
    TextView textMk;
    ProgressBar progressBar;
    ProgressBar progressBarUpload;
    FloatingActionButton fab;
    FloatingActionButton fab1;
    FloatingActionButton fab2;
    TextView textFab1;
    TextView textFab2;
    View mFadedBackground;

    private Animation fab_open, fab_close, rotate_forward, rotate_backward, fade, fade_back_quick;
    private ExpensesAdapter mExpensesAdapter;
    private List<Expense> mExpenseList = new ArrayList<>();
    private Boolean mIsFabOpen = false;
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private String mTitle1, mTitle2, mPrice;
    private String mPictureImagePath = "";
    private FirebaseUser mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

    public static Intent getLaunchIntent(final Activity launchIntent) {
        return new Intent(launchIntent, ExpensesActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expenses);
        ButterKnife.bind(this);
        initRef();
        initSwitcher();
        initRecycle();
        initFAB();
        buttonDeleteAll.setVisibility(FirebaseUtils.isAdmin() ? View.VISIBLE : View.GONE);
        textUserName.setText(mCurrentUser.getDisplayName());
        Calendar month = Calendar.getInstance();
        textMonth.setText(Constants.MONTH_FULL[month.get(Calendar.MONTH)]);
        // define a listener to receive callbacks when certain events happen.
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
                String yearStr = new SimpleDateFormat("yyyy", Locale.US).format(firstDayOfNewMonth);
                String monthStr = new SimpleDateFormat("M", Locale.US).format(firstDayOfNewMonth);
                loadExpenses(yearStr, monthStr);
            }
        });
        buttonDeleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FirebaseUtils.isAdmin()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ExpensesActivity.this);
                    builder.setTitle(R.string.dialog_cost_delete_all_title)
                            .setMessage(R.string.dialog_cost_delete_all_message)
                            .setPositiveButton("Видалити", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    removeAllExpenses();
                                    mExpenseList.clear();
                                    mExpensesAdapter.notifyDataSetChanged();
                                }
                            })
                            .setNegativeButton("Повернутись", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .show();
                }
            }
        });
        String yearStr = new SimpleDateFormat("yyyy", Locale.US).format(new Date());
        String monthStr = new SimpleDateFormat("M", Locale.US).format(new Date());
        loadExpenses(yearStr, monthStr);
    }

    private void initRef() {
        progressBar = findViewById(R.id.progress_bar);
        progressBarUpload = findViewById(R.id.progress_bar_upload);
        textMonth = findViewById(R.id.text_month);
        textUserName = findViewById(R.id.text_user_name);
        mCompactCalendarView = findViewById(R.id.calendar_view);
        textCommon = findViewById(R.id.text_cost_common);
        textMk = findViewById(R.id.text_cost_mk);
        mExpensesList = findViewById(R.id.recycler_view);
        fab = findViewById(R.id.fab);
        fab1 = findViewById(R.id.fab1);
        fab2 = findViewById(R.id.fab2);
        textFab1 = findViewById(R.id.textFab1);
        textFab2 = findViewById(R.id.textFab2);
        mFadedBackground = findViewById(R.id.faded_background);
        buttonDeleteAll = findViewById(R.id.button_cost_delete_all);
    }

    private void initSwitcher() {
        mTotalSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            public View makeView() {
                TextView myText = new TextView(ExpensesActivity.this);
                myText.setTextSize(32);
                myText.setTextColor(Color.BLACK);
                return myText;
            }
        });
    }

    private void initRecycle() {
        mExpensesAdapter = new ExpensesAdapter(this, mExpenseList);
        mExpensesList.setLayoutManager(new LinearLayoutManager(this));
        mExpensesList.setAdapter(mExpensesAdapter);
    }

    private void initFAB() {
        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_backward);
        fade = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade);
        fade_back_quick = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_back_quick);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateFAB();
                addCostDialog(getString(R.string.text_cost_common));
            }
        });
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateFAB();
                addCostDialog(getString(R.string.text_cost_mk));
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateFAB();
            }
        });
        mFadedBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateFAB();
            }
        });
    }
    /*
     int REQUEST_CODE_CAMERA =123;
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, REQUEST_CODE_CAMERA);
     */

    private void loadExpenses(String yearStr, String monthStr) {
        mDatabase.getReference(DefaultConfigurations.DB_EXPENSES)
                .child(yearStr)
                .child(monthStr)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        mExpenseList.clear();
                        mExpensesAdapter.notifyDataSetChanged();
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            Expense expense = data.getValue(Expense.class);
                            if (expense != null && (FirebaseUtils.isAdmin() || expense.getUserId().equals(mCurrentUser.getUid()))) {
                                mExpenseList.add(0, expense);
                            }
                        }
                        mExpensesAdapter.notifyDataSetChanged();
                        calcTotal();
                    }

                    @Override
                    public void onCancelled(final DatabaseError databaseError) {
                    }
                });
    }

    private void removeAllExpenses() {
        String yearStr = new SimpleDateFormat("yyyy", Locale.US).format(new Date());
        String monthStr = new SimpleDateFormat("M", Locale.US).format(new Date());
        mDatabase.getReference(DefaultConfigurations.DB_EXPENSES)
                .child(yearStr)
                .child(monthStr).removeValue();
        calcTotal();
    }

    public void animateFAB() {
        if (mIsFabOpen) {
            fab.startAnimation(rotate_backward);
            fab1.startAnimation(fab_close);
            fab2.startAnimation(fab_close);
            fab1.setClickable(false);
            fab2.setClickable(false);
            textFab1.startAnimation(fab_close);
            textFab2.startAnimation(fab_close);
            mFadedBackground.setClickable(false);
            mFadedBackground.startAnimation(fade_back_quick);
            mIsFabOpen = false;
        } else {
            fab.startAnimation(rotate_forward);
            fab1.startAnimation(fab_open);
            fab2.startAnimation(fab_open);
            textFab1.startAnimation(fab_open);
            textFab2.startAnimation(fab_open);
            fab1.setClickable(true);
            fab2.setClickable(true);
            mFadedBackground.setClickable(true);
            mFadedBackground.startAnimation(fade);
            mIsFabOpen = true;
        }
    }

    private void addCostDialog(final String title2) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Введіть опис");
        @SuppressLint("InflateParams") View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_expense, null, false);
        builder.setView(viewInflated);
        final EditText inputTitle1 = viewInflated.findViewById(R.id.text_title1);
        final EditText inputPrice = viewInflated.findViewById(R.id.text_price);
        builder.setPositiveButton("ДОБАВИТИ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String title1 = inputTitle1.getText().toString();
                String price = inputPrice.getText().toString();
                addExpense(title1, title2, price, "");
            }
        });
        builder.setNeutralButton("+ ФОТО", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mTitle1 = inputTitle1.getText().toString();
                mTitle2 = title2;
                mPrice = inputPrice.getText().toString();
                startCamera();
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

    private void calcTotal() {
        int common = 0;
        int mk = 0;
        for (Expense expense : mExpenseList) {
            if (expense.getTitle2().equals(getString(R.string.text_cost_common)))
                common += expense.getPrice();
            if (expense.getTitle2().equals(getString(R.string.text_cost_mk)))
                mk += expense.getPrice();
        }
        int total = common + mk;
        progressBar.setMax(total);
        progressBar.setProgress(common);
        textCommon.setText(getString(R.string.hrn, DateUtils.getIntWithSpace(common)));
        textMk.setText(getString(R.string.hrn, DateUtils.getIntWithSpace(mk)));
        mTotalSwitcher.setText(getString(R.string.HRN, DateUtils.getIntWithSpace(total)));
    }

    private void addExpense(String title1, String title22, String price, String uri) {
        if (price == null || price.isEmpty())
            price = "0";
        Expense expense = new Expense(title1, title22,
                new SimpleDateFormat("d-M-yyyy", Locale.US).format(new Date()),
                uri,
                mCurrentUser.getUid(),
                mCurrentUser.getDisplayName(),
                Integer.parseInt(price));
        addCostToDb(expense);
    }

    void startCamera() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = timeStamp + ".jpg";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        mPictureImagePath = storageDir.getAbsolutePath() + "/" + imageFileName;
        File file = new File(mPictureImagePath);
        Uri outputFileUri = Uri.fromFile(file);
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        startActivityForResult(cameraIntent, Constants.REQUEST_CODE_CAMERA);
        showProgressBarUpload();
    }

    private void addCostToDb(Expense newExpense) {
        String yearStr = new SimpleDateFormat("yyyy", Locale.US).format(new Date());
        String monthStr = new SimpleDateFormat("M", Locale.US).format(new Date());
        String time = new SimpleDateFormat("HH:mm", Locale.US).format(new Date());
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(DefaultConfigurations.DB_EXPENSES)
                .child(yearStr)
                .child(monthStr);
        String key = ref.push().getKey();
        newExpense.setKey(key);
        newExpense.setTime(time);
        ref.child(key).setValue(newExpense).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                mExpensesList.smoothScrollToPosition(0);
            }
        });
    }

    void showProgressBarUpload() {
        progressBarUpload.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.button_prev)
    void onPrevClicked() {
        setSwitcherAnim(true);
        mCompactCalendarView.showPreviousMonth();
    }

    private void setSwitcherAnim(final boolean right) {
        Animation in;
        Animation out;
        if (right) {
            in = AnimationUtils.loadAnimation(this, R.anim.trans_right_in);
            out = AnimationUtils.loadAnimation(this, R.anim.trans_right_out);
        } else {
            in = AnimationUtils.loadAnimation(this, R.anim.trans_left_in);
            out = AnimationUtils.loadAnimation(this, R.anim.trans_left_out);
        }
        mTotalSwitcher.setInAnimation(in);
        mTotalSwitcher.setOutAnimation(out);
    }

    @OnClick(R.id.button_next)
    void onNextClicked() {
        setSwitcherAnim(false);
        mCompactCalendarView.showNextMonth();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // handle request from camera
        if (requestCode == Constants.REQUEST_CODE_CAMERA) {
            if (resultCode == RESULT_OK) {
                Bitmap bitmap = BitmapFactory.decodeFile(mPictureImagePath);//= imageView.getDrawingCache();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                byte[] bytes = baos.toByteArray();
                // Meta data for imageView
                StorageMetadata metadata = new StorageMetadata.Builder()
                        .setContentType("imageView/jpg")
                        .setCustomMetadata("Title1", mTitle1)
                        .setCustomMetadata("Title2", mTitle2)
                        .setCustomMetadata("Uid", mCurrentUser.getUid())
                        .build();
                // name of file in Storage
                final String filename = DateUtils.getCurrentTimeStamp() + "_" + mCurrentUser.getUid() + ".jpg";
                UploadTask uploadTask = FirebaseStorage.getInstance()
                        .getReference(DefaultConfigurations.STORAGE_EXPENSES)
                        .child(filename)
                        .putBytes(bytes, metadata);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        addExpense(mTitle1, mTitle2, mPrice, filename);
                        hideProgressBarUpload();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        addExpense(mTitle1, mTitle2, mPrice, "");
                        hideProgressBarUpload();
                    }
                });
            } else { //resultCode
                addExpense(mTitle1, mTitle2, mPrice, "");
                hideProgressBarUpload();
            }
        }
        //other request code
    }

    void hideProgressBarUpload() {
        progressBarUpload.setVisibility(View.GONE);
        mExpensesList.scrollToPosition(0);
    }

    @Override
    public void onBackPressed() {
        if (progressBarUpload.getVisibility() == View.VISIBLE) {
            Toast.makeText(this, R.string.photo_uploading, Toast.LENGTH_SHORT).show();
            return;
        }
        super.onBackPressed();
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
    public void onRemoveClicked(final Expense expense) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_cost_delete_title)
                .setMessage(R.string.dialog_cost_delete_message)
                .setPositiveButton("Видалити", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        removeCost(expense);
                    }
                })
                .setNegativeButton("Повернутись", null)
                .show();
    }

    @Override
    public void onPhotoPreviewClicked(final Expense expense) {
        startActivity(PhotoPreviewActivity.getLaunchIntent(this, expense.getUri(), expense.getUserName()));
    }

    private void removeCost(Expense expense) {
        mDatabase.getReference(DefaultConfigurations.DB_EXPENSES)
                .child(DateUtils.getYearFromStr(expense.date)).child(DateUtils.getMonthFromStr(expense.date))
                .child(expense.getKey()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Snackbar.make(mExpensesList, getString(R.string.snack_deleted), Snackbar.LENGTH_LONG).show();
            }
        });
    }
}
