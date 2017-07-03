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

    private static final String FB_LINK = "https://www.fb.com/snoopyagency" ;
    private static final String INST_LINK = "https://www.instagram.com/snoopyagency" ;

    Button btnUser, btnEvents, btnOrder, btnCalendar, btnTeam, btnRequisites, btnSwipe, btnFB, btnInst, btnCall;
    TextView txtSnoopy, txtLink, getTxtLinkDetails;
    SwipeLayout swipeLayout;

    boolean doubleBackToExitPressedOnce = false;
    boolean swipeOpened = false;

    //private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private boolean linkActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        initializeReferences();

        // Enable connection without internet
        mDatabase.setPersistenceEnabled(true);


        txtSnoopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (linkActive) {
                    txtSnoopy.setText("KIDS APP");
                    linkActive = false;
                    txtLink.setText("");
                    getTxtLinkDetails.setText("");
                } else {
                    txtSnoopy.setText("КІДС АПП");
                    linkActive = true;
                    txtLink.setText("Відвідайте нашу сторінку у ФБ!");
                    getTxtLinkDetails.setText("Докладніше >");
                }
            }
        });

        txtLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(linkActive) openBrowser(FB_LINK);
            }
        });
        getTxtLinkDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {if(linkActive) openBrowser(FB_LINK);
            }
        });


        // User & Events buttons
        btnUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });
        btnEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             //   startActivity(new Intent(MainActivity.this, EventsActivity.class));
            }
        });
        // Main Buttons
        btnOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // Intent intent = new Intent(MainActivity.this, OrderActivity.class);
                //startActivity(intent);
            }
        });
        btnCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
                //startActivity(intent);
            }
        });
        btnTeam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(MainActivity.this, TeamActivity.class);
                //startActivity(intent);
            }
        });
        btnRequisites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(MainActivity.this, ReqActivity.class);
                //startActivity(intent);
            }
        });

        // Swipe
        swipeLayout.setSwipeEnabled(false);
        swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
            @Override
            public void onStartOpen(SwipeLayout layout) {
                btnSwipe.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_down, 0, 0);
            }

            @Override
            public void onOpen(SwipeLayout layout) {
                swipeOpened = true;
            }

            @Override
            public void onStartClose(SwipeLayout layout) {
                btnSwipe.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_up, 0, 0);
            }

            @Override
            public void onClose(SwipeLayout layout) {
                swipeOpened = false;
            }

            @Override
            public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {

            }

            @Override
            public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {

            }
        });

        btnSwipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (swipeOpened)
                    swipeLayout.close();
                else
                    swipeLayout.open();

            }
        });

        //

        btnFB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBrowser(FB_LINK);
            }
        });

        btnInst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBrowser(INST_LINK);
            }
        });

        btnCall.setOnClickListener(new View.OnClickListener() {
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
        txtSnoopy = (TextView) findViewById(R.id.txtSnoopy);
        txtLink = (TextView) findViewById(R.id.txtLink);
        getTxtLinkDetails = (TextView) findViewById(R.id.txtLinkDetails);

        btnUser = (Button) findViewById(R.id.btnUser);
        btnEvents = (Button) findViewById(R.id.btnEvents);
        btnOrder = (Button) findViewById(R.id.btnOrder);
        btnCalendar = (Button) findViewById(R.id.btnCalendar);
        btnTeam = (Button) findViewById(R.id.btnTeam);
        btnRequisites = (Button) findViewById(R.id.btnRequisites);
        btnSwipe = (Button) findViewById(R.id.btnSwipe);
        btnInst= (Button) findViewById(R.id.btnInst);
        btnFB= (Button) findViewById(R.id.btnFb);
        btnCall= (Button) findViewById(R.id.btnCall);
        swipeLayout = (SwipeLayout) findViewById(R.id.godfather);

    }

    /**
     * When user press Back button
     * close swipe and show message - double click to exit
     */
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            // TODO (kostul) close application completely
            int pid = android.os.Process.myPid();
            android.os.Process.killProcess(pid);
            return;
        }

        // Swipe down
        if (swipeOpened) {
            swipeLayout.close();
            swipeOpened = false;
            return;
        }

        // Close app after twice click on Back button
        doubleBackToExitPressedOnce = true;
        Toast.makeText(this, R.string.hint_double_press, Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

}
