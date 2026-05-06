package com.urbaneye.app.utils;

import com.urbaneye.app.domain.models.AlertType;

public final class TokenRules {
    public static final int GREEN_ALERT_COST = 2;
    public static final int YELLOW_ALERT_COST = 5;
    public static final int RED_ALERT_COST = 10;
    public static final int REWARDED_AD_TOKENS = 20;

    private TokenRules() {}

    public static int costFor(AlertType type) {
        if (type == AlertType.RED) return RED_ALERT_COST;
        if (type == AlertType.YELLOW) return YELLOW_ALERT_COST;
        return GREEN_ALERT_COST;
    }
}
