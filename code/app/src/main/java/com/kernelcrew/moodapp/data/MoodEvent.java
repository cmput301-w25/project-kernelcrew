package com.kernelcrew.moodapp.data;

import java.util.Date;
import java.util.UUID;

/**
 * A logged mood event owned by a user referenced by that user's firebase auth UID.
 */
public class MoodEvent {
    private String id;
    private String uid;
    private Date created;
    private Emotion emotion;

    public MoodEvent(String uid, Emotion emotion) {
        this.id = UUID.randomUUID().toString();
        this.uid = uid;
        this.created = new Date();
        this.emotion = emotion;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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