package com.urbaneye.app.domain.models;

import com.google.firebase.Timestamp;

public class UserProfile {
    public String id;
    public String uid;
    public String username;
    public String displayName;
    public String email;
    public String photoUrl;
    public int tokens;
    public int xp;
    public int level;
    public int reputation;
    public int reportsConfirmed;
    public int reportsRejected;
    public int alertsPublished;
    public Timestamp createdAt;

    public UserProfile() {}

    public UserProfile(String id, String username, String email) {
        this.id = id;
        this.uid = id;
        this.username = username == null || username.trim().isEmpty() ? "Usuario UrbanEye" : username;
        this.displayName = this.username;
        this.email = email == null ? "" : email;
        this.photoUrl = "";
        this.tokens = 0;
        this.xp = 0;
        this.level = 1;
        this.reputation = 0;
        this.reportsConfirmed = 0;
        this.reportsRejected = 0;
        this.alertsPublished = 0;
        this.createdAt = Timestamp.now();
    }
}
