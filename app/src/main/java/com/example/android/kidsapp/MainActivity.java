package com.example.android.kidsapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
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
    Button buttonSwipe, buttonAbout, buttonFB, buttonLink, buttonWeather;
    TextView textAppName, textLink, textLinkDetails;
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
    private String mAboutText = "";

    // swipe
    private float x1, x2, y1, y2;
    static final int MIN_DISTANCE = 150;

    public static int getStatusBarHeight(Activity context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
/*
        Context context =this;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //status bar height
            int statusBarHeight = getStatusBarHeight(this);

            View view = new View(context);
            view.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            view.getLayoutParams().height = statusBarHeight;
            ((ViewGroup) w.getDecorView()).addView(view);
            view.setBackgroundColor(context.getResources().getColor(R.color.colorPrimaryDark));
        }*/
       /* Window window = getWindow();

// clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

// finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorPrimaryDark));
*/
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

        final boolean isLatestVersion = currentVersion.equals(mLatestVersion);
        if (mLinkActive && !isLatestVersion) {
            textLink.setText(mLinkText);
            textLinkDetails.setText("Докладніше >");
        } else {
            textLink.setText("");
            textLinkDetails.setText("");
        }
        textLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLinkActive && !isLatestVersion) openBrowser(mLink);
            }
        });
        textLinkDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLinkActive && !isLatestVersion) openBrowser(mLink);
            }
        });

        mLinkFb = mRemoteConfig.getString("link_fb");

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
                Intent intent = new Intent(MainActivity.this, NotActivity.class);
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

                Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                intent.putExtra(Constants.EXTRA_ABOUT, mAboutText);
                startActivity(intent);
            }
        });

        buttonFB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBrowser(mLinkFb);
            }
        });

        buttonLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBrowser(mLink);
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

        mDatabase.getReference(Constants.FIREBASE_REF_ABOUT).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null)
                    mAboutText = dataSnapshot.getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


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
        textAppName = (TextView) findViewById(R.id.text_app_name);
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
        buttonLink = (Button) findViewById(R.id.button_link);
        buttonFB = (Button) findViewById(R.id.button_fb);
        buttonWeather = (Button) findViewById(R.id.button_weather);
        swipeLayout = (SwipeLayout) findViewById(R.id.swipe_layout);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                y1 = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                x2 = event.getX();
                float deltaX = x2 - x1;
                if (deltaX > MIN_DISTANCE) {
                    // left2right
                    Intent intent = new Intent(MainActivity.this, UserActivity.class);
                    startActivity(intent);
                    break;
                } else if (deltaX < (0 - MIN_DISTANCE)) {
                    // right2left
                    Intent intent = new Intent(MainActivity.this, NotActivity.class);
                    startActivity(intent);
                    break;
                } else {
                    // consider as something else - a screen tap for example

                }
                y2 = event.getY();
                float deltaY = y2 - y1;
                if (deltaY > MIN_DISTANCE) {
                    // top2bottom
                    swipeLayout.close();
                } else if (deltaY < (0 - MIN_DISTANCE)) {
                    // bottom2top
                    swipeLayout.open();
                } else {
                    // consider as something else - a screen tap for example
                }
                break;
        }
        return super.onTouchEvent(event);
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
