package com.rentflow.crm;

import com.rentflow.ai.model.EventType;
import com.rentflow.crm.dto.LeadCreateRequest;
import com.rentflow.crm.dto.LeadDetailDTO;
import com.rentflow.crm.dto.LeadFollowUpDTO;
import com.rentflow.crm.dto.LeadLostRequest;
import com.rentflow.crm.model.FollowUpType;
import com.rentflow.crm.model.LeadLostReason;
import com.rentflow.crm.model.LeadPriority;
import com.rentflow.crm.model.LeadSource;
import com.rentflow.crm.service.LeadActivityService;
import com.rentflow.crm.service.LeadFollowUpService;
import com.rentflow.crm.service.LeadService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@Order(30)
public class DataInitializerDay26Crm implements CommandLineRunner {

    private final LeadService leadService;
    private final LeadFollowUpService followUpService;
    private final LeadActivityService activityService;

    public DataInitializerDay26Crm(LeadService leadService,
                                  LeadFollowUpService followUpService,
                                  LeadActivityService activityService) {
        this.leadService = leadService;
        this.followUpService = followUpService;
        this.activityService = activityService;
    }

    @Override
    public void run(String... args) {
        String tenantId = "tenant-evergreen";

        // Seed 1: Storefront Rental Request Lead (Corporate Gala)
        LeadCreateRequest l1 = new LeadCreateRequest();
        l1.setFirstName("Jane");
        l1.setLastName("Smith");
        l1.setCompanyName("ABC Events LLC");
        l1.setEmail("jane@abcevents.com");
        l1.setPhone("617-555-0199");
        l1.setSource(LeadSource.STOREFRONT_REQUEST);
        l1.setPriority(LeadPriority.HIGH);
        l1.setEventName("Annual Corporate Gala 2026");
        l1.setEventType(EventType.CORPORATE);
        l1.setEventDate(LocalDate.now().plusDays(8));
        l1.setRentalStartDate(LocalDate.now().plusDays(7));
        l1.setRentalEndDate(LocalDate.now().plusDays(9));
        l1.setVenueName("Boston Harbor Convention Center");
        l1.setGuestCount(200);
        l1.setEstimatedBudget(new BigDecimal("5000.00"));
        l1.setEstimatedValue(new BigDecimal("4850.00"));
        l1.setCustomerNotes("Requires 20 round banquet tables, 200 Chiavari chairs, and full setup assistance by 2 PM.");
        LeadDetailDTO lead1 = leadService.createLead(tenantId, l1, "STOREFRONT_SYSTEM");

        // Seed 2: Phone Inquiry Lead (Emily's Wedding) - Qualified & Assigned
        LeadCreateRequest l2 = new LeadCreateRequest();
        l2.setFirstName("Emily");
        l2.setLastName("Johnson");
        l2.setEmail("emily.j@weddingmail.com");
        l2.setPhone("617-555-0245");
        l2.setSource(LeadSource.PHONE);
        l2.setPriority(LeadPriority.NORMAL);
        l2.setEventName("Johnson-Miller Wedding Reception");
        l2.setEventType(EventType.WEDDING);
        l2.setEventDate(LocalDate.now().plusMonths(2));
        l2.setRentalStartDate(LocalDate.now().plusMonths(2));
        l2.setRentalEndDate(LocalDate.now().plusMonths(2).plusDays(1));
        l2.setVenueName("Willow Creek Estate");
        l2.setGuestCount(150);
        l2.setEstimatedBudget(new BigDecimal("3500.00"));
        l2.setEstimatedValue(new BigDecimal("3000.00"));
        l2.setCustomerNotes("Inquired by phone regarding farm tables, matching cross-back chairs, and outdoor string lighting.");
        l2.setAssignedSalesUserId("sales-sarah");
        l2.setAssignedSalesUserName("Sarah Jenkins");
        LeadDetailDTO lead2 = leadService.createLead(tenantId, l2, "sales-sarah");
        leadService.qualifyLead(tenantId, lead2.getId(), "sales-sarah");

        // Schedule follow-ups for Lead 2
        followUpService.scheduleFollowUp(tenantId, lead2.getId(), FollowUpType.CALL,
                "Call Emily regarding final table layout & draft proposal",
                "Review seating arrangement options and verify delivery access window.",
                LocalDateTime.now().plusDays(1).withHour(10).withMinute(0),
                "sales-sarah", "Sarah Jenkins", "sales-sarah");

        // Seed 3: Website Inquiry Lead (Michael Davis)
        LeadCreateRequest l3 = new LeadCreateRequest();
        l3.setFirstName("Michael");
        l3.setLastName("Davis");
        l3.setCompanyName("Apex Solutions");
        l3.setEmail("michael.davis@apexsolutions.io");
        l3.setPhone("617-555-0811");
        l3.setSource(LeadSource.WEBSITE_INQUIRY);
        l3.setPriority(LeadPriority.NORMAL);
        l3.setEventName("Tech Innovation Summit");
        l3.setEventType(EventType.CONFERENCE);
        l3.setEventDate(LocalDate.now().plusWeeks(3));
        l3.setGuestCount(100);
        l3.setEstimatedValue(new BigDecimal("2500.00"));
        l3.setCustomerNotes("Submitted via website contact form: Inquiring about stage setup, podium, and lounge furniture.");
        l3.setAssignedSalesUserId("sales-sarah");
        l3.setAssignedSalesUserName("Sarah Jenkins");
        leadService.createLead(tenantId, l3, "WEBSITE");

        // Seed 4: Closed Lost Lead (Competitor)
        LeadCreateRequest l4 = new LeadCreateRequest();
        l4.setFirstName("David");
        l4.setLastName("Miller");
        l4.setEmail("david.m@bostonparty.org");
        l4.setPhone("617-555-0992");
        l4.setSource(LeadSource.EMAIL);
        l4.setPriority(LeadPriority.LOW);
        l4.setEventName("Community Milestone Celebration");
        l4.setEventType(EventType.BIRTHDAY);
        l4.setEstimatedValue(new BigDecimal("1200.00"));
        l4.setCustomerNotes("Looking for tent canopy and cocktail tables.");
        LeadDetailDTO lead4 = leadService.createLead(tenantId, l4, "sales-sarah");
        leadService.markLost(tenantId, lead4.getId(),
                new LeadLostRequest(LeadLostReason.COMPETITOR, "Client selected local competitor offering package discount."),
                "sales-sarah");

        // Schedule an overdue follow-up on Lead 1 to demonstrate overdue alerts
        followUpService.scheduleFollowUp(tenantId, lead1.getId(), FollowUpType.EMAIL,
                "Initial follow-up email on Corporate Gala request",
                "Acknowledge storefront request and verify venue loading dock instructions.",
                LocalDateTime.now().minusDays(1).withHour(11).withMinute(0),
                "sales-sarah", "Sarah Jenkins", "SYSTEM");
    }
}
