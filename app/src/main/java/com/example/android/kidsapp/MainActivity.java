package com.example.android.kidsapp;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.kidsapp.utils.SwipeLayout;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private static final String FB_LINK = "https://www.fb.com/snoopyagency";
    private static final String INST_LINK = "https://www.instagram.com/snoopyagency";

    Button buttonUser, buttonEvents, buttonZvit, buttonCalendar, buttonMk, buttonVyt, buttonSwipe, buttonFB, buttonInst, buttonCall;
    TextView textSnoopy, textLink, textLinkDetails;
    SwipeLayout swipeLayout;

    boolean mDoubleBackToExitPressedOnce = false;
    boolean mSwipeOpened = false;

    //private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private boolean mLinkActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        initializeReferences();

        // Enable connection without internet
        mDatabase.setPersistenceEnabled(true);


        textSnoopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLinkActive) {
                    mLinkActive = false;
                    textLink.setText("");
                    textLinkDetails.setText("");
                } else {
                    mLinkActive = true;
                    textLink.setText("Відвідайте нашу сторінку у ФБ!");
                    textLinkDetails.setText("Докладніше >");
                }
            }
        });

        textLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLinkActive) openBrowser(FB_LINK);
            }
        });
        textLinkDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLinkActive) openBrowser(FB_LINK);
            }
        });


        // User & Events buttons
        buttonUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });
        buttonEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //   startActivity(new Intent(MainActivity.this, EventsActivity.class));
            }
        });
        // Main Buttons
        buttonZvit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ZvitActivity.class);
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
                Intent intent = new Intent(MainActivity.this, MkActivity.class);
                startActivity(intent);
            }
        });
        buttonVyt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VytActivity.class);
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


        // Swipe
        swipeLayout.setSwipeEnabled(false);
        swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
            @Override
            public void onStartOpen(SwipeLayout layout) {
                buttonSwipe.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_down2, 0, 0);
            }

            @Override
            public void onOpen(SwipeLayout layout) {
                mSwipeOpened = true;
            }

            @Override
            public void onStartClose(SwipeLayout layout) {
                buttonSwipe.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_up2, 0, 0);
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

        //

        buttonFB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBrowser(FB_LINK);
            }
        });

        buttonInst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBrowser(INST_LINK);
            }
        });

        buttonCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:0681990655"));
                startActivity(callIntent);
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
    private void initializeReferences() {
        textSnoopy = (TextView) findViewById(R.id.text_snoopy);
        textLink = (TextView) findViewById(R.id.text_link);
        textLinkDetails = (TextView) findViewById(R.id.text_link_details);

        buttonUser = (Button) findViewById(R.id.button_user);
        buttonEvents = (Button) findViewById(R.id.button_events);
        buttonZvit = (Button) findViewById(R.id.button_zvit);
        buttonCalendar = (Button) findViewById(R.id.button_calendar);
        buttonMk = (Button) findViewById(R.id.button_mk);
        buttonVyt = (Button) findViewById(R.id.button_vyt);

        buttonSwipe = (Button) findViewById(R.id.button_swipe);
        buttonInst = (Button) findViewById(R.id.button_inst);
        buttonFB = (Button) findViewById(R.id.button_fb);
        buttonCall = (Button) findViewById(R.id.button_call);
        swipeLayout = (SwipeLayout) findViewById(R.id.swipe_layout);

    }

    /**
     * When user press Back button
     * close swipe and show message - double click to exit
     */
    @Override
    public void onBackPressed() {
        if (mDoubleBackToExitPressedOnce) {
            super.onBackPressed();
            // TODO (kostul) close application completely
            int pid = android.os.Process.myPid();
            android.os.Process.killProcess(pid);
            return;
        }

        // Swipe down
        if (mSwipeOpened) {
            swipeLayout.close();
            mSwipeOpened = false;
            return;
        }

        // Close app after twice click on Back button
        mDoubleBackToExitPressedOnce = true;
        Toast.makeText(this, R.string.hint_double_press, Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                mDoubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

}
