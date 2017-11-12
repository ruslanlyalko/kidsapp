package com.ruslanlyalko.kidsapp.presentation.ui.notifications;

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
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.ruslanlyalko.kidsapp.R;
import com.ruslanlyalko.kidsapp.common.Constants;
import com.ruslanlyalko.kidsapp.common.Keys;
import com.ruslanlyalko.kidsapp.data.Utils;
import com.ruslanlyalko.kidsapp.data.configuration.DefaultConfigurations;
import com.ruslanlyalko.kidsapp.data.models.Notification;

public class NotificationDetailsActivity extends AppCompatActivity {

    //VIEWS
    private Toolbar toolbar;
    private CollapsingToolbarLayout toolbarLayout;
    private TextView textDescription;//, textTitle2;
    private ImageView imageView;
    private FloatingActionButton fab;

    // VARIABLES
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    String notKey;
    Notification notification;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_details);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            notKey = bundle.getString(Keys.Extras.EXTRA_NOT_ID);
        }
        initRef();
        loadNotFromDB();
    }

    private void loadNotFromDB() {
        if (notKey == null || notKey.isEmpty()) return;
        DatabaseReference ref = database.getReference(DefaultConfigurations.DB_NOTIFICATIONS).child(notKey);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                notification = dataSnapshot.getValue(Notification.class);
                updateUI();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void updateUI() {
        if (notification != null) {
            if (mMenu != null) {
                mMenu.findItem(R.id.action_delete).setVisible(Utils.isAdmin()
                        || notification.getUserId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()));
                mMenu.findItem(R.id.action_edit).setVisible(Utils.isAdmin()
                        || notification.getUserId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()));
            }
            toolbarLayout.setTitle(notification.getTitle1());
            //textTitle2.setText(notification.getTitle2());
            textDescription.setText(notification.getDescription());
            fab.setVisibility((notification.getLink() != null && !notification.getLink().isEmpty())
                    ? View.VISIBLE : View.GONE);
/*
            if (notification.getImageUri() != null && !notification.getImageUri().isEmpty()) {

                StorageReference ref = storage.getReference(Constants.STORAGE_MK).child(notification.getImageUri());
                Glide.with(this).using(new FirebaseImageLoader()).load(ref).into(imageView);
            }*/
        } else {
            // notification == null
            toolbar.setTitle(R.string.title_activity_notification_item);
        }
    }

    private void initRef() {
        toolbarLayout = findViewById(R.id.toolbar_layout);
        textDescription = findViewById(R.id.text_description);
        //textTitle2 =  findViewById(R.id.text_title2);
        imageView = findViewById(R.id.image_view);
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                CustomTabsIntent customTabsIntent = builder.build();
                builder.setToolbarColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
                customTabsIntent.launchUrl(NotificationDetailsActivity.this, Uri.parse(notification.getLink()));
            }
        });
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
        mMenu = menu;
        if (notification != null) {
            mMenu.findItem(R.id.action_delete).setVisible(Utils.isAdmin()
                    || notification.getUserId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()));
            mMenu.findItem(R.id.action_edit).setVisible(Utils.isAdmin()
                    || notification.getUserId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        if (id == R.id.action_edit) {
            if (Utils.isAdmin()
                    || notification.getUserId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                editMk();
                // onBackPressed();
            }
        }
        if (id == R.id.action_delete) {
            if (Utils.isAdmin()
                    || notification.getUserId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                deleteMk();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void editMk() {
        Intent intent = new Intent(NotificationDetailsActivity.this, NotificationsEditActivity.class);
        intent.putExtra(Keys.Extras.EXTRA_ITEM_ID, notKey);
        startActivityForResult(intent, Constants.REQUEST_CODE_EDIT);
    }

    private void deleteMk() {
        AlertDialog.Builder builder = new AlertDialog.Builder(NotificationDetailsActivity.this);
        AlertDialog dialog = builder.setTitle(R.string.dialog_delete_notification_title)
                .setMessage(R.string.dialog_delete_notification_message)
                .setPositiveButton("Видалити", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        database.getReference(DefaultConfigurations.DB_NOTIFICATIONS).child(notification.getKey()).removeValue();
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
