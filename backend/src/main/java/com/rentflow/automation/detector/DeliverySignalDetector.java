package com.rentflow.automation.detector;

import com.rentflow.automation.model.AutomationActionType;
import com.rentflow.automation.model.BusinessSignalCategory;
import com.rentflow.automation.model.BusinessSignalSeverity;
import com.rentflow.automation.model.BusinessSignalType;
import com.rentflow.delivery.model.Delivery;
import com.rentflow.delivery.model.DeliveryStatus;
import com.rentflow.delivery.model.Driver;
import com.rentflow.delivery.repository.DeliveryRepository;
import com.rentflow.delivery.repository.DriverRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;

@Component
public class DeliverySignalDetector implements BusinessSignalDetector {

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Override
    public List<BusinessSignalType> getSupportedTypes() {
        return List.of(
            BusinessSignalType.DELIVERY_MISSING_DRIVER,
            BusinessSignalType.DELIVERY_AT_RISK_TOMORROW
        );
    }

    @Override
    public List<DetectedSignal> detect(String tenantId) {
        List<DetectedSignal> signals = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        List<Delivery> deliveries = deliveryRepository.findByTenantId(tenantId);
        List<Driver> activeDrivers = driverRepository.findByTenantIdAndActiveTrue(tenantId);
        String suggestedDriverId = !activeDrivers.isEmpty() ? activeDrivers.get(0).getId().toString() : null;
        String suggestedDriverName = !activeDrivers.isEmpty() ? activeDrivers.get(0).getFullName() : null;

        for (Delivery d : deliveries) {
            if (d.getStatus() == DeliveryStatus.CANCELLED || d.getStatus() == DeliveryStatus.DELIVERED) {
                continue;
            }

            LocalDate schedDate = d.getScheduledDate();
            if (schedDate == null) continue;

            boolean isTodayOrTomorrow = schedDate.equals(today) || schedDate.equals(tomorrow);

            // 1. DELIVERY_MISSING_DRIVER
            if (d.getDriverId() == null && (isTodayOrTomorrow || schedDate.isBefore(today.plusDays(3)))) {
                Map<String, Object> evidence = new LinkedHashMap<>();
                evidence.put("deliveryNumber", d.getDeliveryNumber());
                evidence.put("scheduledDate", schedDate.toString());
                evidence.put("scheduledStartTime", d.getScheduledStartTime());
                evidence.put("scheduledEndTime", d.getScheduledEndTime());
                evidence.put("deliveryAddress", d.getDeliveryAddressSnapshot());
                evidence.put("currentStatus", d.getStatus().name());
                evidence.put("availableDriversCount", activeDrivers.size());
                if (suggestedDriverName != null) {
                    evidence.put("suggestedDriver", suggestedDriverName);
                }

                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("deliveryId", d.getId().toString());
                payload.put("deliveryNumber", d.getDeliveryNumber());
                if (suggestedDriverId != null) {
                    payload.put("driverId", suggestedDriverId);
                    payload.put("driverName", suggestedDriverName);
                }

                signals.add(new DetectedSignal(
                    BusinessSignalType.DELIVERY_MISSING_DRIVER,
                    BusinessSignalCategory.DELIVERY,
                    "DELIVERY",
                    d.getId().toString(),
                    d.getDeliveryNumber(),
                    schedDate.equals(today) ? BusinessSignalSeverity.CRITICAL : BusinessSignalSeverity.HIGH,
                    tenantId + ":DELIVERY_MISSING_DRIVER:" + d.getId(),
                    evidence,
                    AutomationActionType.ASSIGN_DRIVER,
                    payload,
                    "Delivery " + d.getDeliveryNumber() + " has no driver assigned",
                    "Delivery " + d.getDeliveryNumber() + " scheduled for " + schedDate + " is missing an assigned driver.",
                    "Deliveries without drivers risk missed customer appointment windows, SLA penalties, and equipment transit delays."
                ));
            }

            // 2. DELIVERY_AT_RISK_TOMORROW
            if (schedDate.equals(tomorrow) && (d.getDriverId() == null || d.getVehicleId() == null)) {
                Map<String, Object> evidence = new LinkedHashMap<>();
                evidence.put("deliveryNumber", d.getDeliveryNumber());
                evidence.put("scheduledDate", schedDate.toString());
                evidence.put("missingDriver", d.getDriverId() == null);
                evidence.put("missingVehicle", d.getVehicleId() == null);
                evidence.put("deliveryStatus", d.getStatus().name());

                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("taskTitle", "Expedite Logistics: Delivery " + d.getDeliveryNumber() + " at risk tomorrow");
                payload.put("entityType", "DELIVERY");
                payload.put("entityId", d.getId().toString());
                payload.put("priority", "HIGH");
                payload.put("assignedRole", "ROLE_LOGISTICS");

                signals.add(new DetectedSignal(
                    BusinessSignalType.DELIVERY_AT_RISK_TOMORROW,
                    BusinessSignalCategory.DELIVERY,
                    "DELIVERY",
                    d.getId().toString(),
                    d.getDeliveryNumber(),
                    BusinessSignalSeverity.HIGH,
                    tenantId + ":DELIVERY_AT_RISK_TOMORROW:" + d.getId(),
                    evidence,
                    AutomationActionType.CREATE_INTERNAL_TASK,
                    payload,
                    "Delivery " + d.getDeliveryNumber() + " is at risk for tomorrow",
                    "Tomorrow's delivery " + d.getDeliveryNumber() + " is missing essential resources (driver: " + (d.getDriverId() != null) + ", vehicle: " + (d.getVehicleId() != null) + ").",
                    "Early logistics risk resolution avoids last-minute dispatch chaos and customer dissatisfaction."
                ));
            }
        }
        return signals;
    }
}
