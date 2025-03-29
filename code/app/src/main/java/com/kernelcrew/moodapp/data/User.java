package com.kernelcrew.moodapp.data;

public class User {
    private final String id; // new field for user id
    private final String name;
    private final boolean followed;

    // New constructor that includes the user id.
    public User(String id, String name, boolean followed) {
        this.id = id;
        this.name = name;
        this.followed = followed;
    }

    // Existing constructor for backward compatibility.
    public User(String name, boolean followed) {
        this("", name, followed);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isFollowed() {
        return followed;
    }
}
