package com.example.android.kidsapp;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.android.kidsapp.utils.Constants;
import com.example.android.kidsapp.utils.User;
import com.example.android.kidsapp.utils.Utils;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class UserSettingsActivity extends AppCompatActivity {

    private static final String TAG = UserSettingsActivity.class.getSimpleName();

    ImageView imageUser1, imageUserLogo;
    EditText inputName, inputEmail, inputPhone, inputBDay, inputCard;
    TextView textTitleName, textTitlePosition;
    FloatingActionButton fab;
    Button btnSignup;

    private Calendar mBirthDay = Calendar.getInstance();

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();

    private DatabaseReference mDatabaseRefCurrentUser;
    private ValueEventListener mUserListener;
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
        setContentView(R.layout.activity_user_settings);

        // Set btn's, text's references and onClickListener's
        initRef();

        btnSignup.setVisibility(Utils.isIsAdmin() ? View.VISIBLE : View.GONE);

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


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isIsAdmin()) {
                    Intent intent = new Intent(UserSettingsActivity.this, DashboardActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(UserSettingsActivity.this, SalaryActivity.class);
                    startActivity(intent);
                }
            }
        });
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UserSettingsActivity.this, SignupActivity.class));
            }
        });

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

        imageUserLogo = (ImageView) findViewById(R.id.image_user_logo);
        imageUser1 = (ImageView) findViewById(R.id.image_user1);
        inputName = (EditText) findViewById(R.id.text_name);
        inputPhone = (EditText) findViewById(R.id.text_phone);
        inputEmail = (EditText) findViewById(R.id.text_email);
        inputBDay = (EditText) findViewById(R.id.text_bday);
        inputCard = (EditText) findViewById(R.id.text_card);

        btnSignup = (Button) findViewById(R.id.button_sign_up);

        fab = (FloatingActionButton) findViewById(R.id.fab);

        textTitleName = (TextView) findViewById(R.id.text_title_name);
        textTitlePosition = (TextView) findViewById(R.id.text_position_title);
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
            mDatabaseRefCurrentUser.updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Toast.makeText(UserSettingsActivity.this, R.string.toast_data_updated, Toast.LENGTH_SHORT).show();
                }
            });

    }

    private void initCurrentUserData() {

        final FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            mDatabaseRefCurrentUser = mDatabase.getReference(Constants.FIREBASE_REF_USERS).child(currentUser.getUid());

            mUserListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);

                    textTitleName.setText(user.getUserName());

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

                    StorageReference storageRef = storage.getReference(Constants.FIREBASE_STORAGE_PICTURES)
                            .child("cover5.png");
                    Glide.with(UserSettingsActivity.this).using(new FirebaseImageLoader()).load(storageRef).into(imageUser1);

                    // draw First Letters from UserName
                    Bitmap bitmap = Bitmap.createBitmap(70, 70, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);

                    String textToDraw = getFirstLetters(currentUser.getDisplayName());
                    Paint paint = new Paint();
                    paint.setColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
                    paint.setTextSize(32);
                    canvas.drawText(textToDraw, 15, 46, paint);

                    imageUserLogo.setImageBitmap(bitmap);

                    //imageUserLogo.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));

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

    private String getFirstLetters(String displayName) {

        int ind = displayName.indexOf(' ');

        return displayName.substring(0, 1) + displayName.substring(ind + 1, ind + 2);
    }

    private void logout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_logout_title)
                .setMessage(R.string.dialog_logout_message)
                .setPositiveButton("Вийти", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mAuth.signOut();

                        Intent intent = new Intent(UserSettingsActivity.this,
                                LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton("Повернутись", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
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