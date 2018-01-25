package com.ruslanlyalko.kidsapp.data;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ruslanlyalko.kidsapp.data.configuration.DefaultConfigurations;
import com.ruslanlyalko.kidsapp.data.models.User;

public class FirebaseUtils {

    private static boolean mIsAdmin;

    public static boolean isAdmin() {
        return mIsAdmin;
    }

    public static void setIsAdmin(boolean mIsAdmin) {
        FirebaseUtils.mIsAdmin = mIsAdmin;
    }

    public static void clearPushToken() {
        FirebaseDatabase.getInstance()
                .getReference(DefaultConfigurations.DB_USERS)
                .child(FirebaseAuth.getInstance().getUid())
                .child("token")
                .setValue("");
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
