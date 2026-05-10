package com.urbaneye.app.ui.main;

import android.Manifest;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.mapbox.maps.MapView;
import com.urbaneye.app.R;
import com.urbaneye.app.domain.models.Alert;
import com.urbaneye.app.maps.MapController;
import com.urbaneye.app.services.LocationService;
import com.urbaneye.app.ui.alerts.PublishAlertActivity;
import com.urbaneye.app.ui.auth.LoginActivity;
import com.urbaneye.app.ui.profile.ProfileActivity;
import com.urbaneye.app.viewmodels.MainMapViewModel;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private MapController mapController;
    private LocationService locationService;
    private TextView cityText;
    private TextView alertsSummaryText;
    private boolean cameraCentered;
    private boolean cityResolved;

    private final ActivityResultLauncher<String[]> permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> startLocationIfAllowed());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_main);
        MapView mapView = findViewById(R.id.mapView);
        FrameLayout markerOverlay = findViewById(R.id.markerOverlay);
        cityText = findViewById(R.id.cityText);
        alertsSummaryText = findViewById(R.id.alertsSummaryText);
        mapController = new MapController(this, mapView, markerOverlay);
        locationService = new LocationService(this);
        MainMapViewModel viewModel = new ViewModelProvider(this).get(MainMapViewModel.class);

        mapController.initialize(() -> {
            startLocationIfAllowed();
            viewModel.observeActiveAlerts().observe(this, resource -> {
                if (resource.status.name().equals("SUCCESS")) {
                    mapController.renderAlerts(resource.data);
                    renderAlertSummary(resource.data);
                }
                if (resource.status.name().equals("ERROR")) showSnack(resource.message);
            });
        });
        findViewById(R.id.publishButton).setOnClickListener(v -> startActivity(new Intent(this, PublishAlertActivity.class)));
        findViewById(R.id.profileButton).setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
    }

    private void startLocationIfAllowed() {
        if (!locationService.hasLocationPermission()) {
            permissionLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
            return;
        }
        locationService.observeLocation().observe(this, location -> {
            if (location != null) {
                if (!cameraCentered) {
                    mapController.centerOn(location);
                    cameraCentered = true;
                }
                if (!cityResolved) {
                    cityText.setText(resolveCity(location.getLatitude(), location.getLongitude()));
                    cityResolved = true;
                }
            }
        });
    }

    private String resolveCity(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, new Locale("es", "CL"));
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String city = address.getLocality() != null ? address.getLocality() : address.getSubAdminArea();
                String country = address.getCountryName() != null ? address.getCountryName() : "Chile";
                if (city != null && !city.isEmpty()) return city + ", " + country;
            }
        } catch (IOException ignored) {
            return "Ubicación actual";
        }
        return "Ubicación actual";
    }

    private void renderAlertSummary(List<Alert> alerts) {
        if (alerts == null || alerts.isEmpty()) {
            alertsSummaryText.setText("No hay alertas activas cerca. Sé el primero en reportar algo importante.");
            return;
        }
        Alert first = alerts.get(0);
        String address = first.address == null || first.address.isEmpty() ? "Dirección protegida" : first.address;
        alertsSummaryText.setText(first.type.name() + " · " + first.title + "\n" + address + " · ahora · reputación alta");
    }

    private void showSnack(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getColor(R.color.urban_surface_high))
                .setTextColor(getColor(R.color.urban_text))
                .show();
    }
}
