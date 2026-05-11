package com.urbaneye.app.ui.alerts;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
import com.urbaneye.app.R;
import com.urbaneye.app.ads.RewardedAdManager;
import com.urbaneye.app.domain.models.AlertType;
import com.urbaneye.app.repositories.geocoding.AddressSuggestion;
import com.urbaneye.app.repositories.geocoding.GeocodingRepository;
import com.urbaneye.app.services.LocationService;
import com.urbaneye.app.ui.alerts.adapters.AddressSuggestionAdapter;
import com.urbaneye.app.utils.TokenRules;
import com.urbaneye.app.viewmodels.ProfileViewModel;
import com.urbaneye.app.viewmodels.PublishAlertViewModel;

import java.util.List;

public class PublishAlertActivity extends AppCompatActivity {
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private final Handler reverseHandler = new Handler(Looper.getMainLooper());
    private final RewardedAdManager adManager = new RewardedAdManager();
    private GeocodingRepository geocodingRepository;
    private AddressSuggestionAdapter suggestionAdapter;
    private MapView mapView;
    private LocationService locationService;
    private EditText addressInput;
    private TextView addressStatusText;
    private TextView tokensText;
    private RecyclerView suggestionsList;
    private View userLocationMarker;
    private View userGlow;
    private View rewardAdButton;
    private Double selectedLatitude;
    private Double selectedLongitude;
    private String selectedAddress;
    private Double currentLatitude;
    private Double currentLongitude;
    private boolean selectingAddress;
    private boolean cameraCenteredOnGps;
    private boolean useCurrentAfterPermission;
    private boolean userPulseStarted;
    private int currentTokens;

    private final ActivityResultLauncher<String[]> permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
        boolean granted = locationService.hasLocationPermission();
        if (!granted) {
            showSnack("Permiso de ubicación rechazado. Puedes buscar una dirección manualmente.");
            return;
        }
        observeLocation();
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_alert);
        PublishAlertViewModel viewModel = new ViewModelProvider(this).get(PublishAlertViewModel.class);
        ProfileViewModel profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        geocodingRepository = new GeocodingRepository(this);
        locationService = new LocationService(this);
        mapView = findViewById(R.id.publishMapView);
        addressInput = findViewById(R.id.addressInput);
        addressStatusText = findViewById(R.id.addressStatusText);
        tokensText = findViewById(R.id.tokensText);
        suggestionsList = findViewById(R.id.suggestionsList);
        userLocationMarker = findViewById(R.id.userLocationMarker);
        userGlow = findViewById(R.id.userGlow);
        rewardAdButton = findViewById(R.id.rewardAdButton);
        mapView.getMapboxMap().loadStyleUri(Style.DARK, style -> startLocationIfAllowed(false));
        mapView.setOnTouchListener((view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                reverseHandler.removeCallbacksAndMessages(null);
                reverseHandler.postDelayed(this::reverseGeocodeCameraCenter, 650L);
            }
            return false;
        });

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
        ProgressBar progress = findViewById(R.id.progress);
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId != null) {
            profileViewModel.observeUser(userId).observe(this, resource -> {
                if (resource.status.name().equals("SUCCESS") && resource.data != null) {
                    currentTokens = Math.max(0, resource.data.tokens);
                    tokensText.setText("Tokens: " + currentTokens);
                }
                if (resource.status.name().equals("ERROR")) showSnack(resource.message);
            });
        }
        adManager.loadRewardedAd(this);

        suggestionAdapter = new AddressSuggestionAdapter(this::selectSuggestion);
        suggestionsList.setLayoutManager(new LinearLayoutManager(this));
        suggestionsList.setAdapter(suggestionAdapter);
        addressInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (selectingAddress) return;
                clearSelectedAddress();
                searchHandler.removeCallbacksAndMessages(null);
                searchHandler.postDelayed(() -> searchAddress(s.toString()), 350L);
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        findViewById(R.id.useCurrentLocationButton).setOnClickListener(v -> useCurrentLocation());
        rewardAdButton.setOnClickListener(v -> showRewardedAd(profileViewModel));
        findViewById(R.id.publishButton).setOnClickListener(v -> {
            String userId = FirebaseAuth.getInstance().getUid();
            if (userId == null) {
                showSnack("Debes iniciar sesión.");
                return;
            }
            if (selectedLatitude == null || selectedLongitude == null || selectedAddress == null) {
                showSnack("Selecciona una dirección, usa tu ubicación actual o mueve el mapa.");
                return;
            }
            if (title.getText().toString().trim().isEmpty() || description.getText().toString().trim().isEmpty()) {
                showSnack("Agrega título y descripción para publicar.");
                return;
            }
            AlertType type = AlertType.valueOf(typeSpinner.getSelectedItem().toString());
            int cost = TokenRules.costFor(type);
            if (currentTokens < cost) {
                showInsufficientTokens(cost);
                return;
            }
            int greenMinutes = Integer.parseInt(durationSpinner.getSelectedItem().toString().replace(" min", ""));
            viewModel.publish(type, title.getText().toString().trim(), description.getText().toString().trim(), selectedAddress, selectedLatitude, selectedLongitude, userId, greenMinutes).observe(this, resource -> {
                progress.setVisibility(resource.status.name().equals("LOADING") ? View.VISIBLE : View.GONE);
                if (resource.status.name().equals("SUCCESS")) {
                    showSnack("Alerta publicada en el mapa.");
                    finish();
                }
                if (resource.status.name().equals("ERROR")) {
                    if (resource.message != null && resource.message.contains("Ver anuncio")) rewardAdButton.setVisibility(View.VISIBLE);
                    showSnack(resource.message);
                }
            });
        });
    }

    private void showInsufficientTokens(int cost) {
        rewardAdButton.setVisibility(View.VISIBLE);
        showSnack("Necesitas " + cost + " tokens. Ver anuncio para ganar 20");
    }

    private void showRewardedAd(ProfileViewModel profileViewModel) {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            showSnack("Debes iniciar sesión.");
            return;
        }
        adManager.showRewardedAd(this, new RewardedAdManager.RewardCallback() {
            @Override
            public void onRewardEarned() {
                profileViewModel.rewardAdTokens(userId).observe(PublishAlertActivity.this, resource -> {
                    if (resource.status.name().equals("SUCCESS")) {
                        rewardAdButton.setVisibility(View.GONE);
                        showSnack("+20 tokens agregados");
                    }
                    if (resource.status.name().equals("ERROR")) showSnack(resource.message);
                });
            }

            @Override
            public void onAdUnavailable(String message) {
                showSnack("No se pudo cargar el anuncio, intenta nuevamente");
            }
        });
    }

    private void startLocationIfAllowed(boolean requestPermission) {
        if (!locationService.hasLocationPermission()) {
            if (requestPermission) permissionLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
            else showSnack("Activa el GPS para priorizar sugerencias cercanas.");
            return;
        }
        observeLocation();
    }

    private void observeLocation() {
        locationService.observeLocation().observe(this, location -> {
            if (location == null) return;
            currentLatitude = location.getLatitude();
            currentLongitude = location.getLongitude();
            renderUserMarker();
            if (useCurrentAfterPermission) {
                useCurrentAfterPermission = false;
                cameraCenteredOnGps = true;
                centerMap(currentLatitude, currentLongitude, 16.2);
                selectPoint(currentLatitude, currentLongitude, "Tu ubicación actual", true);
                return;
            }
            if (!cameraCenteredOnGps) {
                cameraCenteredOnGps = true;
                centerMap(currentLatitude, currentLongitude, 15.4);
                selectPoint(currentLatitude, currentLongitude, "Tu ubicación actual", true);
            }
        });
    }

    private void searchAddress(String query) {
        if (query == null || query.trim().length() < 2) {
            suggestionsList.setVisibility(View.GONE);
            addressStatusText.setText("Escribe al menos 2 letras para ver sugerencias cercanas.");
            return;
        }
        addressStatusText.setText("Buscando lugares cercanos...");
        geocodingRepository.autocomplete(query, currentLatitude, currentLongitude).observe(this, resource -> {
            if (resource.status.name().equals("SUCCESS") && resource.data != null) updateSuggestions(resource.data);
            if (resource.status.name().equals("ERROR")) {
                suggestionsList.setVisibility(View.GONE);
                addressStatusText.setText("No se pudieron cargar sugerencias.");
                showSnack(resource.message);
            }
        });
    }

    private void updateSuggestions(List<AddressSuggestion> suggestions) {
        suggestionAdapter.submitList(suggestions);
        suggestionsList.setVisibility(suggestions == null || suggestions.isEmpty() ? View.GONE : View.VISIBLE);
        if (suggestions == null || suggestions.isEmpty()) {
            addressStatusText.setText("Sin resultados cercanos. Intenta con otra calle o lugar.");
        } else {
            addressStatusText.setText("Selecciona un resultado o mueve el mapa para ajustar el punto.");
        }
    }

    private void selectSuggestion(AddressSuggestion suggestion) {
        hideKeyboard();
        suggestionsList.setVisibility(View.GONE);
        selectingAddress = true;
        addressInput.setText(suggestion.name);
        addressInput.setSelection(addressInput.getText().length());
        selectingAddress = false;
        selectedAddress = suggestion.address;
        selectedLatitude = suggestion.latitude;
        selectedLongitude = suggestion.longitude;
        addressStatusText.setText(suggestion.address + " · " + suggestion.distanceLabel());
        centerMap(suggestion.latitude, suggestion.longitude, 16.6);
    }

    private void useCurrentLocation() {
        if (!locationService.hasLocationPermission()) {
            useCurrentAfterPermission = true;
            startLocationIfAllowed(true);
            return;
        }
        if (currentLatitude == null || currentLongitude == null) {
            useCurrentAfterPermission = true;
            showSnack("Esperando señal GPS para usar tu ubicación actual.");
            observeLocation();
            return;
        }
        hideKeyboard();
        suggestionsList.setVisibility(View.GONE);
        centerMap(currentLatitude, currentLongitude, 16.2);
        selectPoint(currentLatitude, currentLongitude, "Tu ubicación actual", true);
    }

    private void reverseGeocodeCameraCenter() {
        Point center = mapView.getMapboxMap().getCameraState().getCenter();
        double latitude = center.latitude();
        double longitude = center.longitude();
        addressStatusText.setText("Actualizando dirección del punto central...");
        geocodingRepository.reverseGeocode(latitude, longitude).observe(this, resource -> {
            if (resource.status.name().equals("SUCCESS") && resource.data != null) {
                AddressSuggestion suggestion = resource.data;
                selectedLatitude = latitude;
                selectedLongitude = longitude;
                selectedAddress = suggestion.address;
                selectingAddress = true;
                addressInput.setText(suggestion.name);
                addressInput.setSelection(addressInput.getText().length());
                selectingAddress = false;
                addressStatusText.setText(suggestion.address);
            }
            if (resource.status.name().equals("ERROR")) {
                selectPoint(latitude, longitude, "Punto seleccionado", false);
                showSnack(resource.message);
            }
        });
    }

    private void selectPoint(double latitude, double longitude, String address, boolean reverseGeocode) {
        selectedLatitude = latitude;
        selectedLongitude = longitude;
        selectedAddress = address;
        selectingAddress = true;
        addressInput.setText(address);
        addressInput.setSelection(addressInput.getText().length());
        selectingAddress = false;
        addressStatusText.setText(address);
        if (reverseGeocode) reverseGeocodeCameraCenter();
    }

    private void clearSelectedAddress() {
        selectedLatitude = null;
        selectedLongitude = null;
        selectedAddress = null;
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

    private void renderUserMarker() {
        userLocationMarker.setVisibility(View.VISIBLE);
        if (userPulseStarted) return;
        userPulseStarted = true;
        pulseUserGlow();
    }

    private void pulseUserGlow() {
        if (isFinishing() || userGlow == null || userGlow.getWindowToken() == null) {
            userPulseStarted = false;
            return;
        }
        userGlow.setScaleX(0.78f);
        userGlow.setScaleY(0.78f);
        userGlow.setAlpha(0.9f);
        userGlow.animate().scaleX(1.35f).scaleY(1.35f).alpha(0.2f).setDuration(1100L).withEndAction(this::pulseUserGlow).start();
    }

    private void hideKeyboard() {
        InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus() == null ? addressInput : getCurrentFocus();
        if (manager != null) manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        addressInput.clearFocus();
    }

    private void showSnack(String message) {
        Snackbar.make(findViewById(android.R.id.content), message == null ? "Ocurrió un problema." : message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getColor(R.color.urban_surface_high))
                .setTextColor(getColor(R.color.urban_text))
                .show();
    }
}
