package com.rentflow.aisales;

import com.rentflow.ai.dto.QuoteDTO;
import com.rentflow.ai.model.Product;
import com.rentflow.ai.model.ProductStatus;
import com.rentflow.ai.model.QuoteStatus;
import com.rentflow.ai.repository.ProductRepository;
import com.rentflow.ai.service.QuoteService;
import com.rentflow.aisales.dto.*;
import com.rentflow.aisales.service.AiResponseValidatorService;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class AiSalesSecurityTest {

    @Autowired
    private AiSalesToolRegistry toolRegistry;

    @Autowired
    private QuoteService quoteService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AiResponseValidatorService validatorService;

    private final String tenantId = "test-sec-tenant";
    private Product table;

    @BeforeEach
    public void setup() {
        table = new Product();
        table.setTenantId(tenantId);
        table.setSku("TBL-RND-60");
        table.setName("60in Round Folding Table");
        table.setRentalPrice(BigDecimal.valueOf(18.00));
        table.setQuantityOwned(50);
        table.setStatus(ProductStatus.ACTIVE);
        table = productRepository.save(table);
    }

    @Test
    public void testAiQuoteCreationIsStrictlyDraftStatus() {
        ToolCallRequestDTO req = new ToolCallRequestDTO("createQuoteDraft", Map.of(
            "items", List.of(Map.of("productId", table.getId().toString(), "quantity", 5)),
            "customerEmail", "testprospect@example.com"
        ));

        ToolCallResultDTO res = toolRegistry.executeTool(tenantId, "SALES", req);
        assertTrue(res.isSuccess());

        Map<?, ?> map = (Map<?, ?>) res.getResult();
        assertEquals("DRAFT", map.get("status"), "Autonomous quote draft must always have status DRAFT");

        UUID quoteId = (UUID) map.get("quoteId");
        Optional<QuoteDTO> saved = quoteService.getQuoteById(tenantId, quoteId, "SALES");
        assertTrue(saved.isPresent());
        assertEquals(QuoteStatus.DRAFT, saved.get().getStatus());
    }

    @Test
    public void testResponseValidatorStripsInternalCostLeaks() {
        AiSalesChatResponseDTO dto = new AiSalesChatResponseDTO();
        dto.setReplyText("Our purchase cost is $5.00 and our profit margin is 45% for these chairs.");

        validatorService.validateCustomerResponse(dto);

        assertFalse(dto.getReplyText().contains("purchase cost"));
        assertFalse(dto.getReplyText().contains("profit margin is"));
        assertTrue(dto.getReplyText().contains("pricing estimate"));
    }
}
