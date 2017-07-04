package com.example.android.kidsapp;

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
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText inputEmail;
    private Button btnReset;
    private FirebaseAuth auth;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
        setContentView(R.layout.activity_reset_password);

        // Firebase
        auth = FirebaseAuth.getInstance();
        // Init btn's and others ui elements
        initializeReferences();
        // when user click on btnReset do the password reset
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { resetPassword();}
        });
    }


    private void resetPassword() {
        String email = inputEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplication(), "Введіть Вашу електронну скриньку", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ResetPasswordActivity.this, "Інструкція по відновленню пароля вислана на вашу скриньку!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ResetPasswordActivity.this, "Не можливо відправити лист. Можливо скринька не вірна!", Toast.LENGTH_SHORT).show();
                        }

                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void initializeReferences() {
        inputEmail = (EditText) findViewById(R.id.text_email);
        btnReset = (Button) findViewById(R.id.button_reset_password);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

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
