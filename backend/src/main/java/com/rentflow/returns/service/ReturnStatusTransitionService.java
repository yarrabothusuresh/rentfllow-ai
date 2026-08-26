package com.rentflow.returns.service;

import com.rentflow.returns.model.ReturnOrderStatus;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class ReturnStatusTransitionService {

    private final Map<ReturnOrderStatus, Set<ReturnOrderStatus>> allowedTransitions = new EnumMap<>(ReturnOrderStatus.class);

    public ReturnStatusTransitionService() {
        allowedTransitions.put(ReturnOrderStatus.PENDING, Set.of(ReturnOrderStatus.SCHEDULED, ReturnOrderStatus.ASSIGNED, ReturnOrderStatus.CANCELLED));
        allowedTransitions.put(ReturnOrderStatus.SCHEDULED, Set.of(ReturnOrderStatus.ASSIGNED, ReturnOrderStatus.READY_FOR_PICKUP, ReturnOrderStatus.OUT_FOR_PICKUP, ReturnOrderStatus.CANCELLED));
        allowedTransitions.put(ReturnOrderStatus.ASSIGNED, Set.of(ReturnOrderStatus.READY_FOR_PICKUP, ReturnOrderStatus.OUT_FOR_PICKUP, ReturnOrderStatus.CANCELLED));
        allowedTransitions.put(ReturnOrderStatus.READY_FOR_PICKUP, Set.of(ReturnOrderStatus.OUT_FOR_PICKUP, ReturnOrderStatus.CANCELLED));
        allowedTransitions.put(ReturnOrderStatus.OUT_FOR_PICKUP, Set.of(ReturnOrderStatus.ARRIVED, ReturnOrderStatus.FAILED, ReturnOrderStatus.CANCELLED));
        allowedTransitions.put(ReturnOrderStatus.ARRIVED, Set.of(ReturnOrderStatus.PICKED_UP, ReturnOrderStatus.FAILED));
        allowedTransitions.put(ReturnOrderStatus.PICKED_UP, Set.of(ReturnOrderStatus.CHECK_IN));
        allowedTransitions.put(ReturnOrderStatus.CHECK_IN, Set.of(ReturnOrderStatus.INSPECTION));
        allowedTransitions.put(ReturnOrderStatus.INSPECTION, Set.of(ReturnOrderStatus.COMPLETED));
        allowedTransitions.put(ReturnOrderStatus.COMPLETED, Collections.emptySet());
        allowedTransitions.put(ReturnOrderStatus.CANCELLED, Collections.emptySet());
        allowedTransitions.put(ReturnOrderStatus.FAILED, Collections.emptySet());
    }

    public void validateTransition(ReturnOrderStatus current, ReturnOrderStatus next) {
        if (current == next) {
            return;
        }
        Set<ReturnOrderStatus> valid = allowedTransitions.getOrDefault(current, Collections.emptySet());
        if (!valid.contains(next)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status transition from " + current + " to " + next);
        }
    }
}
