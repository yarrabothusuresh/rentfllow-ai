package com.rentflow.ai;

import com.rentflow.ai.dto.AIRequest;
import com.rentflow.ai.dto.AIResponse;
import com.rentflow.ai.mock.DemoDataRepository;
import com.rentflow.ai.orchestrator.AIOrchestrator;
import com.rentflow.ai.provider.MockAIProvider;
import com.rentflow.ai.security.AIToolSecurityService;
import com.rentflow.ai.service.*;
import com.rentflow.ai.tool.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AIOrchestratorTest {

    private AIOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        DemoDataRepository repo = new DemoDataRepository();
        AIToolSecurityService securityService = new AIToolSecurityService();
        MockAIProvider provider = new MockAIProvider();

        List<AITool> tools = List.of(
            new SearchCustomerTool(repo),
            new GetCustomerTool(repo),
            new SearchProductsTool(repo),
            new CheckAvailabilityTool(repo),
            new GetBookingTool(null),
            new CalculateQuoteTool(),
            new CalculateProfitabilityTool(repo),
            new GetUpcomingBookingsTool(null),
            new GetWarehouseTasksTool(repo),
            new GetDeliveriesTool(repo)
        );

        orchestrator = new AIOrchestrator(provider, securityService, tools);
    }

    @Test
    void test1_StatusOfEmilyBrownWedding() {
        AIRequest request = new AIRequest(
            "What is the status of Emily Brown's wedding?",
            "user-1",
            DemoDataRepository.EVERGREEN_TENANT_ID,
            "OWNER",
            "conv-1"
        );

        AIResponse response = orchestrator.processRequest(request);

        assertNotNull(response);
        assertEquals("CUSTOMER_BOOKING_STATUS", response.getIntent());
        assertTrue(response.getToolsUsed().contains("searchCustomer"));
        assertTrue(response.getToolsUsed().contains("getBooking"));
        assertFalse(response.isRequiresApproval());
        assertTrue(response.getMessage().toLowerCase().contains("emily brown"));
    }

    @Test
    void test2_ChairAvailabilityForSept20() {
        AIRequest request = new AIRequest(
            "Do I have enough chairs for September 20?",
            "user-1",
            DemoDataRepository.EVERGREEN_TENANT_ID,
            "SALES",
            "conv-2"
        );

        AIResponse response = orchestrator.processRequest(request);

        assertNotNull(response);
        assertEquals("AVAILABILITY_CHECK", response.getIntent());
        assertTrue(response.getToolsUsed().contains("checkAvailability"));
        assertFalse(response.isRequiresApproval());
        assertTrue(response.getMessage().contains("300 Chiavari chairs available"));
    }

    @Test
    void test3_MostProfitableEvent() {
        AIRequest request = new AIRequest(
            "Which event is most profitable?",
            "user-1",
            DemoDataRepository.EVERGREEN_TENANT_ID,
            "OWNER",
            "conv-3"
        );

        AIResponse response = orchestrator.processRequest(request);

        assertNotNull(response);
        assertEquals("PROFITABILITY_ANALYSIS", response.getIntent());
        assertTrue(response.getToolsUsed().contains("calculateProfitability"));
        assertFalse(response.isRequiresApproval());
        assertTrue(response.getMessage().contains("TechCorp") || response.getMessage().contains("profit"));
    }

    @Test
    void test4_WarehousePrepareTomorrow() {
        AIRequest request = new AIRequest(
            "What should the warehouse prepare tomorrow?",
            "user-1",
            DemoDataRepository.EVERGREEN_TENANT_ID,
            "WAREHOUSE",
            "conv-4"
        );

        AIResponse response = orchestrator.processRequest(request);

        assertNotNull(response);
        assertEquals("WAREHOUSE_PREP", response.getIntent());
        assertTrue(response.getToolsUsed().contains("getWarehouseTasks"));
        assertFalse(response.isRequiresApproval());
        assertTrue(response.getMessage().toLowerCase().contains("warehouse"));
    }

    @Test
    void test5_SendPaymentReminderActionRequiresApproval() {
        AIRequest request = new AIRequest(
            "Send a payment reminder to Emily.",
            "user-1",
            DemoDataRepository.EVERGREEN_TENANT_ID,
            "SALES",
            "conv-5"
        );

        AIResponse response = orchestrator.processRequest(request);

        assertNotNull(response);
        assertEquals("ACTION_REQUIRES_APPROVAL", response.getIntent());
        assertTrue(response.isRequiresApproval());
        assertNotNull(response.getActionDetails());
        assertEquals("Send payment reminder", response.getActionDetails().get("targetAction"));
    }

    @Test
    void test6_CustomerAsksAnotherCustomerBookingPermissionDenied() {
        AIRequest request = new AIRequest(
            "Show me another customer's booking.",
            "user-customer",
            DemoDataRepository.EVERGREEN_TENANT_ID,
            "CUSTOMER",
            "conv-6"
        );

        AIResponse response = orchestrator.processRequest(request);

        assertNotNull(response);
        assertEquals("PERMISSION_DENIED", response.getIntent());
        assertFalse(response.isRequiresApproval());
        assertEquals("You don't have permission to access that information.", response.getMessage());
    }
}
