package com.ruslanlyalko.kidsapp.presentation.ui;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.ruslanlyalko.kidsapp.R;
import com.ruslanlyalko.kidsapp.common.Keys;
import com.ruslanlyalko.kidsapp.data.Utils;
import com.ruslanlyalko.kidsapp.data.configuration.DefaultConfigurations;
import com.ruslanlyalko.kidsapp.data.models.User;
import com.ruslanlyalko.kidsapp.presentation.ui.about.AboutActivity;
import com.ruslanlyalko.kidsapp.presentation.ui.calendar.CalendarActivity;
import com.ruslanlyalko.kidsapp.presentation.ui.expenses.ExpensesActivity;
import com.ruslanlyalko.kidsapp.presentation.ui.mk.MkTabActivity;
import com.ruslanlyalko.kidsapp.presentation.ui.notifications.NotificationsActivity;
import com.ruslanlyalko.kidsapp.presentation.ui.profile.ProfileActivity;
import com.ruslanlyalko.kidsapp.presentation.ui.report.ReportActivity;
import com.ruslanlyalko.kidsapp.presentation.widget.SwipeLayout;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    static final int MIN_DISTANCE = 150;

    @BindView(R.id.text_app_name) TextView mAppNameText;
    @BindView(R.id.text_link) TextView mLinkText;
    @BindView(R.id.text_link_details) TextView mLinkDetailsText;
    @BindView(R.id.button_arrow) Button mArrowButton;
    @BindView(R.id.swipe_layout) SwipeLayout mSwipeLayout;

    boolean mDoubleBackToExitPressedOnce = false;
    boolean mSwipeOpened = false;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseRemoteConfig mRemoteConfig = FirebaseRemoteConfig.getInstance();
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private boolean mLinkActive = false;
    private String mLink;
    private String mLinkFb;
    private String mAboutText = "";
    private float x1;
    private float y1;
    private boolean mIsLatestVersion;

    public static Intent getLaunchIntent(final AppCompatActivity launchActivity) {
        return new Intent(launchActivity, MainActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initRemoteConfig();
        initCurrentUser();
        initSwipes();
    }

    private void initRemoteConfig() {
        mRemoteConfig.setConfigSettings(new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(true)
                .build());
        HashMap<String, Object> defaults = new HashMap<>();
        defaults.put("link_show", false);
        defaults.put("latest_version", "1.7");
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

    private void initCurrentUser() {
        if (mAuth.getCurrentUser() != null) {
            mDatabase.getReference(DefaultConfigurations.DB_USERS)
                    .child(mAuth.getCurrentUser().getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(final DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);
                            if (user != null)
                                Utils.setIsAdmin(user.getUserIsAdmin());
                        }

                        @Override
                        public void onCancelled(final DatabaseError databaseError) {
                        }
                    });
        } else {
            finish();
        }
        mDatabase.getReference(DefaultConfigurations.DB_ABOUT)
                .addListenerForSingleValueEvent(new ValueEventListener() {
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

    private void initSwipes() {
        // Swipe
        mSwipeLayout.setSwipeEnabled(false);
        mSwipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
            @Override
            public void onStartOpen(SwipeLayout layout) {
                mArrowButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_down3, 0, 0);
            }

            @Override
            public void onOpen(SwipeLayout layout) {
                mSwipeOpened = true;
            }

            @Override
            public void onStartClose(SwipeLayout layout) {
                mArrowButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_up3, 0, 0);
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
    }

    private void updateLink() {
        mLinkActive = mRemoteConfig.getBoolean("link_show");
        String linkText = mRemoteConfig.getString("link_text");
        mLink = mRemoteConfig.getString("link");
        String latestVersion = mRemoteConfig.getString("latest_version");
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String currentVersion = pInfo != null ? pInfo.versionName : "";
        mIsLatestVersion = currentVersion.equals(latestVersion);
        if (mLinkActive && !mIsLatestVersion) {
            mLinkText.setText(linkText);
            mLinkDetailsText.setText("Докладніше >");
        } else {
            mLinkText.setText("");
            mLinkDetailsText.setText("");
        }
        mLinkFb = mRemoteConfig.getString("link_fb");
    }

    @OnClick({R.id.text_link, R.id.text_link_details})
    void onTopLinkClicked() {
        if (mLinkActive && !mIsLatestVersion) openBrowser(mLink);
    }

    private void openBrowser(String url) {
        Uri uri = Uri.parse(url); // missing 'http://' will cause crashed
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    @OnClick(R.id.button_arrow)
    void onArrowClicked() {
        if (mSwipeOpened)
            mSwipeLayout.close();
        else
            mSwipeLayout.open();
    }

    @OnClick(R.id.button_profile)
    void onProfileClicked() {
        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.button_events)
    void onEventsClicked() {
        Intent intent = new Intent(MainActivity.this, NotificationsActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.button_report)
    void onReportClicked() {
        Intent intent = new Intent(MainActivity.this, ReportActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.button_calendar)
    void onCalendarClicked() {
        Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.button_mk)
    void onMkClicked() {
        Intent intent = new Intent(MainActivity.this, MkTabActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.button_expenses)
    void onExpensesClicked() {
        Intent intent = new Intent(MainActivity.this, ExpensesActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.button_about)
    void onAboutClicked() {
        Intent intent = new Intent(MainActivity.this, AboutActivity.class);
        intent.putExtra(Keys.Extras.EXTRA_ABOUT, mAboutText);
        startActivity(intent);
    }

    @OnClick(R.id.button_fb)
    void onFbClicked() {
        openBrowser(mLinkFb);
    }

    @OnClick(R.id.button_link)
    void onLinkClicked() {
        openBrowser(mLink);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                y1 = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                float x2 = event.getX();
                float deltaX = x2 - x1;
                if (deltaX > MIN_DISTANCE) {
                    // left2right
                    Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    break;
                } else if (deltaX < (0 - MIN_DISTANCE)) {
                    // right2left
                    Intent intent = new Intent(MainActivity.this, NotificationsActivity.class);
                    startActivity(intent);
                    break;
                }
// else {
//                    // consider as something else - a screen tap for example
//                }
                float y2 = event.getY();
                float deltaY = y2 - y1;
                if (deltaY > MIN_DISTANCE) {
                    // top2bottom
                    mSwipeLayout.close();
                } else if (deltaY < (0 - MIN_DISTANCE)) {
                    // bottom2top
                    mSwipeLayout.open();
                }
//                else {
//                    // consider as something else - a screen tap for example
//                }
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onBackPressed() {
        // Swipe down
        if (mSwipeOpened) {
            mSwipeLayout.close();
            mSwipeOpened = false;
            return;
        }
        if (mDoubleBackToExitPressedOnce) {
            super.onBackPressed();
            int pid = android.os.Process.myPid();
            android.os.Process.killProcess(pid);
            return;
        }
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
