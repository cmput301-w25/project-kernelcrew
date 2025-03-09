package com.kernelcrew.moodapp.data;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * A logged mood event owned by a user referenced by that user's firebase auth UID.
 */
public class MoodEvent implements Serializable {
    private String id;
    private String uid;
    private Date created;
    private Emotion emotion;
    private String trigger;
    private String socialSituation;
    private String reason;
    private String photoUrl;
    private Double latitude;
    private Double longitude;

    // Empty constructor for Firestore deserialization
    public MoodEvent() { }

    /**
     * Constructor for a new MoodEvent with additional details.
     */
    public MoodEvent(String uid, Emotion emotion, String trigger, String socialSituation,
                     String reason, String photoUrl, Double latitude, Double longitude) {
        this.id = UUID.randomUUID().toString();
        this.uid = uid;
        this.created = new Date();
        this.emotion = emotion;
        this.trigger = trigger;
        this.socialSituation = socialSituation;
        this.reason = reason;
        this.photoUrl = photoUrl;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters and setters
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

    public String getTrigger() {
        return trigger;
    }
    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }

    public String getSocialSituation() {
        return socialSituation;
    }
    public void setSocialSituation(String socialSituation) {
        this.socialSituation = socialSituation;
    }

    public String getReason() {
        return reason;
    }
    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }
    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Double getLatitude() {
        return latitude;
    }
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public long getTimestamp() {
        return this.created.getTime();
    }
}
