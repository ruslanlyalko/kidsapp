package com.ruslanlyalko.kidsapp.data;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ruslanlyalko.kidsapp.data.configuration.DefaultConfigurations;
import com.ruslanlyalko.kidsapp.data.models.MessageType;
import com.ruslanlyalko.kidsapp.data.models.Notification;
import com.ruslanlyalko.kidsapp.data.models.PushNotification;
import com.ruslanlyalko.kidsapp.data.models.User;

public class FirebaseUtils {

    private static boolean mIsAdmin;

    public static boolean isAdmin() {
        return mIsAdmin;
    }

    public static void setIsAdmin(boolean mIsAdmin) {
        FirebaseUtils.mIsAdmin = mIsAdmin;
    }

    public static void updateNotificationsForAllUsers(final String messageKey, final String title1, final String title2, MessageType messageType) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        final Notification notification1 = new Notification(messageKey);
        FirebaseDatabase.getInstance()
                .getReference(DefaultConfigurations.DB_USERS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        for (DataSnapshot userSS : dataSnapshot.getChildren()) {
                            User user = userSS.getValue(User.class);
                            if (user != null) {//todo:&& !user.getUserId().equals(FirebaseAuth.getInstance().getUid())) {
                                sendUserNotification(user.getUserId(), notification1);
                                sendPushNotification(new PushNotification(title1,
                                        title2,
                                        user.getToken(), messageKey,
                                        currentUser.getUid(),
                                        currentUser.getDisplayName(),
                                        messageType));
                            }
                        }
                    }

                    @Override
                    public void onCancelled(final DatabaseError databaseError) {
                    }
                });
    }

    private static void sendUserNotification(String userId, Notification notification1) {
        FirebaseDatabase.getInstance()
                .getReference(DefaultConfigurations.DB_USERS_NOTIFICATIONS)
                .child(userId)
                .child(notification1.getKey())
                .setValue(notification1);
    }

    private static void sendPushNotification(PushNotification notification) {
        if (notification.getToken() != null && !notification.getToken().isEmpty())
            FirebaseDatabase.getInstance()
                    .getReference(DefaultConfigurations.DB_PUSH_NOTIFICATIONS)
                    .push()
                    .setValue(notification);
    }

    public static void clearNotificationsForAllUsers(final String notKey) {
        FirebaseDatabase.getInstance()
                .getReference(DefaultConfigurations.DB_USERS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        for (DataSnapshot userSS : dataSnapshot.getChildren()) {
                            User user = userSS.getValue(User.class);
                            if (user != null)
                                FirebaseDatabase.getInstance()
                                        .getReference(DefaultConfigurations.DB_USERS_NOTIFICATIONS)
                                        .child(user.getUserId())
                                        .child(notKey)
                                        .removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(final DatabaseError databaseError) {
                    }
                });
    }

    public static void markNotificationsAsRead(String key) {
        FirebaseDatabase.getInstance()
                .getReference(DefaultConfigurations.DB_USERS_NOTIFICATIONS)
                .child(FirebaseAuth.getInstance().getUid())
                .child(key)
                .removeValue();
    }
}
