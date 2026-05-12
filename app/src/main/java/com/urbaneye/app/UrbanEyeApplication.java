package com.urbaneye.app;

import android.app.Application;

import com.google.android.gms.ads.MobileAds;
import com.mapbox.common.MapboxOptions;

public class UrbanEyeApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        MapboxOptions.accessToken = resolveMapboxToken();
        MobileAds.initialize(this);
    }

    private String resolveMapboxToken() {
        String token = getString(R.string.mapbox_access_token);
        if ((token == null || token.trim().isEmpty() || token.startsWith("${")) && BuildConfig.MAPBOX_ACCESS_TOKEN != null) {
            token = BuildConfig.MAPBOX_ACCESS_TOKEN;
        }
        return token == null ? "" : token.trim();
    }
}
