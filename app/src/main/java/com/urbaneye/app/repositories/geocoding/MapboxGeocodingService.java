package com.urbaneye.app.repositories.geocoding;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MapboxGeocodingService {
    @GET("geocoding/v5/mapbox.places/{query}.json")
    Call<MapboxGeocodingResponse> search(
            @Path("query") String query,
            @Query("access_token") String accessToken,
            @Query("autocomplete") boolean autocomplete,
            @Query("limit") int limit,
            @Query("language") String language,
            @Query("types") String types
    );
}
