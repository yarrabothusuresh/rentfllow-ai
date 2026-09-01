package com.rentflow.integration;

import com.rentflow.ai.model.Product;
import com.rentflow.ai.model.ProductCategory;
import com.rentflow.ai.model.ProductStatus;
import com.rentflow.ai.repository.ProductRepository;
import com.rentflow.integration.controller.ExternalApiController;
import com.rentflow.integration.dto.ExternalCustomerDTO;
import com.rentflow.integration.dto.ExternalProductDTO;
import com.rentflow.integration.dto.ExternalQuoteRequestDTO;
import com.rentflow.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class ExternalApiControllerTest {

    @Autowired
    private ExternalApiController externalApiController;

    @Autowired
    private ProductRepository productRepository;

    private final String tenantId = "test-tenant-external-api";

    @BeforeEach
    public void setUp() {
        SecurityUtils.setTestTenantId(tenantId);
    }

    @Test
    public void testCreateAndGetExternalCustomer() {
        ExternalCustomerDTO dto = new ExternalCustomerDTO();
        dto.setName("John Smith Events");
        dto.setEmail("john@smith-events.com");
        dto.setPhone("555-0199");
        dto.setCompany("Smith Events Co");

        ResponseEntity<ExternalCustomerDTO> response = externalApiController.createCustomer(dto);
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals("John Smith Events", response.getBody().getName());

        List<ExternalCustomerDTO> list = externalApiController.getCustomers();
        assertFalse(list.isEmpty());
    }

    @Test
    public void testGetExternalProducts() {
        Product p = new Product();
        p.setTenantId(tenantId);
        p.setName("Chavari Gold Chair");
        p.setSku("CHV-GLD-01");
        p.setRentalPrice(BigDecimal.valueOf(8.50));
        p.setStatus(ProductStatus.ACTIVE);
        p.setQuantityOwned(200);
        productRepository.save(p);

        List<ExternalProductDTO> products = externalApiController.getProducts();
        assertFalse(products.isEmpty());
        assertTrue(products.stream().anyMatch(prod -> "Chavari Gold Chair".equals(prod.getName())));
    }

    @Test
    public void testExternalQuoteRequest() {
        Product p = new Product();
        p.setTenantId(tenantId);
        p.setName("Round Dining Table 60in");
        p.setSku("TBL-RND-60");
        p.setRentalPrice(BigDecimal.valueOf(25.00));
        p.setStatus(ProductStatus.ACTIVE);
        p.setQuantityOwned(50);
        p = productRepository.save(p);

        ExternalQuoteRequestDTO dto = new ExternalQuoteRequestDTO();
        dto.setCustomerName("Sarah Jenkins");
        dto.setCustomerEmail("sarah@jenkins.org");
        dto.setCustomerPhone("555-0822");
        dto.setCompanyName("Jenkins Foundation");
        dto.setEventName("Annual Gala 2026");
        dto.setEventStartDate(LocalDate.now().plusDays(14));
        dto.setEventEndDate(LocalDate.now().plusDays(16));
        dto.setItems(List.of(new ExternalQuoteRequestDTO.ExternalQuoteRequestItemDTO(p.getId(), 10)));

        ResponseEntity<Map<String, Object>> res = externalApiController.createQuoteRequest(dto);
        assertNotNull(res.getBody());
        assertEquals("DRAFT", res.getBody().get("status"));
        assertNotNull(res.getBody().get("quoteId"));
    }
}
