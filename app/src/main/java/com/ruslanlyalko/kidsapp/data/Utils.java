package com.ruslanlyalko.kidsapp.data;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ruslanlyalko.kidsapp.data.configuration.DefaultConfigurations;
import com.ruslanlyalko.kidsapp.data.models.Notif;
import com.ruslanlyalko.kidsapp.data.models.User;

public class Utils {

    private static boolean mIsAdmin;

    public static boolean isAdmin() {
        return mIsAdmin;
    }

    public static void setIsAdmin(boolean mIsAdmin) {
        Utils.mIsAdmin = mIsAdmin;
    }

    public static void updateNotificationsForAllUsers(final String notKey) {
        final Notif notif1 = new Notif(notKey);
        FirebaseDatabase.getInstance()
                .getReference(DefaultConfigurations.DB_USERS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        for (DataSnapshot userSS : dataSnapshot.getChildren()) {
                            User user = userSS.getValue(User.class);
                            if (user != null && !user.getUserId().equals(FirebaseAuth.getInstance().getUid()))
                                FirebaseDatabase.getInstance()
                                        .getReference(DefaultConfigurations.DB_USERS_NOTIFICATIONS)
                                        .child(user.getUserId())
                                        .child(notif1.getKey())
                                        .setValue(notif1);
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
