package com.rentflow.aisales.tool;

import com.rentflow.ai.dto.QuoteDTO;
import com.rentflow.ai.dto.QuoteItemDTO;
import com.rentflow.ai.model.Customer;
import com.rentflow.ai.model.PricingStrategy;
import com.rentflow.ai.model.Product;
import com.rentflow.ai.model.QuoteStatus;
import com.rentflow.ai.repository.CustomerRepository;
import com.rentflow.ai.repository.ProductRepository;
import com.rentflow.ai.service.QuoteService;
import com.rentflow.aisales.dto.ToolCallRequestDTO;
import com.rentflow.aisales.dto.ToolCallResultDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Component("aiSalesCreateQuoteDraftTool")
public class CreateQuoteDraftTool implements AiSalesTool {

    private final QuoteService quoteService;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;

    public CreateQuoteDraftTool(QuoteService quoteService, ProductRepository productRepository, CustomerRepository customerRepository) {
        this.quoteService = quoteService;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    public String getName() {
        return "createQuoteDraft";
    }

    @Override
    public String getDescription() {
        return "Create a commercial quote in DRAFT status for sales staff review. Never creates confirmed bookings or charges cards.";
    }

    @Override
    public boolean isCustomerVisible() {
        return true;
    }

    @Override
    public boolean isMutating() {
        return true;
    }

    @Override
    public Set<String> getAllowedRoles() {
        return Set.of("CUSTOMER", "SALES", "ADMIN", "OWNER", "STAFF");
    }

    @Override
    public ToolCallResultDTO execute(String tenantId, String userRole, ToolCallRequestDTO request) {
        Map<String, Object> args = request.getArguments();

        // 1. Resolve or create customer
        UUID customerId = resolveCustomerId(tenantId, args);

        QuoteDTO dto = new QuoteDTO();
        dto.setCustomerId(customerId);
        dto.setEventId(UUID.randomUUID());
        dto.setStatus(QuoteStatus.DRAFT);
        dto.setQuoteDate(LocalDate.now());
        dto.setValidUntil(LocalDate.now().plusDays(7));

        LocalDateTime startDt = parseDateTime(args.get("startDate"), true);
        LocalDateTime endDt = parseDateTime(args.get("endDate"), false);
        dto.setRentalStartDateTime(startDt);
        dto.setRentalEndDateTime(endDt);

        boolean deliveryRequired = args.get("deliveryRequired") == null || Boolean.parseBoolean(args.get("deliveryRequired").toString());
        dto.setDeliveryFee(deliveryRequired ? BigDecimal.valueOf(150.00) : BigDecimal.ZERO);
        dto.setTaxRate(new BigDecimal("8.25"));
        dto.setNotes(args.get("notes") != null ? args.get("notes").toString() : "Generated via RentFlow AI Sales Agent inquiry");
        dto.setInternalNotes("Pending human review and approval. Prepared autonomously based on customer conversation.");

        List<QuoteItemDTO> items = new ArrayList<>();
        Object itemsObj = args.get("items");
        if (itemsObj instanceof List<?> rawList) {
            for (Object itemObj : rawList) {
                if (itemObj instanceof Map<?, ?> itemMap) {
                    String pIdStr = (String) itemMap.get("productId");
                    int qty = itemMap.get("quantity") != null ? Integer.parseInt(itemMap.get("quantity").toString()) : 1;
                    if (pIdStr != null) {
                        try {
                            UUID pId = UUID.fromString(pIdStr);
                            Optional<Product> prodOpt = productRepository.findByTenantIdAndId(tenantId, pId);
                            if (prodOpt.isPresent()) {
                                Product p = prodOpt.get();
                                QuoteItemDTO item = new QuoteItemDTO();
                                item.setProductId(p.getId());
                                item.setDescription(p.getName());
                                item.setQuantity(qty);
                                item.setUnitPrice(p.getRentalPrice());
                                item.setPricingStrategy(PricingStrategy.PER_EVENT);
                                item.setRentalDays(1);
                                items.add(item);
                            }
                        } catch (Exception ignored) {}
                    }
                }
            }
        }
        dto.setItems(items);

        try {
            QuoteDTO created = quoteService.createQuote(tenantId, dto, "AI_SALES_AGENT");

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("quoteId", created.getId());
            result.put("quoteNumber", created.getQuoteNumber());
            result.put("status", created.getStatus().name());
            result.put("totalAmount", created.getTotalAmount());
            result.put("subtotal", created.getSubtotal());
            result.put("deliveryFee", created.getDeliveryFee());
            result.put("taxAmount", created.getTaxAmount());
            result.put("message", "Draft quote created successfully. Awaiting human sales approval.");

            return ToolCallResultDTO.success(getName(), result, false);
        } catch (Exception e) {
            return ToolCallResultDTO.failure(getName(), "Failed to create quote draft: " + e.getMessage(), false);
        }
    }

    private UUID resolveCustomerId(String tenantId, Map<String, Object> args) {
        String custIdStr = (String) args.get("customerId");
        if (custIdStr != null && !custIdStr.trim().isEmpty()) {
            try {
                return UUID.fromString(custIdStr);
            } catch (Exception ignored) {}
        }

        String email = args.get("customerEmail") != null && !args.get("customerEmail").toString().trim().isEmpty() ?
            args.get("customerEmail").toString().trim() : "inquiry@client.com";

        Optional<Customer> existing = customerRepository.findFirstByTenantIdAndEmailIgnoreCase(tenantId, email);
        if (existing.isPresent()) {
            return existing.get().getId();
        }

        Customer newCust = new Customer();
        newCust.setTenantId(tenantId);
        newCust.setCustomerNumber(String.format("CUST-%s", UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase()));
        newCust.setFirstName(args.get("customerName") != null ? args.get("customerName").toString() : "Valued Prospect");
        newCust.setEmail(email);
        newCust.setCustomerType(com.rentflow.ai.model.CustomerType.INDIVIDUAL);
        newCust.setPhone(args.get("customerPhone") != null ? args.get("customerPhone").toString() : null);
        newCust.setBillingAddress(args.get("deliveryAddress") != null ? args.get("deliveryAddress").toString() : null);
        newCust = customerRepository.save(newCust);
        return newCust.getId();
    }

    private LocalDateTime parseDateTime(Object val, boolean isStart) {
        if (val == null) {
            LocalDate base = isStart ? LocalDate.now().plusDays(7) : LocalDate.now().plusDays(9);
            return isStart ? base.atTime(9, 0) : base.atTime(18, 0);
        }
        String str = val.toString().trim();
        if (str.length() == 10) {
            LocalDate ld = LocalDate.parse(str);
            return isStart ? ld.atTime(9, 0) : ld.atTime(18, 0);
        }
        return LocalDateTime.parse(str);
    }
}
