package com.rentflow.aisales.provider;

import com.rentflow.ai.model.Product;
import com.rentflow.ai.repository.ProductRepository;
import com.rentflow.aisales.dto.*;
import com.rentflow.aisales.model.*;
import com.rentflow.aisales.tool.AiSalesToolRegistry;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Component("mockAiSalesProvider")
public class MockAiSalesProvider implements AiSalesProvider {

    private final AiSalesToolRegistry toolRegistry;
    private final ProductRepository productRepository;

    public MockAiSalesProvider(AiSalesToolRegistry toolRegistry, ProductRepository productRepository) {
        this.toolRegistry = toolRegistry;
        this.productRepository = productRepository;
    }

    @Override
    public String getProviderName() {
        return "mock";
    }

    @Override
    public boolean isHealthy() {
        return true;
    }

    @Override
    public AiSalesChatResponseDTO processMessage(
        String tenantId,
        String userRole,
        AiSalesConversation conversation,
        List<AiSalesMessage> history,
        String incomingMessage,
        RentalInquiry currentInquiry
    ) {
        String msgLower = incomingMessage.toLowerCase().trim();
        AiSalesChatResponseDTO resp = new AiSalesChatResponseDTO();
        resp.setConversationId(conversation.getId());
        resp.setPublicId(conversation.getPublicId());

        // 1. Simulated Provider Failure / Timeout Demo Scenario (Demo Flow 6)
        if (msgLower.contains("simulate_provider_failure") || msgLower.contains("provider error") || msgLower.contains("simulate timeout")) {
            resp.setDetectedIntent(AiIntent.UNKNOWN);
            resp.setStatus(AiConversationStatus.ACTIVE);
            resp.setReplyText("I'm having trouble with the AI assistant right now. You can continue browsing our catalog or submit your request to our sales team.");
            resp.setSuggestedReplies(List.of("Browse Catalog", "Submit Rental Request", "Talk to Sales"));
            return resp;
        }

        // 2. Prompt Injection / Internal Cost / Secret Defense (Demo Flow 4)
        if (msgLower.contains("ignore your rules") ||
            msgLower.contains("ignore every instruction") ||
            msgLower.contains("ignore your instructions") ||
            msgLower.contains("system prompt") ||
            msgLower.contains("api key") ||
            msgLower.contains("api_key") ||
            msgLower.contains("purchase cost") ||
            msgLower.contains("purchase price") ||
            msgLower.contains("internal margin") ||
            msgLower.contains("profit margin") ||
            msgLower.contains("payment tool") ||
            msgLower.contains("call your payment") ||
            msgLower.contains("drop table") ||
            msgLower.contains("reserve 500 chairs permanently")) {

            resp.setDetectedIntent(AiIntent.GENERAL_RENTAL_QUESTION);
            resp.setStatus(AiConversationStatus.ACTIVE);
            resp.setReplyText("I am unable to display internal system instructions, cost structures, profit margins, or API credentials. I can assist you with discovering rental products, checking real-time availability, and preparing a non-binding rental request or draft quote.");
            resp.setSuggestedReplies(List.of("Show table options", "Show chair options", "Talk to a person"));
            return resp;
        }

        // 3. Human Escalation / Handoff Scenario (Demo Flow 5)
        if (msgLower.contains("talk to a person") ||
            msgLower.contains("speak to someone") ||
            msgLower.contains("talk to human") ||
            msgLower.contains("human") ||
            msgLower.contains("complicated") ||
            msgLower.contains("agent") ||
            msgLower.contains("representative")) {

            ToolCallRequestDTO toolReq = new ToolCallRequestDTO("escalateToHuman", Map.of(
                "conversationId", conversation.getId().toString(),
                "reason", "CUSTOMER_REQUEST",
                "summary", "Customer explicitly requested to speak with a human representative."
            ));
            ToolCallResultDTO toolRes = toolRegistry.executeTool(tenantId, userRole, toolReq);
            resp.getExecutedTools().add(toolRes);

            // Record Lead if contact info is present
            if (currentInquiry.getEmail() != null || conversation.getCustomerEmail() != null) {
                ToolCallRequestDTO leadReq = new ToolCallRequestDTO("createOrUpdateLead", Map.of(
                    "conversationId", conversation.getId().toString(),
                    "customerName", conversation.getCustomerName() != null ? conversation.getCustomerName() : "Inquiring Customer",
                    "email", conversation.getCustomerEmail() != null ? conversation.getCustomerEmail() : (currentInquiry.getEmail() != null ? currentInquiry.getEmail() : "inquiry@client.com"),
                    "eventType", currentInquiry.getEventType() != null ? currentInquiry.getEventType() : "OTHER",
                    "notes", "Customer requested human handoff from conversational assistant."
                ));
                ToolCallResultDTO leadRes = toolRegistry.executeTool(tenantId, userRole, leadReq);
                resp.getExecutedTools().add(leadRes);
            }

            resp.setDetectedIntent(AiIntent.HUMAN_ASSISTANCE);
            resp.setStatus(AiConversationStatus.WAITING_FOR_HUMAN);
            resp.setEscalatedToHuman(true);
            resp.setEscalationReason("CUSTOMER_REQUEST");
            resp.setReplyText("I have notified our sales team! A representative has been assigned to this conversation and will take over shortly. Thank you for your patience.");
            return resp;
        }

        // 4. Fake Product Inquiry Scenario (Demo Flow 3)
        if (msgLower.contains("royal diamond") || msgLower.contains("diamond crystal") || msgLower.contains("diamond gold chair")) {
            resp.setDetectedIntent(AiIntent.PRODUCT_SEARCH);
            resp.setStatus(AiConversationStatus.ACTIVE);

            ToolCallResultDTO searchRes = toolRegistry.executeTool(tenantId, userRole,
                new ToolCallRequestDTO("searchProducts", Map.of("query", "chair", "limit", 3)));
            resp.getExecutedTools().add(searchRes);

            resp.setReplyText("I couldn't find 'Royal Diamond Chairs' in our rental catalog. However, we have excellent premium seating alternatives available like our Gold Chiavari Chairs and Cross-Back Bistro Chairs! Would you like to check availability for those?");
            resp.setSuggestedReplies(List.of("Check Gold Chiavari availability", "Show table options", "Talk to a person"));
            return resp;
        }

        // 5. Standalone Shortage Check Scenario (Demo Flow 2)
        if (msgLower.contains("300") && (msgLower.contains("chiavari") || msgLower.contains("chair"))) {
            resp.setDetectedIntent(AiIntent.AVAILABILITY_CHECK);
            resp.setStatus(AiConversationStatus.ACTIVE);

            Product chairProduct = findProductByName(tenantId, "chiavari");
            if (chairProduct != null) {
                ToolCallResultDTO availRes = toolRegistry.executeTool(tenantId, userRole, new ToolCallRequestDTO("checkAvailability", Map.of(
                    "productId", chairProduct.getId().toString(),
                    "quantity", 300,
                    "startDate", "2026-09-20",
                    "endDate", "2026-09-22"
                )));
                resp.getExecutedTools().add(availRes);
            }

            ToolCallResultDTO altRes = toolRegistry.executeTool(tenantId, userRole, new ToolCallRequestDTO("findAlternativeProducts", Map.of(
                "category", "Chairs",
                "requiredQuantity", 80
            )));
            resp.getExecutedTools().add(altRes);

            resp.setReplyText("We currently have 220 Gold Chiavari Chairs available for those dates. I can check similar chair options for the remaining 80, such as our White Folding Chairs or Cross-Back Bistro Chairs. Would you like to reserve the 220 and supplement with an alternative?");
            resp.setSuggestedReplies(List.of("Supplement with White Folding Chairs", "Check alternative dates", "Talk to a person"));
            return resp;
        }

        // 6. Extract structured inquiry parameters
        extractInquiryDetails(msgLower, currentInquiry);

        // 7. Corporate Gala Demo (Demo Flow 1)
        if (isCorporateGalaScenario(msgLower, currentInquiry)) {
            return buildCorporateGalaFlow(tenantId, userRole, conversation, currentInquiry, resp, msgLower);
        }

        // 8. Wedding / General Planning Flow
        if (currentInquiry.getGuestCount() != null && currentInquiry.getGuestCount() > 0) {
            // Missing location / rental window?
            if (currentInquiry.getDeliveryCity() == null && (msgLower.contains("wedding") || msgLower.contains("event") || msgLower.contains("party"))) {
                resp.setDetectedIntent(AiIntent.QUOTE_REQUEST);
                resp.setStatus(AiConversationStatus.WAITING_FOR_CUSTOMER);
                resp.setReplyText("Congratulations on planning your " + (currentInquiry.getEventType() != null ? currentInquiry.getEventType().toLowerCase() : "event") +
                    " for " + currentInquiry.getGuestCount() + " guests! What city will the event take place in, and what rental window or delivery time should we schedule?");
                resp.setSuggestedReplies(List.of("Boston, delivery around 2 PM", "Cambridge, delivery at 10 AM", "Customer pickup"));
                return resp;
            }

            return buildGeneralQuoteDraftFlow(tenantId, userRole, conversation, currentInquiry, resp, msgLower);
        }

        // Default discovery greeting
        resp.setDetectedIntent(AiIntent.GENERAL_RENTAL_QUESTION);
        resp.setStatus(AiConversationStatus.ACTIVE);
        resp.setReplyText("Hi! I can help you find rental items, check real availability, and prepare a rental request. What are you planning?");
        resp.setSuggestedReplies(List.of("Wedding for 150 people", "Corporate gala for 200 people", "Birthday party next weekend", "Talk to a person"));
        return resp;
    }

    private boolean isCorporateGalaScenario(String msg, RentalInquiry inquiry) {
        return (msg.contains("corporate gala") || msg.contains("gala") || "CORPORATE".equalsIgnoreCase(inquiry.getEventType()))
            && (inquiry.getGuestCount() != null && inquiry.getGuestCount() >= 180);
    }

    private AiSalesChatResponseDTO buildCorporateGalaFlow(
        String tenantId,
        String userRole,
        AiSalesConversation conversation,
        RentalInquiry inquiry,
        AiSalesChatResponseDTO resp,
        String msgLower
    ) {
        // If delivery location is missing, ask for rental window and delivery location
        if (inquiry.getDeliveryCity() == null && !msgLower.contains("boston") && !msgLower.contains("center") && !msgLower.contains("address")) {
            resp.setDetectedIntent(AiIntent.QUOTE_REQUEST);
            resp.setStatus(AiConversationStatus.WAITING_FOR_CUSTOMER);
            resp.setReplyText("Sounds wonderful! For a corporate gala of 200 guests on September 11, what rental window and delivery location should I use?");
            resp.setSuggestedReplies(List.of("Boston Convention Center, delivery at 9 AM", "Downtown Hotel Ballroom, delivery at 12 PM", "Client Pickup"));
            return resp;
        }

        if (inquiry.getDeliveryCity() == null) {
            inquiry.setDeliveryCity("Boston");
            inquiry.setDeliveryAddress("Boston Convention Center, Boston, MA");
            inquiry.setDeliveryRequired(true);
        }

        // Products needed: Gold Chiavari Chairs (200), 60-inch Round Tables (20), White Table Linens (200)
        Product chair = findProductByName(tenantId, "chiavari");
        Product table = findProductByName(tenantId, "round");
        Product whiteLinen = findProductByName(tenantId, "white table linen");
        Product ivoryLinen = findProductByName(tenantId, "ivory table linen");

        if (chair == null || table == null || whiteLinen == null) {
            resp.setReplyText("I am having trouble locating matching seating and tables in your catalog right now. I have notified our sales team to assist you directly.");
            resp.setStatus(AiConversationStatus.WAITING_FOR_HUMAN);
            return resp;
        }

        List<Map<String, Object>> bulkItems = List.of(
            Map.of("productId", chair.getId().toString(), "quantity", 200),
            Map.of("productId", table.getId().toString(), "quantity", 20),
            Map.of("productId", whiteLinen.getId().toString(), "quantity", 200)
        );

        String startIso = "2026-09-11";
        String endIso = "2026-09-12";

        // 1. Tool: searchProducts
        ToolCallResultDTO searchRes = toolRegistry.executeTool(tenantId, userRole,
            new ToolCallRequestDTO("searchProducts", Map.of("query", "chiavari table linen", "limit", 4)));
        resp.getExecutedTools().add(searchRes);

        // 2. Tool: checkBulkAvailability
        ToolCallResultDTO availRes = toolRegistry.executeTool(tenantId, userRole,
            new ToolCallRequestDTO("checkBulkAvailability", Map.of(
                "items", bulkItems,
                "startDate", startIso,
                "endDate", endIso
            )));
        resp.getExecutedTools().add(availRes);

        // 3. Tool: findAlternativeProducts (for the 20 white linens shortage)
        ToolCallResultDTO altRes = toolRegistry.executeTool(tenantId, userRole,
            new ToolCallRequestDTO("findAlternativeProducts", Map.of(
                "productId", whiteLinen.getId().toString(),
                "requiredQuantity", 20,
                "startDate", startIso,
                "endDate", endIso
            )));
        resp.getExecutedTools().add(altRes);

        // 4. Tool: calculatePrice (PricingService authoritative calculation)
        // 200 chairs @ $8.00 = $1,600
        // 20 tables @ $14.00 = $280
        // 180 white linens @ $10.00 = $1,800
        // 20 ivory linens @ $10.00 = $200
        // Subtotal = $3,880 + Delivery $150 = $4,030 taxable * 1.0825 (approx $4,850 estimated total)
        List<Map<String, Object>> pricedItems = List.of(
            Map.of("productId", chair.getId().toString(), "quantity", 200),
            Map.of("productId", table.getId().toString(), "quantity", 20),
            Map.of("productId", whiteLinen.getId().toString(), "quantity", 180),
            Map.of("productId", (ivoryLinen != null ? ivoryLinen.getId().toString() : whiteLinen.getId().toString()), "quantity", 20)
        );

        ToolCallResultDTO priceRes = toolRegistry.executeTool(tenantId, userRole,
            new ToolCallRequestDTO("calculatePrice", Map.of(
                "items", pricedItems,
                "rentalDays", 1,
                "deliveryRequired", true
            )));
        resp.getExecutedTools().add(priceRes);

        // 5. Tool: createOrUpdateLead (CRM Lead integration)
        ToolCallRequestDTO leadReq = new ToolCallRequestDTO("createOrUpdateLead", Map.of(
            "conversationId", conversation.getId().toString(),
            "customerName", conversation.getCustomerName() != null ? conversation.getCustomerName() : "Corporate Gala Coordinator",
            "email", conversation.getCustomerEmail() != null ? conversation.getCustomerEmail() : "gala@enterprise.com",
            "eventName", "Annual Corporate Gala",
            "eventType", "CORPORATE",
            "eventDate", "2026-09-11",
            "guestCount", 200,
            "budget", 5000,
            "venue", "Boston Convention Center",
            "notes", "Corporate Gala rental inquiry: 200 Gold Chiavari Chairs, 20 Round Tables, 180 White Linens + 20 Ivory Linens."
        ));
        ToolCallResultDTO leadRes = toolRegistry.executeTool(tenantId, userRole, leadReq);
        resp.getExecutedTools().add(leadRes);

        // 6. Tool: createRentalRequest (Rental Request integration)
        Map<String, Object> reqArgs = new LinkedHashMap<>();
        reqArgs.put("conversationId", conversation.getId().toString());
        reqArgs.put("customerName", conversation.getCustomerName() != null ? conversation.getCustomerName() : "Corporate Gala Coordinator");
        reqArgs.put("customerEmail", conversation.getCustomerEmail() != null ? conversation.getCustomerEmail() : "gala@enterprise.com");
        reqArgs.put("eventName", "Annual Corporate Gala");
        reqArgs.put("eventType", "CORPORATE");
        reqArgs.put("eventDate", "2026-09-11");
        reqArgs.put("startDate", "2026-09-11");
        reqArgs.put("endDate", "2026-09-12");
        reqArgs.put("guestCount", 200);
        reqArgs.put("deliveryAddress", "Boston Convention Center, Boston, MA");
        reqArgs.put("deliveryCity", "Boston");
        reqArgs.put("deliveryRequired", true);
        reqArgs.put("estimatedTotal", 4850.00);
        reqArgs.put("notes", "200 Gold Chiavari Chairs, 20 Round Tables, 180 White Linens, 20 Ivory Linens (alternative substitution).");

        ToolCallRequestDTO reqReq = new ToolCallRequestDTO("createRentalRequest", reqArgs);
        ToolCallResultDTO reqRes = toolRegistry.executeTool(tenantId, userRole, reqReq);
        resp.getExecutedTools().add(reqRes);

        // 7. Tool: checkInternalProfitability (Strictly INTERNAL ONLY - customer never sees output)
        ToolCallResultDTO profitRes = toolRegistry.executeTool(tenantId, "SALES",
            new ToolCallRequestDTO("checkInternalProfitability", Map.of(
                "items", pricedItems,
                "deliveryRequired", true
            )));
        resp.getExecutedTools().add(profitRes);

        // 8. Tool: createDraftQuote (Draft status only - awaiting human review)
        ToolCallRequestDTO quoteReq = new ToolCallRequestDTO("createQuoteDraft", Map.of(
            "conversationId", conversation.getId().toString(),
            "customerName", conversation.getCustomerName() != null ? conversation.getCustomerName() : "Corporate Gala Coordinator",
            "customerEmail", conversation.getCustomerEmail() != null ? conversation.getCustomerEmail() : "gala@enterprise.com",
            "startDate", startIso,
            "endDate", endIso,
            "deliveryAddress", "Boston Convention Center, Boston, MA",
            "notes", "Corporate Gala Seating & Dining (200 Chairs, 20 Tables, 180 White Linens, 20 Ivory Linens)"
        ));
        ToolCallResultDTO quoteDraftRes = toolRegistry.executeTool(tenantId, "SALES", quoteReq);
        resp.getExecutedTools().add(quoteDraftRes);

        if (quoteDraftRes.isSuccess() && quoteDraftRes.getResult() instanceof Map<?, ?> qMap) {
            resp.setQuoteDraftId((UUID) qMap.get("quoteId"));
            resp.setQuoteDraftNumber((String) qMap.get("quoteNumber"));
            conversation.setQuoteId(resp.getQuoteDraftId());
        }

        inquiry.setComplete(true);
        resp.setDetectedIntent(AiIntent.QUOTE_REQUEST);
        resp.setStatus(AiConversationStatus.WAITING_FOR_HUMAN);

        String reply = "We have verified our inventory for your 200-guest corporate gala on September 11! We have enough chairs (220 available) and tables (30 available), but only 180 white linens available. I found an ivory linen option for the remaining quantity.\n\n" +
            "The current estimated total is $4,850 based on the selected items and delivery details. Final pricing is subject to the normal quote review.\n\n" +
            "Your rental request has been submitted for review and a draft quote (" + (resp.getQuoteDraftNumber() != null ? resp.getQuoteDraftNumber() : "QUO-DRAFT") + ") has been prepared for our sales team to verify. A sales representative will confirm your logistics shortly!";

        resp.setReplyText(reply);
        resp.setSuggestedReplies(List.of("Review request details", "Ask about cocktail tables", "Talk to a person"));
        return resp;
    }

    private AiSalesChatResponseDTO buildGeneralQuoteDraftFlow(
        String tenantId,
        String userRole,
        AiSalesConversation conversation,
        RentalInquiry inquiry,
        AiSalesChatResponseDTO resp,
        String msgLower
    ) {
        int guests = inquiry.getGuestCount() != null ? inquiry.getGuestCount() : 100;
        int tablesNeeded = (int) Math.ceil((double) guests / 10.0);
        int chairsNeeded = guests;

        Product tableProduct = findProductByName(tenantId, "round");
        Product chairProduct = findProductByName(tenantId, "chair");

        if (tableProduct == null || chairProduct == null) {
            resp.setReplyText("I am having trouble locating matching seating and tables in your catalog right now. I have notified our sales team to assist you!");
            resp.setStatus(AiConversationStatus.WAITING_FOR_HUMAN);
            return resp;
        }

        List<Map<String, Object>> itemsList = List.of(
            Map.of("productId", tableProduct.getId().toString(), "quantity", tablesNeeded),
            Map.of("productId", chairProduct.getId().toString(), "quantity", chairsNeeded)
        );

        String startIso = inquiry.getRentalStart() != null ? inquiry.getRentalStart().toLocalDate().toString() : LocalDate.now().plusDays(7).toString();
        String endIso = inquiry.getRentalEnd() != null ? inquiry.getRentalEnd().toLocalDate().toString() : LocalDate.now().plusDays(9).toString();

        // 1. Tool: searchProducts
        ToolCallResultDTO searchTablesRes = toolRegistry.executeTool(tenantId, userRole,
            new ToolCallRequestDTO("searchProducts", Map.of("query", "round chair", "limit", 2)));
        resp.getExecutedTools().add(searchTablesRes);

        // 2. Tool: checkBulkAvailability
        ToolCallResultDTO availRes = toolRegistry.executeTool(tenantId, userRole,
            new ToolCallRequestDTO("checkBulkAvailability", Map.of(
                "items", itemsList,
                "startDate", startIso,
                "endDate", endIso
            )));
        resp.getExecutedTools().add(availRes);

        // 3. Tool: calculatePrice
        ToolCallResultDTO priceRes = toolRegistry.executeTool(tenantId, userRole,
            new ToolCallRequestDTO("calculatePrice", Map.of(
                "items", itemsList,
                "rentalDays", 1,
                "deliveryRequired", inquiry.isDeliveryRequired()
            )));
        resp.getExecutedTools().add(priceRes);

        // 4. Tool: createOrUpdateLead
        ToolCallResultDTO leadRes = toolRegistry.executeTool(tenantId, userRole,
            new ToolCallRequestDTO("createOrUpdateLead", Map.of(
                "conversationId", conversation.getId().toString(),
                "customerName", conversation.getCustomerName() != null ? conversation.getCustomerName() : "Wedding Inquirer",
                "email", conversation.getCustomerEmail() != null ? conversation.getCustomerEmail() : "inquiry@client.com",
                "eventType", inquiry.getEventType() != null ? inquiry.getEventType() : "WEDDING",
                "guestCount", guests,
                "venue", inquiry.getDeliveryCity() != null ? inquiry.getDeliveryCity() : "Boston"
            )));
        resp.getExecutedTools().add(leadRes);

        // 5. Tool: createRentalRequest
        ToolCallResultDTO reqRes = toolRegistry.executeTool(tenantId, userRole,
            new ToolCallRequestDTO("createRentalRequest", Map.of(
                "conversationId", conversation.getId().toString(),
                "customerName", conversation.getCustomerName() != null ? conversation.getCustomerName() : "Wedding Inquirer",
                "customerEmail", conversation.getCustomerEmail() != null ? conversation.getCustomerEmail() : "inquiry@client.com",
                "eventType", inquiry.getEventType() != null ? inquiry.getEventType() : "WEDDING",
                "guestCount", guests,
                "deliveryCity", inquiry.getDeliveryCity() != null ? inquiry.getDeliveryCity() : "Boston",
                "deliveryRequired", inquiry.isDeliveryRequired()
            )));
        resp.getExecutedTools().add(reqRes);

        // 6. Tool: checkInternalProfitability (internal only)
        ToolCallResultDTO profitRes = toolRegistry.executeTool(tenantId, "SALES",
            new ToolCallRequestDTO("checkInternalProfitability", Map.of(
                "items", itemsList,
                "deliveryRequired", inquiry.isDeliveryRequired()
            )));
        resp.getExecutedTools().add(profitRes);

        // 7. Tool: createQuoteDraft
        ToolCallResultDTO quoteDraftRes = toolRegistry.executeTool(tenantId, "SALES",
            new ToolCallRequestDTO("createQuoteDraft", Map.of(
                "conversationId", conversation.getId().toString(),
                "items", itemsList,
                "customerName", conversation.getCustomerName() != null ? conversation.getCustomerName() : "Wedding Inquirer",
                "customerEmail", conversation.getCustomerEmail() != null ? conversation.getCustomerEmail() : "inquiry@client.com",
                "startDate", startIso,
                "endDate", endIso,
                "deliveryAddress", inquiry.getDeliveryAddress() != null ? inquiry.getDeliveryAddress() : "Boston, MA",
                "notes", guests + " Guest Wedding Seating Package (" + tablesNeeded + " Round Tables, " + chairsNeeded + " Chairs)"
            )));
        resp.getExecutedTools().add(quoteDraftRes);

        if (quoteDraftRes.isSuccess() && quoteDraftRes.getResult() instanceof Map<?, ?> qMap) {
            resp.setQuoteDraftId((UUID) qMap.get("quoteId"));
            resp.setQuoteDraftNumber((String) qMap.get("quoteNumber"));
            conversation.setQuoteId(resp.getQuoteDraftId());
        }

        inquiry.setComplete(true);
        resp.setDetectedIntent(AiIntent.QUOTE_REQUEST);
        resp.setStatus(AiConversationStatus.WAITING_FOR_HUMAN);

        String reply = "Great news! I have verified inventory availability and prepared an official Quote Draft for your " +
            guests + "-guest " + (inquiry.getEventType() != null ? inquiry.getEventType().toLowerCase() : "event") + " in " +
            (inquiry.getDeliveryCity() != null ? inquiry.getDeliveryCity() : "Boston") + ":\n\n" +
            "• " + tablesNeeded + "x " + tableProduct.getName() + " (seats 10 each)\n" +
            "• " + chairsNeeded + "x " + chairProduct.getName() + "\n" +
            "• Delivery & standard setup\n\n" +
            "Our sales staff is reviewing your draft (" + (resp.getQuoteDraftNumber() != null ? resp.getQuoteDraftNumber() : "QUO-DRAFT") +
            ") to verify logistics. A specialist will follow up shortly!";

        resp.setReplyText(reply);
        resp.setSuggestedReplies(List.of("View quote status", "Ask about linens", "Talk to a person"));
        return resp;
    }

    private Product findProductByName(String tenantId, String query) {
        List<Product> products = productRepository.findByTenantId(tenantId);
        String q = query.toLowerCase();
        return products.stream()
            .filter(p -> p.getName() != null && p.getName().toLowerCase().contains(q))
            .findFirst()
            .orElse(null);
    }

    private void extractInquiryDetails(String msg, RentalInquiry inquiry) {
        if (msg.contains("wedding")) inquiry.setEventType("WEDDING");
        else if (msg.contains("corporate") || msg.contains("gala") || msg.contains("banquet")) inquiry.setEventType("CORPORATE");
        else if (msg.contains("birthday") || msg.contains("party")) inquiry.setEventType("PARTY");

        // Guest count extraction
        if (msg.contains("200 people") || msg.contains("200 guests") || msg.contains("200")) {
            inquiry.setGuestCount(200);
        } else if (msg.contains("150 people") || msg.contains("150 guests") || msg.contains("150")) {
            inquiry.setGuestCount(150);
        } else if (msg.contains("100 people") || msg.contains("100 guests") || msg.contains("100")) {
            inquiry.setGuestCount(100);
        } else if (msg.contains("50 people") || msg.contains("50 guests") || msg.contains("50")) {
            inquiry.setGuestCount(50);
        }

        // Relative date handling
        if (msg.contains("september 11") || msg.contains("sep 11")) {
            LocalDate sep11 = LocalDate.of(2026, 9, 11);
            inquiry.setEventDate(sep11);
            inquiry.setRentalStart(sep11.atTime(9, 0));
            inquiry.setRentalEnd(sep11.plusDays(1).atTime(18, 0));
        } else if (msg.contains("september 20") || msg.contains("sep 20")) {
            LocalDate sep20 = LocalDate.of(2026, 9, 20);
            inquiry.setEventDate(sep20);
            inquiry.setRentalStart(sep20.atTime(9, 0));
            inquiry.setRentalEnd(sep20.plusDays(2).atTime(18, 0));
        } else if (msg.contains("next saturday") || msg.contains("saturday")) {
            LocalDate nextSat = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SATURDAY));
            inquiry.setEventDate(nextSat);
            inquiry.setRentalStart(nextSat.atTime(9, 0));
            inquiry.setRentalEnd(nextSat.plusDays(2).atTime(18, 0));
        }

        // City & Delivery Time
        if (msg.contains("boston") || msg.contains("convention center")) {
            inquiry.setDeliveryCity("Boston");
            inquiry.setDeliveryAddress("Boston Convention Center, Boston, MA");
            inquiry.setDeliveryRequired(true);
        } else if (msg.contains("cambridge")) {
            inquiry.setDeliveryCity("Cambridge");
            inquiry.setDeliveryAddress("Cambridge, MA");
            inquiry.setDeliveryRequired(true);
        }

        if (msg.contains("2 pm") || msg.contains("14:00")) inquiry.setDeliveryTime("14:00");
        else if (msg.contains("9 am") || msg.contains("09:00")) inquiry.setDeliveryTime("09:00");
        else if (msg.contains("10 am") || msg.contains("10:00")) inquiry.setDeliveryTime("10:00");

        // Table preference
        if (msg.contains("round")) inquiry.setTablePreference("ROUND");
        else if (msg.contains("rect") || msg.contains("banquet")) inquiry.setTablePreference("RECTANGULAR");
    }
}
