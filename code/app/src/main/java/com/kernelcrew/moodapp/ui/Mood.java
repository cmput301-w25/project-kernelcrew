package com.kernelcrew.moodapp.ui;

public class Mood {
    private String id;
    private String userName;
    private String moodText;
    private long timestamp;

    // Required empty constructor for Firestore deserialization
    public Mood() {}

    public Mood(String id, String userName, String moodText, long timestamp) {
        this.id = id;
        this.userName = userName;
        this.moodText = moodText;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMoodText() {
        return moodText;
    }
    public void setMoodText(String moodText) {
        this.moodText = moodText;
    }

    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
