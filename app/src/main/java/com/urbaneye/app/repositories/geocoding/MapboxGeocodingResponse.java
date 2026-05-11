package com.urbaneye.app.repositories.geocoding;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MapboxGeocodingResponse {
    public List<Feature> features;

    public static class Feature {
        @SerializedName("place_name")
        public String placeName;
        public String text;
        public List<Double> center;
        public Properties properties;
    }

    public static class Properties {
        public String address;
        public String category;
    }
}
