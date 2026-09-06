package com.rentflow.automation.action;

import com.rentflow.automation.model.AutomationActionType;
import com.rentflow.delivery.model.Delivery;
import com.rentflow.delivery.model.DeliveryStatus;
import com.rentflow.delivery.model.Driver;
import com.rentflow.delivery.repository.DeliveryRepository;
import com.rentflow.delivery.repository.DriverRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class AssignDriverActionHandler implements AutomationActionHandler {

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Override
    public AutomationActionType getActionType() {
        return AutomationActionType.ASSIGN_DRIVER;
    }

    @Override
    public RevalidationResult revalidate(String tenantId, Map<String, Object> payload) {
        if (payload == null || !payload.containsKey("deliveryId")) {
            return RevalidationResult.stale("Missing deliveryId in action payload");
        }
        UUID deliveryId;
        try {
            deliveryId = UUID.fromString(payload.get("deliveryId").toString());
        } catch (Exception e) {
            return RevalidationResult.stale("Invalid deliveryId: " + payload.get("deliveryId"));
        }

        Delivery delivery = deliveryRepository.findByTenantIdAndId(tenantId, deliveryId).orElse(null);
        if (delivery == null) {
            return RevalidationResult.stale("Delivery not found or belongs to another tenant");
        }

        if (delivery.getStatus() == DeliveryStatus.CANCELLED || delivery.getStatus() == DeliveryStatus.DELIVERED) {
            return RevalidationResult.stale("Delivery " + delivery.getDeliveryNumber() + " is already " + delivery.getStatus());
        }

        if (delivery.getDriverId() != null) {
            return RevalidationResult.alreadyCompleted("Driver is already assigned to Delivery " + delivery.getDeliveryNumber());
        }

        return RevalidationResult.valid();
    }

    @Override
    public ActionResult execute(String tenantId, Map<String, Object> payload, String executedBy) {
        UUID deliveryId = UUID.fromString(payload.get("deliveryId").toString());
        Delivery delivery = deliveryRepository.findByTenantIdAndId(tenantId, deliveryId).orElseThrow();

        UUID driverId = null;
        if (payload.containsKey("driverId") && payload.get("driverId") != null) {
            try {
                driverId = UUID.fromString(payload.get("driverId").toString());
            } catch (Exception ignored) {}
        }

        if (driverId == null) {
            // Pick first active driver
            Driver firstDriver = driverRepository.findByTenantIdAndActiveTrue(tenantId).stream().findFirst().orElse(null);
            if (firstDriver != null) {
                driverId = firstDriver.getId();
            }
        }

        if (driverId == null) {
            return ActionResult.failure("Cannot assign driver", "No active drivers available for tenant " + tenantId);
        }

        Driver driver = driverRepository.findById(driverId).orElseThrow();
        delivery.setDriverId(driver.getId());
        if (delivery.getStatus() == DeliveryStatus.PENDING) {
            delivery.setStatus(DeliveryStatus.ASSIGNED);
        }
        deliveryRepository.save(delivery);

        return ActionResult.success(
            "Assigned Driver " + driver.getFullName() + " to Delivery " + delivery.getDeliveryNumber(),
            Map.of("deliveryNumber", delivery.getDeliveryNumber(), "driverId", driver.getId().toString(), "driverName", driver.getFullName())
        );
    }
}
