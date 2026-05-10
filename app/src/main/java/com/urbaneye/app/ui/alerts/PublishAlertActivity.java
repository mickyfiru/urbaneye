package com.urbaneye.app.ui.alerts;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
import com.urbaneye.app.R;
import com.urbaneye.app.domain.models.AlertType;
import com.urbaneye.app.repositories.geocoding.AddressSuggestion;
import com.urbaneye.app.repositories.geocoding.GeocodingRepository;
import com.urbaneye.app.services.LocationService;
import com.urbaneye.app.viewmodels.PublishAlertViewModel;

import java.util.ArrayList;
import java.util.List;

public class PublishAlertActivity extends AppCompatActivity {
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private final GeocodingRepository geocodingRepository = new GeocodingRepository();
    private ArrayAdapter<AddressSuggestion> addressAdapter;
    private MapView mapView;
    private Double selectedLatitude;
    private Double selectedLongitude;
    private String selectedAddress;
    private boolean selectingAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_alert);
        PublishAlertViewModel viewModel = new ViewModelProvider(this).get(PublishAlertViewModel.class);
        mapView = findViewById(R.id.publishMapView);
        mapView.getMapboxMap().loadStyleUri(Style.DARK, style -> centerOnLastKnownLocation());

        Spinner typeSpinner = findViewById(R.id.typeSpinner);
        Spinner durationSpinner = findViewById(R.id.durationSpinner);
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, R.layout.item_spinner_urban, new String[]{"GREEN", "YELLOW", "RED"});
        typeAdapter.setDropDownViewResource(R.layout.item_dropdown_urban);
        ArrayAdapter<String> durationAdapter = new ArrayAdapter<>(this, R.layout.item_spinner_urban, new String[]{"15 min", "30 min", "60 min"});
        durationAdapter.setDropDownViewResource(R.layout.item_dropdown_urban);
        typeSpinner.setAdapter(typeAdapter);
        durationSpinner.setAdapter(durationAdapter);
        EditText title = findViewById(R.id.titleInput);
        EditText description = findViewById(R.id.descriptionInput);
        AutoCompleteTextView addressInput = findViewById(R.id.addressInput);
        ProgressBar progress = findViewById(R.id.progress);
        View selectedPin = findViewById(R.id.selectedPin);

        addressAdapter = new ArrayAdapter<>(this, R.layout.item_dropdown_urban, new ArrayList<>());
        addressInput.setAdapter(addressAdapter);
        addressInput.setThreshold(3);
        addressInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (selectingAddress) return;
                selectedLatitude = null;
                selectedLongitude = null;
                selectedAddress = null;
                selectedPin.setVisibility(View.GONE);
                searchHandler.removeCallbacksAndMessages(null);
                searchHandler.postDelayed(() -> searchAddress(s.toString()), 450L);
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });
        addressInput.setOnItemClickListener((parent, view, position, id) -> {
            AddressSuggestion suggestion = addressAdapter.getItem(position);
            if (suggestion != null) {
                selectedLatitude = suggestion.latitude;
                selectedLongitude = suggestion.longitude;
                selectedAddress = suggestion.label;
                selectingAddress = true;
                addressInput.setText(suggestion.label, false);
                selectingAddress = false;
                selectedPin.setVisibility(View.VISIBLE);
                centerMap(suggestion.latitude, suggestion.longitude, 16.5);
            }
        });

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        findViewById(R.id.publishButton).setOnClickListener(v -> {
            String userId = FirebaseAuth.getInstance().getUid();
            if (userId == null) {
                showSnack("Debes iniciar sesión.");
                return;
            }
            if (selectedLatitude == null || selectedLongitude == null || selectedAddress == null) {
                showSnack("Selecciona una dirección desde las sugerencias.");
                return;
            }
            if (title.getText().toString().trim().isEmpty() || description.getText().toString().trim().isEmpty()) {
                showSnack("Agrega título y descripción para publicar.");
                return;
            }
            AlertType type = AlertType.valueOf(typeSpinner.getSelectedItem().toString());
            int greenMinutes = Integer.parseInt(durationSpinner.getSelectedItem().toString().replace(" min", ""));
            viewModel.publish(type, title.getText().toString().trim(), description.getText().toString().trim(), selectedAddress, selectedLatitude, selectedLongitude, userId, greenMinutes).observe(this, resource -> {
                progress.setVisibility(resource.status.name().equals("LOADING") ? View.VISIBLE : View.GONE);
                if (resource.status.name().equals("SUCCESS")) {
                    showSnack("Alerta publicada en el mapa.");
                    finish();
                }
                if (resource.status.name().equals("ERROR")) showSnack(resource.message);
            });
        });
    }

    private void searchAddress(String query) {
        geocodingRepository.autocomplete(query).observe(this, resource -> {
            if (resource.status.name().equals("SUCCESS") && resource.data != null) updateSuggestions(resource.data);
            if (resource.status.name().equals("ERROR")) showSnack(resource.message);
        });
    }

    private void updateSuggestions(List<AddressSuggestion> suggestions) {
        addressAdapter.clear();
        addressAdapter.addAll(suggestions);
        addressAdapter.notifyDataSetChanged();
    }

    private void centerOnLastKnownLocation() {
        LocationService locationService = new LocationService(this);
        if (!locationService.hasLocationPermission()) return;
        locationService.observeLocation().observe(this, location -> {
            if (location != null && selectedLatitude == null) centerMap(location.getLatitude(), location.getLongitude(), 14.8);
        });
    }

    private void centerMap(double latitude, double longitude, double zoom) {
        CameraOptions camera = new CameraOptions.Builder()
                .center(Point.fromLngLat(longitude, latitude))
                .zoom(zoom)
                .pitch(50.0)
                .bearing(-10.0)
                .build();
        mapView.getMapboxMap().setCamera(camera);
    }

    private void showSnack(String message) {
        Snackbar.make(findViewById(android.R.id.content), message == null ? "Ocurrió un problema." : message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getColor(R.color.urban_surface_high))
                .setTextColor(getColor(R.color.urban_text))
                .show();
    }
}
