package com.urbaneye.app.domain.models;

import com.google.firebase.Timestamp;

public class Vote {
    public String id;
    public String alertId;
    public String userId;
    public VoteType type;
    public Timestamp createdAt;

    public Vote() {}

    public Vote(String alertId, String userId, VoteType type) {
        this.alertId = alertId;
        this.userId = userId;
        this.type = type;
        this.createdAt = Timestamp.now();
    }
}
