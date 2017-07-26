package com.example.android.kidsapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.android.kidsapp.utils.Constants;
import com.example.android.kidsapp.utils.User;
import com.example.android.kidsapp.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class UserSettingsActivity extends AppCompatActivity {

    EditText inputName, inputEmail, inputPhone, inputBDay, inputCard, inputFirstDate, inputTime;
    LinearLayout panelFirstDate;

    private Calendar mBirthDay = Calendar.getInstance();

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();


    String mUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);

        initRef();


        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mUid = bundle.getString(Constants.EXTRA_UID, FirebaseAuth.getInstance().getCurrentUser().getUid());
        }

        boolean isCurrentUser = mUid.equals(mAuth.getCurrentUser().getUid());
        // user can change only they own emails
        inputEmail.setEnabled(isCurrentUser);
        panelFirstDate.setVisibility(Utils.isAdmin() && !isCurrentUser ? View.VISIBLE : View.GONE);

        final DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                mBirthDay.set(Calendar.YEAR, year);
                mBirthDay.set(Calendar.MONTH, monthOfYear);
                mBirthDay.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.US);

                inputBDay.setText(sdf.format(mBirthDay.getTime()));
            }
        };

        // Pop up the Date Picker after user clicked on editText
        inputBDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(UserSettingsActivity.this, dateSetListener,
                        mBirthDay.get(Calendar.YEAR), mBirthDay.get(Calendar.MONTH),
                        mBirthDay.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        inputCard.addTextChangedListener(mWatcher);

        initCurrentUserData();
    }

    private String mNumber = "";

    private TextWatcher mWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (mNumber.length() < s.length()) {

                switch (s.length()) {
                    case 5:
                        s.insert(4, " ");

                        break;
                    case 10:
                        s.insert(9, " ");
                        break;
                    case 15:
                        s.insert(14, " ");
                        break;
                }
            }
            mNumber = s.toString();
        }
    };

    private void initRef() {

        panelFirstDate = (LinearLayout) findViewById(R.id.panel_first_date);
        inputTime = (EditText) findViewById(R.id.text_time);
        inputFirstDate = (EditText) findViewById(R.id.text_first_date);
        inputName = (EditText) findViewById(R.id.text_name);
        inputPhone = (EditText) findViewById(R.id.text_phone);
        inputEmail = (EditText) findViewById(R.id.text_email);
        inputBDay = (EditText) findViewById(R.id.text_bday);
        inputCard = (EditText) findViewById(R.id.text_card);

    }

    private void saveChanges() {

        final String name = inputName.getText().toString().trim();
        final String phone = inputPhone.getText().toString().trim();
        final String email = inputEmail.getText().toString().trim();
        final String bday = inputBDay.getText().toString().trim();
        final String card = inputCard.getText().toString().trim();

        final String tname = inputName.getTag().toString().trim();
        final String tphone = inputPhone.getTag().toString().trim();
        final String temail = inputEmail.getTag().toString().trim();
        final String tbday = inputBDay.getTag().toString().trim();
        final String tcard = inputCard.getTag().toString().trim();

        Map<String, Object> childUpdates = new HashMap<>();

        boolean needUpdate = false;
        if (!name.equals(tname)) {
            childUpdates.put("userName", name);
            needUpdate = true;
        }
        if (!phone.equals(tphone)) {
            childUpdates.put("userPhone", phone);
            needUpdate = true;
        }
        if (!bday.equals(tbday)) {
            childUpdates.put("userBDay", bday);
            needUpdate = true;
        }
        if (!card.equals(tcard)) {
            childUpdates.put("userCard", card);
            needUpdate = true;
        }
        if (!email.equals(temail)) {
            childUpdates.put("userEmail", email);
            mAuth.getCurrentUser().updateEmail(email);
            needUpdate = true;
        }

        if (needUpdate)
            mDatabase.getReference(Constants.FIREBASE_REF_USERS).child(mUid).updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Toast.makeText(UserSettingsActivity.this, R.string.toast_data_updated, Toast.LENGTH_SHORT).show();
                }
            });

    }

    private void initCurrentUserData() {

        DatabaseReference ref = mDatabase.getReference(Constants.FIREBASE_REF_USERS).child(mUid);


        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                inputName.setText(user.getUserName());
                inputName.setTag(user.getUserName());

                inputPhone.setText(user.getUserPhone());
                inputPhone.setTag(user.getUserPhone());

                inputEmail.setText(user.getUserEmail());
                inputEmail.setTag(user.getUserEmail());

                inputBDay.setText(user.getUserBDay());
                inputBDay.setTag(user.getUserBDay());

                inputCard.setText(user.getUserCard());
                inputCard.setTag(user.getUserCard());

                inputFirstDate.setText(user.getUserFirstDate());
                inputTime.setText(user.getUserTimeStart() + " - " + user.getUserTimeEnd());


                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

                Date dt = new Date();
                try {
                    dt = sdf.parse(user.getUserBDay());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                mBirthDay.setTime(dt);
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
        super.onBackPressed();
        saveChanges();
    }
}