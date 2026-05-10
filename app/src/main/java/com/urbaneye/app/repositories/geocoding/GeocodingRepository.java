package com.urbaneye.app.repositories.geocoding;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.urbaneye.app.BuildConfig;
import com.urbaneye.app.utils.Resource;

import java.util.ArrayList;
import java.util.List;

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

    public LiveData<Resource<List<AddressSuggestion>>> autocomplete(String query) {
        MutableLiveData<Resource<List<AddressSuggestion>>> result = new MutableLiveData<>(Resource.loading());
        if (query == null || query.trim().length() < 3) {
            result.setValue(Resource.success(new ArrayList<>()));
            return result;
        }
        if (BuildConfig.MAPBOX_ACCESS_TOKEN == null || BuildConfig.MAPBOX_ACCESS_TOKEN.isEmpty()) {
            result.setValue(Resource.error("Configura MAPBOX_ACCESS_TOKEN para buscar direcciones."));
            return result;
        }
        service.search(query.trim(), BuildConfig.MAPBOX_ACCESS_TOKEN, true, 5, "es", "address,place,poi,locality,neighborhood")
                .enqueue(new Callback<MapboxGeocodingResponse>() {
                    @Override
                    public void onResponse(Call<MapboxGeocodingResponse> call, Response<MapboxGeocodingResponse> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            result.setValue(Resource.error("No se pudo buscar la dirección."));
                            return;
                        }
                        List<AddressSuggestion> suggestions = new ArrayList<>();
                        if (response.body().features != null) {
                            for (MapboxGeocodingResponse.Feature feature : response.body().features) {
                                if (feature.center != null && feature.center.size() >= 2) {
                                    suggestions.add(new AddressSuggestion(feature.placeName, feature.center.get(1), feature.center.get(0)));
                                }
                            }
                        }
                        result.setValue(Resource.success(suggestions));
                    }

                    @Override
                    public void onFailure(Call<MapboxGeocodingResponse> call, Throwable t) {
                        result.setValue(Resource.error(t.getMessage()));
                    }
                });
        return result;
    }
}
