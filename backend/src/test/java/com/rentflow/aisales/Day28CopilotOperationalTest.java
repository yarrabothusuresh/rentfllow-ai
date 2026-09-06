package com.rentflow.aisales;

import com.rentflow.ai.dto.BookingAttentionItemDTO;
import com.rentflow.ai.model.Booking;
import com.rentflow.ai.model.BookingStatus;
import com.rentflow.ai.repository.BookingRepository;
import com.rentflow.ai.service.BookingAttentionService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class Day28CopilotOperationalTest {

    private static final String TENANT_ID = "99999999-9999-9999-9999-999999999999";

    @Autowired
    private AiCopilotService copilotService;

    @Autowired
    private AiSalesToolRegistry toolRegistry;

    @Autowired
    private BookingAttentionService bookingAttentionService;

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    public void testBookingAttentionService_CalculatesSignals() {
        List<BookingAttentionItemDTO> items = bookingAttentionService.getBookingsNeedingAttention(TENANT_ID, 30);
        assertNotNull(items);
        // Each item must have severity and signals
        for (BookingAttentionItemDTO item : items) {
            assertNotNull(item.getBookingId());
            assertNotNull(item.getSeverity());
            assertFalse(item.getSignals().isEmpty());
        }
    }

    @Test
    public void testGetBookingsNeedingAttentionTool_Executes() {
        ToolCallRequestDTO req = new ToolCallRequestDTO("getBookingsNeedingAttention", Map.of("daysAhead", 7));
        ToolCallResultDTO result = toolRegistry.executeTool(TENANT_ID, "OPERATIONS", req);

        assertTrue(result.isSuccess());
        Map<String, Object> data = result.getData();
        assertNotNull(data);
        assertTrue(data.containsKey("totalBookingsNeedingAttention"));
        assertTrue(data.containsKey("attentionItems"));
    }

    @Test
    public void testGetWarehouseBlockersTool_Executes() {
        ToolCallRequestDTO req = new ToolCallRequestDTO("getWarehouseBlockers", Map.of());
        ToolCallResultDTO result = toolRegistry.executeTool(TENANT_ID, "WAREHOUSE", req);

        assertTrue(result.isSuccess());
        Map<String, Object> data = result.getData();
        assertNotNull(data);
        assertTrue(data.containsKey("totalOpenExceptions"));
        assertTrue(data.containsKey("unassignedPickOrdersCount"));
    }

    @Test
    public void testGetDeliveryRisksTool_Executes() {
        ToolCallRequestDTO req = new ToolCallRequestDTO("getDeliveryRisks", Map.of("daysAhead", 1));
        ToolCallResultDTO result = toolRegistry.executeTool(TENANT_ID, "OPERATIONS", req);

        assertTrue(result.isSuccess());
        Map<String, Object> data = result.getData();
        assertNotNull(data);
        assertTrue(data.containsKey("atRiskDeliveriesCount"));
    }

    @Test
    public void testGetSalesFollowUpsDueTool_Executes() {
        ToolCallRequestDTO req = new ToolCallRequestDTO("getSalesFollowUpsDue", Map.of());
        ToolCallResultDTO result = toolRegistry.executeTool(TENANT_ID, "SALES", req);

        assertTrue(result.isSuccess());
        Map<String, Object> data = result.getData();
        assertNotNull(data);
        assertTrue(data.containsKey("totalOverdueCount"));
        assertTrue(data.containsKey("dueTodayCount"));
    }

    @Test
    public void testSearchCustomersTool_Disambiguation() {
        ToolCallRequestDTO req = new ToolCallRequestDTO("searchCustomers", Map.of("query", "test"));
        ToolCallResultDTO result = toolRegistry.executeTool(TENANT_ID, "SALES", req);

        assertTrue(result.isSuccess());
        Map<String, Object> data = result.getData();
        assertNotNull(data);
        assertTrue(data.containsKey("matchCount"));
        assertTrue(data.containsKey("requiresDisambiguation"));
    }

    @Test
    public void testCopilotChat_ContextualPageQuery_ExplainsBlocker() {
        // Create an unconfirmed/blocked booking
        Booking booking = new Booking();
        booking.setTenantId(TENANT_ID);
        booking.setBookingNumber("BKG-CTX-01");
        booking.setCustomerId(UUID.randomUUID());
        booking.setQuoteId(UUID.randomUUID());
        booking.setEventId(UUID.randomUUID());
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setBookingDate(LocalDate.now());
        booking.setRentalStartDateTime(LocalDateTime.now().plusDays(1));
        booking.setRentalEndDateTime(LocalDateTime.now().plusDays(3));
        booking.setSubtotal(new BigDecimal("1000.00"));
        booking.setTotalAmount(new BigDecimal("1000.00"));
        booking.setContractSigned(false); // unsigned contract blocker
        booking.setDepositRequired(new BigDecimal("250.00"));
        booking.setDepositPaid(BigDecimal.ZERO); // unpaid deposit blocker
        Booking saved = bookingRepository.save(booking);

        CopilotResponseDTO initial = copilotService.startConversation(TENANT_ID, "OPERATIONS", "user-ops", "BOOKING", saved.getId().toString());

        CopilotChatRequestDTO chatReq = new CopilotChatRequestDTO();
        chatReq.setMessage("Why is this blocked?");
        chatReq.setPageContextType("BOOKING");
        chatReq.setPageContextId(saved.getId().toString());

        CopilotResponseDTO response = copilotService.processMessage(TENANT_ID, "OPERATIONS", "user-ops", initial.getConversationId(), chatReq);
        assertNotNull(response);
        assertEquals(CopilotIntent.ENTITY_EXPLANATION, response.getDetectedIntent());
        assertTrue(response.getMessage().contains("contract is unsigned") || response.getMessage().contains("deposit is unpaid"));
    }
}
