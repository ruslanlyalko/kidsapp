package com.ruslanlyalko.kidsapp.presentation.ui.main.clients.birthdays.edit;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ruslanlyalko.kidsapp.R;
import com.ruslanlyalko.kidsapp.common.DateUtils;
import com.ruslanlyalko.kidsapp.common.Keys;
import com.ruslanlyalko.kidsapp.data.configuration.DefaultConfigurations;
import com.ruslanlyalko.kidsapp.data.models.Birthday;
import com.ruslanlyalko.kidsapp.presentation.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BirthdaysEditActivity extends BaseActivity {

    @BindView(R.id.edit_child_name1) EditText mEditChildName1;
    @BindView(R.id.edit_child_date1) TextView mEditChildDate1;
    @BindView(R.id.edit_description) EditText mEditDescription;
    @BindView(R.id.seek_kids_count) SeekBar mSeekKidsCount;
    @BindView(R.id.checkbox_birthday_mk) CheckBox mCheckboxBirthdayMk;
    @BindView(R.id.checkbox_birthday_aqua) CheckBox mCheckboxBirthdayAqua;
    @BindView(R.id.checkbox_birthday_cinema) CheckBox mCheckboxBirthdayCinema;
    @BindView(R.id.text_kids_count) TextView mTextKidsCount;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
    private Birthday mBirthday = new Birthday();
    private String mContactKey;
    private boolean mNeedToSave = false;
    private boolean mIsNew = false;

    public static Intent getLaunchIntent(final Context launchIntent, final Birthday birthday) {
        Intent intent = new Intent(launchIntent, BirthdaysEditActivity.class);
        intent.putExtra(Keys.Extras.EXTRA_ITEM_ID, birthday);
        return intent;
    }

    public static Intent getLaunchIntent(final Context launchIntent, String contackKey) {
        Intent intent = new Intent(launchIntent, BirthdaysEditActivity.class);
        intent.putExtra(Keys.Extras.EXTRA_CLIENT_PHONE, contackKey);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.nothing);
        setContentView(R.layout.activity_birthday_edit);
        ButterKnife.bind(this);
        parseExtras();
        setupChangeWatcher();
        setupView();
    }

    private void parseExtras() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mBirthday = (Birthday) bundle.getSerializable(Keys.Extras.EXTRA_ITEM_ID);
            mContactKey = bundle.getString(Keys.Extras.EXTRA_CLIENT_PHONE);
        }
        mIsNew = mBirthday == null;
        if (mIsNew) {
            mBirthday = new Birthday(mContactKey);
        }
    }

    private void setupChangeWatcher() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mNeedToSave = true;
            }
        };
        mEditChildName1.addTextChangedListener(watcher);
        mEditChildDate1.addTextChangedListener(watcher);
        mEditChildDate1.setText(DateUtils.toString(mBirthday.getBdDate(), "dd.MM.yyyy"));
        mEditDescription.addTextChangedListener(watcher);
        CompoundButton.OnCheckedChangeListener checkedWatcher = (compoundButton, b) -> mNeedToSave = true;
        mCheckboxBirthdayAqua.setOnCheckedChangeListener(checkedWatcher);
        mCheckboxBirthdayMk.setOnCheckedChangeListener(checkedWatcher);
        mCheckboxBirthdayCinema.setOnCheckedChangeListener(checkedWatcher);
        mSeekKidsCount.setMax(30);
        mTextKidsCount.setText(getString(R.string.text_kids_count, String.valueOf(0)));
        mSeekKidsCount.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(final SeekBar seekBar, final int i, final boolean b) {
                mTextKidsCount.setText(getString(R.string.text_kids_count, String.valueOf(i)));
            }

            @Override
            public void onStartTrackingTouch(final SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(final SeekBar seekBar) {
            }
        });
    }

    private void setupView() {
        if (mIsNew) {
            setTitle(R.string.title_activity_add);
        } else {
            setTitle(R.string.title_activity_edit);
            mEditDescription.setText(mBirthday.getDescription());
            mEditChildName1.setText(mBirthday.getChildName());
            mEditChildDate1.setText(DateUtils.toString(mBirthday.getBdDate(), "dd.MM.yyyy"));
            mSeekKidsCount.setProgress(mBirthday.getKidsCount());
            mCheckboxBirthdayAqua.setChecked(mBirthday.getAqua());
            mCheckboxBirthdayMk.setChecked(mBirthday.getMk());
            mCheckboxBirthdayCinema.setChecked(mBirthday.getCinema());
        }
        mNeedToSave = false;
    }

    @Override
    public void onBackPressed() {
        if (mNeedToSave) {
            AlertDialog.Builder builder = new AlertDialog.Builder(BirthdaysEditActivity.this);
            builder.setTitle(R.string.dialog_discart_changes)
                    .setPositiveButton(R.string.action_discard, (dialog, which) -> {
                        mNeedToSave = false;
                        onBackPressed();
                    })
                    .setNegativeButton(R.string.action_cancel, null)
                    .show();
        } else {
            super.onBackPressed();
            overridePendingTransition(R.anim.nothing, R.anim.fadeout);
        }
    }

    private void updateModel() {
        mBirthday.setDescription(mEditDescription.getText().toString().trim());
        mBirthday.setKidsCount(mSeekKidsCount.getProgress());
        mBirthday.setChildName(mEditChildName1.getText().toString().trim());
        mBirthday.setBdDate(DateUtils.parse(mEditChildDate1.getText().toString().trim(), "dd.MM.yyyy"));
        mBirthday.setAqua(mCheckboxBirthdayAqua.isChecked());
        mBirthday.setMk(mCheckboxBirthdayMk.isChecked());
        mBirthday.setCinema(mCheckboxBirthdayCinema.isChecked());
    }

    private void addClient() {
        updateModel();
        mIsNew = false;
        DatabaseReference ref = database.getReference(DefaultConfigurations.DB_BIRTHDAYS)
                .push();
        mBirthday.setKey(ref.getKey());
        ref.setValue(mBirthday).addOnCompleteListener(task -> {
            Snackbar.make(mEditChildName1, getString(R.string.client_added), Snackbar.LENGTH_SHORT).show();
            mNeedToSave = false;
            onBackPressed();
        });
    }

    private void updateClient() {
        updateModel();
        database.getReference(DefaultConfigurations.DB_BIRTHDAYS)
                .child(mBirthday.getKey())
                .setValue(mBirthday)
                .addOnCompleteListener(task -> {
                    Toast.makeText(BirthdaysEditActivity.this, getString(R.string.mk_updated), Toast.LENGTH_SHORT).show();
                    mNeedToSave = false;
                    onBackPressed();
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save) {
            if (mIsNew)
                addClient();
            else
                updateClient();
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.layout_date)
    public void onDateClicked() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(mBirthday.getBdDate());
        new DatePickerDialog(BirthdaysEditActivity.this, (datePicker, year, month, day)
                -> {
            mBirthday.setBdDate(DateUtils.getDate(year, month, day));
            mEditChildDate1.setText(DateUtils.toString(mBirthday.getBdDate(), "dd.MM.yyyy"));
        },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }
}
