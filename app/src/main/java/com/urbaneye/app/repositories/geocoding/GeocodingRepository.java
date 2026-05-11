package com.urbaneye.app.repositories.geocoding;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.urbaneye.app.BuildConfig;
import com.urbaneye.app.utils.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GeocodingRepository {
    private final MapboxGeocodingService service;

    public GeocodingRepository() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.mapbox.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(MapboxGeocodingService.class);
    }

    public LiveData<Resource<List<AddressSuggestion>>> autocomplete(String query, Double userLatitude, Double userLongitude) {
        MutableLiveData<Resource<List<AddressSuggestion>>> result = new MutableLiveData<>(Resource.loading());
        if (query == null || query.trim().length() < 2) {
            result.setValue(Resource.success(new ArrayList<>()));
            return result;
        }
        if (!hasMapboxToken(result)) return result;
        String proximity = proximityValue(userLatitude, userLongitude);
        service.search(query.trim(), BuildConfig.MAPBOX_ACCESS_TOKEN, true, 8, "es", "address,poi,place,locality,neighborhood", proximity)
                .enqueue(new Callback<MapboxGeocodingResponse>() {
                    @Override
                    public void onResponse(Call<MapboxGeocodingResponse> call, Response<MapboxGeocodingResponse> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            result.setValue(Resource.error("Mapbox no respondió. Intenta nuevamente."));
                            return;
                        }
                        List<AddressSuggestion> suggestions = mapSuggestions(response.body(), userLatitude, userLongitude);
                        result.setValue(Resource.success(suggestions));
                    }

                    @Override
                    public void onFailure(Call<MapboxGeocodingResponse> call, Throwable t) {
                        result.setValue(Resource.error("Sin internet o Mapbox no disponible."));
                    }
                });
        return result;
    }

    public LiveData<Resource<AddressSuggestion>> reverseGeocode(double latitude, double longitude) {
        MutableLiveData<Resource<AddressSuggestion>> result = new MutableLiveData<>(Resource.loading());
        if (!hasMapboxToken(result)) return result;
        String coordinates = String.format(Locale.US, "%.7f,%.7f", longitude, latitude);
        service.search(coordinates, BuildConfig.MAPBOX_ACCESS_TOKEN, false, 1, "es", "address,poi,place,locality,neighborhood", null)
                .enqueue(new Callback<MapboxGeocodingResponse>() {
                    @Override
                    public void onResponse(Call<MapboxGeocodingResponse> call, Response<MapboxGeocodingResponse> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            result.setValue(Resource.error("No se pudo actualizar la dirección."));
                            return;
                        }
                        List<AddressSuggestion> suggestions = mapSuggestions(response.body(), latitude, longitude);
                        if (suggestions.isEmpty()) {
                            result.setValue(Resource.success(new AddressSuggestion("Punto seleccionado", "Dirección no disponible", latitude, longitude, 0)));
                            return;
                        }
                        result.setValue(Resource.success(suggestions.get(0)));
                    }

                    @Override
                    public void onFailure(Call<MapboxGeocodingResponse> call, Throwable t) {
                        result.setValue(Resource.error("Sin internet. No se pudo actualizar la dirección."));
                    }
                });
        return result;
    }

    private <T> boolean hasMapboxToken(MutableLiveData<Resource<T>> result) {
        if (BuildConfig.MAPBOX_ACCESS_TOKEN == null || BuildConfig.MAPBOX_ACCESS_TOKEN.isEmpty()) {
            result.setValue(Resource.error("Configura MAPBOX_ACCESS_TOKEN para usar Mapbox."));
            return false;
        }
        return true;
    }

    private List<AddressSuggestion> mapSuggestions(MapboxGeocodingResponse body, Double originLatitude, Double originLongitude) {
        List<AddressSuggestion> suggestions = new ArrayList<>();
        if (body.features == null) return suggestions;
        for (MapboxGeocodingResponse.Feature feature : body.features) {
            if (feature.center == null || feature.center.size() < 2) continue;
            double longitude = feature.center.get(0);
            double latitude = feature.center.get(1);
            String name = feature.text;
            String address = feature.placeName;
            if (feature.properties != null && feature.properties.address != null && !feature.properties.address.trim().isEmpty()) {
                address = feature.properties.address + " · " + feature.placeName;
            }
            suggestions.add(new AddressSuggestion(name, address, latitude, longitude, distanceBetween(originLatitude, originLongitude, latitude, longitude)));
        }
        return suggestions;
    }

    private String proximityValue(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) return "ip";
        return String.format(Locale.US, "%.7f,%.7f", longitude, latitude);
    }

    private double distanceBetween(Double originLatitude, Double originLongitude, double latitude, double longitude) {
        if (originLatitude == null || originLongitude == null) return 0;
        double earthRadius = 6371000.0;
        double dLat = Math.toRadians(latitude - originLatitude);
        double dLng = Math.toRadians(longitude - originLongitude);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(originLatitude)) * Math.cos(Math.toRadians(latitude))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return earthRadius * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
