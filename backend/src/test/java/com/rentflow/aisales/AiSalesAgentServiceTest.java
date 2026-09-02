package com.rentflow.aisales;

import com.rentflow.ai.model.Product;
import com.rentflow.ai.model.ProductStatus;
import com.rentflow.ai.repository.ProductRepository;
import com.rentflow.aisales.dto.AiSalesChatRequestDTO;
import com.rentflow.aisales.dto.AiSalesChatResponseDTO;
import com.rentflow.aisales.model.AiConversationStatus;
import com.rentflow.aisales.model.AiSalesConversation;
import com.rentflow.aisales.repository.AiSalesConversationRepository;
import com.rentflow.aisales.repository.AiSalesMessageRepository;
import com.rentflow.aisales.repository.AiUsageRecordRepository;
import com.rentflow.aisales.service.AiSalesAgentService;
import com.rentflow.aisales.service.AiSalesConversationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class AiSalesAgentServiceTest {

    @Autowired
    private AiSalesAgentService agentService;

    @Autowired
    private AiSalesConversationService conversationService;

    @Autowired
    private AiSalesConversationRepository conversationRepository;

    @Autowired
    private AiSalesMessageRepository messageRepository;

    @Autowired
    private AiUsageRecordRepository usageRepository;

    @Autowired
    private ProductRepository productRepository;

    private final String tenantId = "test-agent-tenant";

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
    public void testEndToEndChatFlowAndPersistence() {
        AiSalesChatRequestDTO req1 = new AiSalesChatRequestDTO();
        req1.setMessage("I need tables and chairs for a wedding for 100 people in Boston. We want round tables.");
        req1.setCustomerName("Alice Wonderland");
        req1.setCustomerEmail("alice@example.com");

        AiSalesChatResponseDTO resp1 = agentService.handleMessage(tenantId, "CUSTOMER", req1);

        assertNotNull(resp1.getConversationId());
        assertNotNull(resp1.getReplyText());

        // Check messages persisted (customer message + AI reply)
        long count = messageRepository.countByTenantIdAndConversationId(tenantId, resp1.getConversationId());
        assertEquals(2, count);

        // Check usage record created
        long usageCount = usageRepository.countByTenantIdAndCreatedAtAfter(tenantId, java.time.LocalDateTime.now().minusMinutes(5));
        assertTrue(usageCount > 0);
    }

    @Test
    public void testHumanTakeoverPausesAiAutonomousReply() {
        AiSalesChatRequestDTO req1 = new AiSalesChatRequestDTO();
        req1.setMessage("Hello");
        AiSalesChatResponseDTO resp1 = agentService.handleMessage(tenantId, "CUSTOMER", req1);

        // Sales agent takes over
        boolean takenOver = conversationService.takeOverConversation(tenantId, resp1.getConversationId(), "sales_rep_1");
        assertTrue(takenOver);

        // Subsequent customer message should NOT trigger autonomous AI answers
        AiSalesChatRequestDTO req2 = new AiSalesChatRequestDTO();
        req2.setConversationId(resp1.getConversationId());
        req2.setMessage("Can you lower the price?");

        AiSalesChatResponseDTO resp2 = agentService.handleMessage(tenantId, "CUSTOMER", req2);
        assertEquals(AiConversationStatus.HUMAN_ACTIVE, resp2.getStatus());
        assertTrue(resp2.getReplyText().contains("attended to by our sales team") || resp2.getReplyText().contains("currently attending"));
    }
}
