package com.kernelcrew.moodapp.data;

public class User {
    private final String uid;
    private final String name;
    private final boolean followed;

    public User(String uid, String name, boolean followed) {
        this.uid = uid;
        this.name = name;
        this.followed = followed;
    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public boolean isFollowed() {
        return followed;
    }
}