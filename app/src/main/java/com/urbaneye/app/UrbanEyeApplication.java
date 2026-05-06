package com.urbaneye.app;

import android.app.Application;

import com.google.android.gms.ads.MobileAds;
import com.mapbox.common.MapboxOptions;

public class UrbanEyeApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        MapboxOptions.accessToken = BuildConfig.MAPBOX_ACCESS_TOKEN;
        MobileAds.initialize(this);
    }
}
