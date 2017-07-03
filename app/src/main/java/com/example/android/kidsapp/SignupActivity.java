package com.example.android.kidsapp;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.example.android.kidsapp.utils.User;
import com.example.android.kidsapp.utils.Constants;

public class SignupActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword, inputFirstName, inputSecondName, inputLastName, inputPhone;
    private Button btnSignUp, btnResetPassword;
    private ProgressBar progressBar;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseRefCurrentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
        setContentView(R.layout.activity_signup);
        //Get Firebase mFirebaseAuth instance
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        initializeReferences();

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });

        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });

    }

    private void resetPassword() {
        startActivity(new Intent(SignupActivity.this, ResetPasswordActivity.class));
    }

    /*
    *
     */
    private void signUp() {
        final String firstName = inputFirstName.getText().toString().trim();
        final String secondName = inputSecondName.getText().toString().trim();
        final String lastName = inputLastName.getText().toString().trim();
        final String phone = inputPhone.getText().toString().trim();
        final String email = inputEmail.getText().toString().trim();
        final String password = inputPassword.getText().toString().trim();

        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(secondName) || TextUtils.isEmpty(lastName)) {
            //Toast.makeText(getApplicationContext(), "Enter name!", Toast.LENGTH_SHORT).show();
            inputFirstName.setError("Enter name!");
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            //Toast.makeText(getApplicationContext(), "Enter phone!", Toast.LENGTH_SHORT).show();
            inputPhone.setError("Enter phone!");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        //create user
        mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the mFirebaseAuth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (task.isSuccessful()) {
                            UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(firstName+" "+lastName)
                                    .build();
                            mFirebaseAuth.getCurrentUser().updateProfile(profileUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    String uId = mFirebaseAuth.getCurrentUser().getUid();
                                    createUserData(firstName, secondName,lastName,phone, email, uId);
                                    Toast.makeText(SignupActivity.this, R.string.toast_user_created +" "+ email , Toast.LENGTH_SHORT).show();

                                    onBackPressed();
                                }
                            });

                            } else {
                            Toast.makeText(SignupActivity.this, "Authentication failed." + task.getException(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void createUserData(String name, String secondName, String lastName, String phone, String email, String uId) {
        mDatabaseRefCurrentUser = mFirebaseDatabase.getReference(Constants.FIREBASE_REF_USERS).child(uId);
        User user = new User(name, secondName, lastName, phone, email,"01.06.1991");
        mDatabaseRefCurrentUser.setValue(user);
    }

    private void initializeReferences() {
        btnSignUp = (Button) findViewById(R.id.sign_up_button);
        inputFirstName = (EditText) findViewById(R.id.firstName);
        inputSecondName= (EditText) findViewById(R.id.secondName);
        inputLastName = (EditText) findViewById(R.id.lastName);
        inputPhone = (EditText) findViewById(R.id.phone);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        btnResetPassword = (Button) findViewById(R.id.btn_reset_password);
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
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
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
    }
}