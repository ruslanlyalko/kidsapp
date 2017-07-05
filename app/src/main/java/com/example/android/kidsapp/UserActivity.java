package com.example.android.kidsapp;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import com.example.android.kidsapp.utils.User;
import com.example.android.kidsapp.utils.Constants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class UserActivity extends AppCompatActivity {

    private static final String TAG = UserActivity.class.getSimpleName();

    ImageView imageUser;
    EditText inputName, inputEmail, inputPhone, inputBDay;
    // ProgressBar progressBar;

    private Calendar mBirthDay = Calendar.getInstance();

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private FirebaseStorage mStorage = FirebaseStorage.getInstance();

    private DatabaseReference mDatabaseRefCurrentUser;
    private ValueEventListener mUserListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
        setContentView(R.layout.activity_user);

        // Set btn's, text's references and onClickListener's
        initializeReferences();

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
                new DatePickerDialog(UserActivity.this, dateSetListener,
                        mBirthDay.get(Calendar.YEAR), mBirthDay.get(Calendar.MONTH),
                        mBirthDay.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        initializeCurrentUserData();
    }

    private void initializeReferences() {

        inputName = (EditText) findViewById(R.id.text_name);
        inputPhone = (EditText) findViewById(R.id.text_phone);
        inputEmail = (EditText) findViewById(R.id.text_email);
        inputBDay = (EditText) findViewById(R.id.text_bday);
    }

    private void saveChanges() {

//        progressBar.setVisibility(View.VISIBLE);

        final String name = inputName.getText().toString().trim();
        final String phone = inputPhone.getText().toString().trim();
        final String email = inputEmail.getText().toString().trim();
        final String bday = inputBDay.getText().toString().trim();

        final String tname = inputName.getTag().toString().trim();
        final String tphone = inputPhone.getTag().toString().trim();
        final String temail = inputEmail.getTag().toString().trim();
        final String tbday = inputBDay.getTag().toString().trim();


        User user = new User(name, phone, email, bday);

        Map<String, Object> userValue = user.toMap();

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
        if (needUpdate)
            mDatabaseRefCurrentUser.updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Toast.makeText(UserActivity.this, R.string.toast_data_updated, Toast.LENGTH_SHORT).show();
                    // progressBar.setVisibility(View.GONE);
                }
            });

        // TODO update Email!

    }

    private void initializeCurrentUserData() {

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            mDatabaseRefCurrentUser = mDatabase.getReference(Constants.FIREBASE_REF_USERS).child(currentUser.getUid());

            mUserListener = new ValueEventListener() {
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

                    Log.w(TAG, "Failed to load data. " + databaseError.toException());
                }
            };

            mDatabaseRefCurrentUser.addValueEventListener(mUserListener);
        } else {
            Log.w(TAG, "Error. User is not logged in");
            onDestroy();
        }
    }

    private void logout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_logout_title)
                .setMessage(R.string.dialog_logout_message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mAuth.signOut();
                        onBackPressed();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                //alert icon if we need
                //.setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mDatabaseRefCurrentUser.addValueEventListener(mUserListener);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mUserListener != null) {
            mDatabaseRefCurrentUser.removeEventListener(mUserListener);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_user, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        switch (id) {
            case R.id.action_logout: {
                logout();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        saveChanges();
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
    }
}