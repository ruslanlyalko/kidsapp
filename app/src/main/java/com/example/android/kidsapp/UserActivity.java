package com.example.android.kidsapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.kidsapp.utils.Constants;
import com.example.android.kidsapp.utils.User;
import com.example.android.kidsapp.utils.UsersAdapter;
import com.example.android.kidsapp.utils.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;


public class UserActivity extends AppCompatActivity {


    ImageView imageUser1, imageUserLogo;
    TextView textEmail, textPhone, textBDay, textCard;
    TextView textTitleName, textTitlePosition, textTime;
    FloatingActionButton fab;
    CardView cardFriends;


    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();

    private String mUID;
    private User mUser;
    private List<User> userList = new ArrayList<>();
    private UsersAdapter adapter;
    private RecyclerView recyclerView;
    private LinearLayout linearPhoneCall;
    private boolean needLoadFriends;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
        setContentView(R.layout.activity_user);


        initCollapsingToolbar();

        initRef();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mUID = bundle.getString(Constants.EXTRA_UID, FirebaseAuth.getInstance().getCurrentUser().getUid());
        } else
            mUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        needLoadFriends = mUID.equals(mAuth.getCurrentUser().getUid());

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final boolean myPage = mUser.getUserId().equals(mAuth.getCurrentUser().getUid());
                if (Utils.isIsAdmin() && myPage) {
                    Intent intent = new Intent(UserActivity.this, DashboardActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(UserActivity.this, SalaryActivity.class);
                    intent.putExtra(Constants.EXTRA_UID, mUID);
                    startActivity(intent);
                }
            }
        });


        initRecycle();

        loadUsers();
    }

    private void initRecycle() {

        if (needLoadFriends) {
            cardFriends.setVisibility(View.VISIBLE);
            adapter = new UsersAdapter(this, userList);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);
        } else {
            cardFriends.setVisibility(View.GONE);
        }

    }

    private void loadUsers() {

        userList.clear();
        if (needLoadFriends)
            adapter.notifyDataSetChanged();

        mDatabase.getReference(Constants.FIREBASE_REF_USERS)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        User user = dataSnapshot.getValue(User.class);
                        if (user != null) {
                            if (user.getUserId().equals(mUID)) {
                                mUser = user;
                                updateUI(user);
                            } else if (needLoadFriends) {
                                userList.add(0, user);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }


    private void initRef() {

        imageUserLogo = (ImageView) findViewById(R.id.image_user_logo);
        imageUser1 = (ImageView) findViewById(R.id.image_user1);

        textTitleName = (TextView) findViewById(R.id.text_title_name);
        textTitlePosition = (TextView) findViewById(R.id.text_position_title);
        textTime = (TextView) findViewById(R.id.text_time);

        textPhone = (TextView) findViewById(R.id.text_phone);
        textEmail = (TextView) findViewById(R.id.text_email);
        textBDay = (TextView) findViewById(R.id.text_bday);
        textCard = (TextView) findViewById(R.id.text_card);

        fab = (FloatingActionButton) findViewById(R.id.fab);

        cardFriends = (CardView) findViewById(R.id.card_friends);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        linearPhoneCall = (LinearLayout) findViewById(R.id.linear_phone_call);
    }

    private void initCollapsingToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(" ");
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        appBarLayout.setExpanded(true);

        // hiding & showing the title when toolbar expanded & collapsed
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbar.setTitle(mUser.getUserName());
                    fab.setVisibility(View.GONE);
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbar.setTitle(" ");
                    fab.setVisibility(View.VISIBLE);
                    isShow = false;
                }
            }
        });
    }


    private void updateUI(User user) {
        if (user == null) return;
        final boolean myPage = mUser.getUserId().equals(mAuth.getCurrentUser().getUid());

        // if current user is admin or open his friends
        fab.setVisibility(Utils.isIsAdmin() || myPage ? View.VISIBLE : View.GONE);

        textTitleName.setText(user.getUserName());
        textTitlePosition.setText(user.getUserPositionTitle());
        textPhone.setText(user.getUserPhone());
        textEmail.setText(user.getUserEmail());
        textBDay.setText(user.getUserBDay());
        textCard.setText(user.getUserCard());
        textTime.setText(user.getUserTimeStart() + " - " + user.getUserTimeEnd());

        final String phone = user.getUserPhone();
        linearPhoneCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + phone));
                startActivity(callIntent);
            }
        });

       /* StorageReference storageRef = storage.getReference(Constants.FIREBASE_STORAGE_PICTURES)
                .child("cover5.png");
        Glide.with(UserActivity.this).using(new FirebaseImageLoader()).load(storageRef).into(imageUser1);
*/
        // draw First Letters from UserName
        Bitmap bitmap = Bitmap.createBitmap(70, 70, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        String textToDraw = Utils.getFirstLetters(user.getUserName());
        Paint paint = new Paint();
        paint.setColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
        paint.setTextSize(32);
        canvas.drawText(textToDraw, 15, 46, paint);

        imageUserLogo.setImageBitmap(bitmap);
    }


    private void logout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_logout_title)
                .setMessage(R.string.dialog_logout_message)
                .setPositiveButton("Вийти", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mAuth.signOut();

                        Intent intent = new Intent(UserActivity.this,
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_user, menu);

        menu.findItem(R.id.action_add_user).setVisible(Utils.isIsAdmin());
        menu.findItem(R.id.action_settings).setVisible(Utils.isIsAdmin());
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

            case R.id.action_add_user: {
                startActivity(new Intent(UserActivity.this, SignupActivity.class));
                return true;
            }
            case R.id.action_settings: {
                // todo start activity settings
                return true;
            }
            case R.id.action_logout: {
                logout();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
    }
}