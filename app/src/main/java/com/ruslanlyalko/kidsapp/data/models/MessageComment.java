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
    private String file;
    private Date date;
    private boolean removed;

    public MessageComment() {
    }

    public MessageComment(final String key, final String message, FirebaseUser user) {
        this.key = key;
        this.message = message;
        this.userId = user.getUid();
        this.userName = user.getDisplayName();
        this.date = new Date();
    }

    public MessageComment(final String key, final String message, final String file, FirebaseUser user) {
        this.key = key;
        this.file = file;
        this.message = message;
        this.userId = user.getUid();
        this.userName = user.getDisplayName();
        this.date = new Date();
    }

    public boolean getRemoved() {
        return removed;
    }

    public void setRemoved(final boolean removed) {
        this.removed = removed;
    }

    public String getFile() {
        return file;
    }

    public void setFile(final String file) {
        this.file = file;
    }

    @Override
    public int hashCode() {
        int result = getKey() != null ? getKey().hashCode() : 0;
        result = 31 * result + (getMessage() != null ? getMessage().hashCode() : 0);
        result = 31 * result + (getUserId() != null ? getUserId().hashCode() : 0);
        result = 31 * result + (getUserName() != null ? getUserName().hashCode() : 0);
        result = 31 * result + (getDate() != null ? getDate().hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof MessageComment)) return false;
        MessageComment that = (MessageComment) o;
        if (getKey() != null ? !getKey().equals(that.getKey()) : that.getKey() != null)
            return false;
        if (getMessage() != null ? !getMessage().equals(that.getMessage()) : that.getMessage() != null)
            return false;
        if (getUserId() != null ? !getUserId().equals(that.getUserId()) : that.getUserId() != null)
            return false;
        if (getUserName() != null ? !getUserName().equals(that.getUserName()) : that.getUserName() != null)
            return false;
        return getDate() != null ? getDate().equals(that.getDate()) : that.getDate() == null;
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
