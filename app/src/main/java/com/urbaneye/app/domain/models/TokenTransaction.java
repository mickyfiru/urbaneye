package com.urbaneye.app.domain.models;

import com.google.firebase.Timestamp;

public class TokenTransaction {
    public String id;
    public String userId;
    public int amount;
    public String reason;
    public Timestamp createdAt;

    public TokenTransaction() {}

    public TokenTransaction(String userId, int amount, String reason) {
        this.userId = userId;
        this.amount = amount;
        this.reason = reason;
        this.createdAt = Timestamp.now();
    }
}
