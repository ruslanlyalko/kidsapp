package com.ruslanlyalko.kidsapp.presentation.ui.profile.settings;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ruslanlyalko.kidsapp.R;
import com.ruslanlyalko.kidsapp.common.Keys;
import com.ruslanlyalko.kidsapp.data.Utils;
import com.ruslanlyalko.kidsapp.data.configuration.DefaultConfigurations;
import com.ruslanlyalko.kidsapp.data.models.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ProfileSettingsActivity extends AppCompatActivity {

    EditText inputEmail, inputPhone, inputBDay, inputCard, inputFirstDate, inputPassword1, inputPassword2;
    TextView textName;
    LinearLayout panelFirstDate, panelPassword;
    Button buttonChangePassword;

    private Calendar mBirthDay = Calendar.getInstance();

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private String mNumber = "";
    private String mUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.nothing);
        setContentView(R.layout.activity_profile_settings);
        initRef();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mUid = bundle.getString(Keys.Extras.EXTRA_UID, FirebaseAuth.getInstance().getCurrentUser().getUid());
        }
        boolean isCurrentUser = mUid.equals(mAuth.getCurrentUser().getUid());
        // user can change only they own emails
        inputEmail.setEnabled(isCurrentUser);
        inputPassword1.setEnabled(isCurrentUser);
        inputPassword2.setEnabled(isCurrentUser);
        panelPassword.setEnabled(isCurrentUser);
        buttonChangePassword.setEnabled(isCurrentUser);
        buttonChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword();
            }
        });
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
                new DatePickerDialog(ProfileSettingsActivity.this, dateSetListener,
                        mBirthDay.get(Calendar.YEAR), mBirthDay.get(Calendar.MONTH),
                        mBirthDay.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        inputCard.addTextChangedListener(mWatcher);
        initCurrentUserData();
    }

    private void changePassword() {
        final String password1 = inputPassword1.getText().toString().trim();
        final String password2 = inputPassword2.getText().toString().trim();
        inputPassword1.setError(null);
        inputPassword2.setError(null);
        if (password1.length() <= 0) {
            return;
        }
        if (password1.length() < 6) {
            inputPassword1.setError(getString(R.string.toast_minimum_password));
            inputPassword1.requestFocus();
            return;
        }
        if (!password1.equals(password2)) {
            inputPassword2.setError(getString(R.string.toast_different_password));
            inputPassword2.requestFocus();
            return;
        }
        mAuth.getCurrentUser().updatePassword(password1);
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("userPassword", password1);
        mDatabase.getReference(DefaultConfigurations.DB_USERS).child(mUid).updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(ProfileSettingsActivity.this, R.string.toast_data_updated, Toast.LENGTH_SHORT).show();
            }
        });
    }

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
        panelPassword = findViewById(R.id.panel_password);
        buttonChangePassword = findViewById(R.id.button_change_password);
        panelFirstDate = findViewById(R.id.panel_first_date);
        textName = findViewById(R.id.text_name);
        inputFirstDate = findViewById(R.id.text_first_date);
        inputPhone = findViewById(R.id.text_phone);
        inputEmail = findViewById(R.id.text_email);
        inputBDay = findViewById(R.id.text_bday);
        inputCard = findViewById(R.id.text_card);
        inputPassword1 = findViewById(R.id.text_password1);
        inputPassword2 = findViewById(R.id.text_password2);
    }

    private void saveChanges() {
        final String phone = inputPhone.getText().toString().trim();
        final String email = inputEmail.getText().toString().trim();
        final String birthday = inputBDay.getText().toString().trim();
        final String card = inputCard.getText().toString().trim();
        final String tPhone = inputPhone.getTag().toString().trim();
        final String tEmail = inputEmail.getTag().toString().trim();
        final String tBirthday = inputBDay.getTag().toString().trim();
        final String tCard = inputCard.getTag().toString().trim();
        Map<String, Object> childUpdates = new HashMap<>();
        boolean needUpdate = false;
        if (!phone.equals(tPhone)) {
            childUpdates.put("userPhone", phone);
            needUpdate = true;
        }
        if (!birthday.equals(tBirthday)) {
            childUpdates.put("userBDay", birthday);
            needUpdate = true;
        }
        if (!card.equals(tCard)) {
            childUpdates.put("userCard", card);
            needUpdate = true;
        }
        if (!email.equals(tEmail)) {
            childUpdates.put("userEmail", email);
            mAuth.getCurrentUser().updateEmail(email);
            needUpdate = true;
        }
        if (needUpdate)
            mDatabase.getReference(DefaultConfigurations.DB_USERS).child(mUid).updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Toast.makeText(ProfileSettingsActivity.this, R.string.toast_data_updated, Toast.LENGTH_SHORT).show();
                }
            });
        else
            Toast.makeText(ProfileSettingsActivity.this, R.string.toast_nothing_to_change, Toast.LENGTH_SHORT).show();
    }

    private void initCurrentUserData() {
        DatabaseReference ref = mDatabase.getReference(DefaultConfigurations.DB_USERS).child(mUid);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                textName.setText(user.getUserName());
                inputPhone.setText(user.getUserPhone());
                inputPhone.setTag(user.getUserPhone());
                inputEmail.setText(user.getUserEmail());
                inputEmail.setTag(user.getUserEmail());
                inputBDay.setText(user.getUserBDay());
                inputBDay.setTag(user.getUserBDay());
                inputCard.setText(user.getUserCard());
                inputCard.setTag(user.getUserCard());
                inputFirstDate.setText(user.getUserFirstDate());
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
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        if (id == R.id.action_save) {
            saveChanges();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.nothing, R.anim.fadeout);
    }
}