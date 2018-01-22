package com.ruslanlyalko.kidsapp.presentation.ui.main.messages;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ruslanlyalko.kidsapp.R;
import com.ruslanlyalko.kidsapp.data.FirebaseUtils;
import com.ruslanlyalko.kidsapp.data.configuration.DefaultConfigurations;
import com.ruslanlyalko.kidsapp.data.models.Message;
import com.ruslanlyalko.kidsapp.data.models.Notification;
import com.ruslanlyalko.kidsapp.presentation.ui.main.messages.adapter.MessagesAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MessagesActivity extends AppCompatActivity {

    @BindView(R.id.fab) FloatingActionButton fab;
    @BindView(R.id.recycler_view) RecyclerView mMessagesList;

    private MessagesAdapter mMessagesAdapter;
    private List<Message> mMessageList = new ArrayList<>();
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();

    public static Intent getLaunchIntent(final Activity launchIntent) {
        return new Intent(launchIntent, MessagesActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        setContentView(R.layout.activity_message);
        ButterKnife.bind(this);
        initRecycler();
        loadMessages();
        fab.setVisibility(FirebaseUtils.isAdmin() ? View.VISIBLE : View.GONE);
        loadBadge();
    }

    private void initRecycler() {
        mMessagesAdapter = new MessagesAdapter(this, mMessageList);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 1);
        mMessagesList.setLayoutManager(mLayoutManager);
        mMessagesList.setItemAnimator(new DefaultItemAnimator());
        mMessagesList.setAdapter(mMessagesAdapter);
    }

    private void loadMessages() {
        mMessageList.clear();
        FirebaseDatabase.getInstance().getReference(DefaultConfigurations.DB_MESSAGES)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Message message = dataSnapshot.getValue(Message.class);
                        if (message != null) {
                            mMessageList.add(0, message);
                            mMessagesAdapter.notifyItemInserted(0);
                            mMessagesList.smoothScrollToPosition(0);
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        Message message = dataSnapshot.getValue(Message.class);
                        updateNot(message);
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        Message message = dataSnapshot.getValue(Message.class);
                        removeNot(message.getKey());
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    private void loadBadge() {
        mDatabase.getReference(DefaultConfigurations.DB_USERS_NOTIFICATIONS)
                .child(mUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        List<Notification> notifications = new ArrayList<>();
                        for (DataSnapshot notifSS : dataSnapshot.getChildren()) {
                            Notification notification = notifSS.getValue(Notification.class);
                            notifications.add(notification);
                        }
                        mMessagesAdapter.updateNotifications(notifications);
                    }

                    @Override
                    public void onCancelled(final DatabaseError databaseError) {
                    }
                });
    }

    private void updateNot(Message message) {
        int ind = 0;
        for (Message m : mMessageList) {
            if (m.getKey().equals(message.getKey())) {
                break;
            }
            ind++;
        }
        if (ind < mMessageList.size()) {
            mMessageList.set(ind, message);
            mMessagesAdapter.notifyItemChanged(ind);
            mMessagesList.smoothScrollToPosition(ind);
        }
    }

    private void removeNot(String key) {
        int ind = 0;
        for (Message m : mMessageList) {
            if (m.getKey().equals(key)) {
                break;
            }
            ind++;
        }
        if (ind < mMessageList.size()) {
            mMessageList.remove(ind);
            mMessagesAdapter.notifyItemRemoved(ind);
        }
    }

    @OnClick(R.id.fab)
    void onFabCLicked() {
        addNotification();
    }

    private void addNotification() {
        Intent intent = new Intent(MessagesActivity.this, MessageEditActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
    }
}
