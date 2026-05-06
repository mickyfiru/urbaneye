package com.urbaneye.app.maps;

import android.content.Context;
import android.location.Location;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
import com.urbaneye.app.R;
import com.urbaneye.app.domain.models.Alert;
import com.urbaneye.app.domain.models.AlertType;

import java.util.List;

public class MapController {
    private final Context context;
    private final MapView mapView;
    private final FrameLayout markerOverlay;

    public MapController(Context context, MapView mapView, FrameLayout markerOverlay) {
        this.context = context;
        this.mapView = mapView;
        this.markerOverlay = markerOverlay;
    }

    public void initialize(Runnable onReady) {
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS, style -> onReady.run());
    }

    public void centerOn(Location location) {
        CameraOptions camera = new CameraOptions.Builder()
                .center(Point.fromLngLat(location.getLongitude(), location.getLatitude()))
                .zoom(15.0)
                .pitch(45.0)
                .build();
        mapView.getMapboxMap().setCamera(camera);
    }

    public void renderAlerts(List<Alert> alerts) {
        markerOverlay.removeAllViews();
        int index = 0;
        for (Alert alert : alerts) {
            TextView marker = buildMarker(alert);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER;
            params.leftMargin = offsetFor(index, 92, 5);
            params.topMargin = offsetFor(index, 68, 7);
            markerOverlay.addView(marker, params);
            index++;
        }
    }

    private TextView buildMarker(Alert alert) {
        TextView marker = new TextView(context);
        marker.setText(symbolFor(alert.type) + " " + alert.title);
        marker.setTextColor(0xFFFFFFFF);
        marker.setTextSize(alert.type == AlertType.RED ? 16 : 14);
        marker.setGravity(Gravity.CENTER);
        marker.setPadding(18, 10, 18, 10);
        marker.setBackgroundColor(colorFor(alert.type));
        marker.setElevation(alert.type == AlertType.RED ? 18f : 10f);
        marker.setAlpha(0.94f);
        marker.animate().translationYBy(-10f).setDuration(850L).withEndAction(() -> marker.animate().translationYBy(10f).setDuration(850L).start()).start();
        return marker;
    }

    private int colorFor(AlertType type) {
        if (type == AlertType.RED) return ContextCompat.getColor(context, R.color.alert_red);
        if (type == AlertType.YELLOW) return ContextCompat.getColor(context, R.color.alert_yellow);
        return ContextCompat.getColor(context, R.color.alert_green);
    }

    private String symbolFor(AlertType type) {
        if (type == AlertType.RED) return "⚠";
        if (type == AlertType.YELLOW) return "●";
        return "✓";
    }

    private int offsetFor(int index, int amplitude, int seed) {
        return ((index * seed) % 9 - 4) * amplitude;
    }
}
