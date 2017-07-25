package com.example.android.kidsapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.kidsapp.utils.Constants;
import com.example.android.kidsapp.utils.SwipeLayout;
import com.example.android.kidsapp.utils.User;
import com.example.android.kidsapp.utils.Utils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    Button buttonUser, buttonEvents, buttonReport, buttonCalendar, buttonMk, buttonVyt;
    Button buttonSwipe, buttonAbout, buttonFB, buttonNotebook, buttonWeather;
    TextView textSnoopy, textLink, textLinkDetails;
    SwipeLayout swipeLayout;

    boolean mDoubleBackToExitPressedOnce = false;
    boolean mSwipeOpened = false;


    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseRemoteConfig mRemoteConfig = FirebaseRemoteConfig.getInstance();
    private DatabaseReference mDatabaseRefCurrentUser;
    private ValueEventListener mUserListener;
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private FirebaseUser currentUserAuth;

    private boolean mLinkActive = false;
    private String mLink;
    private String mLinkText;
    private String mLinkFb;
    private String mLatestVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        initRef();

        initRemoteConfig();
        // Enable connection without internet
        mDatabase.setPersistenceEnabled(true);

        initCurrentUser();

        initButtons();

        initSwipes();

    }

    private void initRemoteConfig() {
        mRemoteConfig.setConfigSettings(new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(true)
                .build());

        HashMap<String, Object> defaults = new HashMap<>();
        defaults.put("link_show", false);

        defaults.put("latest_version", "3.5");
        defaults.put("link", "https://www.fb.com/snoopyagency");

        defaults.put("link_text", "Відвідайте нашу сторінку у ФБ!");
        defaults.put("link_fb", "https://www.fb.com/snoopyagency");

        mRemoteConfig.setDefaults(defaults);
        final Task<Void> fetch = mRemoteConfig.fetch(0);

        fetch.addOnSuccessListener(this, new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mRemoteConfig.activateFetched();
                updateLink();
            }
        });
    }

    private void updateLink() {
        mLinkActive = mRemoteConfig.getBoolean("link_show");
        mLinkText = mRemoteConfig.getString("link_text");
        mLink = mRemoteConfig.getString("link");
        mLatestVersion = mRemoteConfig.getString("latest_version");

        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        String currentVersion = pInfo.versionName;

        if (mLinkActive && !currentVersion.equals(mLatestVersion)) {
            textLink.setText(mLinkText);
            textLinkDetails.setText("Докладніше >");
        } else {
            textLink.setText("");
            textLinkDetails.setText("");
        }
        textLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLinkActive) openBrowser(mLink);
            }
        });
        textLinkDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLinkActive) openBrowser(mLink);
            }
        });

        mLinkFb = mRemoteConfig.getString("link_fb");

        //todo
    }


    private void initSwipes() {
        // Swipe
        swipeLayout.setSwipeEnabled(false);
        swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
            @Override
            public void onStartOpen(SwipeLayout layout) {
                buttonSwipe.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_down3, 0, 0);
            }

            @Override
            public void onOpen(SwipeLayout layout) {
                mSwipeOpened = true;
            }

            @Override
            public void onStartClose(SwipeLayout layout) {
                buttonSwipe.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_up3, 0, 0);
            }

            @Override
            public void onClose(SwipeLayout layout) {
                mSwipeOpened = false;
            }

            @Override
            public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {

            }

            @Override
            public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {

            }
        });

        buttonSwipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSwipeOpened)
                    swipeLayout.close();
                else
                    swipeLayout.open();

            }
        });
    }

    private void initButtons() {

        // User & Events buttons
        buttonUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, UserActivity.class);
                startActivity(intent);
            }
        });
        buttonEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
                startActivity(intent);
            }
        });
        // Main Buttons
        buttonReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ReportActivity.class);
                startActivity(intent);
            }
        });
        buttonCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
                startActivity(intent);
            }
        });
        buttonMk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MkTabActivity.class);
                startActivity(intent);
            }
        });
        buttonVyt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CostsActivity.class);
                startActivity(intent);
            }
        });

        buttonAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PackageInfo pInfo = null;
                try {
                    pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                String version = pInfo.versionName;

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(R.string.dialog_about_title)
                        .setMessage(getString(R.string.dialog_about_message) + "" + version)
                        .setPositiveButton("Ок", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
            }
        });

        buttonFB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBrowser(mLinkFb);
            }
        });

        buttonNotebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // todo private notebook
            }
        });

        buttonWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //todo weather

            }
        });


    }


    private void initCurrentUser() {

        currentUserAuth = mAuth.getCurrentUser();
        if (currentUserAuth != null) {
            mDatabaseRefCurrentUser = mDatabase.getReference(Constants.FIREBASE_REF_USERS).child(currentUserAuth.getUid());

            mUserListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    //todo user
                    Utils.setIsAdmin(user.getUserIsAdmin());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };

            mDatabaseRefCurrentUser.addValueEventListener(mUserListener);
        } else {
            onDestroy();
        }
    }


    void openBrowser(String url) {
        Uri uri = Uri.parse(url); // missing 'http://' will cause crashed
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    /**
     * Set references to Buttons etc
     */
    private void initRef() {
        textSnoopy = (TextView) findViewById(R.id.text_snoopy);
        textLink = (TextView) findViewById(R.id.text_link);
        textLinkDetails = (TextView) findViewById(R.id.text_link_details);

        buttonUser = (Button) findViewById(R.id.button_user);
        buttonEvents = (Button) findViewById(R.id.button_events);
        buttonReport = (Button) findViewById(R.id.button_report);
        buttonCalendar = (Button) findViewById(R.id.button_calendar);
        buttonMk = (Button) findViewById(R.id.button_mk);
        buttonVyt = (Button) findViewById(R.id.button_vyt);

        buttonSwipe = (Button) findViewById(R.id.button_swipe);
        buttonAbout = (Button) findViewById(R.id.button_about);
        buttonNotebook = (Button) findViewById(R.id.button_notebook);
        buttonFB = (Button) findViewById(R.id.button_fb);
        buttonWeather = (Button) findViewById(R.id.button_weather);
        swipeLayout = (SwipeLayout) findViewById(R.id.swipe_layout);

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

    /**
     * When user press Back buttonExpand
     * close swipe and show message - double click to exit
     */
    @Override
    public void onBackPressed() {

        // Swipe down
        if (mSwipeOpened) {
            swipeLayout.close();
            mSwipeOpened = false;
            return;
        }
        if (mDoubleBackToExitPressedOnce) {
            super.onBackPressed();
            // TODO (kostul) close application completely
            int pid = android.os.Process.myPid();
            android.os.Process.killProcess(pid);
            return;
        }

        // Close app after twice click on Back buttonExpand
        mDoubleBackToExitPressedOnce = true;
        Toast.makeText(this, R.string.hint_double_press, Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                mDoubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            finish();
        }
    }
}
