package com.rentflow.automation;

import com.rentflow.automation.detector.DetectedSignal;
import com.rentflow.automation.model.*;
import com.rentflow.automation.repository.AiRecommendationRepository;
import com.rentflow.automation.repository.BusinessSignalRepository;
import com.rentflow.automation.service.BusinessSignalService;
import com.rentflow.automation.service.RecommendationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class Day29RecommendationAndExplanationTest {

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private BusinessSignalService businessSignalService;

    @Autowired
    private AiRecommendationRepository recommendationRepository;

    @Autowired
    private BusinessSignalRepository businessSignalRepository;

    private final String tenantId = "test-tenant-day29-recs";

    @BeforeEach
    void setup() {
        recommendationRepository.deleteAll();
        businessSignalRepository.deleteAll();
    }

    @Test
    void testRecommendationNumberingAndExplanation() {
        DetectedSignal detected = new DetectedSignal(
            BusinessSignalType.QUOTE_EXPIRING_SOON,
            BusinessSignalCategory.SALES,
            "QUOTE",
            UUID.randomUUID().toString(),
            "QUO-000999",
            BusinessSignalSeverity.HIGH,
            tenantId + ":QUOTE_EXPIRING_SOON:QUO-000999",
            Map.of("daysUntilExpiry", 1, "totalAmount", "3400.00", "customerName", "Global Media"),
            AutomationActionType.SEND_QUOTE_REMINDER,
            Map.of("quoteNumber", "QUO-000999", "recipientEmail", "media@global.com"),
            "Quote QUO-000999 is expiring tomorrow",
            "Quote QUO-000999 for Global Media worth $3400.00 expires tomorrow.",
            "Expiring quotes require immediate sales touch to prevent loss."
        );

        BusinessSignal signal = businessSignalService.recordOrUpdateSignal(tenantId, detected);
        AiRecommendation rec1 = recommendationService.createOrUpdateRecommendation(tenantId, signal, detected, null);

        assertNotNull(rec1);
        assertEquals("REC-000001", rec1.getRecommendationNumber(), "First recommendation should be formatted REC-000001");
        assertEquals(RecommendationStatus.NEW, rec1.getStatus());
        assertEquals(RecommendationPriority.HIGH, rec1.getPriority());
        assertEquals(AutomationActionType.SEND_QUOTE_REMINDER, rec1.getSuggestedActionType());
        assertTrue(rec1.getDetailedExplanation().contains("Global Media"), "Explanation must retain customer context");
        assertTrue(rec1.getDetailedExplanation().contains("3400.00"), "Explanation must retain financial evidence");

        // Second recommendation should be REC-000002
        DetectedSignal detected2 = new DetectedSignal(
            BusinessSignalType.DELIVERY_MISSING_DRIVER,
            BusinessSignalCategory.DELIVERY,
            "DELIVERY",
            UUID.randomUUID().toString(),
            "DEL-000888",
            BusinessSignalSeverity.CRITICAL,
            tenantId + ":DELIVERY_MISSING_DRIVER:DEL-000888",
            Map.of("scheduledDate", "2026-09-06"),
            AutomationActionType.ASSIGN_DRIVER,
            Map.of("deliveryNumber", "DEL-000888"),
            "Delivery DEL-000888 has no driver",
            "Delivery DEL-000888 scheduled for tomorrow has no driver.",
            "Late driver assignment causes dispatch bottlenecks."
        );

        BusinessSignal signal2 = businessSignalService.recordOrUpdateSignal(tenantId, detected2);
        AiRecommendation rec2 = recommendationService.createOrUpdateRecommendation(tenantId, signal2, detected2, null);
        assertEquals("REC-000002", rec2.getRecommendationNumber(), "Second recommendation should be formatted REC-000002");
    }

    @Test
    void testRecommendationReviewAndDismissal() {
        DetectedSignal detected = new DetectedSignal(
            BusinessSignalType.BOOKING_MARGIN_LOW,
            BusinessSignalCategory.PROFITABILITY,
            "BOOKING",
            UUID.randomUUID().toString(),
            "BKG-000777",
            BusinessSignalSeverity.MEDIUM,
            tenantId + ":BOOKING_MARGIN_LOW:BKG-000777",
            Map.of("marginPct", "12.5%"),
            AutomationActionType.CREATE_INTERNAL_TASK,
            Map.of("taskTitle", "Review margin on BKG-000777"),
            "Low margin alert on BKG-000777",
            "Booking BKG-000777 has 12.5% margin.",
            "Ensure operational profitability."
        );

        BusinessSignal signal = businessSignalService.recordOrUpdateSignal(tenantId, detected);
        AiRecommendation rec = recommendationService.createOrUpdateRecommendation(tenantId, signal, detected, null);

        // 1. Mark Reviewed
        AiRecommendation reviewed = recommendationService.markReviewed(rec.getId(), tenantId, "Operations Lead");
        assertEquals(RecommendationStatus.REVIEWED, reviewed.getStatus());
        assertEquals("Operations Lead", reviewed.getReviewedBy());

        // 2. Dismiss with reason
        AiRecommendation dismissed = recommendationService.dismissRecommendation(rec.getId(), tenantId, "Operations Lead", "Known promotional exception approved by VP");
        assertEquals(RecommendationStatus.DISMISSED, dismissed.getStatus());
        assertEquals("Known promotional exception approved by VP", dismissed.getDismissReason());

        // Underlying signal should be marked IGNORED
        BusinessSignal updatedSignal = businessSignalRepository.findById(signal.getId()).orElseThrow();
        assertEquals(BusinessSignalStatus.IGNORED, updatedSignal.getStatus());
    }
}
