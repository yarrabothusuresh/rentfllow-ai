package com.rentflow.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentflow.ai.model.Product;
import com.rentflow.ai.model.ProductStatus;
import com.rentflow.ai.model.ProductType;
import com.rentflow.ai.repository.ProductRepository;
import com.rentflow.claims.model.ClaimStatus;
import com.rentflow.claims.model.ClaimType;
import com.rentflow.claims.model.DamageClaim;
import com.rentflow.claims.repository.DamageClaimRepository;
import com.rentflow.crm.dto.LeadCreateRequest;
import com.rentflow.crm.model.Lead;
import com.rentflow.crm.model.LeadSource;
import com.rentflow.crm.model.LeadStage;
import com.rentflow.crm.repository.LeadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Validates strict HTTP API multi-tenant isolation.
 * Verifies that when Tenant B sends HTTP requests with X-Tenant-Id header,
 * the backend properly scopes queries to Tenant B and NEVER leaks Tenant A data.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class HttpCrossTenantSecurityTest {

    private static final String TENANT_A = "11111111-1111-1111-1111-111111111111";
    private static final String TENANT_B = "22222222-2222-2222-2222-222222222222";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private LeadRepository leadRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private DamageClaimRepository damageClaimRepository;

    private Lead leadA;
    private Product prodA;
    private DamageClaim claimA;

    @BeforeEach
    public void setUp() {
        // Clear any leftover thread context
        SecurityUtils.clearContext();

        // Seed Tenant A data
        leadA = new Lead();
        leadA.setTenantId(TENANT_A);
        leadA.setLeadNumber("LEAD-A-HTTP-001");
        leadA.setFirstName("Alice");
        leadA.setLastName("Confidential");
        leadA.setEmail("alice@tenanta.com");
        leadA.setEventName("Tenant A VIP Event");
        leadA.setStage(LeadStage.NEW);
        leadA.setSource(LeadSource.WEBSITE);
        leadA.setEstimatedValue(BigDecimal.valueOf(15000));
        leadA = leadRepository.save(leadA);

        prodA = new Product();
        prodA.setId(UUID.randomUUID());
        prodA.setTenantId(TENANT_A);
        prodA.setName("Tenant A Premium Sound System");
        prodA.setSku("SKU-TENANT-A-SOUND");
        prodA.setProductType(ProductType.RENTAL_ITEM);
        prodA.setStatus(ProductStatus.ACTIVE);
        prodA.setQuantityOwned(10);
        prodA.setRentalPrice(BigDecimal.valueOf(1200));
        prodA = productRepository.save(prodA);

        claimA = new DamageClaim();
        claimA.setTenantId(TENANT_A);
        claimA.setClaimNumber("CLM-A-HTTP-001");
        claimA.setClaimType(ClaimType.DAMAGE);
        claimA.setStatus(ClaimStatus.OPEN);
        claimA.setBookingId(UUID.randomUUID());
        claimA.setReturnOrderId(UUID.randomUUID());
        claimA.setCustomerId(UUID.randomUUID());
        claimA.setEstimatedTotalCost(BigDecimal.valueOf(450));
        claimA = damageClaimRepository.save(claimA);
    }

    @Test
    @DisplayName("GET /api/crm/leads: Tenant B cannot see Tenant A's leads via HTTP")
    public void testCrmLeadsHttpTenantIsolation() throws Exception {
        // Tenant A sees lead A
        mockMvc.perform(get("/api/crm/leads")
                .header("X-Tenant-Id", TENANT_A)
                .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[?(@.id == '" + leadA.getId().toString() + "')]").exists());

        // Tenant B must NOT see lead A
        mockMvc.perform(get("/api/crm/leads")
                .header("X-Tenant-Id", TENANT_B)
                .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id == '" + leadA.getId().toString() + "')]").doesNotExist());
    }

    @Test
    @DisplayName("GET /api/crm/leads/{id}: Tenant B gets 404 when accessing Tenant A's lead")
    public void testCrmLeadDetailHttpTenantIsolation() throws Exception {
        // Tenant A accesses lead A -> 200 OK
        mockMvc.perform(get("/api/crm/leads/" + leadA.getId())
                .header("X-Tenant-Id", TENANT_A)
                .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(leadA.getId().toString()))
                .andExpect(jsonPath("$.firstName").value("Alice"));

        // Tenant B attempts to access lead A -> 404 Not Found
        mockMvc.perform(get("/api/crm/leads/" + leadA.getId())
                .header("X-Tenant-Id", TENANT_B)
                .header("X-User-Role", "ADMIN"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/crm/leads: Tenant B creates lead correctly stamped with Tenant B ID")
    public void testCrmLeadCreateHttpTenantStamping() throws Exception {
        LeadCreateRequest req = new LeadCreateRequest();
        req.setFirstName("Bob");
        req.setLastName("TenantB");
        req.setEmail("bob@tenantb.com");
        req.setEventName("Tenant B Gala");

        mockMvc.perform(post("/api/crm/leads")
                .header("X-Tenant-Id", TENANT_B)
                .header("X-User-Role", "SALES")
                .header("X-User-Name", "Tenant B Agent")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.firstName").value("Bob"));

        // Verify that the new lead belongs to Tenant B in repository, not Tenant A
        Lead createdLead = leadRepository.findAll().stream()
                .filter(l -> "bob@tenantb.com".equals(l.getEmail()))
                .findFirst()
                .orElseThrow();
        assertEquals(TENANT_B, createdLead.getTenantId(), "Lead created by Tenant B must have Tenant B's tenantId");
    }

    @Test
    @DisplayName("GET /api/inventory/v2/products: Tenant B cannot see Tenant A's products via HTTP")
    public void testInventoryProductsHttpTenantIsolation() throws Exception {
        // Tenant A sees prod A
        mockMvc.perform(get("/api/inventory/v2/products")
                .header("X-Tenant-Id", TENANT_A)
                .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '" + prodA.getId().toString() + "')]").exists());

        // Tenant B must NOT see prod A
        mockMvc.perform(get("/api/inventory/v2/products")
                .header("X-Tenant-Id", TENANT_B)
                .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '" + prodA.getId().toString() + "')]").doesNotExist());
    }

    @Test
    @DisplayName("GET /api/damage-claims: Tenant B cannot see Tenant A's damage claims via HTTP")
    public void testDamageClaimHttpTenantIsolation() throws Exception {
        // Tenant A can access their claims
        mockMvc.perform(get("/api/damage-claims")
                .header("X-Tenant-Id", TENANT_A)
                .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '" + claimA.getId().toString() + "')]").exists());

        // Tenant B must NOT see Tenant A's claim
        mockMvc.perform(get("/api/damage-claims")
                .header("X-Tenant-Id", TENANT_B)
                .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '" + claimA.getId().toString() + "')]").doesNotExist());
    }

    @Test
    @DisplayName("Verify ThreadLocal context is safely cleared after HTTP request")
    public void testThreadLocalCleanupAfterRequest() throws Exception {
        mockMvc.perform(get("/api/crm/leads")
                .header("X-Tenant-Id", TENANT_B)
                .header("X-User-Role", "SALES")
                .header("X-User-Name", "Tenant B Sales User"))
                .andExpect(status().isOk());

        // Context must be null/cleared on the thread now
        assertFalse(SecurityUtils.hasExplicitTenantContext(), "ThreadLocal tenant context must be cleared after HTTP request");
    }
}
