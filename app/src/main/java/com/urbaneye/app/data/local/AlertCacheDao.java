package com.urbaneye.app.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AlertCacheDao {
    @Query("SELECT * FROM alert_cache ORDER BY updatedAt DESC LIMIT 250")
    List<AlertCacheEntity> recentAlerts();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<AlertCacheEntity> alerts);

    @Query("DELETE FROM alert_cache WHERE updatedAt < :threshold")
    void deleteOlderThan(long threshold);
}
