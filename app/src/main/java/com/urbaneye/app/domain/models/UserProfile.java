package com.urbaneye.app.domain.models;

import com.google.firebase.Timestamp;

public class UserProfile {
    public String id;
    public String username;
    public String email;
    public String photoUrl;
    public int tokens;
    public int xp;
    public int level;
    public int reputation;
    public int reportsConfirmed;
    public int reportsRejected;
    public Timestamp createdAt;

    public UserProfile() {}

    public UserProfile(String id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.photoUrl = "";
        this.tokens = 20;
        this.xp = 0;
        this.level = 1;
        this.reputation = 100;
        this.reportsConfirmed = 0;
        this.reportsRejected = 0;
        this.createdAt = Timestamp.now();
    }
}
