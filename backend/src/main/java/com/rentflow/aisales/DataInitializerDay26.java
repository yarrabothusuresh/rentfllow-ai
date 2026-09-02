package com.rentflow.aisales;

import com.rentflow.ai.model.Customer;
import com.rentflow.ai.repository.CustomerRepository;
import com.rentflow.aisales.model.*;
import com.rentflow.aisales.repository.*;
import com.rentflow.aisales.service.AiSalesSettingsService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
@Order(30)
public class DataInitializerDay26 implements CommandLineRunner {

    private final AiPromptTemplateRepository promptTemplateRepository;
    private final AiTenantSettingsRepository settingsRepository;
    private final AiSalesConversationRepository conversationRepository;
    private final AiSalesMessageRepository messageRepository;
    private final RentalInquiryRepository inquiryRepository;
    private final AiEscalationRepository escalationRepository;
    private final CustomerRepository customerRepository;

    public DataInitializerDay26(
        AiPromptTemplateRepository promptTemplateRepository,
        AiTenantSettingsRepository settingsRepository,
        AiSalesConversationRepository conversationRepository,
        AiSalesMessageRepository messageRepository,
        RentalInquiryRepository inquiryRepository,
        AiEscalationRepository escalationRepository,
        CustomerRepository customerRepository
    ) {
        this.promptTemplateRepository = promptTemplateRepository;
        this.settingsRepository = settingsRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.inquiryRepository = inquiryRepository;
        this.escalationRepository = escalationRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    public void run(String... args) {
        seedPromptTemplates();
        seedTenantSettings("tenant-evergreen");
        seedDemoConversations("tenant-evergreen");
    }

    private void seedPromptTemplates() {
        if (promptTemplateRepository.count() > 0) return;

        AiPromptTemplate sysPrompt = new AiPromptTemplate();
        sysPrompt.setName("Default Sales Assistant Prompt");
        sysPrompt.setVersion("1.0.0");
        sysPrompt.setType(AiPromptType.SALES_SYSTEM);
        sysPrompt.setContent("""
            You are the RentFlow AI Sales Assistant for party, wedding, and event equipment rentals.
            Your role is to understand event requirements, recommend suitable rental items, verify inventory availability, and prepare draft quotes for human approval.
            Never promise unavailable stock. Never expose internal profit margins or purchase costs.
            """);
        promptTemplateRepository.save(sysPrompt);

        AiPromptTemplate inqPrompt = new AiPromptTemplate();
        inqPrompt.setName("Inquiry Extraction Prompt");
        inqPrompt.setVersion("1.0.0");
        inqPrompt.setType(AiPromptType.INQUIRY_EXTRACTION);
        inqPrompt.setContent("Extract eventType, guestCount, eventDate, deliveryCity, and tablePreference from user messages.");
        promptTemplateRepository.save(inqPrompt);
    }

    private void seedTenantSettings(String tenantId) {
        if (settingsRepository.findByTenantId(tenantId).isPresent()) return;

        AiTenantSettings s = new AiTenantSettings();
        s.setTenantId(tenantId);
        s.setAiEnabled(true);
        s.setAiProvider("mock");
        s.setAiModel("mock-sales-v1");
        s.setCustomerAiEnabled(true);
        s.setInternalSalesAssistantEnabled(true);
        s.setHumanQuoteApprovalRequired(true);
        s.setDailyRequestLimit(500);
        s.setMaxConversationMessages(20);
        s.setTargetGrossMarginPct(30.0);
        s.setLowMarginThresholdPct(20.0);
        settingsRepository.save(s);
    }

    private void seedDemoConversations(String tenantId) {
        if (conversationRepository.countByTenantId(tenantId) > 0) return;

        UUID custId = null;
        Optional<Customer> custOpt = customerRepository.findByTenantId(tenantId).stream().findFirst();
        if (custOpt.isPresent()) {
            custId = custOpt.get().getId();
        }

        // Demo Conv 1: Wedding 100 Guests
        AiSalesConversation conv1 = new AiSalesConversation();
        conv1.setTenantId(tenantId);
        conv1.setPublicId("conv_wedding100demo");
        conv1.setCustomerId(custId);
        conv1.setCustomerName("Sarah Jenkins");
        conv1.setCustomerEmail("sarah.jenkins@example.com");
        conv1.setChannel(AiSalesChannel.CUSTOMER_PORTAL);
        conv1.setStatus(AiConversationStatus.WAITING_FOR_HUMAN);
        conv1.setDetectedIntent(AiIntent.QUOTE_REQUEST);
        conv1.setStartedAt(LocalDateTime.now().minusHours(2));
        conv1.setLastMessageAt(LocalDateTime.now().minusMinutes(15));
        conv1 = conversationRepository.save(conv1);

        RentalInquiry inq1 = new RentalInquiry();
        inq1.setTenantId(tenantId);
        inq1.setConversationId(conv1.getId());
        inq1.setCustomerId(custId);
        inq1.setEventType("WEDDING");
        inq1.setEventName("Jenkins Wedding Reception");
        inq1.setGuestCount(100);
        inq1.setEventDate(LocalDate.now().plusDays(14));
        inq1.setRentalStart(LocalDate.now().plusDays(14).atTime(9, 0));
        inq1.setRentalEnd(LocalDate.now().plusDays(16).atTime(18, 0));
        inq1.setDeliveryAddress("450 Beacon St, Boston, MA");
        inq1.setDeliveryCity("Boston");
        inq1.setDeliveryTime("14:00");
        inq1.setDeliveryRequired(true);
        inq1.setTablePreference("ROUND");
        inq1.setChairPreference("CHIAVARI");
        inq1.setBudget(BigDecimal.valueOf(2500.00));
        inq1.setComplete(true);
        inq1.setNotes("100 guest reception setup. 10 round tables, 100 chairs.");
        inquiryRepository.save(inq1);

        saveMessage(tenantId, conv1.getId(), AiSenderType.CUSTOMER, "Hi! I need tables and chairs for a wedding for 100 people in Boston.");
        saveMessage(tenantId, conv1.getId(), AiSenderType.AI, "Congratulations! For 100 guests, I recommend 10 60-inch round tables and 100 chairs. What time should delivery be scheduled?");
        saveMessage(tenantId, conv1.getId(), AiSenderType.CUSTOMER, "Delivery around 2 PM would be perfect. We love round tables.");
        saveMessage(tenantId, conv1.getId(), AiSenderType.AI, "Great news! I have verified inventory availability and prepared an official Quote Draft for your 100-guest wedding in Boston. Our sales staff is reviewing your draft and will send the official finalized quote shortly!");

        // Demo Conv 2: Escalation demo
        AiSalesConversation conv2 = new AiSalesConversation();
        conv2.setTenantId(tenantId);
        conv2.setPublicId("conv_customproductdemo");
        conv2.setCustomerName("David Miller");
        conv2.setCustomerEmail("david.miller@techcorp.com");
        conv2.setChannel(AiSalesChannel.WEB_CHAT);
        conv2.setStatus(AiConversationStatus.ESCALATED);
        conv2.setDetectedIntent(AiIntent.HUMAN_ASSISTANCE);
        conv2.setStartedAt(LocalDateTime.now().minusHours(4));
        conv2.setLastMessageAt(LocalDateTime.now().minusHours(3));
        conv2 = conversationRepository.save(conv2);

        RentalInquiry inq2 = new RentalInquiry();
        inq2.setTenantId(tenantId);
        inq2.setConversationId(conv2.getId());
        inq2.setEventType("CORPORATE");
        inq2.setGuestCount(250);
        inq2.setDeliveryCity("Cambridge");
        inq2.setNotes("Custom branded stage staging and AV lighting package required.");
        inquiryRepository.save(inq2);

        saveMessage(tenantId, conv2.getId(), AiSenderType.CUSTOMER, "Do you have custom branded 40ft illuminated stages?");
        saveMessage(tenantId, conv2.getId(), AiSenderType.AI, "We have modular staging platforms, but custom branding and illuminated trusses require our senior production team. Would you like me to connect you with our events director?");
        saveMessage(tenantId, conv2.getId(), AiSenderType.CUSTOMER, "Yes please, connect me with someone.");
        saveMessage(tenantId, conv2.getId(), AiSenderType.AI, "I have notified our sales team! A representative has been assigned to this conversation and will take over shortly.");

        AiEscalation esc = new AiEscalation();
        esc.setTenantId(tenantId);
        esc.setConversationId(conv2.getId());
        esc.setReason(AiEscalationReason.CUSTOM_PRODUCT);
        esc.setPriority(AiEscalationPriority.HIGH);
        esc.setStatus(AiEscalationStatus.OPEN);
        esc.setSummary("Customer inquired about custom branded 40ft illuminated stage staging for 250 guests in Cambridge.");
        escalationRepository.save(esc);
    }

    private void saveMessage(String tenantId, UUID convId, AiSenderType sender, String content) {
        AiSalesMessage m = new AiSalesMessage();
        m.setTenantId(tenantId);
        m.setConversationId(convId);
        m.setSenderType(sender);
        m.setContent(content);
        messageRepository.save(m);
    }
}
