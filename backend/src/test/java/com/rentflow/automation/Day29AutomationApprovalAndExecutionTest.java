package com.rentflow.automation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentflow.automation.detector.DetectedSignal;
import com.rentflow.automation.model.*;
import com.rentflow.automation.repository.AiRecommendationRepository;
import com.rentflow.automation.repository.AutomationApprovalRepository;
import com.rentflow.automation.repository.AutomationExecutionRepository;
import com.rentflow.automation.service.*;
import com.rentflow.delivery.model.Delivery;
import com.rentflow.delivery.model.DeliveryStatus;
import com.rentflow.delivery.model.Driver;
import com.rentflow.delivery.repository.DeliveryRepository;
import com.rentflow.delivery.repository.DriverRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class Day29AutomationApprovalAndExecutionTest {

    @Autowired
    private AutomationApprovalService approvalService;

    @Autowired
    private AutomationExecutionService executionService;

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private BusinessSignalService businessSignalService;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private AutomationApprovalRepository approvalRepository;

    @Autowired
    private AutomationExecutionRepository executionRepository;

    @Autowired
    private AiRecommendationRepository recommendationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private final String tenantId = "test-tenant-day29-exec";

    @BeforeEach
    void setup() {
        approvalRepository.deleteAll();
        executionRepository.deleteAll();
        recommendationRepository.deleteAll();
    }

    @Test
    void testHumanApprovalAndExecutionFlow() throws Exception {
        // Setup Delivery and Driver
        Driver driver = new Driver();
        driver.setTenantId(tenantId);
        driver.setFullName("Marcus Cole");
        driver.setActive(true);
        driver = driverRepository.save(driver);

        Delivery delivery = new Delivery();
        delivery.setTenantId(tenantId);
        delivery.setDeliveryNumber("DEL-TEST-APP1");
        delivery.setBookingId(UUID.randomUUID());
        delivery.setWarehouseOrderId(UUID.randomUUID());
        delivery.setCustomerId(UUID.randomUUID());
        delivery.setEventId(UUID.randomUUID());
        delivery.setScheduledDate(LocalDate.now().plusDays(1));
        delivery.setStatus(DeliveryStatus.PENDING);
        delivery.setDriverId(null);
        delivery = deliveryRepository.save(delivery);

        DetectedSignal detected = new DetectedSignal(
            BusinessSignalType.DELIVERY_MISSING_DRIVER,
            BusinessSignalCategory.DELIVERY,
            "DELIVERY",
            delivery.getId().toString(),
            delivery.getDeliveryNumber(),
            BusinessSignalSeverity.HIGH,
            tenantId + ":DELIVERY_MISSING_DRIVER:" + delivery.getId(),
            Map.of("deliveryNumber", delivery.getDeliveryNumber()),
            AutomationActionType.ASSIGN_DRIVER,
            Map.of("deliveryId", delivery.getId().toString(), "driverId", driver.getId().toString()),
            "Delivery without driver",
            "DEL-TEST-APP1 has no driver",
            "Urgent delivery requirement"
        );

        BusinessSignal signal = businessSignalService.recordOrUpdateSignal(tenantId, detected);
        AiRecommendation rec = recommendationService.createOrUpdateRecommendation(tenantId, signal, detected, null);

        // 1. Create Approval Request
        AutomationApproval approval = approvalService.createApprovalRequest(
            tenantId, rec.getId(), AutomationActionType.ASSIGN_DRIVER, rec.getSuggestedActionPayloadJson(), "SYSTEM"
        );
        assertNotNull(approval);
        assertEquals(AutomationApprovalStatus.PENDING, approval.getStatus());

        // 2. Customer cannot approve
        assertThrows(SecurityException.class, () -> {
            approvalService.approveAndExecute(approval.getId(), tenantId, "customer-user", "Customer approval", "ROLE_CUSTOMER");
        }, "Customer role must be blocked from approving internal automations");

        // 3. Manager approves
        AutomationExecution exec = approvalService.approveAndExecute(
            approval.getId(), tenantId, "Operations Manager", "Confirmed available driver", "ROLE_MANAGER"
        );
        assertNotNull(exec);
        assertEquals(AutomationExecutionStatus.EXECUTED, exec.getExecutionStatus());
        assertTrue(exec.getResultSummary().contains("Assigned Driver Marcus Cole"));

        // 4. Verify Delivery state changed in database
        Delivery updatedDel = deliveryRepository.findById(delivery.getId()).orElseThrow();
        assertEquals(driver.getId(), updatedDel.getDriverId(), "Delivery should now have assigned driver");

        // 5. Verify Recommendation resolved
        AiRecommendation updatedRec = recommendationRepository.findById(rec.getId()).orElseThrow();
        assertEquals(RecommendationStatus.RESOLVED, updatedRec.getStatus());
    }

    @Test
    void testPreExecutionRevalidationDetectsStaleEntity() {
        // Create delivery that ALREADY has a driver assigned
        Driver driver = new Driver();
        driver.setTenantId(tenantId);
        driver.setFullName("Elena Vance");
        driver.setActive(true);
        driver = driverRepository.save(driver);

        Delivery delivery = new Delivery();
        delivery.setTenantId(tenantId);
        delivery.setDeliveryNumber("DEL-TEST-STALE");
        delivery.setBookingId(UUID.randomUUID());
        delivery.setWarehouseOrderId(UUID.randomUUID());
        delivery.setCustomerId(UUID.randomUUID());
        delivery.setEventId(UUID.randomUUID());
        delivery.setScheduledDate(LocalDate.now().plusDays(1));
        delivery.setStatus(DeliveryStatus.ASSIGNED);
        delivery.setDriverId(driver.getId()); // Already assigned!
        delivery = deliveryRepository.save(delivery);

        String payloadJson = "{\"deliveryId\":\"" + delivery.getId() + "\",\"driverId\":\"" + driver.getId() + "\"}";

        AutomationExecution exec = executionService.executeAction(
            tenantId,
            null,
            null,
            null,
            AutomationActionType.ASSIGN_DRIVER,
            payloadJson,
            tenantId + ":TEST:STALE:" + UUID.randomUUID(),
            "SYSTEM"
        );

        assertEquals(AutomationExecutionStatus.SKIPPED, exec.getExecutionStatus(), "Should be skipped because already completed");
        assertTrue(exec.getResultSummary().contains("Already completed"));
    }

    @Test
    void testApprovalRejectionFlow() {
        DetectedSignal detected = new DetectedSignal(
            BusinessSignalType.BOOKING_MARGIN_LOW,
            BusinessSignalCategory.PROFITABILITY,
            "BOOKING",
            UUID.randomUUID().toString(),
            "BKG-REJ-01",
            BusinessSignalSeverity.HIGH,
            tenantId + ":BOOKING_MARGIN_LOW:BKG-REJ-01",
            Map.of("margin", "10%"),
            AutomationActionType.CREATE_INTERNAL_TASK,
            Map.of("taskTitle", "Task 1"),
            "Low Margin",
            "Summary",
            "Importance"
        );

        BusinessSignal signal = businessSignalService.recordOrUpdateSignal(tenantId, detected);
        AiRecommendation rec = recommendationService.createOrUpdateRecommendation(tenantId, signal, detected, null);

        AutomationApproval approval = approvalService.createApprovalRequest(
            tenantId, rec.getId(), AutomationActionType.CREATE_INTERNAL_TASK, "{\"taskTitle\":\"Task 1\"}", "SYSTEM"
        );

        // Reject
        AutomationApproval rejected = approvalService.rejectApproval(
            approval.getId(), tenantId, "Sales Director", "Margin reduction is pre-agreed for annual client", "ROLE_ADMIN"
        );

        assertEquals(AutomationApprovalStatus.REJECTED, rejected.getStatus());
        assertEquals("Margin reduction is pre-agreed for annual client", rejected.getRejectionReason());

        AiRecommendation recAfter = recommendationRepository.findById(rec.getId()).orElseThrow();
        assertEquals(RecommendationStatus.REJECTED, recAfter.getStatus());
    }
}
