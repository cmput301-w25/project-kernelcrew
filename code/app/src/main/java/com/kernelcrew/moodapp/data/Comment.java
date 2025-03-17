package com.kernelcrew.moodapp.data;

import android.graphics.Bitmap;

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
public class Comment implements Serializable {
    private String id;
    private String uid;
    private String username;
    private String moodEventId;
    private Date created;
    private String commentText;


    /**
     * Empty constructor for Firestore deserialization. Do not use.
     */
    public Comment() { }

    /**
     * Constructor for a new Comment with additional details.
     * Will assign this comment a new random id.
     */
    public Comment(String uid, String username, String moodEventId, String commentText) {
        this.id = UUID.randomUUID().toString();
        this.uid = uid;
        this.username = username;
        this.moodEventId = moodEventId;
        this.created = new Date();
        this.commentText = commentText;
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

    public String getCommentText() {
        return commentText;
    }
    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public String getMoodEventId() {return moodEventId;}
    public void setMoodEventId(String moodEventId) {this.moodEventId = moodEventId;}

    public long getTimestamp() {return this.created.getTime();}
}
