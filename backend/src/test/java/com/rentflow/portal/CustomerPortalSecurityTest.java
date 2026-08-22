package com.rentflow.portal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentflow.ai.model.Customer;
import com.rentflow.ai.model.CustomerStatus;
import com.rentflow.ai.model.Quote;
import com.rentflow.ai.model.QuoteStatus;
import com.rentflow.ai.repository.CustomerRepository;
import com.rentflow.ai.repository.QuoteRepository;
import com.rentflow.portal.dto.CustomerLoginRequestDTO;
import com.rentflow.portal.model.CustomerUser;
import com.rentflow.portal.repository.CustomerUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class CustomerPortalSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerUserRepository customerUserRepository;

    @Autowired
    private QuoteRepository quoteRepository;

    private String tenantId = "99999999-9999-9999-9999-999999999999";
    private UUID emilyCustomerId = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private UUID customerBId = UUID.fromString("77777777-7777-7777-7777-777777777777");
    private UUID quoteBId = UUID.fromString("88888888-8888-8888-8888-888888888888");

    @BeforeEach
    public void setUp() {
        if (!customerRepository.existsById(emilyCustomerId)) {
            Customer emily = new Customer();
            emily.setId(emilyCustomerId);
            emily.setTenantId(tenantId);
            emily.setCustomerNumber("CUS-SEC-EMILY-" + UUID.randomUUID().toString().substring(0, 6));
            emily.setFirstName("Emily");
            emily.setLastName("Brown");
            emily.setCompanyName("Brown Wedding");
            emily.setEmail("customer@abcevents.demo");
            emily.setStatus(CustomerStatus.ACTIVE);
            customerRepository.save(emily);
        }

        if (!customerRepository.existsById(customerBId)) {
            Customer custB = new Customer();
            custB.setId(customerBId);
            custB.setTenantId(tenantId);
            custB.setCustomerNumber("CUS-SEC-CUSTB-" + UUID.randomUUID().toString().substring(0, 6));
            custB.setFirstName("Robert");
            custB.setLastName("Vance");
            custB.setCompanyName("XYZ Events Inc.");
            custB.setEmail("customer.b@xyzevents.demo");
            custB.setStatus(CustomerStatus.ACTIVE);
            customerRepository.save(custB);
        }

        if (customerUserRepository.findByEmailIgnoreCase("customer@abcevents.demo").isEmpty()) {
            CustomerUser cu = new CustomerUser();
            cu.setTenantId(tenantId);
            cu.setCustomerId(emilyCustomerId);
            cu.setUserId(UUID.randomUUID());
            cu.setEmail("customer@abcevents.demo");
            cu.setPasswordHash("demo");
            cu.setActive(true);
            customerUserRepository.save(cu);
        }

        if (!quoteRepository.existsById(quoteBId)) {
            Quote qB = new Quote();
            qB.setId(quoteBId);
            qB.setTenantId(tenantId);
            qB.setQuoteNumber("QUO-SEC-B");
            qB.setCustomerId(customerBId);
            qB.setEventId(UUID.randomUUID());
            qB.setStatus(QuoteStatus.SENT);
            qB.setQuoteDate(LocalDate.now());
            qB.setValidUntil(LocalDate.now().plusDays(14));
            qB.setRentalStartDateTime(LocalDateTime.now().plusDays(10));
            qB.setRentalEndDateTime(LocalDateTime.now().plusDays(12));
            quoteRepository.save(qB);
        }
    }

    @Test
    @DisplayName("POST /api/portal/auth/login succeeds")
    public void testPortalLoginApi() throws Exception {
        CustomerLoginRequestDTO req = new CustomerLoginRequestDTO();
        req.setEmail("customer@abcevents.demo");
        req.setPassword("demo");

        mockMvc.perform(post("/api/portal/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    @DisplayName("GET /api/portal/dashboard succeeds")
    public void testGetDashboardApi() throws Exception {
        mockMvc.perform(get("/api/portal/dashboard")
                .header("X-Tenant-Id", tenantId)
                .header("X-Customer-Id", emilyCustomerId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName").exists());
    }

    @Test
    @DisplayName("GET /api/portal/quotes/{otherCustomerQuoteId} returns 403 FORBIDDEN")
    public void testCrossCustomerQuoteForbidden() throws Exception {
        mockMvc.perform(get("/api/portal/quotes/" + quoteBId)
                .header("X-Tenant-Id", tenantId)
                .header("X-Customer-Id", emilyCustomerId.toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/portal/requests creates new support message")
    public void testCreateRequestApi() throws Exception {
        Map<String, String> body = Map.of(
                "type", "BILLING_QUESTION",
                "subject", "Invoice Query",
                "message", "Can I get a copy of my tax breakdown?"
        );

        mockMvc.perform(post("/api/portal/requests")
                .header("X-Tenant-Id", tenantId)
                .header("X-Customer-Id", emilyCustomerId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.subject").value("Invoice Query"));
    }
}
