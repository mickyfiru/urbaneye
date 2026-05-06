package com.urbaneye.app.ui.main;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.mapbox.maps.MapView;
import com.urbaneye.app.R;
import com.urbaneye.app.maps.MapController;
import com.urbaneye.app.services.LocationService;
import com.urbaneye.app.ui.alerts.PublishAlertActivity;
import com.urbaneye.app.ui.auth.LoginActivity;
import com.urbaneye.app.ui.profile.ProfileActivity;
import com.urbaneye.app.viewmodels.MainMapViewModel;

public class MainActivity extends AppCompatActivity {
    private MapController mapController;
    private LocationService locationService;

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
        mapController = new MapController(this, mapView, markerOverlay);
        locationService = new LocationService(this);
        MainMapViewModel viewModel = new ViewModelProvider(this).get(MainMapViewModel.class);

        mapController.initialize(() -> {
            startLocationIfAllowed();
            viewModel.observeActiveAlerts().observe(this, resource -> {
                if (resource.status.name().equals("SUCCESS")) mapController.renderAlerts(resource.data);
                if (resource.status.name().equals("ERROR")) Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
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
            if (location != null) mapController.centerOn(location);
        });
    }
}
