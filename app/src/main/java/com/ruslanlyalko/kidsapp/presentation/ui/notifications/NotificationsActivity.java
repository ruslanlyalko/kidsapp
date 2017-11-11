package com.ruslanlyalko.kidsapp.presentation.ui.notifications;

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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.ruslanlyalko.kidsapp.R;
import com.ruslanlyalko.kidsapp.data.configuration.DefaultConfigurations;
import com.ruslanlyalko.kidsapp.data.models.Notification;
import com.ruslanlyalko.kidsapp.presentation.ui.notifications.adapter.NotificationsAdapter;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationsAdapter adapter;
    private List<Notification> notificationList;

    private FloatingActionButton fab;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        setContentView(R.layout.activity_notification);
        initRef();
        notificationList = new ArrayList<>();
        adapter = new NotificationsAdapter(this, notificationList);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        loadNotifications();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNotification();
            }
        });
    }

    private void addNotification() {
        Intent intent = new Intent(NotificationsActivity.this, NotificationsEditActivity.class);
        startActivity(intent);
    }

    private void loadNotifications() {
        notificationList.clear();
        FirebaseDatabase.getInstance().getReference(DefaultConfigurations.DB_NOTIFICATIONS)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Notification notification = dataSnapshot.getValue(Notification.class);
                        if (notification != null) {
                            notificationList.add(0, notification);
                            adapter.notifyItemInserted(0);
                            recyclerView.smoothScrollToPosition(0);
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        Notification notification = dataSnapshot.getValue(Notification.class);
                        updateNot(notification);
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        Notification notification = dataSnapshot.getValue(Notification.class);
                        removeNot(notification.getKey());
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    private void updateNot(Notification notification) {
        int ind = 0;
        for (Notification m : notificationList) {
            if (m.getKey().equals(notification.getKey())) {
                break;
            }
            ind++;
        }
        if (ind < notificationList.size()) {
            notificationList.set(ind, notification);
            adapter.notifyItemChanged(ind);
            recyclerView.smoothScrollToPosition(ind);
        }
    }

    private void removeNot(String key) {
        int ind = 0;
        for (Notification m : notificationList) {
            if (m.getKey().equals(key)) {
                break;
            }
            ind++;
        }
        if (ind < notificationList.size()) {
            notificationList.remove(ind);
            adapter.notifyItemRemoved(ind);
        }
    }

    private void initRef() {
        fab = findViewById(R.id.fab);
        recyclerView = findViewById(R.id.recycler_view);
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
