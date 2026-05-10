package com.urbaneye.app.models;

public class Alert {

    private String id;
    private String title;
    private String description;
    private String type;
    private double latitude;
    private double longitude;
    private String createdBy;

    public Alert() {
        // Constructor vacío requerido por Firebase
    }

    public Alert(String id,
                 String title,
                 String description,
                 String type,
                 double latitude,
                 double longitude,
                 String createdBy) {

        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.createdBy = createdBy;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
