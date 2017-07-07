package com.example.android.kidsapp;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.android.kidsapp.utils.SwipeLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

public class ZvitActivity extends AppCompatActivity {

    private static final String TAG = ZvitActivity.class.getSimpleName();

    int roomTotal, room60, room40, room20;
    int bdayTotal, bday50, bday25, bdayMk;
    int mkTotal, mkT1, mkT2, mk1, mk2;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private FirebaseStorage mStorage = FirebaseStorage.getInstance();

    private DatabaseReference mDatabaseRefCurrentUser;
    private ValueEventListener mUserListener;


    TextView textRoom60, textRoom40, textRoom20, textRoomTotal;
    TextView textBday50, textBday25, textBdayTotal, textBdayMk;
    TextView textMk1, textMk2, textMkT1, textMkT2, textMkTotal;

    SeekBar seekRoom60, seekRoom40, seekRoom20;
    SeekBar seekBday50, seekBday25, seekBdayMk;
    SeekBar seekMkT1, seekMkT2, seekMk1, seekMk2;

    EditText inputRoom60, inputRoom40, inputRoom20;
    SwipeLayout swipeLayout, swipeLayout2, swipeLayout3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
        setContentView(R.layout.activity_zvit);

        initializeReferences();


        swipeLayout2.addDrag(SwipeLayout.DragEdge.Right, R.id.swipe_menu2);
        swipeLayout2.setRightSwipeEnabled(true);
        swipeLayout2.setBottomSwipeEnabled(false);

        swipeLayout3.addDrag(SwipeLayout.DragEdge.Right, R.id.swipe_menu3);
        swipeLayout3.setRightSwipeEnabled(true);
        swipeLayout3.setBottomSwipeEnabled(false);

        swipeLayout.addDrag(SwipeLayout.DragEdge.Right, R.id.swipe_menu);
        swipeLayout.setRightSwipeEnabled(true);
        swipeLayout.setBottomSwipeEnabled(false);


        seekRoom60.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                room60 = progress;
                updateRoomTotal();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekRoom40.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                room40 = progress;
                updateRoomTotal();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekRoom20.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                room20 = progress;
                updateRoomTotal();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        seekBday50.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                bday50 = progress;
                updateBdayTotal();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBday25.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                bday25 = progress;
                updateBdayTotal();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBdayMk.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                bdayMk = progress;
                textBdayMk.setText("Проведено Майстер Класів - " + bdayMk);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // MK

        seekMkT1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mkT1 = progress;
                updateMkTotal();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekMkT2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mkT2 = progress;
                updateMkTotal();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekMk1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mk1 = progress;
                updateMkTotal();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekMk2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mk2 = progress;
                updateMkTotal();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    void updateRoomTotal() {
        textRoom60.setText("60грн х " + room60 + " = " + (room60 * 60) + " ГРН");
        textRoom40.setText("40грн х " + room40 + " = " + (room40 * 40) + " ГРН");
        textRoom20.setText("20грн х " + room20 + " = " + (room20 * 20) + " ГРН");

        inputRoom60.setText(String.valueOf(room60));
        inputRoom40.setText(String.valueOf(room40));
        inputRoom20.setText(String.valueOf(room20));

        roomTotal = room60 * 60 + room40 * 40 + room20 * 20;

        textRoomTotal.setText((roomTotal) + " ГРН");

        updateTitel();
    }

    void updateBdayTotal() {
        if (bday25 > 0)
            bdayMk = 1;
        else
            bdayMk = 0;
        if (bday25 > 10)
            bdayMk = 2;

        if (bday25 > 20)
            bdayMk = 3;
        seekBdayMk.setProgress(bdayMk);

        textBday50.setText("50грн х " + bday50 + " = " + (bday50 * 50) + " ГРН");
        textBday25.setText("25грн х " + bday25 + " = " + (bday25 * 25) + " ГРН");
        textBdayMk.setText("Проведено Майстер Класів - " + bdayMk);

        bdayTotal = bday50 * 50 + bday25 * 25;

        textBdayTotal.setText((bdayTotal) + " ГРН");

        updateTitel();
    }

    void updateMkTotal() {
        int tar1 = 30 + mkT1 * 10;
        int tar2 = 30 + mkT2 * 10;

        mkTotal = tar1 * mk1 + tar2 * mk2;

        textMkT1.setText("Тариф 1:  " + tar1 + " грн  x");
        textMkT2.setText("Тариф 2:  " + tar2 + " грн  x");

        textMk1.setText("  " + mk1 + " = " + (tar1 * mk1) + " ГРН");
        textMk2.setText("  " + mk2 + " = " + (tar2 * mk2) + " ГРН");

        textMkTotal.setText(mkTotal + " ГРН");
        updateTitel();
    }

    void updateTitel() {
        String activityName = getResources().getString(R.string.title_activity_zvit);
        setTitle(activityName + " (" + (roomTotal + bdayTotal + mkTotal) + " ГРН)");
    }

    private void initializeReferences() {

        // Room
        swipeLayout = (SwipeLayout) findViewById(R.id.swipe_layout);
        textRoomTotal = (TextView) findViewById(R.id.text_room_total);
        textRoom60 = (TextView) findViewById(R.id.text_room_60);
        textRoom40 = (TextView) findViewById(R.id.text_room_40);
        textRoom20 = (TextView) findViewById(R.id.text_room_20);

        seekRoom60 = (SeekBar) findViewById(R.id.seek_room_60);
        seekRoom40 = (SeekBar) findViewById(R.id.seek_room_40);
        seekRoom20 = (SeekBar) findViewById(R.id.seek_room_20);

        inputRoom60 = (EditText) findViewById(R.id.input_room_60);
        inputRoom40 = (EditText) findViewById(R.id.input_room_40);
        inputRoom20 = (EditText) findViewById(R.id.input_room_20);


        // BirthDay
        swipeLayout2 = (SwipeLayout) findViewById(R.id.swipe_layout2);
        textBdayTotal = (TextView) findViewById(R.id.text_bday_total);
        textBday50 = (TextView) findViewById(R.id.text_bday_50);
        textBday25 = (TextView) findViewById(R.id.text_bday_25);
        textBdayMk = (TextView) findViewById(R.id.text_bday_mk_done);

        seekBday50 = (SeekBar) findViewById(R.id.seek_bday_50);
        seekBday25 = (SeekBar) findViewById(R.id.seek_bday_25);
        seekBdayMk = (SeekBar) findViewById(R.id.seek_bday_mk_done);

        // MK
        swipeLayout3 = (SwipeLayout) findViewById(R.id.swipe_layout3);

        seekMk1 = (SeekBar) findViewById(R.id.seek_mk_1);
        seekMk2 = (SeekBar) findViewById(R.id.seek_mk_2);
        seekMkT1 = (SeekBar) findViewById(R.id.seek_mk_t1);
        seekMkT2 = (SeekBar) findViewById(R.id.seek_mk_t2);

        textMkTotal = (TextView) findViewById(R.id.text_mk_total);
        textMk1 = (TextView) findViewById(R.id.text_mk_1);
        textMk2 = (TextView) findViewById(R.id.text_mk_2);
        textMkT1 = (TextView) findViewById(R.id.text_mk_t1);
        textMkT2 = (TextView) findViewById(R.id.text_mk_t2);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_zvit, menu);
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
            case R.id.action_add: {
                //todo add zvit
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
    }
}
