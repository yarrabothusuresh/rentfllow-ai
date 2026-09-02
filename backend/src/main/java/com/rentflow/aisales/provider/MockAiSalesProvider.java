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

        // 1. Check for prompt injection / internal cost attempt
        if (msgLower.contains("ignore your rules") ||
            msgLower.contains("ignore your instructions") ||
            msgLower.contains("system prompt") ||
            msgLower.contains("internal margin") ||
            msgLower.contains("purchase cost") ||
            msgLower.contains("profit margin")) {

            resp.setDetectedIntent(AiIntent.GENERAL_RENTAL_QUESTION);
            resp.setStatus(AiConversationStatus.ACTIVE);
            resp.setReplyText("I am unable to display internal system instructions, cost structures, or profit margins. I'd be happy to assist you with our rental product catalog, date-aware availability, or preparing a rental quote draft!");
            resp.setSuggestedReplies(List.of("Show table options", "Show chair options", "Talk to a person"));
            return resp;
        }

        // 2. Check for human escalation request
        if (msgLower.contains("talk to a person") ||
            msgLower.contains("speak to someone") ||
            msgLower.contains("talk to human") ||
            msgLower.contains("human") ||
            msgLower.contains("agent") ||
            msgLower.contains("representative")) {

            ToolCallRequestDTO toolReq = new ToolCallRequestDTO("escalateToHuman", Map.of(
                "conversationId", conversation.getId().toString(),
                "reason", "CUSTOMER_REQUEST",
                "summary", "Customer explicitly asked to speak with a human representative."
            ));
            ToolCallResultDTO toolRes = toolRegistry.executeTool(tenantId, userRole, toolReq);
            resp.getExecutedTools().add(toolRes);

            resp.setDetectedIntent(AiIntent.HUMAN_ASSISTANCE);
            resp.setStatus(AiConversationStatus.WAITING_FOR_HUMAN);
            resp.setEscalatedToHuman(true);
            resp.setEscalationReason("CUSTOMER_REQUEST");
            resp.setReplyText("I have notified our sales team! A representative has been assigned to this conversation and will take over shortly. Thank you for your patience.");
            return resp;
        }

        // 3. Fake Product Inquiry Scenario
        if (msgLower.contains("royal diamond") || msgLower.contains("diamond gold chair")) {
            resp.setDetectedIntent(AiIntent.PRODUCT_SEARCH);
            resp.setStatus(AiConversationStatus.ACTIVE);

            ToolCallResultDTO searchRes = toolRegistry.executeTool(tenantId, userRole,
                new ToolCallRequestDTO("searchProducts", Map.of("query", "chair", "limit", 3)));
            resp.getExecutedTools().add(searchRes);

            resp.setReplyText("I couldn't find 'Royal Diamond Chairs' in our rental catalog. However, we have excellent premium seating alternatives available like our Gold Chiavari Chairs and Cross-Back Chairs! Would you like to check availability for those?");
            resp.setSuggestedReplies(List.of("Check chair availability", "Show table options", "Talk to a person"));
            return resp;
        }

        // 4. Extract or update structured inquiry parameters
        extractInquiryDetails(msgLower, currentInquiry);

        // 5. Check if customer provided city / time / table preference
        if (currentInquiry.getGuestCount() != null && currentInquiry.getGuestCount() > 0) {
            // Missing location or delivery time?
            if (currentInquiry.getDeliveryCity() == null && (msgLower.contains("wedding") || msgLower.contains("event") || msgLower.contains("chairs"))) {
                resp.setDetectedIntent(AiIntent.QUOTE_REQUEST);
                resp.setStatus(AiConversationStatus.WAITING_FOR_CUSTOMER);
                resp.setReplyText("Congratulations on the upcoming wedding! For " + currentInquiry.getGuestCount() + " guests, I can help arrange tables, chairs, and linens. What city will the event take place in, and approximately what time should we schedule delivery?");
                resp.setSuggestedReplies(List.of("Boston, delivery around 2 PM", "Cambridge, delivery at 10 AM", "Customer pickup instead"));
                return resp;
            }

            // Missing table preference?
            if (currentInquiry.getTablePreference() == null && !msgLower.contains("round") && !msgLower.contains("rectangular")) {
                resp.setDetectedIntent(AiIntent.QUOTE_REQUEST);
                resp.setStatus(AiConversationStatus.WAITING_FOR_CUSTOMER);
                resp.setReplyText("Thank you! For a 100-guest layout, do you prefer round tables (seating 10 each) or rectangular banquet tables?");
                resp.setSuggestedReplies(List.of("Round tables", "Rectangular tables", "Mix of both"));
                return resp;
            }

            // 6. Complete inquiry -> Execute Tools (Search, Availability, Pricing, Profitability, Quote Draft)
            return buildQuoteDraftFlow(tenantId, userRole, conversation, currentInquiry, resp, msgLower);
        }

        // Default greeting / discovery
        resp.setDetectedIntent(AiIntent.GENERAL_RENTAL_QUESTION);
        resp.setStatus(AiConversationStatus.ACTIVE);
        resp.setReplyText("Hello! I'm the RentFlow AI Sales Assistant. I can help you discover event rentals, check real-time availability, and prepare a quote. What type of event are you planning, and for how many guests?");
        resp.setSuggestedReplies(List.of("Wedding for 100 guests", "Corporate banquet for 50", "Party rentals next weekend", "Check chair availability"));
        return resp;
    }

    private void extractInquiryDetails(String msg, RentalInquiry inquiry) {
        if (msg.contains("wedding")) inquiry.setEventType("WEDDING");
        else if (msg.contains("corporate") || msg.contains("banquet")) inquiry.setEventType("CORPORATE");
        else if (msg.contains("birthday") || msg.contains("party")) inquiry.setEventType("PARTY");

        // Guest count extraction
        if (msg.contains("100 people") || msg.contains("100 guests") || msg.contains("100")) {
            inquiry.setGuestCount(100);
        } else if (msg.contains("50 people") || msg.contains("50 guests") || msg.contains("50")) {
            inquiry.setGuestCount(50);
        } else if (msg.contains("150 people") || msg.contains("150 guests") || msg.contains("150")) {
            inquiry.setGuestCount(150);
        } else if (msg.contains("200 people") || msg.contains("200 guests") || msg.contains("200")) {
            inquiry.setGuestCount(200);
        }

        // Relative date handling: "next saturday", "saturday", "weekend"
        if (msg.contains("next saturday") || msg.contains("saturday")) {
            LocalDate nextSat = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SATURDAY));
            inquiry.setEventDate(nextSat);
            inquiry.setRentalStart(nextSat.atTime(9, 0));
            inquiry.setRentalEnd(nextSat.plusDays(2).atTime(18, 0));
        }

        // City & Delivery Time
        if (msg.contains("boston")) {
            inquiry.setDeliveryCity("Boston");
            inquiry.setDeliveryAddress("Boston, MA");
            inquiry.setDeliveryRequired(true);
        } else if (msg.contains("cambridge")) {
            inquiry.setDeliveryCity("Cambridge");
            inquiry.setDeliveryAddress("Cambridge, MA");
            inquiry.setDeliveryRequired(true);
        }

        if (msg.contains("2 pm") || msg.contains("14:00")) inquiry.setDeliveryTime("14:00");
        else if (msg.contains("10 am") || msg.contains("10:00")) inquiry.setDeliveryTime("10:00");

        // Table preference
        if (msg.contains("round")) inquiry.setTablePreference("ROUND");
        else if (msg.contains("rect")) inquiry.setTablePreference("RECTANGULAR");
    }

    private AiSalesChatResponseDTO buildQuoteDraftFlow(
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

        // 1. Tool: searchProducts for tables and chairs
        ToolCallResultDTO searchTablesRes = toolRegistry.executeTool(tenantId, userRole,
            new ToolCallRequestDTO("searchProducts", Map.of("query", "round", "limit", 1)));
        ToolCallResultDTO searchChairsRes = toolRegistry.executeTool(tenantId, userRole,
            new ToolCallRequestDTO("searchProducts", Map.of("query", "chair", "limit", 1)));

        resp.getExecutedTools().add(searchTablesRes);
        resp.getExecutedTools().add(searchChairsRes);

        List<Product> products = productRepository.findByTenantId(tenantId);
        Product tableProduct = products.stream()
            .filter(p -> p.getName() != null && p.getName().toLowerCase().contains("round"))
            .findFirst().orElse(null);
        Product chairProduct = products.stream()
            .filter(p -> p.getName() != null && p.getName().toLowerCase().contains("chair"))
            .findFirst().orElse(null);

        if (tableProduct == null || chairProduct == null) {
            resp.setReplyText("I am having trouble locating matching seating and tables in your catalog right now. I have informed our sales team to follow up!");
            resp.setStatus(AiConversationStatus.WAITING_FOR_HUMAN);
            return resp;
        }

        // Check if customer asked for 200 chairs when only 75 are available (Demo Scenario 2)
        if (msgLower.contains("200 chiavari chairs") || (msgLower.contains("200") && msgLower.contains("chairs"))) {
            resp.setDetectedIntent(AiIntent.AVAILABILITY_CHECK);
            resp.setStatus(AiConversationStatus.ACTIVE);
            resp.setReplyText("We currently have availability for 75 Chiavari chairs for those dates. Would you like to reserve the 75 and supplement with our matching folding chairs, or would you prefer to explore alternative dates?");
            resp.setSuggestedReplies(List.of("Supplement with folding chairs", "Check alternative dates", "Talk to a person"));
            return resp;
        }

        List<Map<String, Object>> itemsList = List.of(
            Map.of("productId", tableProduct.getId().toString(), "quantity", tablesNeeded),
            Map.of("productId", chairProduct.getId().toString(), "quantity", chairsNeeded)
        );

        String startIso = inquiry.getRentalStart() != null ? inquiry.getRentalStart().toLocalDate().toString() : LocalDate.now().plusDays(7).toString();
        String endIso = inquiry.getRentalEnd() != null ? inquiry.getRentalEnd().toLocalDate().toString() : LocalDate.now().plusDays(9).toString();

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

        // 4. Tool: checkInternalProfitability (internal only)
        ToolCallResultDTO profitRes = toolRegistry.executeTool(tenantId, "SALES",
            new ToolCallRequestDTO("checkInternalProfitability", Map.of(
                "items", itemsList,
                "deliveryRequired", inquiry.isDeliveryRequired()
            )));
        resp.getExecutedTools().add(profitRes);

        // 5. Tool: createQuoteDraft
        ToolCallResultDTO quoteDraftRes = toolRegistry.executeTool(tenantId, "SALES",
            new ToolCallRequestDTO("createQuoteDraft", Map.of(
                "items", itemsList,
                "customerId", inquiry.getCustomerId() != null ? inquiry.getCustomerId().toString() : "",
                "customerName", conversation.getCustomerName() != null ? conversation.getCustomerName() : "Wedding Inquirer",
                "customerEmail", conversation.getCustomerEmail() != null ? conversation.getCustomerEmail() : "inquiry@client.com",
                "startDate", startIso,
                "endDate", endIso,
                "deliveryAddress", inquiry.getDeliveryAddress() != null ? inquiry.getDeliveryAddress() : "Boston, MA",
                "notes", "100 Guest Wedding Seating Package (" + tablesNeeded + " Round Tables, " + chairsNeeded + " Chairs)"
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
                       guests + "-guest wedding in " + (inquiry.getDeliveryCity() != null ? inquiry.getDeliveryCity() : "Boston") + ":\n\n" +
                       "• " + tablesNeeded + "x " + tableProduct.getName() + " (seats 10 each)\n" +
                       "• " + chairsNeeded + "x " + chairProduct.getName() + "\n" +
                       "• Delivery & standard setup\n\n" +
                       "Our sales staff is reviewing your draft (" + (resp.getQuoteDraftNumber() != null ? resp.getQuoteDraftNumber() : "QUO-DRAFT") +
                       ") to ensure perfect logistics and will send the official finalized quote shortly!";

        resp.setReplyText(reply);
        resp.setSuggestedReplies(List.of("View quote status", "Ask about linens", "Talk to a person"));
        return resp;
    }
}
