package com.ruslanlyalko.kidsapp.presentation.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.ruslanlyalko.kidsapp.R;
import com.ruslanlyalko.kidsapp.presentation.ui.main.expenses.ExpensesActivity;
import com.ruslanlyalko.kidsapp.presentation.ui.main.messages.details.MessageDetailsActivity;
import com.ruslanlyalko.kidsapp.presentation.ui.main.mk.MkTabActivity;
import com.ruslanlyalko.kidsapp.presentation.ui.main.report.ReportActivity;
import com.ruslanlyalko.kidsapp.presentation.ui.splash.SplashActivity;

import java.util.Map;
import java.util.Random;

/**
 * Created by Ruslan Lyalko
 * on 21.01.2018.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            Map<String, String> payload = remoteMessage.getData();
            showNotification(payload);
        }
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
    }

    private void showNotification(final Map<String, String> payload) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentTitle(payload.get("title"));
        builder.setContentText(payload.get("message"));
        builder.setTicker(payload.get("message"));
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        builder.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND);
        builder.setAutoCancel(true);
        builder.setVibrate(new long[]{0,
                200, 200,
                200, 300,
                100, 100,
                100, 100,
                100, 100
        });
        Intent resultIntent;
        switch (payload.get("type")) {
            case "REPORT":
                resultIntent = ReportActivity.getLaunchIntent(this,
                        payload.get("reportDate"),
                        payload.get("reportUserName"),
                        payload.get("reportUserId"));
                break;
            case "COMMENT":
                resultIntent = MessageDetailsActivity.getLaunchIntent(this, payload.get("messageKey"));
                break;
            case "EXPENSE":
                resultIntent = ExpensesActivity.getLaunchIntent(this);
                break;
            case "MK":
                resultIntent = MkTabActivity.getLaunchIntent(this);
                break;
            default:
                resultIntent = SplashActivity.getLaunchIntent(this);
        }
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(resultIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String CHANNEL_ID = "my_channel_01";// The id of the channel.
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, payload.get("type"), importance);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(mChannel);
                builder.setChannelId(CHANNEL_ID);
            }
        }
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(pendingIntent);
        if (notificationManager != null) {
            notificationManager.notify(new Random().nextInt(250), builder.build());
        }
    }
}
