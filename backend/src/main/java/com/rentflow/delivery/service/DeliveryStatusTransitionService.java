package com.rentflow.delivery.service;

import com.rentflow.delivery.model.DeliveryStatus;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Service
public class DeliveryStatusTransitionService {

    private static final Map<DeliveryStatus, Set<DeliveryStatus>> ALLOWED_TRANSITIONS = Map.of(
            DeliveryStatus.PENDING, Set.of(DeliveryStatus.SCHEDULED, DeliveryStatus.CANCELLED),
            DeliveryStatus.SCHEDULED, Set.of(DeliveryStatus.ASSIGNED, DeliveryStatus.CANCELLED),
            DeliveryStatus.ASSIGNED, Set.of(DeliveryStatus.SCHEDULED, DeliveryStatus.ASSIGNED, DeliveryStatus.READY, DeliveryStatus.OUT_FOR_DELIVERY, DeliveryStatus.CANCELLED),
            DeliveryStatus.READY, Set.of(DeliveryStatus.OUT_FOR_DELIVERY, DeliveryStatus.CANCELLED),
            DeliveryStatus.OUT_FOR_DELIVERY, Set.of(DeliveryStatus.ARRIVED, DeliveryStatus.FAILED),
            DeliveryStatus.ARRIVED, Set.of(DeliveryStatus.SETUP_IN_PROGRESS, DeliveryStatus.DELIVERED, DeliveryStatus.FAILED),
            DeliveryStatus.SETUP_IN_PROGRESS, Set.of(DeliveryStatus.DELIVERED, DeliveryStatus.FAILED),
            DeliveryStatus.DELIVERED, Set.of(),
            DeliveryStatus.CANCELLED, Set.of(),
            DeliveryStatus.FAILED, Set.of()
    );

    public void validateTransition(DeliveryStatus currentStatus, DeliveryStatus targetStatus) {
        if (currentStatus == targetStatus) return;

        Set<DeliveryStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(currentStatus, EnumSet.noneOf(DeliveryStatus.class));
        if (!allowed.contains(targetStatus)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid delivery status transition from " + currentStatus + " to " + targetStatus
            );
        }
    }
}
