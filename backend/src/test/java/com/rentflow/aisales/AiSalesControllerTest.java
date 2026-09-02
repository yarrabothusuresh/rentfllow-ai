package com.rentflow.aisales;

import com.rentflow.ai.dto.QuoteDTO;
import com.rentflow.ai.model.Customer;
import com.rentflow.ai.model.QuoteStatus;
import com.rentflow.ai.repository.CustomerRepository;
import com.rentflow.ai.service.QuoteService;
import com.rentflow.aisales.controller.AiSalesController;
import com.rentflow.aisales.controller.CustomerAiSalesController;
import com.rentflow.aisales.dto.AiSalesChatRequestDTO;
import com.rentflow.aisales.dto.AiSalesChatResponseDTO;
import com.rentflow.aisales.dto.AiSalesDashboardDTO;
import com.rentflow.security.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
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
public class AiSalesControllerTest {

    @Autowired
    private AiSalesController salesController;

    @Autowired
    private CustomerAiSalesController customerController;

    @Autowired
    private QuoteService quoteService;

    @Autowired
    private CustomerRepository customerRepository;

    private final String tenantId = "test-controller-tenant";

    @BeforeEach
    public void setup() {
        SecurityUtils.setTestTenantId(tenantId);
    }

    @AfterEach
    public void tearDown() {
        SecurityUtils.clearTestTenantId();
    }

    @Test
    public void testGetDashboard() {
        ResponseEntity<AiSalesDashboardDTO> resp = salesController.getDashboard();
        assertEquals(200, resp.getStatusCode().value());
        assertNotNull(resp.getBody());
    }

    @Test
    public void testCustomerPortalStartConversation() {
        AiSalesChatRequestDTO req = new AiSalesChatRequestDTO();
        req.setMessage("Hi! I need tables for a banquet for 50 people.");

        ResponseEntity<AiSalesChatResponseDTO> resp = customerController.startOrResumeConversation(req);
        assertEquals(200, resp.getStatusCode().value());
        assertNotNull(resp.getBody());
        assertNotNull(resp.getBody().getConversationId());
    }

    @Test
    public void testApproveQuoteEndpoint() {
        Customer c = new Customer();
        c.setTenantId(tenantId);
        c.setCustomerNumber("CUST-991122");
        c.setFirstName("Approved Customer");
        c.setEmail("approved@example.com");
        c = customerRepository.save(c);

        QuoteDTO q = new QuoteDTO();
        q.setCustomerId(c.getId());
        q.setEventId(UUID.randomUUID());
        q.setStatus(QuoteStatus.DRAFT);
        q.setQuoteDate(LocalDate.now());
        q.setValidUntil(LocalDate.now().plusDays(7));
        q.setRentalStartDateTime(LocalDateTime.now().plusDays(5));
        q.setRentalEndDateTime(LocalDateTime.now().plusDays(7));
        q.setSubtotal(BigDecimal.valueOf(500.00));
        q.setTotalAmount(BigDecimal.valueOf(541.25));
        q.setItems(List.of());

        QuoteDTO created = quoteService.createQuote(tenantId, q, "TEST");

        ResponseEntity<Map<String, Object>> approveResp = salesController.approveQuote(created.getId());
        assertEquals(200, approveResp.getStatusCode().value());
        assertTrue((Boolean) approveResp.getBody().get("success"));
        assertEquals("SENT", approveResp.getBody().get("status"));
    }
}
