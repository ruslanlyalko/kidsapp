package com.example.android.kidsapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

public class MainActivity extends AppCompatActivity {

    private static final String FB_LINK = "https://www.fb.com/snoopyagency";
    private static final String INST_LINK = "https://www.instagram.com/snoopyagency";

    Button buttonUser, buttonEvents, buttonZvit, buttonCalendar, buttonMk, buttonVyt;
    Button buttonSwipe, buttonAbout, buttonFB, buttonInst, buttonCall;
    TextView textSnoopy, textLink, textLinkDetails;
    SwipeLayout swipeLayout;

    boolean mDoubleBackToExitPressedOnce = false;
    boolean mSwipeOpened = false;


    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference mDatabaseRefCurrentUser;
    private ValueEventListener mUserListener;
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private boolean mLinkActive = false;
    private FirebaseUser currentUserAuth;
    private User mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        initializeReferences();

        // Enable connection without internet
        mDatabase.setPersistenceEnabled(true);

        initCurrentUser();

        initLink();

        initButtons();

        initSwipes();

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
                startActivity(new Intent(MainActivity.this, UserActivity.class));
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
                Intent intent = new Intent(MainActivity.this, ReportActivity.class);
                startActivity(intent);
            }
        });
        buttonCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
                intent.putExtra(Constants.EXTRA_IS_ADMIN, mCurrentUser.getUserIsAdmin());
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
                Intent intent = new Intent(MainActivity.this, CostsActivity.class);
                intent.putExtra(Constants.EXTRA_IS_ADMIN, mCurrentUser.getUserIsAdmin());
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

        buttonAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(R.string.dialog_about_title)
                        .setMessage(R.string.dialog_about_message)
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

    private void initLink() {
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
    }

    private void initCurrentUser() {

        currentUserAuth = mAuth.getCurrentUser();
        if (currentUserAuth != null) {
            mDatabaseRefCurrentUser = mDatabase.getReference(Constants.FIREBASE_REF_USERS).child(currentUserAuth.getUid());

            mUserListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    mCurrentUser = user;
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
    private void initializeReferences() {
        textSnoopy = (TextView) findViewById(R.id.text_snoopy);
        textLink = (TextView) findViewById(R.id.text_link);
        textLinkDetails = (TextView) findViewById(R.id.text_link_details);

        buttonUser = (Button) findViewById(R.id.button_user);
        buttonEvents = (Button) findViewById(R.id.button_events);
        buttonZvit = (Button) findViewById(R.id.button_report);
        buttonCalendar = (Button) findViewById(R.id.button_calendar);
        buttonMk = (Button) findViewById(R.id.button_mk);
        buttonVyt = (Button) findViewById(R.id.button_vyt);

        buttonSwipe = (Button) findViewById(R.id.button_swipe);
        buttonAbout = (Button) findViewById(R.id.button_about);
        buttonInst = (Button) findViewById(R.id.button_inst);
        buttonFB = (Button) findViewById(R.id.button_fb);
        buttonCall = (Button) findViewById(R.id.button_call);
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
     * When user press Back button
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


    @Override
    protected void onResume() {
        super.onResume();

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            finish();
        }
    }
}
