package com.urbaneye.app.repositories.geocoding;

import java.util.Locale;

public class AddressSuggestion {
    public final String name;
    public final String address;
    public final double latitude;
    public final double longitude;
    public final double distanceMeters;

    public AddressSuggestion(String name, String address, double latitude, double longitude, double distanceMeters) {
        this.name = name == null || name.trim().isEmpty() ? "Ubicación seleccionada" : name;
        this.address = address == null || address.trim().isEmpty() ? this.name : address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distanceMeters = Math.max(0, distanceMeters);
    }

    public String distanceLabel() {
        if (distanceMeters <= 0) return "Cerca de ti";
        if (distanceMeters < 1000) return String.format(Locale.getDefault(), "%.0f m", distanceMeters);
        return String.format(Locale.getDefault(), "%.1f km", distanceMeters / 1000.0);
    }

    @Override
    public String toString() {
        return name + " · " + address;
    }
}
