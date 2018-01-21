package com.ruslanlyalko.kidsapp.data.models;

import com.google.firebase.auth.FirebaseUser;

import java.util.Date;

/**
 * Created by Ruslan Lyalko
 * on 21.01.2018.
 */

public class MessageComment {

    private String key;
    private String message;
    private String userId;
    private String userName;
    private Date date;

    public MessageComment() {
    }

    public MessageComment(final String key, final String message, FirebaseUser user) {
        this.key = key;
        this.message = message;
        this.userId = user.getUid();
        this.userName = user.getDisplayName();
        this.date = new Date();
    }

    public String getKey() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(final String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(final String userName) {
        this.userName = userName;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(final Date date) {
        this.date = date;
    }
}
