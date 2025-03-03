package com.kernelcrew.moodapp.data;

import java.util.Date;
import java.util.UUID;

/**
 * A logged mood event owned by a user referenced by that user's firebase auth UID.
 */
public class MoodEvent {
    private String id;
    private String userId;
    private Date created;
    private Emotion emotion;

    public MoodEvent(String userId, Emotion emotion) {
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.created = new Date();
        this.emotion = emotion;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Emotion getEmotion() {
        return emotion;
    }

    public void setEmotion(Emotion emotion) {
        this.emotion = emotion;
    }
}