package com.rentflow.delivery.service;

import com.rentflow.delivery.model.Delivery;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class RouteOptimizationService {

    public List<Delivery> optimizeDeliverySequence(List<Delivery> deliveries) {
        if (deliveries == null || deliveries.size() <= 1) {
            return deliveries != null ? deliveries : new ArrayList<>();
        }

        // Sort by scheduled start time first, preserving time window constraints
        List<Delivery> sorted = new ArrayList<>(deliveries);
        sorted.sort(Comparator.comparing(d -> d.getScheduledStartTime() != null ? d.getScheduledStartTime() : "00:00"));

        for (int i = 0; i < sorted.size(); i++) {
            sorted.get(i).setSequenceNumber(i + 1);
        }
        return sorted;
    }

    public double calculateDistanceMiles(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return 5.0; // Default estimate
        }
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return Math.round(3958.8 * c * 10.0) / 10.0; // Miles
    }
}
