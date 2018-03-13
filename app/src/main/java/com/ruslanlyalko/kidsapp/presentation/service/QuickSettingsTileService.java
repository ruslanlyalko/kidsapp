package com.ruslanlyalko.kidsapp.presentation.service;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ruslanlyalko.kidsapp.R;
import com.ruslanlyalko.kidsapp.common.DateUtils;
import com.ruslanlyalko.kidsapp.data.configuration.DefaultConfigurations;
import com.ruslanlyalko.kidsapp.data.models.Report;
import com.ruslanlyalko.kidsapp.data.models.User;
import com.ruslanlyalko.kidsapp.presentation.ui.splash.SplashActivity;

import java.util.Date;

/**
 * Created by Ruslan Lyalko
 * on 13.03.2018.
 */
@TargetApi(Build.VERSION_CODES.N)
public class QuickSettingsTileService extends TileService {

    private String mUId;
    private String mDateYear;
    private String mDateMonth;
    private String mDateDay;
    private Report mReport;

    public QuickSettingsTileService() {
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Tile tile = getQsTile();
            if (tile != null) {
                tile.setIcon(Icon.createWithResource(getApplicationContext(), R.drawable.ic_tile_24dp));
                tile.setLabel("Ввійдіть в додаток!");
                tile.updateTile();
            }
            return;
        }
        Tile tile = getQsTile();
        if (tile != null) {
            tile.setIcon(Icon.createWithResource(getApplicationContext(), R.drawable.ic_cloud_download_black_24dp));
            tile.setLabel("Оновлення");
            tile.updateTile();
        }
        mUId = FirebaseAuth.getInstance().getUid();
        mDateDay = DateUtils.toString(new Date(), "d");
        mDateMonth = DateUtils.toString(new Date(), "M");
        mDateYear = DateUtils.toString(new Date(), "yyyy");
        Toast.makeText(getApplicationContext(), mUId, Toast.LENGTH_SHORT).show();
        FirebaseDatabase.getInstance().getReference(DefaultConfigurations.DB_USERS)
                .child(mUId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        if (user != null && user.getUserIsAdmin()) {
                            showReportsData();
                        } else {
                            Tile tile = getQsTile();
                            if (tile != null) {
                                tile.setIcon(Icon.createWithResource(getApplicationContext(), R.drawable.ic_tile_24dp));
                                tile.setLabel("Відкритии KIDS APP");
                                tile.updateTile();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(final DatabaseError databaseError) {
                    }
                });
    }

    @Override
    public void onClick() {
        Context context = getApplicationContext();
        startActivityAndCollapse(SplashActivity.getLaunchIntent(context));
    }

    void showReportsData() {
        FirebaseDatabase.getInstance().getReference(DefaultConfigurations.DB_REPORTS)
                .child(mDateYear).child(mDateMonth).child(mDateDay)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getChildrenCount() == 0) {
                            Tile tile = getQsTile();
                            if (tile != null) {
                                tile.setIcon(Icon.createWithResource(getApplicationContext(), R.drawable.ic_tile_24dp));
                                tile.setState(Tile.STATE_INACTIVE);
                                tile.setLabel("Немає звітів за сьогодні!");
                                tile.updateTile();
                            }
                        } else {
                            int total = 0;
                            Date date = null;
                            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                mReport = dataSnapshot1.getValue(Report.class);
                                if (mReport != null) {
                                    total += mReport.getTotal();
                                    if (mReport.getCheckedListDone() && mReport.getCheckedListTime() != null)
                                        date = mReport.getCheckedListTime();
                                }
                            }
                            Tile tile = getQsTile();
                            if (tile != null) {
                                tile.setState(Tile.STATE_ACTIVE);
                                if (total > 0) {
                                    tile.setLabel("Виручка: " + total + "грн");
                                } else {
                                    if (date != null)
                                        tile.setLabel("Чек-лист: " +
                                                DateUtils.toString(mReport.getCheckedListTime(), "HH:mm"));
                                    else
                                        tile.setLabel("Чек-лист не пройдено");
                                }
                                tile.setIcon(Icon.createWithResource(getApplicationContext(), R.drawable.ic_tile_24dp));
                                tile.updateTile();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(final DatabaseError databaseError) {
                    }
                });
    }
}
