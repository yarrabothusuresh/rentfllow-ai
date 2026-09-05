package com.rentflow.aisales;

import com.rentflow.aisales.dto.CopilotChatRequestDTO;
import com.rentflow.aisales.dto.CopilotResponseDTO;
import com.rentflow.aisales.dto.ToolCallRequestDTO;
import com.rentflow.aisales.dto.ToolCallResultDTO;
import com.rentflow.aisales.model.CopilotIntent;
import com.rentflow.aisales.service.AiCopilotService;
import com.rentflow.aisales.tool.AiSalesToolRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class Day28CopilotAnalyticsTest {

    private static final String TENANT_ID = "99999999-9999-9999-9999-999999999999";

    @Autowired
    private AiCopilotService copilotService;

    @Autowired
    private AiSalesToolRegistry toolRegistry;

    @Test
    public void testExecutiveDashboardTool_OwnerAllowed_ReturnsKpis() {
        ToolCallRequestDTO req = new ToolCallRequestDTO("getExecutiveDashboard", Map.of("dateRange", "THIS_MONTH"));
        ToolCallResultDTO result = toolRegistry.executeTool(TENANT_ID, "OWNER", req);

        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertTrue(result.getData().containsKey("bookedRevenue"));
        assertTrue(result.getData().containsKey("grossMarginPercent"));
        assertTrue(result.getData().containsKey("bookingsCount"));
    }

    @Test
    public void testExecutiveDashboardTool_CustomerForbidden() {
        ToolCallRequestDTO req = new ToolCallRequestDTO("getExecutiveDashboard", Map.of("dateRange", "THIS_MONTH"));
        ToolCallResultDTO result = toolRegistry.executeTool(TENANT_ID, "CUSTOMER", req);

        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("Permission denied"));
    }

    @Test
    public void testProfitabilitySummaryTool_ReturnsSummary() {
        ToolCallRequestDTO req = new ToolCallRequestDTO("getProfitabilitySummary", Map.of("dateRange", "THIS_MONTH"));
        ToolCallResultDTO result = toolRegistry.executeTool(TENANT_ID, "FINANCE", req);

        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertTrue(result.getData().containsKey("totalBookings"));
        assertTrue(result.getData().containsKey("totalRevenue"));
    }

    @Test
    public void testMarginTrendTool_ExplainsDriversAndNotice() {
        ToolCallRequestDTO req = new ToolCallRequestDTO("getMarginTrend", Map.of());
        ToolCallResultDTO result = toolRegistry.executeTool(TENANT_ID, "OWNER", req);

        assertTrue(result.isSuccess());
        Map<String, Object> data = result.getData();
        assertNotNull(data);
        assertTrue(data.containsKey("currentPeriodMargin"));
        assertTrue(data.containsKey("primaryDrivers"));
        assertTrue(data.containsKey("dataQualityNotice"));
    }

    @Test
    public void testArAgingTool_CalculatesOverdueBuckets() {
        ToolCallRequestDTO req = new ToolCallRequestDTO("getArAging", Map.of());
        ToolCallResultDTO result = toolRegistry.executeTool(TENANT_ID, "FINANCE", req);

        assertTrue(result.isSuccess());
        Map<String, Object> data = result.getData();
        assertNotNull(data);
        assertTrue(data.containsKey("totalOverdueMoreThan30Days"));
        assertTrue(data.containsKey("topDebtorCustomers"));
    }

    @Test
    public void testCopilotChat_ExecutiveSummaryQuestion() {
        CopilotResponseDTO initial = copilotService.startConversation(TENANT_ID, "OWNER", "user-owner", null, null);
        assertNotNull(initial.getConversationId());

        CopilotChatRequestDTO chatReq = new CopilotChatRequestDTO();
        chatReq.setMessage("How are we doing this month?");

        CopilotResponseDTO response = copilotService.processMessage(TENANT_ID, "OWNER", "user-owner", initial.getConversationId(), chatReq);
        assertNotNull(response);
        assertEquals(CopilotIntent.EXECUTIVE_SUMMARY, response.getDetectedIntent());
        assertTrue(response.isDeterministic());
        assertFalse(response.getDataBlocks().isEmpty());
    }

    @Test
    public void testCopilotChat_MarginFallQuestion() {
        CopilotResponseDTO initial = copilotService.startConversation(TENANT_ID, "OWNER", "user-owner", null, null);

        CopilotChatRequestDTO chatReq = new CopilotChatRequestDTO();
        chatReq.setMessage("Why did margin fall?");

        CopilotResponseDTO response = copilotService.processMessage(TENANT_ID, "OWNER", "user-owner", initial.getConversationId(), chatReq);
        assertNotNull(response);
        assertEquals(CopilotIntent.MARGIN_DROP_EXPLANATION, response.getDetectedIntent());
        assertTrue(response.getMessage().contains("Margin Trend Analysis"));
    }

    @Test
    public void testCopilotChat_HistoricalDataBoundary_NoHallucination() {
        CopilotResponseDTO initial = copilotService.startConversation(TENANT_ID, "OWNER", "user-owner", null, null);

        CopilotChatRequestDTO chatReq = new CopilotChatRequestDTO();
        chatReq.setMessage("What was our profit in 2018?");

        CopilotResponseDTO response = copilotService.processMessage(TENANT_ID, "OWNER", "user-owner", initial.getConversationId(), chatReq);
        assertNotNull(response);
        assertEquals(CopilotIntent.HISTORICAL_ANALYSIS, response.getDetectedIntent());
        assertTrue(response.getMessage().contains("No historical records exist"));
    }
}
