package com.ruslanlyalko.kidsapp.presentation.ui.main.clients.contacts.details;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ruslanlyalko.kidsapp.R;
import com.ruslanlyalko.kidsapp.common.DateUtils;
import com.ruslanlyalko.kidsapp.common.Keys;
import com.ruslanlyalko.kidsapp.data.FirebaseUtils;
import com.ruslanlyalko.kidsapp.data.configuration.DefaultConfigurations;
import com.ruslanlyalko.kidsapp.data.models.Birthday;
import com.ruslanlyalko.kidsapp.data.models.Contact;
import com.ruslanlyalko.kidsapp.presentation.base.BaseActivity;
import com.ruslanlyalko.kidsapp.presentation.ui.main.clients.birthdays.adapter.BirthdaysAdapter;
import com.ruslanlyalko.kidsapp.presentation.ui.main.clients.birthdays.adapter.OnBirthdaysClickListener;
import com.ruslanlyalko.kidsapp.presentation.ui.main.clients.birthdays.edit.BirthdaysEditActivity;
import com.ruslanlyalko.kidsapp.presentation.ui.main.clients.contacts.edit.ContactEditActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ContactDetailsActivity extends BaseActivity implements OnBirthdaysClickListener {

    @BindView(R.id.text_phone1) TextView mEditPhone1;
    @BindView(R.id.text_phone2) TextView mEditPhone2;
    @BindView(R.id.edit_child_name1) TextView mEditChildName1;
    @BindView(R.id.edit_child_date1) TextView mEditChildDate1;
    @BindView(R.id.edit_child_name2) TextView mEditChildName2;
    @BindView(R.id.edit_child_date2) TextView mEditChildDate2;
    @BindView(R.id.edit_child_name3) TextView mEditChildName3;
    @BindView(R.id.edit_child_date3) TextView mEditChildDate3;
    @BindView(R.id.edit_description) TextView mEditDescription;
    @BindView(R.id.list_birthdays) RecyclerView mListBirthdays;
    @BindView(R.id.layout_child1) LinearLayout mLayoutChild1;
    @BindView(R.id.layout_child2) LinearLayout mLayoutChild2;
    @BindView(R.id.layout_child3) LinearLayout mLayoutChild3;
    @BindView(R.id.button_add_birthday) Button mButtonAddBirthday;
    private BirthdaysAdapter mBirthdaysAdapter = new BirthdaysAdapter(this);
    private Contact mContact;
    private String mContactKey = "";

    public static Intent getLaunchIntent(final Context launchIntent, final Contact contact) {
        Intent intent = new Intent(launchIntent, ContactDetailsActivity.class);
        intent.putExtra(Keys.Extras.EXTRA_ITEM_ID, contact);
        return intent;
    }

    public static Intent getLaunchIntent(final Context launchIntent, final String contactKey) {
        Intent intent = new Intent(launchIntent, ContactDetailsActivity.class);
        intent.putExtra(Keys.Extras.EXTRA_CONTACT_KEY, contactKey);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_details);
        ButterKnife.bind(this);
        parseExtras();
        setupRecycler();
        setupView();
    }

    private void parseExtras() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mContact = (Contact) bundle.getSerializable(Keys.Extras.EXTRA_ITEM_ID);
            mContactKey = bundle.getString(Keys.Extras.EXTRA_CONTACT_KEY);
        }
    }

    private void setupRecycler() {
        mListBirthdays.setLayoutManager(new LinearLayoutManager(this));
        mListBirthdays.setAdapter(mBirthdaysAdapter);
    }

    private void setupView() {
        if (mContact == null) {
            loadDetails();
            return;
        }
        setTitle(mContact.getName());
        mEditChildName1.setText(mContact.getChildName1());
        mEditChildName2.setText(mContact.getChildName2());
        mEditChildName3.setText(mContact.getChildName3());
        mEditChildDate1.setText(DateUtils.toString(mContact.getChildBd1(), "dd.MM.yyyy"));
        mEditChildDate2.setText(DateUtils.toString(mContact.getChildBd2(), "dd.MM.yyyy"));
        mEditChildDate3.setText(DateUtils.toString(mContact.getChildBd3(), "dd.MM.yyyy"));
        mLayoutChild1.setVisibility(mContact.getChildName1() != null && !mContact.getChildName1().isEmpty() ? View.VISIBLE : View.GONE);
        mLayoutChild2.setVisibility(mContact.getChildName2() != null && !mContact.getChildName2().isEmpty() ? View.VISIBLE : View.GONE);
        mLayoutChild3.setVisibility(mContact.getChildName3() != null && !mContact.getChildName3().isEmpty() ? View.VISIBLE : View.GONE);
        mEditPhone1.setText(mContact.getPhone());
        mEditPhone2.setText(mContact.getPhone2());
        mEditPhone2.setVisibility(mContact.getPhone2() != null & !mContact.getPhone2().isEmpty() ? View.VISIBLE : View.GONE);
        mEditDescription.setText(mContact.getDescription());
        mEditDescription.setVisibility(mContact.getDescription() != null & !mContact.getDescription().isEmpty() ? View.VISIBLE : View.GONE);
        loadBirthdays();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_delete).setVisible(FirebaseUtils.isAdmin());
        menu.findItem(R.id.action_edit).setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_edit:
                startActivity(ContactEditActivity.getLaunchIntent(this, mContact));
                break;
            case R.id.action_delete:
                Toast.makeText(this, "Not Implemented", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(DefaultConfigurations.DB_CONTACTS)
                .child(mContactKey);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                mContact = dataSnapshot.getValue(Contact.class);
                setupView();
            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {
            }
        });
    }

    private void loadBirthdays() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(DefaultConfigurations.DB_BIRTHDAYS);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                List<Birthday> birthdays = new ArrayList<>();
                for (DataSnapshot birthdaySS : dataSnapshot.getChildren()) {
                    Birthday birthday = birthdaySS.getValue(Birthday.class);
                    if (birthday != null && birthday.getContactKey().equals(mContact.getKey())) {
                        birthdays.add(birthday);
                    }
                }
                mBirthdaysAdapter.setData(birthdays);
                //onFilterTextChanged();
            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onEditClicked(final int position) {
        startActivity(BirthdaysEditActivity.getLaunchIntent(this, mBirthdaysAdapter.getItem(position)));
    }

    @Override
    public void onItemClicked(final int position) {
    }

    @OnClick(R.id.button_add_birthday)
    public void onViewClicked() {
        startActivity(BirthdaysEditActivity.getLaunchIntent(this, mContact.getKey()));
    }

    @OnClick({R.id.text_phone1, R.id.text_phone2})
    public void onViewClicked(View view) {
        Intent callIntent;
        switch (view.getId()) {
            case R.id.text_phone1:
                callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + mContact.getPhone()));
                startActivity(callIntent);
                break;
            case R.id.text_phone2:
                callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + mContact.getPhone()));
                startActivity(callIntent);
                break;
        }
    }
}
