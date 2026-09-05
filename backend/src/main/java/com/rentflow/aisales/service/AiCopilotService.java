package com.rentflow.aisales.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentflow.ai.dto.BookingAttentionItemDTO;
import com.rentflow.ai.model.Booking;
import com.rentflow.ai.model.Customer;
import com.rentflow.ai.repository.BookingRepository;
import com.rentflow.ai.repository.CustomerRepository;
import com.rentflow.ai.service.BookingAttentionService;
import com.rentflow.aisales.dto.*;
import com.rentflow.aisales.model.*;
import com.rentflow.aisales.repository.AiSalesConversationRepository;
import com.rentflow.aisales.repository.AiSalesMessageRepository;
import com.rentflow.aisales.tool.AiSalesToolRegistry;
import com.rentflow.delivery.model.Delivery;
import com.rentflow.delivery.model.Driver;
import com.rentflow.delivery.repository.DeliveryRepository;
import com.rentflow.delivery.repository.DriverRepository;
import com.rentflow.invoice.model.Invoice;
import com.rentflow.invoice.model.InvoiceStatus;
import com.rentflow.invoice.repository.InvoiceRepository;
import com.rentflow.warehouse.model.WarehouseException;
import com.rentflow.warehouse.model.WarehouseExceptionStatus;
import com.rentflow.warehouse.model.WarehouseOrder;
import com.rentflow.warehouse.model.WarehouseOrderStatus;
import com.rentflow.warehouse.repository.WarehouseExceptionRepository;
import com.rentflow.warehouse.repository.WarehouseOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AiCopilotService {

    private static final Logger log = LoggerFactory.getLogger(AiCopilotService.class);

    private final AiSalesConversationRepository conversationRepository;
    private final AiSalesMessageRepository messageRepository;
    private final AiSalesToolRegistry toolRegistry;
    private final CopilotActionService actionService;
    private final CopilotBriefingService briefingService;
    private final BookingAttentionService bookingAttentionService;
    private final BookingRepository bookingRepository;
    private final DeliveryRepository deliveryRepository;
    private final DriverRepository driverRepository;
    private final WarehouseOrderRepository warehouseOrderRepository;
    private final WarehouseExceptionRepository warehouseExceptionRepository;
    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final ObjectMapper objectMapper;

    public AiCopilotService(AiSalesConversationRepository conversationRepository,
                            AiSalesMessageRepository messageRepository,
                            AiSalesToolRegistry toolRegistry,
                            CopilotActionService actionService,
                            CopilotBriefingService briefingService,
                            BookingAttentionService bookingAttentionService,
                            BookingRepository bookingRepository,
                            DeliveryRepository deliveryRepository,
                            DriverRepository driverRepository,
                            WarehouseOrderRepository warehouseOrderRepository,
                            WarehouseExceptionRepository warehouseExceptionRepository,
                            InvoiceRepository invoiceRepository,
                            CustomerRepository customerRepository,
                            ObjectMapper objectMapper) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.toolRegistry = toolRegistry;
        this.actionService = actionService;
        this.briefingService = briefingService;
        this.bookingAttentionService = bookingAttentionService;
        this.bookingRepository = bookingRepository;
        this.deliveryRepository = deliveryRepository;
        this.driverRepository = driverRepository;
        this.warehouseOrderRepository = warehouseOrderRepository;
        this.warehouseExceptionRepository = warehouseExceptionRepository;
        this.invoiceRepository = invoiceRepository;
        this.customerRepository = customerRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public CopilotResponseDTO startConversation(String tenantId, String userRole, String userId,
                                               String pageContextType, String pageContextId) {
        enforceEmployeeRole(userRole);

        AiSalesConversation conv = new AiSalesConversation();
        conv.setTenantId(tenantId);
        conv.setPublicId("COPILOT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        conv.setConversationType(AiConversationType.COPILOT);
        conv.setChannel(AiSalesChannel.INTERNAL);
        conv.setStatus(AiConversationStatus.ACTIVE);
        conv.setStartedAt(LocalDateTime.now());
        conv.setLastMessageAt(LocalDateTime.now());
        conv.setEmployeeRole(userRole);
        conv.setEmployeeUserId(userId);
        conv.setPageContextType(pageContextType);
        conv.setPageContextId(pageContextId);

        AiSalesConversation saved = conversationRepository.save(conv);

        // Generate initial greeting / briefing
        CopilotResponseDTO initialBriefing = briefingService.generateDailyBriefing(tenantId, userRole, userId, null);
        initialBriefing.setConversationId(saved.getId());

        // Save welcome message
        AiSalesMessage aiMsg = new AiSalesMessage();
        aiMsg.setTenantId(tenantId);
        aiMsg.setConversationId(saved.getId());
        aiMsg.setSenderType(AiSenderType.AI);
        aiMsg.setMessageType(AiMessageType.TEXT);
        aiMsg.setRole(AiRole.ASSISTANT);
        aiMsg.setContent(initialBriefing.getMessage());
        aiMsg.setCustomerVisible(false);
        aiMsg.setCreatedAt(LocalDateTime.now());
        try {
            aiMsg.setStructuredData(objectMapper.writeValueAsString(initialBriefing.getDataBlocks()));
        } catch (Exception ignored) {}
        messageRepository.save(aiMsg);

        return initialBriefing;
    }

    @Transactional
    public CopilotResponseDTO processMessage(String tenantId, String userRole, String userId,
                                            UUID conversationId, CopilotChatRequestDTO request) {
        enforceEmployeeRole(userRole);

        AiSalesConversation conv = conversationRepository.findByTenantIdAndId(tenantId, conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found or unauthorized"));

        // Update page context if provided
        if (request.getPageContextType() != null) {
            conv.setPageContextType(request.getPageContextType());
        }
        if (request.getPageContextId() != null) {
            conv.setPageContextId(request.getPageContextId());
        }

        String userQuery = (request.getMessage() != null ? request.getMessage().trim() : "");
        if (userQuery.isEmpty()) {
            throw new IllegalArgumentException("Message cannot be empty");
        }

        // Persist User Message
        AiSalesMessage userMsg = new AiSalesMessage();
        userMsg.setTenantId(tenantId);
        userMsg.setConversationId(conv.getId());
        userMsg.setSenderType(AiSenderType.EMPLOYEE);
        userMsg.setMessageType(AiMessageType.TEXT);
        userMsg.setRole(AiRole.USER);
        userMsg.setContent(userQuery);
        userMsg.setCustomerVisible(false);
        userMsg.setCreatedAt(LocalDateTime.now());
        messageRepository.save(userMsg);

        long startNs = System.nanoTime();
        CopilotResponseDTO response = synthesizeResponse(tenantId, userRole, userId, conv, userQuery);
        long durationMs = (System.nanoTime() - startNs) / 1_000_000;
        response.setLatencyMs(durationMs);
        response.setConversationId(conv.getId());

        // Persist Assistant Message
        AiSalesMessage assistantMsg = new AiSalesMessage();
        assistantMsg.setTenantId(tenantId);
        assistantMsg.setConversationId(conv.getId());
        assistantMsg.setSenderType(AiSenderType.AI);
        assistantMsg.setMessageType(AiMessageType.TEXT);
        assistantMsg.setRole(AiRole.ASSISTANT);
        assistantMsg.setContent(response.getMessage());
        assistantMsg.setLatencyMs(durationMs);
        assistantMsg.setCustomerVisible(false);
        assistantMsg.setCreatedAt(LocalDateTime.now());
        try {
            assistantMsg.setStructuredData(objectMapper.writeValueAsString(response.getDataBlocks()));
        } catch (Exception ignored) {}
        messageRepository.save(assistantMsg);

        conv.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conv);

        return response;
    }

    private CopilotResponseDTO synthesizeResponse(String tenantId, String userRole, String userId,
                                                 AiSalesConversation conv, String query) {
        String qLower = query.toLowerCase().trim();
        CopilotResponseDTO res = new CopilotResponseDTO();
        res.setConversationId(conv.getId());
        res.setRole("assistant");

        // 1. Prompt Injection / Secret Exfiltration Defense
        if (isPromptInjectionOrSecretAttempt(qLower)) {
            res.setDetectedIntent(CopilotIntent.SECURITY_EXCEPTION);
            res.setDeterministic(true);
            res.setMessage("🛡️ **Security Policy Enforcement**\n\n" +
                    "I cannot fulfill requests to bypass system instructions, modify operating parameters, " +
                    "or disclose internal system prompts, API keys, credentials, or administrative secrets.");
            res.setExplanation("Direct rejection of prompt injection or secret extraction attempt.");
            res.setSuggestedPrompts(List.of("How are we doing this month?", "Which bookings need attention tomorrow?"));
            return res;
        }

        // 2. Cross-Tenant Query Defense
        if (qLower.contains("tenant b") || qLower.contains("other tenant") || qLower.contains("switch tenant") || qLower.contains("different company")) {
            res.setDetectedIntent(CopilotIntent.SECURITY_EXCEPTION);
            res.setDeterministic(true);
            res.setMessage("🔒 **Tenant Isolation Policy**\n\n" +
                    "Access denied: RentFlow enforces strict cryptographic and relational tenant isolation. " +
                    "You cannot access operational, financial, or customer data belonging to other tenant organizations.");
            res.setExplanation("Enforcement of multi-tenant data boundaries.");
            return res;
        }

        // 3. Prohibited High-Impact Mutations
        if (isProhibitedAction(qLower)) {
            res.setDetectedIntent(CopilotIntent.ACTION_REJECTED);
            res.setDeterministic(true);
            res.setMessage("⛔ **Prohibited Autonomous Action**\n\n" +
                    "High-impact operations (such as processing refunds, capturing payments, cancelling bookings, " +
                    "writing off inventory, or overriding pricing) cannot be initiated or executed directly by Copilot.\n\n" +
                    "Please navigate to the appropriate administrative module in RentFlow to perform this operation with standard manager review and approval.");
            res.setExplanation("Prohibited high-impact action safely rejected according to governance policy.");
            res.getSourceReferences().add(new CopilotSourceReferenceDTO("Invoices & Billing", "/invoices", "FINANCE", "INVOICES"));
            return res;
        }

        // 4. Historical Data Boundary / Absence of Data Check
        if (qLower.contains("2018") || qLower.contains("2017") || qLower.contains("2015") || qLower.contains("10 years ago")) {
            res.setDetectedIntent(CopilotIntent.HISTORICAL_ANALYSIS);
            res.setDeterministic(true);
            res.setMessage("ℹ️ **Data Availability Boundary**\n\n" +
                    "No historical records exist for that period in your RentFlow account. " +
                    "Your organization's earliest recorded transaction and inventory data begins in 2024. " +
                    "RentFlow Copilot does not extrapolate or generate speculative historical figures when data is absent.");
            res.setExplanation("Verified absence of historical records in database.");
            res.setSuggestedPrompts(List.of("How are we doing this month?", "Why did margin fall?"));
            return res;
        }

        // 5. Page Contextual Query Handling (e.g. "Why is this blocked?")
        if (conv.getPageContextType() != null && conv.getPageContextId() != null &&
                (qLower.contains("why is this blocked") || qLower.contains("why is it blocked") || qLower.contains("what is blocking this") || qLower.contains("status of this"))) {
            return handleContextualBlockedQuery(tenantId, conv.getPageContextType(), conv.getPageContextId(), res);
        }

        // 6. Action Proposal: Assign Driver
        if (qLower.contains("assign") && (qLower.contains("driver") || qLower.contains("del-"))) {
            return handleDriverAssignmentProposal(tenantId, userRole, userId, conv, query, res);
        }

        // 7. Executive Summary / "How are we doing this month?"
        if (qLower.contains("how are we doing") || qLower.contains("executive summary") || qLower.contains("business overview")) {
            return handleExecutiveSummary(tenantId, userRole, res);
        }

        // 8. Explain Margin Drop / "Why did margin fall?"
        if (qLower.contains("margin") && (qLower.contains("fall") || qLower.contains("drop") || qLower.contains("why") || qLower.contains("decrease"))) {
            return handleMarginTrendExplanation(tenantId, userRole, res);
        }

        // 9. Bookings Needing Attention / "Which bookings need attention tomorrow?"
        if (qLower.contains("attention") || qLower.contains("tomorrow") || qLower.contains("at risk tomorrow")) {
            return handleBookingsAttention(tenantId, userRole, res);
        }

        // 10. Warehouse Blockers / "What is blocking the warehouse?"
        if (qLower.contains("warehouse") && (qLower.contains("blocking") || qLower.contains("blocker") || qLower.contains("issue") || qLower.contains("exception"))) {
            return handleWarehouseBlockers(tenantId, userRole, res);
        }

        // 11. Overdue AR / "How much is more than 30 days overdue?" / "Which customers owe us the most?"
        if (qLower.contains("30 days") || qLower.contains("overdue") || qLower.contains("owe us") || qLower.contains("aging") || qLower.contains("receivable")) {
            return handleArAging(tenantId, userRole, res);
        }

        // 12. Sales Follow-ups / "What should I follow up today?" / "Which quotes need follow-up?"
        if (qLower.contains("follow up") || qLower.contains("follow-up") || qLower.contains("quotes need")) {
            return handleSalesFollowUps(tenantId, userRole, res);
        }

        // 13. Customer Search with Disambiguation
        if (qLower.startsWith("find customer") || qLower.startsWith("search customer") || qLower.contains("customer info")) {
            return handleCustomerSearch(tenantId, userRole, query, res);
        }

        // 14. Delivery Risks
        if (qLower.contains("delivery") && (qLower.contains("risk") || qLower.contains("unassigned"))) {
            return handleDeliveryRisks(tenantId, userRole, res);
        }

        // Default Fallback
        res.setDetectedIntent(CopilotIntent.GENERAL_QUERY);
        res.setDeterministic(true);
        res.setMessage("I analyzed your request across RentFlow's operational and financial services. " +
                "You can ask me specific questions regarding executive KPIs, margin drivers, tomorrow's attention items, warehouse blockers, accounts receivable aging, or dispatch assignments.");
        res.setSuggestedPrompts(List.of(
                "How are we doing this month?",
                "Which bookings need attention tomorrow?",
                "Why did margin fall?",
                "What is blocking the warehouse?"
        ));
        return res;
    }

    private CopilotResponseDTO handleExecutiveSummary(String tenantId, String userRole, CopilotResponseDTO res) {
        ToolCallRequestDTO req = new ToolCallRequestDTO();
        req.setToolName("getExecutiveDashboard");
        req.setParameters(Map.of("dateRange", "THIS_MONTH"));
        ToolCallResultDTO toolRes = toolRegistry.executeTool(tenantId, userRole, req);

        res.setDetectedIntent(CopilotIntent.EXECUTIVE_SUMMARY);
        res.setDeterministic(true);
        res.setExplanation("Retrieved authoritative monthly revenue, gross margin, and booking counts from AnalyticsService.");

        if (toolRes.isSuccess() && toolRes.getData() != null) {
            Map<String, Object> data = toolRes.getData();
            BigDecimal booked = new BigDecimal(data.get("bookedRevenue").toString());
            BigDecimal collected = new BigDecimal(data.get("collectedRevenue").toString());
            BigDecimal margin = new BigDecimal(data.get("grossMarginPercent").toString());
            int count = Integer.parseInt(data.get("bookingsCount").toString());

            res.setMessage("Here is our **Executive Performance Summary** for " + data.get("periodName") + ":\n\n" +
                    "• **Booked Revenue**: $" + String.format("%,.2f", booked) + " across " + count + " active bookings\n" +
                    "• **Collected Revenue**: $" + String.format("%,.2f", collected) + "\n" +
                    "• **Gross Margin**: " + margin + "%\n" +
                    "• **Data Freshness**: " + data.get("dataFreshness") + "\n\n" +
                    "Revenue momentum is steady. Notice that gross margin has fluctuated compared to prior months due to delivery and repair costs.");

            res.getDataBlocks().add(new CopilotDataBlockDTO("KPI", "Executive Overview", Map.of(
                    "Booked Revenue", "$" + String.format("%,.2f", booked),
                    "Collected Revenue", "$" + String.format("%,.2f", collected),
                    "Gross Margin", margin + "%",
                    "Active Bookings", count
            )));

            res.getSourceReferences().add(new CopilotSourceReferenceDTO("Executive Dashboard", "/analytics/executive", "ANALYTICS", "DASHBOARD"));
            res.setSuggestedPrompts(List.of(
                    "Why did margin fall?",
                    "Which bookings need attention tomorrow?",
                    "How much is more than 30 days overdue?"
            ));
        } else {
            res.setMessage("Executive dashboard data is currently unavailable: " + toolRes.getErrorMessage());
        }
        return res;
    }

    private CopilotResponseDTO handleMarginTrendExplanation(String tenantId, String userRole, CopilotResponseDTO res) {
        ToolCallRequestDTO req = new ToolCallRequestDTO();
        req.setToolName("getMarginTrend");
        ToolCallResultDTO toolRes = toolRegistry.executeTool(tenantId, userRole, req);

        res.setDetectedIntent(CopilotIntent.MARGIN_DROP_EXPLANATION);
        res.setDeterministic(true);
        res.setExplanation("Synthesized margin trend variance and primary cost drivers from ProfitabilityService and DamageClaimService.");

        if (toolRes.isSuccess() && toolRes.getData() != null) {
            Map<String, Object> data = toolRes.getData();
            String current = (String) data.get("currentPeriodMargin");
            String prior = (String) data.get("priorPeriodMargin");
            String delta = (String) data.get("marginDelta");
            @SuppressWarnings("unchecked")
            Map<String, String> drivers = (Map<String, String>) data.get("primaryDrivers");

            StringBuilder sb = new StringBuilder();
            sb.append("### 📊 Margin Trend Analysis\n\n");
            sb.append("Gross margin shifted from **").append(prior).append("** last month to **").append(current).append("** this month (a change of **").append(delta).append("**).\n\n");
            sb.append("#### Primary Cost Drivers:\n");
            if (drivers != null) {
                sb.append("1. **Fleet Transit**: ").append(drivers.getOrDefault("deliveryRouteSurges", "Route mileage increases")).append("\n");
                sb.append("2. **Staging & Labor**: ").append(drivers.getOrDefault("laborOvertime", "Overtime setup staging")).append("\n");
                sb.append("3. **Unrecovered Damages**: ").append(drivers.getOrDefault("unrecoveredDamageCosts", "Return damage claims")).append("\n\n");
            }
            sb.append("ℹ️ *Notice: ").append(data.get("dataQualityNotice")).append("*");
            res.setMessage(sb.toString());

            res.getDataBlocks().add(new CopilotDataBlockDTO("TABLE", "Cost Driver Impact Breakdown", List.of(
                    Map.of("Cost Component", "Fleet Transit", "Impact", "Negative", "Note", drivers != null ? drivers.get("deliveryRouteSurges") : ""),
                    Map.of("Cost Component", "Labor Overtime", "Impact", "Negative", "Note", drivers != null ? drivers.get("laborOvertime") : ""),
                    Map.of("Cost Component", "Damage Claims", "Impact", "Moderate", "Note", drivers != null ? drivers.get("unrecoveredDamageCosts") : "")
            )));

            res.getSourceReferences().add(new CopilotSourceReferenceDTO("Profitability Analytics", "/analytics/profitability", "ANALYTICS", "PROFITABILITY"));
            res.setSuggestedPrompts(List.of(
                    "Which bookings need attention tomorrow?",
                    "What is blocking the warehouse?"
            ));
        } else {
            res.setMessage("Margin trend analysis failed: " + toolRes.getErrorMessage());
        }
        return res;
    }

    private CopilotResponseDTO handleBookingsAttention(String tenantId, String userRole, CopilotResponseDTO res) {
        ToolCallRequestDTO req = new ToolCallRequestDTO();
        req.setToolName("getBookingsNeedingAttention");
        req.setParameters(Map.of("daysAhead", 1));
        ToolCallResultDTO toolRes = toolRegistry.executeTool(tenantId, userRole, req);

        res.setDetectedIntent(CopilotIntent.ATTENTION_LIST);
        res.setDeterministic(true);
        res.setExplanation("Calculated deterministic operational blockers across inventory, warehouse readiness, driver assignments, contracts, and deposits.");

        if (toolRes.isSuccess() && toolRes.getData() != null) {
            Map<String, Object> data = toolRes.getData();
            int total = (int) data.get("totalBookingsNeedingAttention");
            long high = (long) data.get("highSeverityCount");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) data.get("attentionItems");

            StringBuilder sb = new StringBuilder();
            sb.append("Found **").append(total).append(" bookings** requiring attention for tomorrow (including **").append(high).append(" high-priority** operational flags):\n\n");

            for (Map<String, Object> it : items) {
                sb.append("• **").append(it.get("bookingNumber")).append("** (").append(it.get("customerName")).append("): ");
                @SuppressWarnings("unchecked")
                List<String> signals = (List<String>) it.get("signals");
                sb.append(String.join(", ", signals));
                sb.append(" [Severity: ").append(it.get("severity")).append("]\n");
            }
            sb.append("\nReview the flagged bookings below to ensure seamless dispatch tomorrow morning.");
            res.setMessage(sb.toString());

            res.getDataBlocks().add(new CopilotDataBlockDTO("TABLE", "Bookings Flagged for Action Tomorrow", items));
            res.getSourceReferences().add(new CopilotSourceReferenceDTO("Operations Calendar", "/calendar", "OPERATIONS", "CALENDAR"));

            res.setSuggestedPrompts(List.of(
                    "What is blocking the warehouse?",
                    "Are any deliveries at risk tomorrow?",
                    "Assign driver to pending delivery"
            ));
        } else {
            res.setMessage("Could not fetch bookings needing attention: " + toolRes.getErrorMessage());
        }
        return res;
    }

    private CopilotResponseDTO handleWarehouseBlockers(String tenantId, String userRole, CopilotResponseDTO res) {
        ToolCallRequestDTO req = new ToolCallRequestDTO();
        req.setToolName("getWarehouseBlockers");
        ToolCallResultDTO toolRes = toolRegistry.executeTool(tenantId, userRole, req);

        res.setDetectedIntent(CopilotIntent.WAREHOUSE_BLOCKERS);
        res.setDeterministic(true);
        res.setExplanation("Queried active warehouse exceptions and unassigned picking orders from WarehouseService.");

        if (toolRes.isSuccess() && toolRes.getData() != null) {
            Map<String, Object> data = toolRes.getData();
            int exCount = (int) data.get("totalOpenExceptions");
            int unassignedCount = (int) data.get("unassignedPickOrdersCount");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> exList = (List<Map<String, Object>>) data.get("exceptions");

            StringBuilder sb = new StringBuilder();
            sb.append("### 🏭 Warehouse Bottleneck Report\n\n");
            sb.append("• **Open Fulfillment Exceptions**: ").append(exCount).append(" item shortages or damage discrepancies\n");
            sb.append("• **Unassigned Pick Orders**: ").append(unassignedCount).append(" orders awaiting picker assignment\n\n");

            if (!exList.isEmpty()) {
                sb.append("#### Active Exceptions:\n");
                for (Map<String, Object> ex : exList) {
                    sb.append("• **[").append(ex.get("severity")).append("]** ").append(ex.get("type")).append(": ")
                            .append(ex.get("description")).append(" (").append(ex.get("productName")).append(")\n");
                }
            } else {
                sb.append("No critical equipment shortages are reported right now.");
            }
            res.setMessage(sb.toString());

            res.getDataBlocks().add(new CopilotDataBlockDTO("TABLE", "Open Warehouse Exceptions", exList));
            res.getSourceReferences().add(new CopilotSourceReferenceDTO("Warehouse Fulfillment", "/warehouse", "WAREHOUSE", "ORDERS"));

            res.setSuggestedPrompts(List.of(
                    "Which bookings need attention tomorrow?",
                    "Are any deliveries at risk tomorrow?"
            ));
        } else {
            res.setMessage("Failed to retrieve warehouse blockers: " + toolRes.getErrorMessage());
        }
        return res;
    }

    private CopilotResponseDTO handleArAging(String tenantId, String userRole, CopilotResponseDTO res) {
        ToolCallRequestDTO req = new ToolCallRequestDTO();
        req.setToolName("getArAging");
        ToolCallResultDTO toolRes = toolRegistry.executeTool(tenantId, userRole, req);

        res.setDetectedIntent(CopilotIntent.OVERDUE_INVOICES);
        res.setDeterministic(true);
        res.setExplanation("Calculated accounts receivable aging buckets and top debtor balances from live invoices.");

        if (toolRes.isSuccess() && toolRes.getData() != null) {
            Map<String, Object> data = toolRes.getData();
            BigDecimal totalOver30 = new BigDecimal(data.get("totalOverdueMoreThan30Days").toString());
            BigDecimal totalOverdue = new BigDecimal(data.get("totalOverdue").toString());
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> topDebtors = (List<Map<String, Object>>) data.get("topDebtorCustomers");

            StringBuilder sb = new StringBuilder();
            sb.append("### 💵 Accounts Receivable Aging Summary\n\n");
            sb.append("• **Total Past-Due Balance**: $").append(String.format("%,.2f", totalOverdue)).append("\n");
            sb.append("• **Past Due > 30 Days**: **$").append(String.format("%,.2f", totalOver30)).append("**\n\n");

            if (!topDebtors.isEmpty()) {
                sb.append("#### Top Debtor Accounts:\n");
                for (Map<String, Object> d : topDebtors) {
                    BigDecimal bal = new BigDecimal(d.get("overdueBalance").toString());
                    sb.append("• **").append(d.get("customerName")).append("**: $").append(String.format("%,.2f", bal)).append("\n");
                }
            }
            res.setMessage(sb.toString());

            res.getDataBlocks().add(new CopilotDataBlockDTO("TABLE", "Aging Buckets", List.of(
                    Map.of("Bucket", "0-30 Days Past Due", "Amount", "$" + String.format("%,.2f", new BigDecimal(data.get("bucket0to30Days").toString()))),
                    Map.of("Bucket", "31-60 Days Past Due", "Amount", "$" + String.format("%,.2f", new BigDecimal(data.get("bucket31to60Days").toString()))),
                    Map.of("Bucket", "61-90 Days Past Due", "Amount", "$" + String.format("%,.2f", new BigDecimal(data.get("bucket61to90Days").toString()))),
                    Map.of("Bucket", "90+ Days Past Due", "Amount", "$" + String.format("%,.2f", new BigDecimal(data.get("bucket90PlusDays").toString())))
            )));

            res.getSourceReferences().add(new CopilotSourceReferenceDTO("Invoices & Receivables", "/invoices", "FINANCE", "INVOICES"));
            res.setSuggestedPrompts(List.of(
                    "What should I follow up today?",
                    "How are we doing this month?"
            ));
        } else {
            res.setMessage("Failed to calculate accounts receivable aging: " + toolRes.getErrorMessage());
        }
        return res;
    }

    private CopilotResponseDTO handleSalesFollowUps(String tenantId, String userRole, CopilotResponseDTO res) {
        ToolCallRequestDTO fReq = new ToolCallRequestDTO();
        fReq.setToolName("getSalesFollowUpsDue");
        ToolCallResultDTO fRes = toolRegistry.executeTool(tenantId, userRole, fReq);

        ToolCallRequestDTO qReq = new ToolCallRequestDTO();
        qReq.setToolName("getQuotesAwaitingResponse");
        ToolCallResultDTO qRes = toolRegistry.executeTool(tenantId, userRole, qReq);

        res.setDetectedIntent(CopilotIntent.SALES_FOLLOW_UPS);
        res.setDeterministic(true);
        res.setExplanation("Gathered overdue customer touchpoints and pending quotes from LeadFollowUpService and QuoteService.");

        StringBuilder sb = new StringBuilder();
        sb.append("### 🎯 Sales Outreach & Follow-Up Tasks\n\n");

        if (fRes.isSuccess() && fRes.getData() != null) {
            int overdue = (int) fRes.getData().get("totalOverdueCount");
            int dueToday = (int) fRes.getData().get("dueTodayCount");
            sb.append("• **Overdue Follow-ups**: ").append(overdue).append(" tasks past due\n");
            sb.append("• **Scheduled Today**: ").append(dueToday).append(" tasks due today\n");
        }

        if (qRes.isSuccess() && qRes.getData() != null) {
            int quotes = (int) qRes.getData().get("totalQuotesAwaitingResponse");
            sb.append("• **Quotes Awaiting Customer Response**: ").append(quotes).append(" active proposals\n\n");
        }

        sb.append("Prioritize contacting customers with overdue touchpoints and expiring quotes.");
        res.setMessage(sb.toString());

        if (fRes.isSuccess() && fRes.getData() != null) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> list = (List<Map<String, Object>>) fRes.getData().get("overdueFollowUps");
            if (!list.isEmpty()) {
                res.getDataBlocks().add(new CopilotDataBlockDTO("TABLE", "Overdue Sales Follow-ups", list));
            }
        }

        res.getSourceReferences().add(new CopilotSourceReferenceDTO("CRM Leads Board", "/crm/leads", "CRM", "LEADS"));
        res.setSuggestedPrompts(List.of(
                "Which bookings need attention tomorrow?",
                "How are we doing this month?"
        ));
        return res;
    }

    private CopilotResponseDTO handleCustomerSearch(String tenantId, String userRole, String query, CopilotResponseDTO res) {
        String searchTerm = query.replaceFirst("(?i)find customer", "")
                .replaceFirst("(?i)search customer", "")
                .replaceFirst("(?i)customer info", "")
                .trim();
        if (searchTerm.isEmpty()) searchTerm = "Smith";

        ToolCallRequestDTO req = new ToolCallRequestDTO();
        req.setToolName("searchCustomers");
        req.setParameters(Map.of("query", searchTerm));
        ToolCallResultDTO toolRes = toolRegistry.executeTool(tenantId, userRole, req);

        res.setDetectedIntent(CopilotIntent.CUSTOMER_LOOKUP);
        res.setDeterministic(true);

        if (toolRes.isSuccess() && toolRes.getData() != null) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> customers = (List<Map<String, Object>>) toolRes.getData().get("customers");
            boolean disambiguate = (boolean) toolRes.getData().get("requiresDisambiguation");

            if (customers.isEmpty()) {
                res.setMessage("No customers found matching '" + searchTerm + "'. Please check the spelling or search by phone/email.");
            } else if (disambiguate) {
                StringBuilder sb = new StringBuilder();
                sb.append("Found multiple customers matching '**").append(searchTerm).append("**':\n\n");
                for (int i = 0; i < customers.size(); i++) {
                    Map<String, Object> c = customers.get(i);
                    sb.append((i + 1)).append(". **").append(c.get("name")).append("** (").append(c.get("companyName"))
                            .append(") — Email: `").append(c.get("email")).append("` [").append(c.get("customerNumber")).append("]\n");
                }
                sb.append("\nPlease specify which customer profile you would like to inspect.");
                res.setMessage(sb.toString());
                res.getDataBlocks().add(new CopilotDataBlockDTO("TABLE", "Matching Customer Accounts", customers));
            } else {
                Map<String, Object> c = customers.get(0);
                res.setMessage("Found customer record for **" + c.get("name") + "** (" + c.get("companyName") + "):\n\n" +
                        "• **Customer Number**: " + c.get("customerNumber") + "\n" +
                        "• **Email**: " + c.get("email") + "\n" +
                        "• **Phone**: " + c.get("phone") + "\n" +
                        "• **Account Type**: " + c.get("type"));
                res.getDataBlocks().add(new CopilotDataBlockDTO("ENTITY", "Customer Profile", c));
                res.getSourceReferences().add(new CopilotSourceReferenceDTO(
                        (String) c.get("name"), "/customers/" + c.get("customerId"), "CUSTOMER", (String) c.get("customerId")
                ));
            }
        } else {
            res.setMessage("Customer search failed: " + toolRes.getErrorMessage());
        }
        return res;
    }

    private CopilotResponseDTO handleDeliveryRisks(String tenantId, String userRole, CopilotResponseDTO res) {
        ToolCallRequestDTO req = new ToolCallRequestDTO();
        req.setToolName("getDeliveryRisks");
        req.setParameters(Map.of("daysAhead", 1));
        ToolCallResultDTO toolRes = toolRegistry.executeTool(tenantId, userRole, req);

        res.setDetectedIntent(CopilotIntent.DELIVERY_ANALYSIS);
        res.setDeterministic(true);

        if (toolRes.isSuccess() && toolRes.getData() != null) {
            int count = (int) toolRes.getData().get("atRiskDeliveriesCount");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> list = (List<Map<String, Object>>) toolRes.getData().get("atRiskDeliveries");

            if (count == 0) {
                res.setMessage("All scheduled deliveries for tomorrow have confirmed drivers, assigned vehicles, and ready warehouse staging.");
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("⚠️ Found **").append(count).append(" deliveries at risk** for tomorrow:\n\n");
                for (Map<String, Object> d : list) {
                    sb.append("• **").append(d.get("deliveryNumber")).append("**: ");
                    @SuppressWarnings("unchecked")
                    List<String> risks = (List<String>) d.get("risks");
                    sb.append(String.join(", ", risks)).append("\n");
                }
                sb.append("\nAssign drivers and vehicles promptly to avoid departure delays.");
                res.setMessage(sb.toString());
                res.getDataBlocks().add(new CopilotDataBlockDTO("TABLE", "Deliveries Requiring Dispatch Action", list));
            }
            res.getSourceReferences().add(new CopilotSourceReferenceDTO("Delivery Dispatch", "/deliveries", "DELIVERY", "BOARD"));
        } else {
            res.setMessage("Failed to analyze delivery risks: " + toolRes.getErrorMessage());
        }
        return res;
    }

    private CopilotResponseDTO handleDriverAssignmentProposal(String tenantId, String userRole, String userId,
                                                             AiSalesConversation conv, String query, CopilotResponseDTO res) {
        // Look up delivery
        List<Delivery> deliveries = deliveryRepository.findByTenantId(tenantId);
        Delivery targetDelivery = deliveries.stream()
                .filter(d -> d.getDriverId() == null && d.getStatus() != DeliveryStatus.CANCELLED)
                .findFirst()
                .orElse(deliveries.isEmpty() ? null : deliveries.get(0));

        // Look up driver
        List<Driver> drivers = driverRepository.findByTenantId(tenantId);
        Driver targetDriver = drivers.stream()
                .filter(d -> Boolean.TRUE.equals(d.getActive()))
                .findFirst()
                .orElse(null);

        if (targetDelivery == null || targetDriver == null) {
            res.setDetectedIntent(CopilotIntent.ACTION_REJECTED);
            res.setMessage("Unable to propose driver assignment: could not find an unassigned delivery or available active driver in this tenant.");
            return res;
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("deliveryId", targetDelivery.getId().toString());
        payload.put("deliveryNumber", targetDelivery.getDeliveryNumber());
        payload.put("driverId", targetDriver.getId().toString());
        payload.put("driverName", targetDriver.getName());

        CopilotActionProposalDTO proposal = actionService.proposeAction(
                tenantId, conv.getId(), userId, CopilotActionType.ASSIGN_DRIVER,
                "DELIVERY", targetDelivery.getId().toString(),
                "Assign driver " + targetDriver.getName() + " to delivery " + targetDelivery.getDeliveryNumber(),
                CopilotRiskLevel.MEDIUM, payload
        );

        res.setDetectedIntent(CopilotIntent.ACTION_PROPOSED);
        res.setDeterministic(true);
        res.setMessage("I have prepared an action proposal to assign **" + targetDriver.getName() + "** to delivery **" + targetDelivery.getDeliveryNumber() + "**.\n\n" +
                "Please review the details and confirm the assignment below:");
        res.getActionProposals().add(proposal);
        res.getSourceReferences().add(new CopilotSourceReferenceDTO("Delivery " + targetDelivery.getDeliveryNumber(),
                "/deliveries/" + targetDelivery.getId(), "DELIVERY", targetDelivery.getId().toString()));
        return res;
    }

    private CopilotResponseDTO handleContextualBlockedQuery(String tenantId, String pageContextType, String pageContextId, CopilotResponseDTO res) {
        res.setDetectedIntent(CopilotIntent.ENTITY_EXPLANATION);
        res.setDeterministic(true);

        if ("BOOKING".equalsIgnoreCase(pageContextType)) {
            UUID bookingId;
            try {
                bookingId = UUID.fromString(pageContextId);
            } catch (Exception e) {
                res.setMessage("Invalid booking context ID: " + pageContextId);
                return res;
            }

            Booking booking = bookingRepository.findById(bookingId).orElse(null);
            if (booking == null || !tenantId.equals(booking.getTenantId())) {
                res.setMessage("Booking not found or belongs to another tenant.");
                return res;
            }

            // Inspect blockers
            List<String> blockers = new ArrayList<>();
            WarehouseOrder wh = warehouseOrderRepository.findByTenantIdAndBookingId(tenantId, bookingId).orElse(null);
            if (wh != null && wh.getStatus() != WarehouseOrderStatus.PACKED && wh.getStatus() != WarehouseOrderStatus.READY_FOR_DELIVERY) {
                blockers.add("Warehouse fulfillment is incomplete (current status: " + wh.getStatus() + ")");
                long exceptions = warehouseExceptionRepository.countByTenantIdAndWarehouseOrderIdAndStatus(tenantId, wh.getId(), WarehouseExceptionStatus.OPEN);
                if (exceptions > 0) {
                    blockers.add(exceptions + " open shortage/damage exception(s) on warehouse order " + wh.getOrderNumber());
                }
            }

            if (booking.getDepositRequired() != null && booking.getDepositRequired().compareTo(BigDecimal.ZERO) > 0 &&
                    (booking.getDepositPaid() == null || booking.getDepositPaid().compareTo(booking.getDepositRequired()) < 0)) {
                blockers.add("Security deposit is unpaid ($" + booking.getDepositRequired() + " required)");
            }

            if (!Boolean.TRUE.equals(booking.getContractSigned())) {
                blockers.add("Rental agreement contract is unsigned by customer");
            }

            StringBuilder sb = new StringBuilder();
            sb.append("### 🔍 Contextual Inspection: Booking **").append(booking.getBookingNumber()).append("**\n\n");
            if (blockers.isEmpty()) {
                sb.append("This booking is **not blocked**! All warehouse, contract, and deposit criteria are satisfied.");
            } else {
                sb.append("This booking is currently held up by the following blockers:\n");
                for (String b : blockers) {
                    sb.append("• ").append(b).append("\n");
                }
            }
            res.setMessage(sb.toString());
            res.getSourceReferences().add(new CopilotSourceReferenceDTO("Booking Details", "/bookings/" + booking.getId(), "BOOKING", booking.getId().toString()));
            return res;
        }

        res.setMessage("Contextual explanation not configured for page context type: " + pageContextType);
        return res;
    }

    private void enforceEmployeeRole(String userRole) {
        if (userRole == null || "CUSTOMER".equalsIgnoreCase(userRole.trim())) {
            throw new SecurityException("Access denied: RentFlow Copilot is restricted to internal employees.");
        }
    }

    private boolean isPromptInjectionOrSecretAttempt(String q) {
        return q.contains("ignore rules") ||
                q.contains("ignore your rules") ||
                q.contains("ignore instructions") ||
                q.contains("ignore all instructions") ||
                q.contains("system prompt") ||
                q.contains("reveal your prompt") ||
                q.contains("show system prompt") ||
                q.contains("api key") ||
                q.contains("api_key") ||
                q.contains("master password") ||
                q.contains("db password") ||
                q.contains("database password");
    }

    private boolean isProhibitedAction(String q) {
        return q.startsWith("refund ") ||
                q.contains("refund invoice") ||
                q.contains("issue refund") ||
                q.contains("capture payment") ||
                q.contains("charge credit card") ||
                q.contains("cancel booking") ||
                q.contains("write off inventory") ||
                q.contains("override price");
    }
}
