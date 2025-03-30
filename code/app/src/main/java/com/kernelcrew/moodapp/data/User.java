package com.kernelcrew.moodapp.data;

public class User {
    private final String name;
    private final boolean followed;

    public User(String name, boolean followed) {
        this.name = name;
        this.followed = followed;
    }

    public String getName() {
        return name;
    }

    public boolean isFollowed() {
        return followed;
    }
}
