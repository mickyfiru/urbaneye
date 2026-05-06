package com.urbaneye.app.domain.models;

import com.google.firebase.Timestamp;

public class Alert {
    public String id;
    public AlertType type;
    public String title;
    public String description;
    public double latitude;
    public double longitude;
    public String createdBy;
    public Timestamp createdAt;
    public Timestamp expiresAt;
    public int confirmations;
    public int denies;
    public int reports;
    public AlertStatus status;

    public Alert() {}

    public Alert(AlertType type, String title, String description, double latitude, double longitude, String createdBy, Timestamp expiresAt) {
        this.type = type;
        this.title = title;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.createdBy = createdBy;
        this.createdAt = Timestamp.now();
        this.expiresAt = expiresAt;
        this.confirmations = 0;
        this.denies = 0;
        this.reports = 0;
        this.status = AlertStatus.ACTIVE;
    }
}
