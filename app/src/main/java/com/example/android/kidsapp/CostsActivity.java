package com.example.android.kidsapp;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.example.android.kidsapp.utils.Constants;
import com.example.android.kidsapp.utils.Cost;
import com.example.android.kidsapp.utils.CostsAdapter;
import com.example.android.kidsapp.utils.Mk;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CostsActivity extends AppCompatActivity {

    RecyclerView recyclerView;

    private CostsAdapter adapter;
    private List<Cost> costList = new ArrayList<>();
    private Boolean isFabOpen = false;

    private FloatingActionButton fab, fab1, fab2;
    private Animation fab_open, fab_close, rotate_forward, rotate_backward, fade, fade_back;
    private TextView textFab1, textFab2;
    private View fadedBeckground;
    private FirebaseAuth mAuth= FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        setContentView(R.layout.activity_costs);

        initRef();

       // costList.add(new Cost("123", "Сок", "гарний", "12.07", "", "", 125));

        initRecycle();

        initFAB();

        loadCosts();
    }

    private void initRef() {
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab1 = (FloatingActionButton) findViewById(R.id.fab1);
        fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        textFab1 = (TextView) findViewById(R.id.textFab1);
        textFab2 = (TextView) findViewById(R.id.textFab2);
        fadedBeckground = (View) findViewById(R.id.fadedbackgroud);

    }


    private void initRecycle() {

        adapter = new CostsAdapter(this, costList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

    }

    private void loadCosts() {
        costList.clear();

        // TODO CHECK REFERENCES year / month / id
        FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_REF_COSTS).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Cost cost = dataSnapshot.getValue(Cost.class);
                if (cost != null) {
                    costList.add(0, cost);
                    adapter.notifyDataSetChanged();
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

    private void addCost(Cost newCost) {

        String yearStr = new SimpleDateFormat("yyyy", Locale.US).format(new Date()).toString();
        String monthStr = new SimpleDateFormat("M", Locale.US).format(new Date()).toString();

        String key = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_REF_COSTS)
                .child(yearStr)
                .child(monthStr)
                .push().getKey();

        newCost.setKey(key);

        FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_REF_COSTS)
                .child(key).setValue(newCost);
    }

    private void initFAB() {
        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_backward);
        fade = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade);
        fade_back = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_back);

        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateFAB();
                addCoastDialog("Загальне");
                adapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(0);
            }
        });
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateFAB();
                addCoastDialog("На МК");
                adapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(0);
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateFAB();
            }
        });


        fadedBeckground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateFAB();
            }
        });

    }

    private void addCoastDialog(String title2){

        // TODO SHOW DIALOG
        addCost(new Cost("", "Щось2",title2,
                new SimpleDateFormat("d.M.yyyy", Locale.US).format(new Date()).toString(),
                mAuth.getCurrentUser().getUid(),
                mAuth.getCurrentUser().getDisplayName(),
                255));
    }
    public void animateFAB() {

        if (isFabOpen) {

            fab.startAnimation(rotate_backward);
            fab1.startAnimation(fab_close);
            fab2.startAnimation(fab_close);
            fab1.setClickable(false);
            fab2.setClickable(false);

            textFab1.startAnimation(fab_close);
            textFab2.startAnimation(fab_close);
//            body.startAnimation(fade_back);


            fadedBeckground.setClickable(false);
            fadedBeckground.startAnimation(fade_back);

            isFabOpen = false;

        } else {

            fab.startAnimation(rotate_forward);
            fab1.startAnimation(fab_open);
            fab2.startAnimation(fab_open);
            textFab1.startAnimation(fab_open);
            textFab2.startAnimation(fab_open);
            fab1.setClickable(true);
            fab2.setClickable(true);
//            body.startAnimation(fade);

            fadedBeckground.setClickable(true);
            fadedBeckground.startAnimation(fade);
            isFabOpen = true;
        }
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
