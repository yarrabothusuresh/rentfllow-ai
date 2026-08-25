package com.rentflow.delivery.service;

import org.springframework.stereotype.Service;

@Service
public class GeocodingService {

    public static class Coordinates {
        private final Double latitude;
        private final Double longitude;

        public Coordinates(Double latitude, Double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public Double getLatitude() { return latitude; }
        public Double getLongitude() { return longitude; }
    }

    public Coordinates geocodeAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return new Coordinates(null, null);
        }
        // Basic offline deterministic coordinate generator for testing/demo
        double lat = 40.7128 + (Math.abs(address.hashCode() % 100) * 0.001);
        double lng = -74.0060 + (Math.abs(address.hashCode() % 100) * 0.001);
        return new Coordinates(lat, lng);
    }
}
