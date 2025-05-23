package com.kernelcrew.moodapp.data;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.Exclude;
import com.kernelcrew.moodapp.utils.PhotoUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * A logged mood event owned by a user referenced by that user's firebase auth UID.
 */
public class MoodEvent implements Serializable {
    private String id;
    private String uid;
    private String username;
    private Date created;
    private Emotion emotion;
    private String socialSituation;
    private String reason;
    private Bitmap photo;
    private Double latitude;
    private Double longitude;

    private @NonNull MoodEventVisibility visibility = MoodEventVisibility.PUBLIC;

    /**
     * Empty constructor for Firestore deserialization. Do not use.
     */
    public MoodEvent() { }

    /**
     * Constructor for a new MoodEvent with additional details.
     * Will assign this mood event a new random id.
     */
    public MoodEvent(String uid, String username, Emotion emotion, String socialSituation, String reason,
                     Double latitude, Double longitude) {
        this.id = UUID.randomUUID().toString();
        this.uid = uid;
        this.username = username;
        this.created = new Date();
        this.emotion = emotion;
        this.socialSituation = socialSituation;
        this.reason = reason;
        this.latitude = latitude;
        this.longitude = longitude;
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

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
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
    public void setEmotion(String emotionValue) {
        this.emotion = Emotion.valueOf(emotionValue.toUpperCase());
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

    @Exclude // Don't serialize the Bitmap into firestore
    public Bitmap getPhoto() {
        return photo;
    }
    public void setPhoto(Bitmap photo) {
        this.photo = photo;
    }

    /**
     * PNG encode the photo as a list of bytes (each an int so that it is serializable by
     * Firestore.)
     * @return PNG encoded photo or null
     */
    @Nullable
    public List<Integer> getPhotoBytes() {
        if (photo == null) {
            return null;
        }

        return PhotoUtils.compressPhoto(photo);
    }

    /**
     * Set the photo associated with this mood event.
     * The photo bytes must be PNG encoded as a list of bytes (store as integers in Firestore).
     * @param byteList List of bytes encoding the PNG photo
     */
    public void setPhotoBytes(@Nullable List<Integer> byteList) {
        if (byteList == null) {
            photo = null;
            return;
        }

        photo = PhotoUtils.decodePhoto(byteList);
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

    @Exclude
    public boolean hasLocation() {
        return latitude != null && longitude != null;
    }

    @Exclude
    public long getTimestamp() {
        return this.created.getTime();
    }

    public @NonNull MoodEventVisibility getVisibility() {
        return visibility;
    }

    /**
     * Update the visibility. Cannot change visibility to null.
     * @param visibility New visibility. If null, no change is made.
     */
    public void setVisibility(MoodEventVisibility visibility) {
        if (visibility == null) {
            return;
        }

        this.visibility = visibility;
    }

    @Exclude
    private boolean isSynced = true;

    @Exclude
    public boolean isSynced() {
        return isSynced;
    }

    @Exclude
    public void setSynced(boolean synced) {
        this.isSynced = synced;
    }
}
