package com.urbaneye.app.ads;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.Nullable;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.urbaneye.app.BuildConfig;

public class RewardedAdManager {
    public interface RewardCallback {
        void onRewardEarned();
        void onAdUnavailable(String message);
    }

    @Nullable private RewardedAd rewardedAd;
    private boolean loading;

    public void loadRewardedAd(Context context) {
        if (loading || rewardedAd != null) return;
        loading = true;
        RewardedAd.load(context, BuildConfig.ADMOB_REWARDED_AD_UNIT_ID, new AdRequest.Builder().build(), new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(RewardedAd ad) {
                rewardedAd = ad;
                loading = false;
            }

            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                rewardedAd = null;
                loading = false;
            }
        });
    }

    public void showRewardedAd(Activity activity, RewardCallback callback) {
        if (rewardedAd == null) {
            callback.onAdUnavailable("El anuncio aún no está disponible.");
            loadRewardedAd(activity);
            return;
        }
        rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                rewardedAd = null;
                loadRewardedAd(activity);
            }

            @Override
            public void onAdFailedToShowFullScreenContent(AdError adError) {
                rewardedAd = null;
                callback.onAdUnavailable(adError.getMessage());
                loadRewardedAd(activity);
            }
        });
        rewardedAd.show(activity, rewardItem -> callback.onRewardEarned());
    }
}
