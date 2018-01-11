package com.ruslanlyalko.kidsapp.presentation.ui.profile;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.ruslanlyalko.kidsapp.R;
import com.ruslanlyalko.kidsapp.common.DateUtils;
import com.ruslanlyalko.kidsapp.common.Keys;
import com.ruslanlyalko.kidsapp.data.Utils;
import com.ruslanlyalko.kidsapp.data.configuration.DefaultConfigurations;
import com.ruslanlyalko.kidsapp.data.models.User;
import com.ruslanlyalko.kidsapp.presentation.ui.login.LoginActivity;
import com.ruslanlyalko.kidsapp.presentation.ui.login.SignupActivity;
import com.ruslanlyalko.kidsapp.presentation.ui.profile.adapter.UsersAdapter;
import com.ruslanlyalko.kidsapp.presentation.ui.profile.dashboard.DashboardActivity;
import com.ruslanlyalko.kidsapp.presentation.ui.profile.salary.SalaryActivity;
import com.ruslanlyalko.kidsapp.presentation.ui.profile.settings.ProfileSettingsActivity;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    ImageView imageUser1, imageUserLogo;
    TextView textEmail, textPhone, textBDay, textCard;
    TextView textTitleName, textTitlePosition, textTime;
    TextView textFirstDate;
    LinearLayout panelFirstDate, panelPhoneCall, panelEmail, panelCard;
    FloatingActionButton fab;
    CardView cardFriends;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();

    private String mUID;
    private User mUser;
    private List<User> userList = new ArrayList<>();
    private UsersAdapter adapter;
    private RecyclerView recyclerView;
    private boolean needLoadFriends;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
        setContentView(R.layout.activity_profile);
        initCollapsingToolbar();
        initRef();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mUID = bundle.getString(Keys.Extras.EXTRA_UID, FirebaseAuth.getInstance().getCurrentUser().getUid());
        } else
            mUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        needLoadFriends = mUID.equals(mAuth.getCurrentUser().getUid());
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final boolean myPage = mUser.getUserId().equals(mAuth.getCurrentUser().getUid());
                if (Utils.isAdmin() && myPage) {
                    startActivity(DashboardActivity.getLaunchIntent(ProfileActivity.this));
                } else {
                    startActivity(SalaryActivity.getLaunchIntent(ProfileActivity.this, mUID, mUser));
                }
            }
        });
        initRecycle();
        loadUsers();
    }

    private void initCollapsingToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final CollapsingToolbarLayout collapsingToolbar =
                findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(" ");
        AppBarLayout appBarLayout = findViewById(R.id.appbar);
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
                    fab.hide();
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbar.setTitle(" ");
                    fab.show();
                    isShow = false;
                }
            }
        });
    }

    private void initRef() {
        panelFirstDate = findViewById(R.id.panel_first_date);
        panelPhoneCall = findViewById(R.id.panel_phone);
        panelEmail = findViewById(R.id.panel_email);
        panelCard = findViewById(R.id.panel_card);
        imageUserLogo = findViewById(R.id.image_user_logo);
        imageUser1 = findViewById(R.id.image_user1);
        textTitleName = findViewById(R.id.text_title_name);
        textTitlePosition = findViewById(R.id.text_position_title);
        textTime = findViewById(R.id.text_time);
        textPhone = findViewById(R.id.text_phone);
        textEmail = findViewById(R.id.text_email);
        textBDay = findViewById(R.id.text_bday);
        textCard = findViewById(R.id.text_card);
        textFirstDate = findViewById(R.id.text_first_date);
        fab = findViewById(R.id.fab);
        cardFriends = (CardView) findViewById(R.id.card_friends);
        recyclerView = findViewById(R.id.recycler_view);
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
        mDatabase.getReference(DefaultConfigurations.DB_USERS)
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
                        User user = dataSnapshot.getValue(User.class);
                        if (user != null) {
                            if (user.getUserId().equals(mUID)) {
                                mUser = user;
                                updateUI(user);
                            }
                        }
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

    private void updateUI(User user) {
        if (user == null) return;
        final boolean myPage = mUser.getUserId().equals(mAuth.getCurrentUser().getUid());
        // if current user is admin or open his friends
        fab.setVisibility(Utils.isAdmin() || myPage ? View.VISIBLE : View.GONE);
        if (mUser.getUserIsAdmin() && myPage)
            fab.setImageResource(R.drawable.ic_action_money);
        textTitleName.setText(user.getUserName());
        textTitlePosition.setText(user.getUserPositionTitle());
        textPhone.setText(user.getUserPhone());
        textEmail.setText(user.getUserEmail());
        textBDay.setText(user.getUserBDay());
        textCard.setText(user.getUserCard());
        textTime.setText(user.getUserTimeStart() + " - " + user.getUserTimeEnd());
        textFirstDate.setText(user.getUserFirstDate());
        final String phone = user.getUserPhone();
        panelPhoneCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + phone));
                startActivity(callIntent);
            }
        });
        final String email = user.getUserEmail();
        panelEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(email, email);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(ProfileActivity.this, getString(R.string.text_copied), Toast.LENGTH_SHORT).show();
            }
        });
        final String card = user.getUserCard();
        panelCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(card, card);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(ProfileActivity.this, getString(R.string.text_copied), Toast.LENGTH_SHORT).show();
            }
        });
        if (Utils.isAdmin() && !user.getUserId().equals(mAuth.getCurrentUser().getUid())) {
            panelFirstDate.setVisibility(View.VISIBLE);
        }


       /* StorageReference storageRef = storage.getReference(Constants.STORAGE_PICTURES)
                .child("cover5.png");
        Glide.with(ProfileActivity.this).using(new FirebaseImageLoader()).load(storageRef).into(imageUser1);
*/
        // draw First Letters from UserName
        Bitmap bitmap = Bitmap.createBitmap(70, 70, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        String textToDraw = DateUtils.getFirstLetters(user.getUserName());
        Paint paint = new Paint();
        paint.setColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
        paint.setTextSize(32);
        canvas.drawText(textToDraw, 15, 46, paint);
        imageUserLogo.setImageBitmap(bitmap);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_user, menu);
        boolean isCurrentUserPage = mUID.equals(mAuth.getCurrentUser().getUid());
        menu.findItem(R.id.action_add_user).setVisible(Utils.isAdmin() && isCurrentUserPage);
        menu.findItem(R.id.action_settings).setVisible(Utils.isAdmin() || isCurrentUserPage);
        menu.findItem(R.id.action_logout).setVisible(isCurrentUserPage);
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
                startActivity(new Intent(ProfileActivity.this, SignupActivity.class));
                return true;
            }
            case R.id.action_settings: {
                Intent intent = new Intent(ProfileActivity.this, ProfileSettingsActivity.class);
                intent.putExtra(Keys.Extras.EXTRA_UID, mUID);
                startActivity(intent);
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

    private void logout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_logout_title)
                .setMessage(R.string.dialog_logout_message)
                .setPositiveButton("Вийти", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mAuth.signOut();
                        Intent intent = new Intent(ProfileActivity.this,
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
}