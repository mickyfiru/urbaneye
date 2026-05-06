package com.urbaneye.app.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {AlertCacheEntity.class}, version = 1, exportSchema = false)
public abstract class UrbanEyeDatabase extends RoomDatabase {
    private static volatile UrbanEyeDatabase instance;

    public abstract AlertCacheDao alertCacheDao();

    public static UrbanEyeDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (UrbanEyeDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(), UrbanEyeDatabase.class, "urbaneye.db").build();
                }
            }
        }
        return instance;
    }
}
