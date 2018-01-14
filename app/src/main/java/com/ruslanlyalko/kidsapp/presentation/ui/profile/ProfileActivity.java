package com.ruslanlyalko.kidsapp.presentation.ui.profile;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
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

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.ruslanlyalko.kidsapp.R;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ProfileActivity extends AppCompatActivity {

    @BindView(R.id.text_email) TextView mEmailText;
    @BindView(R.id.text_phone) TextView mPhoneText;
    @BindView(R.id.text_bday) TextView mBDayText;
    @BindView(R.id.text_card) TextView mCardText;
    @BindView(R.id.text_position_title) TextView mTitlePositionText;
    @BindView(R.id.text_time) TextView mTimeText;
    @BindView(R.id.text_first_date) TextView mFirstDateText;
    @BindView(R.id.panel_first_date) LinearLayout mFirsDateLayout;
    @BindView(R.id.panel_phone) LinearLayout mPhoneLayout;
    @BindView(R.id.panel_email) LinearLayout mEmailLayout;
    @BindView(R.id.panel_card) LinearLayout mCardLayout;
    @BindView(R.id.fab) FloatingActionButton fab;
    @BindView(R.id.card_friends) CardView mCardView;
    @BindView(R.id.recycler_view) RecyclerView mRecyclerView;
    @BindView(R.id.image_view_ava) ImageView mAvaImageView;
    @BindView(R.id.image_view_back) ImageView mBackImageView;

    @BindView(R.id.collapsing_toolbar) CollapsingToolbarLayout collapsingToolbar;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private String mUID;
    private User mUser;
    private List<User> userList = new ArrayList<>();
    private UsersAdapter adapter;
    private boolean needLoadFriends;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);
        parseExtras();
        initToolbar();
        initRecycle();
        loadUsers();
    }

    private void parseExtras() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mUID = bundle.getString(Keys.Extras.EXTRA_UID, FirebaseAuth.getInstance().getCurrentUser().getUid());
        } else
            mUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        needLoadFriends = mUID.equals(mAuth.getCurrentUser().getUid());
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initRecycle() {
        if (needLoadFriends) {
            mCardView.setVisibility(View.VISIBLE);
            adapter = new UsersAdapter(this, userList);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mRecyclerView.setAdapter(adapter);
        } else {
            mCardView.setVisibility(View.GONE);
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
        mTitlePositionText.setText(user.getUserPositionTitle());
        collapsingToolbar.setTitle(user.getUserName());
        mPhoneText.setText(user.getUserPhone());
        mEmailText.setText(user.getUserEmail());
        mBDayText.setText(user.getUserBDay());
        mCardText.setText(user.getUserCard());
        mTimeText.setText(user.getUserTimeStart() + " - " + user.getUserTimeEnd());
        mFirstDateText.setText(user.getUserFirstDate());
        final String phone = user.getUserPhone();
        mPhoneLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + phone));
                startActivity(callIntent);
            }
        });
        final String email = user.getUserEmail();
        mEmailLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(email, email);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(ProfileActivity.this, getString(R.string.text_copied), Toast.LENGTH_SHORT).show();
            }
        });
        final String card = user.getUserCard();
        mCardLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(card, card);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(ProfileActivity.this, getString(R.string.text_copied), Toast.LENGTH_SHORT).show();
            }
        });
        if (Utils.isAdmin() && !user.getUserId().equals(mAuth.getCurrentUser().getUid())) {
            mFirsDateLayout.setVisibility(View.VISIBLE);
        }
        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            mAvaImageView.setVisibility(View.VISIBLE);
            mBackImageView.setVisibility(View.VISIBLE);
            if (!isDestroyed())
                Glide.with(this).load(mUser.getAvatar()).into(mAvaImageView);
        } else {
            mAvaImageView.setVisibility(View.GONE);
            mBackImageView.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.fab)
    void onFabClicked() {
        final boolean myPage = mUser.getUserId().equals(mAuth.getCurrentUser().getUid());
        if (Utils.isAdmin() && myPage) {
            startActivity(DashboardActivity.getLaunchIntent(ProfileActivity.this));
        } else {
            startActivity(SalaryActivity.getLaunchIntent(ProfileActivity.this, mUID, mUser));
        }
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