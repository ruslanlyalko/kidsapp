package com.example.android.kidsapp;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
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


public class UserActivity extends AppCompatActivity {

    private final String TAG = "UserActivity.java";

    ProgressBar progressBar;
    TextView textUserName;
    TextView textUserEmail;
    ImageView userImage;

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

        initializeCurrentUserData();

    }

    private void initializeReferences() {
        textUserName = (TextView) findViewById(R.id.textUserName);
        textUserEmail = (TextView) findViewById(R.id.textUserEmail);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        userImage = (ImageView) findViewById(R.id.userImage);
    }

    private void initializeCurrentUserData() {

        progressBar.setVisibility(View.VISIBLE);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            mDatabaseRefCurrentUser = mDatabase.getReference(Constants.FIREBASE_REF_USERS).child(currentUser.getUid());

            mUserListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);

                    textUserName.setText(user.getUserFirstName() + " " + user.getUserLastName());
                    textUserEmail.setText(user.getUserEmail() + "   " + user.getUserPhone());
                    progressBar.setVisibility(View.GONE);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    textUserName.setText("");
                    textUserEmail.setText("");
                    progressBar.setVisibility(View.GONE);
                    Log.w(TAG, "Failed to load data. " + databaseError.toException());
                }
            };

            // Download user profile image from STORAGE
            StorageReference storageReference = mStorage.getReference(Constants.FIREBASE_STORAGE_PICTURES)
                    .child("default_profile_picture.png");

            Glide.with(this /* context */)
                    .using(new FirebaseImageLoader())
                    .load(storageReference)
                    .into(userImage);

        } else {
            Log.w(TAG, "Error. User is not logged in");
            onDestroy();
        }
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
            case R.id.action_settings: {
                // User chose the "Settings" item, show the app settings UI...
                startActivityForResult(new Intent(UserActivity.this, SettingsActivity.class), 0);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        boolean needLogout = data.getBooleanExtra(Constants.EXTRA_NEED_LOGOUT, false);
        if (needLogout) {
            // After Log Out open Login Activity and finish this activity
            // to avoid user press Back and return to UserActivity without logged in
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
    }
}