package com.urbaneye.app.utils;

import com.google.firebase.Timestamp;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public final class AntiSpamRules {
    public static final long PUBLICATION_COOLDOWN_MS = TimeUnit.MINUTES.toMillis(2);
    public static final int MAX_PUBLICATIONS_PER_HOUR = 12;

    private AntiSpamRules() {}

    public static boolean isCoolingDown(Timestamp lastAlertAt) {
        return lastAlertAt != null && System.currentTimeMillis() - lastAlertAt.toDate().getTime() < PUBLICATION_COOLDOWN_MS;
    }

    public static Date oneHourAgo() {
        return new Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1));
    }
}
