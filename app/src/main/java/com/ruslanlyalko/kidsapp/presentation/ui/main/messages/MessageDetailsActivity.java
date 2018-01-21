package com.ruslanlyalko.kidsapp.presentation.ui.main.messages;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ruslanlyalko.kidsapp.R;
import com.ruslanlyalko.kidsapp.common.Constants;
import com.ruslanlyalko.kidsapp.common.Keys;
import com.ruslanlyalko.kidsapp.data.FirebaseUtils;
import com.ruslanlyalko.kidsapp.data.configuration.DefaultConfigurations;
import com.ruslanlyalko.kidsapp.data.models.Message;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MessageDetailsActivity extends AppCompatActivity {

    @BindView(R.id.text_description) TextView textDescription;
    @BindView(R.id.fab) FloatingActionButton fab;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
    private String notKey;
    private Message mMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_details);
        ButterKnife.bind(this);
        parseExtras();
        FirebaseUtils.markNotificationsAsRead(notKey);
        loadNotFromDB();
    }

    private void parseExtras() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            notKey = bundle.getString(Keys.Extras.EXTRA_NOT_ID);
        }
    }

    private void loadNotFromDB() {
        if (notKey == null || notKey.isEmpty()) return;
        DatabaseReference ref = database.getReference(DefaultConfigurations.DB_MESSAGES).child(notKey);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mMessage = dataSnapshot.getValue(Message.class);
                updateUI();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void updateUI() {
        if (mMessage != null) {
            invalidateOptionsMenu();
            getSupportActionBar().setTitle(mMessage.getTitle1());
            textDescription.setText(mMessage.getDescription());
            fab.setVisibility((mMessage.getLink() != null && !mMessage.getLink().isEmpty())
                    ? View.VISIBLE : View.GONE);
        } else {
            getSupportActionBar().setTitle(R.string.title_activity_notification_item);
        }
    }

    @OnClick(R.id.fab)
    void onFabClicked() {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabsIntent = builder.build();
        builder.setToolbarColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
        customTabsIntent.launchUrl(MessageDetailsActivity.this, Uri.parse(mMessage.getLink()));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_CODE_EDIT) {
            loadNotFromDB();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_item, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mMessage != null) {
            menu.findItem(R.id.action_delete).setVisible(FirebaseUtils.isAdmin()
                    || mMessage.getUserId().equals(mUser.getUid()));
            menu.findItem(R.id.action_edit).setVisible(FirebaseUtils.isAdmin()
                    || mMessage.getUserId().equals(mUser.getUid()));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_edit:
                if (FirebaseUtils.isAdmin()
                        || mMessage.getUserId().equals(mUser.getUid())) {
                    editMk();
                }
                break;
            case R.id.action_delete:
                if (FirebaseUtils.isAdmin()
                        || mMessage.getUserId().equals(mUser.getUid())) {
                    deleteMk();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void editMk() {
        Intent intent = new Intent(MessageDetailsActivity.this, MessageEditActivity.class);
        intent.putExtra(Keys.Extras.EXTRA_ITEM_ID, notKey);
        startActivityForResult(intent, Constants.REQUEST_CODE_EDIT);
    }

    private void deleteMk() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MessageDetailsActivity.this);
        AlertDialog dialog = builder.setTitle(R.string.dialog_delete_notification_title)
                .setMessage(R.string.dialog_delete_notification_message)
                .setPositiveButton("Видалити", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        database.getReference(DefaultConfigurations.DB_MESSAGES).child(mMessage.getKey()).removeValue();
                        FirebaseUtils.clearNotificationsForAllUsers(mMessage.getKey());
                        onBackPressed();
                    }
                })
                .setNegativeButton("Повернутись", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                }).create();
        dialog.show();
    }
}
