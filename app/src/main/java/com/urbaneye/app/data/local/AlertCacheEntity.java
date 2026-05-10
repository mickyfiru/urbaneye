package com.urbaneye.app.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "alert_cache")
public class AlertCacheEntity {
    @PrimaryKey
    @NonNull
    public String id;
    public String type;
    public String title;
    public String description;
    public double latitude;
    public double longitude;
    public long updatedAt;

    public AlertCacheEntity(@NonNull String id, String type, String title, String description, double latitude, double longitude, long updatedAt) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.updatedAt = updatedAt;
    }
}
