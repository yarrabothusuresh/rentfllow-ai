package com.rentflow.aisales;

import com.rentflow.ai.model.Product;
import com.rentflow.ai.model.ProductStatus;
import com.rentflow.ai.repository.ProductRepository;
import com.rentflow.aisales.dto.ToolCallRequestDTO;
import com.rentflow.aisales.dto.ToolCallResultDTO;
import com.rentflow.aisales.tool.AiSalesTool;
import com.rentflow.aisales.tool.AiSalesToolRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class AiSalesToolRegistryTest {

    @Autowired
    private AiSalesToolRegistry toolRegistry;

    @Autowired
    private ProductRepository productRepository;

    private final String tenantId = "test-tool-tenant";

    @BeforeEach
    public void setup() {
        Product table = new Product();
        table.setTenantId(tenantId);
        table.setSku("TBL-RND-60");
        table.setName("60in Round Folding Table");
        table.setRentalPrice(BigDecimal.valueOf(18.00));
        table.setQuantityOwned(50);
        table.setStatus(ProductStatus.ACTIVE);
        productRepository.save(table);

        Product chair = new Product();
        chair.setTenantId(tenantId);
        chair.setSku("CHR-CHIAV-GLD");
        chair.setName("Gold Chiavari Chair");
        chair.setRentalPrice(BigDecimal.valueOf(8.50));
        chair.setQuantityOwned(300);
        chair.setStatus(ProductStatus.ACTIVE);
        productRepository.save(chair);
    }

    @Test
    public void testRegistryDiscoversTools() {
        Optional<AiSalesTool> searchTool = toolRegistry.getTool("searchProducts");
        assertTrue(searchTool.isPresent());
        assertTrue(searchTool.get().isCustomerVisible());

        Optional<AiSalesTool> profitTool = toolRegistry.getTool("checkInternalProfitability");
        assertTrue(profitTool.isPresent());
        assertFalse(profitTool.get().isCustomerVisible(), "Profitability tool must not be customer visible");
    }

    @Test
    public void testSearchProductsToolSanitizesFields() {
        ToolCallRequestDTO req = new ToolCallRequestDTO("searchProducts", Map.of("query", "round"));
        ToolCallResultDTO res = toolRegistry.executeTool(tenantId, "CUSTOMER", req);

        assertTrue(res.isSuccess());
        assertFalse(res.isInternalOnly());
        assertNotNull(res.getResult());

        List<?> list = (List<?>) res.getResult();
        assertFalse(list.isEmpty());
        Map<?, ?> first = (Map<?, ?>) list.get(0);
        assertEquals("60in Round Folding Table", first.get("name"));
        assertNotNull(first.get("rentalPrice"));
        assertNull(first.get("purchasePrice"), "Purchase cost must not leak into customer catalog");
    }

    @Test
    public void testCustomerCannotExecuteCheckInternalProfitability() {
        ToolCallRequestDTO req = new ToolCallRequestDTO("checkInternalProfitability", Map.of("items", List.of()));
        ToolCallResultDTO res = toolRegistry.executeTool(tenantId, "CUSTOMER", req);

        assertFalse(res.isSuccess());
        assertTrue(res.getErrorMessage().contains("Permission denied"));
    }

    @Test
    public void testSalesCanExecuteCheckInternalProfitability() {
        ToolCallRequestDTO req = new ToolCallRequestDTO("checkInternalProfitability", Map.of("items", List.of()));
        ToolCallResultDTO res = toolRegistry.executeTool(tenantId, "SALES", req);

        assertTrue(res.isSuccess());
        assertTrue(res.isInternalOnly());
    }
}
