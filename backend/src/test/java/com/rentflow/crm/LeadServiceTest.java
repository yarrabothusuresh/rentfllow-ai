package com.rentflow.crm;

import com.rentflow.ai.model.Customer;
import com.rentflow.ai.model.CustomerType;
import com.rentflow.ai.repository.CustomerRepository;
import com.rentflow.crm.dto.*;
import com.rentflow.crm.model.*;
import com.rentflow.crm.service.LeadActivityService;
import com.rentflow.crm.service.LeadFollowUpService;
import com.rentflow.crm.service.LeadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class LeadServiceTest {

    @Autowired
    private LeadService leadService;

    @Autowired
    private LeadFollowUpService followUpService;

    @Autowired
    private LeadActivityService activityService;

    @Autowired
    private CustomerRepository customerRepository;

    private final String tenantId = "tenant-crm-test";

    @Test
    public void testSequentialLeadNumbering() {
        String num1 = leadService.generateLeadNumber(tenantId);
        assertNotNull(num1);
        assertTrue(num1.startsWith("LEAD-"));

        LeadCreateRequest req = new LeadCreateRequest();
        req.setFirstName("Alice");
        req.setEmail("alice@test.com");
        LeadDetailDTO l1 = leadService.createLead(tenantId, req, "test-user");
        assertNotNull(l1.getLeadNumber());

        LeadDetailDTO l2 = leadService.createLead(tenantId, req, "test-user");
        assertNotNull(l2.getLeadNumber());
        assertNotEquals(l1.getLeadNumber(), l2.getLeadNumber());
    }

    @Test
    public void testLeadLifecycleAndTransitions() {
        LeadCreateRequest req = new LeadCreateRequest();
        req.setFirstName("Bob");
        req.setLastName("Vance");
        req.setEmail("bob@vancerefrigeration.com");
        req.setPhone("555-0123");
        req.setEstimatedValue(new BigDecimal("2500.00"));

        LeadDetailDTO lead = leadService.createLead(tenantId, req, "test-user");
        assertEquals(LeadStage.NEW, lead.getStage());

        // Transition: NEW -> CONTACTED
        lead = leadService.transitionStage(tenantId, lead.getId(), new LeadTransitionRequest(LeadStage.CONTACTED), "test-user");
        assertEquals(LeadStage.CONTACTED, lead.getStage());
        assertNotNull(lead.getLastContactedAt());

        // Transition: CONTACTED -> QUALIFIED
        lead = leadService.qualifyLead(tenantId, lead.getId(), "test-user");
        assertEquals(LeadStage.QUALIFIED, lead.getStage());
        assertNotNull(lead.getQualifiedAt());

        final UUID leadId = lead.getId();
        // Invalid transition: QUALIFIED -> NEW should throw
        assertThrows(IllegalArgumentException.class, () ->
            leadService.transitionStage(tenantId, leadId, new LeadTransitionRequest(LeadStage.NEW), "test-user"));
    }

    @Test
    public void testLostAndReopenWorkflow() {
        LeadCreateRequest req = new LeadCreateRequest();
        req.setFirstName("Charlie");
        req.setEmail("charlie@test.com");
        LeadDetailDTO lead = leadService.createLead(tenantId, req, "test-user");
        final UUID leadId = lead.getId();

        // Mark Lost requires reason
        assertThrows(IllegalArgumentException.class, () ->
            leadService.markLost(tenantId, leadId, new LeadLostRequest(null, "Notes"), "test-user"));

        lead = leadService.markLost(tenantId, lead.getId(), new LeadLostRequest(LeadLostReason.PRICE, "Budget too tight"), "test-user");
        assertEquals(LeadStage.LOST, lead.getStage());
        assertEquals(LeadLostReason.PRICE, lead.getLostReason());
        assertNotNull(lead.getLostAt());

        // Direct transition from LOST is blocked
        assertThrows(IllegalStateException.class, () ->
            leadService.transitionStage(tenantId, leadId, new LeadTransitionRequest(LeadStage.QUALIFIED), "test-user"));

        // Reopen requires reason
        assertThrows(IllegalArgumentException.class, () ->
            leadService.reopenLead(tenantId, leadId, new LeadReopenRequest(""), "test-user"));

        lead = leadService.reopenLead(tenantId, lead.getId(), new LeadReopenRequest("Customer called back with increased budget"), "test-user");
        assertEquals(LeadStage.CONTACTED, lead.getStage());
        assertNotNull(lead.getReopenedAt());
    }

    @Test
    public void testFollowUpSchedulingAndOverdue() {
        LeadCreateRequest req = new LeadCreateRequest();
        req.setFirstName("Diana");
        req.setEmail("diana@test.com");
        LeadDetailDTO lead = leadService.createLead(tenantId, req, "test-user");

        // Schedule follow-up in the past (overdue)
        LeadFollowUpDTO fu = followUpService.scheduleFollowUp(tenantId, lead.getId(), FollowUpType.CALL,
                "Call Diana", "Discuss tent size", LocalDateTime.now().minusHours(2), "sales-1", "Sales Rep", "test-user");

        assertTrue(fu.isOverdue());
        assertEquals(FollowUpStatus.OPEN, fu.getStatus());

        // Complete follow-up
        fu = followUpService.completeFollowUp(tenantId, fu.getId(), "test-user");
        assertEquals(FollowUpStatus.COMPLETED, fu.getStatus());
        assertNotNull(fu.getCompletedAt());
    }

    @Test
    public void testCustomerConversionAndIdempotency() {
        LeadCreateRequest req = new LeadCreateRequest();
        req.setFirstName("Evan");
        req.setLastName("Wright");
        req.setEmail("evan.wright@example.com");
        req.setPhone("555-9876");
        req.setCompanyName("Wright Design Studio");
        LeadDetailDTO lead = leadService.createLead(tenantId, req, "test-user");

        assertNull(lead.getCustomerId());

        // Convert to customer
        lead = leadService.convertCustomer(tenantId, lead.getId(), false, "test-user");
        assertNotNull(lead.getCustomerId());
        UUID firstCustId = lead.getCustomerId();

        // Repeated conversion must be idempotent
        lead = leadService.convertCustomer(tenantId, lead.getId(), false, "test-user");
        assertEquals(firstCustId, lead.getCustomerId());
    }

    @Test
    public void testTenantIsolation() {
        LeadCreateRequest req = new LeadCreateRequest();
        req.setFirstName("Frank");
        req.setEmail("frank@tenantA.com");
        LeadDetailDTO leadA = leadService.createLead("tenant-A", req, "user-A");

        // Tenant B cannot retrieve Tenant A lead
        assertFalse(leadService.getLeadById("tenant-B", leadA.getId()).isPresent());
    }
}
