package com.urbaneye.app.maps;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.ScreenCoordinate;
import com.mapbox.maps.Style;
import com.urbaneye.app.R;
import com.urbaneye.app.domain.models.Alert;
import com.urbaneye.app.domain.models.AlertType;

import java.util.List;

public class MapController {
    private static final String TAG = "UrbanEyeMap";
    private final Context context;
    private final MapView mapView;
    private final FrameLayout markerOverlay;

    public MapController(Context context, MapView mapView, FrameLayout markerOverlay) {
        this.context = context;
        this.mapView = mapView;
        this.markerOverlay = markerOverlay;
    }

    public void initialize(Runnable onReady) {
        mapView.getMapboxMap().loadStyleUri(Style.DARK, style -> onReady.run());
    }

    public void centerOn(Location location) {
        centerOn(location.getLatitude(), location.getLongitude(), 15.2, 48.0);
    }

    public void centerOn(double latitude, double longitude) {
        centerOn(latitude, longitude, 16.0, 50.0);
    }

    private void centerOn(double latitude, double longitude, double zoom, double pitch) {
        CameraOptions camera = new CameraOptions.Builder()
                .center(Point.fromLngLat(longitude, latitude))
                .zoom(zoom)
                .pitch(pitch)
                .bearing(-12.0)
                .build();
        mapView.getMapboxMap().setCamera(camera);
    }

    public void renderAlerts(List<Alert> alerts) {
        markerOverlay.removeAllViews();
        if (alerts == null || alerts.isEmpty()) {
            Log.d(TAG, "Alerts loaded: 0");
            return;
        }
        Log.d(TAG, "Alerts loaded: " + alerts.size());
        markerOverlay.post(() -> {
            markerOverlay.removeAllViews();
            for (Alert alert : alerts) {
                if (!hasValidCoordinates(alert)) {
                    Log.e(TAG, "Invalid alert coordinates: " + safeTitle(alert));
                    continue;
                }
                Log.d(TAG, "Rendering alert: " + safeTitle(alert) + " " + alert.latitude + " " + alert.longitude);
                View marker = buildMarker(alert);
                ScreenCoordinate screenCoordinate = mapView.getMapboxMap().pixelForCoordinate(Point.fromLngLat(alert.longitude, alert.latitude));
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(dp(210), ViewGroup.LayoutParams.WRAP_CONTENT);
                params.leftMargin = (int) screenCoordinate.getX() - dp(105);
                params.topMargin = (int) screenCoordinate.getY() - dp(112);
                markerOverlay.addView(marker, params);
                marker.animate().alpha(1f).translationYBy(-8f).setDuration(260L).start();
            }
        });
    }

    private View buildMarker(Alert alert) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.START);
        card.setPadding(dp(12), dp(10), dp(12), dp(10));
        card.setAlpha(0f);
        card.setElevation(dp(alert.type == AlertType.RED ? 18 : 12));
        card.setBackground(markerBackground(alert.type));

        TextView title = new TextView(context);
        title.setText(symbolFor(alert.type) + "  " + safeTitle(alert));
        title.setTextColor(Color.WHITE);
        title.setTextSize(14f);
        title.setTypeface(title.getTypeface(), android.graphics.Typeface.BOLD);
        title.setSingleLine(true);

        TextView description = new TextView(context);
        description.setText(shortDescription(alert.description));
        description.setTextColor(0xDDEFF6FF);
        description.setTextSize(12f);
        description.setMaxLines(2);
        LinearLayout.LayoutParams descriptionParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        descriptionParams.topMargin = dp(4);

        TextView type = new TextView(context);
        type.setText(alert.type == null ? "GREEN" : alert.type.name());
        type.setTextColor(colorFor(alert.type));
        type.setTextSize(11f);
        type.setTypeface(type.getTypeface(), android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams typeParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        typeParams.topMargin = dp(6);

        card.addView(title, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        card.addView(description, descriptionParams);
        card.addView(type, typeParams);
        return card;
    }

    private GradientDrawable markerBackground(AlertType type) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(0xE6111D33);
        drawable.setCornerRadius(dp(18));
        drawable.setStroke(dp(2), colorFor(type));
        return drawable;
    }

    private boolean hasValidCoordinates(Alert alert) {
        return alert != null
                && alert.latitude != 0d
                && alert.longitude != 0d
                && !Double.isNaN(alert.latitude)
                && !Double.isNaN(alert.longitude)
                && alert.latitude >= -90d
                && alert.latitude <= 90d
                && alert.longitude >= -180d
                && alert.longitude <= 180d;
    }

    private int colorFor(AlertType type) {
        if (type == AlertType.RED) return ContextCompat.getColor(context, R.color.alert_red);
        if (type == AlertType.YELLOW) return ContextCompat.getColor(context, R.color.alert_yellow);
        return ContextCompat.getColor(context, R.color.alert_green);
    }

    private String symbolFor(AlertType type) {
        if (type == AlertType.RED) return "⚠";
        if (type == AlertType.YELLOW) return "●";
        return "●";
    }

    private String safeTitle(Alert alert) {
        if (alert == null || alert.title == null || alert.title.trim().isEmpty()) return "Alerta UrbanEye";
        return alert.title.trim();
    }

    private String shortDescription(String description) {
        if (description == null || description.trim().isEmpty()) return "Sin comentario";
        String value = description.trim();
        return value.length() > 72 ? value.substring(0, 69) + "..." : value;
    }

    private int dp(int value) {
        return (int) (value * context.getResources().getDisplayMetrics().density + 0.5f);
    }
}
