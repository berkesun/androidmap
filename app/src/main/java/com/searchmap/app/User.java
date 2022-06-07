package com.searchmap.app;

import java.util.Objects;

public class User {
    private volatile static User instance;
    private Double lat;
    private Double lon;

    private User() {
    }

    // Double-Check Lock
    // An implementation of double checked
    // locking of Singleton. Intention is to reduce cost
    // of synchronization and improve performance, by only
    // locking critical section of code, the code which
    // creates instance of Singleton class.
    public static User getInstance() {
        if (instance == null) {
            synchronized (User.class) {
                if (instance == null) {
                    instance = new User();
                }
            }
        }
        return instance;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Double.compare(user.getLat(), getLat()) == 0 && Double.compare(user.getLon(), getLon()) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLat(), getLon());
    }
}
