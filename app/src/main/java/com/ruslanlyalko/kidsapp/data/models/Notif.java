package com.ruslanlyalko.kidsapp.data.models;

import java.util.Date;

/**
 * Created by Ruslan Lyalko
 * on 12.01.2018.
 */
public class Notif {

    String key;
    Date createdAt = new Date();

    public Notif() {
    }

    public Notif(final String key) {
        this.key = key;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Notif)) return false;
        Notif notif = (Notif) o;
        return getKey() != null ? getKey().equals(notif.getKey()) : notif.getKey() == null;
    }

    public String getKey() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }
}
