package com.ruslanlyalko.kidsapp.presentation.ui.main.clients.contacts.edit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ruslanlyalko.kidsapp.R;
import com.ruslanlyalko.kidsapp.common.DateUtils;
import com.ruslanlyalko.kidsapp.common.Keys;
import com.ruslanlyalko.kidsapp.data.configuration.DefaultConfigurations;
import com.ruslanlyalko.kidsapp.data.models.Contact;
import com.ruslanlyalko.kidsapp.presentation.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ContactEditActivity extends BaseActivity {

    @BindView(R.id.edit_name) EditText mEditName;
    @BindView(R.id.edit_phone1) EditText mEditPhone1;
    @BindView(R.id.edit_phone2) EditText mEditPhone2;
    @BindView(R.id.edit_child_name1) EditText mEditChildName1;
    @BindView(R.id.edit_child_date1) EditText mEditChildDate1;
    @BindView(R.id.edit_child_name2) EditText mEditChildName2;
    @BindView(R.id.edit_child_date2) EditText mEditChildDate2;
    @BindView(R.id.edit_child_name3) EditText mEditChildName3;
    @BindView(R.id.edit_child_date3) EditText mEditChildDate3;
    @BindView(R.id.edit_description) EditText mEditDescription;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
    private Contact mContact = new Contact();
    private String mClientPhone;
    private boolean mNeedToSave = false;
    private boolean mIsNew = false;

    public static Intent getLaunchIntent(final Context launchIntent, final Contact contact) {
        Intent intent = new Intent(launchIntent, ContactEditActivity.class);
        intent.putExtra(Keys.Extras.EXTRA_ITEM_ID, contact);
        return intent;
    }

    public static Intent getLaunchIntent(final Context launchIntent, String phone) {
        Intent intent = new Intent(launchIntent, ContactEditActivity.class);
        intent.putExtra(Keys.Extras.EXTRA_CLIENT_PHONE, phone);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.nothing);
        setContentView(R.layout.activity_contact_edit);
        ButterKnife.bind(this);
        parseExtras();
        setupChangeWatcher();
        setupView();
    }

    private void parseExtras() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mContact = (Contact) bundle.getSerializable(Keys.Extras.EXTRA_ITEM_ID);
            mClientPhone = bundle.getString(Keys.Extras.EXTRA_CLIENT_PHONE);
        }
        mIsNew = mContact == null;
        if (mIsNew) {
            mContact = new Contact();
            mEditPhone1.setText(mClientPhone);
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
        mEditName.addTextChangedListener(watcher);
        mEditChildName1.addTextChangedListener(watcher);
        mEditChildName2.addTextChangedListener(watcher);
        mEditChildName3.addTextChangedListener(watcher);
        mEditChildDate1.addTextChangedListener(watcher);
        mEditChildDate2.addTextChangedListener(watcher);
        mEditChildDate3.addTextChangedListener(watcher);
        mEditPhone1.addTextChangedListener(watcher);
        mEditPhone2.addTextChangedListener(watcher);
    }

    private void setupView() {
        if (mIsNew) {
            setTitle(R.string.title_activity_add);
        } else {
            setTitle(R.string.title_activity_edit);
            mEditName.setText(mContact.getName());
            mEditChildName1.setText(mContact.getChildName1());
            mEditChildName2.setText(mContact.getChildName2());
            mEditChildName3.setText(mContact.getChildName3());
            mEditChildDate1.setText(DateUtils.toString(mContact.getChildBd1(), "dd.MM.yyyy"));
            mEditChildDate2.setText(DateUtils.toString(mContact.getChildBd2(), "dd.MM.yyyy"));
            mEditChildDate3.setText(DateUtils.toString(mContact.getChildBd3(), "dd.MM.yyyy"));
            mEditPhone1.setText(mContact.getPhone());
            mEditPhone2.setText(mContact.getPhone2());
            mEditDescription.setText(mContact.getDescription());
        }
        mNeedToSave = false;
    }

    @Override
    public void onBackPressed() {
        if (mNeedToSave) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ContactEditActivity.this);
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
        mContact.setName(mEditName.getText().toString().trim());
        mContact.setChildName1(mEditChildName1.getText().toString().trim());
        mContact.setChildName2(mEditChildName2.getText().toString().trim());
        mContact.setChildName3(mEditChildName3.getText().toString().trim());
        mContact.setChildBd1(DateUtils.parse(mEditChildDate1.getText().toString().trim(), "dd.MM.yyyy"));
        mContact.setChildBd2(DateUtils.parse(mEditChildDate2.getText().toString().trim(), "dd.MM.yyyy"));
        mContact.setChildBd3(DateUtils.parse(mEditChildDate3.getText().toString().trim(), "dd.MM.yyyy"));
        mContact.setPhone(mEditPhone1.getText().toString().trim());
        mContact.setPhone2(mEditPhone2.getText().toString().trim());
        mContact.setDescription(mEditDescription.getText().toString().trim());
    }

    private void addClient() {
        updateModel();
        mIsNew = false;
        DatabaseReference ref = database.getReference(DefaultConfigurations.DB_CONTACTS)
                .push();
        mContact.setKey(ref.getKey());
        ref.setValue(mContact).addOnCompleteListener(task -> {
            Snackbar.make(mEditName, getString(R.string.client_added), Snackbar.LENGTH_SHORT).show();
            mNeedToSave = false;
            onBackPressed();
        });
    }

    private void updateClient() {
        updateModel();
        database.getReference(DefaultConfigurations.DB_CONTACTS)
                .child(mContact.getKey())
                .setValue(mContact)
                .addOnCompleteListener(task -> {
                    Toast.makeText(ContactEditActivity.this, getString(R.string.mk_updated), Toast.LENGTH_SHORT).show();
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
}
