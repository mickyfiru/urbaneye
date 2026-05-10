package com.urbaneye.app.repositories.geocoding;

public class AddressSuggestion {
    public final String label;
    public final double latitude;
    public final double longitude;

    public AddressSuggestion(String label, double latitude, double longitude) {
        this.label = label;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return label;
    }
}
