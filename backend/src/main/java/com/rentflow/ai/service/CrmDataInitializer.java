package com.rentflow.ai.service;

import com.rentflow.ai.dto.CustomerDTO;
import com.rentflow.ai.dto.EventDTO;
import com.rentflow.ai.dto.EventRequirementDTO;
import com.rentflow.ai.dto.LeadDTO;
import com.rentflow.ai.mock.DemoDataRepository;
import com.rentflow.ai.model.*;
import com.rentflow.ai.repository.CustomerRepository;
import com.rentflow.ai.repository.EventRepository;
import com.rentflow.ai.repository.EventRequirementRepository;
import com.rentflow.ai.repository.LeadRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Component
public class CrmDataInitializer implements CommandLineRunner {

    public static final UUID EMILY_CUSTOMER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    public static final UUID EMILY_EVENT_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");

    private final LeadRepository leadRepository;
    private final CustomerRepository customerRepository;
    private final EventRepository eventRepository;
    private final EventRequirementRepository requirementRepository;
    private final LeadService leadService;
    private final CustomerService customerService;
    private final EventService eventService;

    public CrmDataInitializer(LeadRepository leadRepository,
                               CustomerRepository customerRepository,
                               EventRepository eventRepository,
                               EventRequirementRepository requirementRepository,
                               LeadService leadService,
                               CustomerService customerService,
                               EventService eventService) {
        this.leadRepository = leadRepository;
        this.customerRepository = customerRepository;
        this.eventRepository = eventRepository;
        this.requirementRepository = requirementRepository;
        this.leadService = leadService;
        this.customerService = customerService;
        this.eventService = eventService;
    }

    @Override
    public void run(String... args) throws Exception {
        String tenantId = DemoDataRepository.EVERGREEN_TENANT_ID;

        if (customerRepository.count() > 0 || leadRepository.count() > 0) {
            return;
        }

        // Seed Customer 1: Emily Brown
        Customer emilyCust = new Customer();
        emilyCust.setId(EMILY_CUSTOMER_ID);
        emilyCust.setTenantId(tenantId);
        emilyCust.setCustomerNumber("CUS-000001");
        emilyCust.setFirstName("Emily");
        emilyCust.setLastName("Brown");
        emilyCust.setCompanyName("Brown Wedding");
        emilyCust.setEmail("emily.brown@example-demo.com");
        emilyCust.setPhone("+1 555-010-1001");
        emilyCust.setCustomerType(CustomerType.INDIVIDUAL);
        emilyCust.setStatus(CustomerStatus.ACTIVE);
        customerRepository.save(emilyCust);

        // Seed Event 1: Emily's Wedding
        Event emilyEvent = new Event();
        emilyEvent.setId(EMILY_EVENT_ID);
        emilyEvent.setTenantId(tenantId);
        emilyEvent.setCustomerId(EMILY_CUSTOMER_ID);
        emilyEvent.setEventName("Emily's Wedding");
        emilyEvent.setEventType(EventType.WEDDING);
        emilyEvent.setEventDate(LocalDate.of(2026, 9, 20));
        emilyEvent.setStartTime("08:00");
        emilyEvent.setEndTime("18:00");
        emilyEvent.setGuestCount(250);
        emilyEvent.setVenueName("Dallas Garden Hall");
        emilyEvent.setVenueAddress("200 Garden Way");
        emilyEvent.setCity("Dallas");
        emilyEvent.setState("TX");
        emilyEvent.setZipCode("75201");
        emilyEvent.setStatus(EventStatus.PLANNING);
        eventRepository.save(emilyEvent);

        // Seed Requirements for Emily's Wedding
        EventRequirement req1 = new EventRequirement();
        req1.setTenantId(tenantId);
        req1.setEventId(EMILY_EVENT_ID);
        req1.setDescription("Chiavari Chairs");
        req1.setQuantity(250);
        req1.setNotes("Gold chiavari chairs with ivory vinyl cushion");
        requirementRepository.save(req1);

        EventRequirement req2 = new EventRequirement();
        req2.setTenantId(tenantId);
        req2.setEventId(EMILY_EVENT_ID);
        req2.setDescription("Round Banquet Tables 60\"");
        req2.setQuantity(25);
        req2.setNotes("Seats 8-10 guests");
        requirementRepository.save(req2);

        EventRequirement req3 = new EventRequirement();
        req3.setTenantId(tenantId);
        req3.setEventId(EMILY_EVENT_ID);
        req3.setDescription("White Table Linens");
        req3.setQuantity(25);
        req3.setNotes("120 inch round white linens");
        requirementRepository.save(req3);

        EventRequirement req4 = new EventRequirement();
        req4.setTenantId(tenantId);
        req4.setEventId(EMILY_EVENT_ID);
        req4.setDescription("Wireless LED Uplights");
        req4.setQuantity(10);
        req4.setNotes("Battery powered uplighting");
        requirementRepository.save(req4);

        // Seed Demo Lead 1: Emily Brown
        LeadDTO emilyLead = new LeadDTO();
        emilyLead.setFirstName("Emily");
        emilyLead.setLastName("Brown");
        emilyLead.setCompanyName("Brown Wedding");
        emilyLead.setEmail("emily.brown@example-demo.com");
        emilyLead.setPhone("+1 555-010-1001");
        emilyLead.setSource(LeadSource.WEBSITE);
        emilyLead.setEventType(EventType.WEDDING);
        emilyLead.setEventDate(LocalDate.of(2026, 9, 20));
        emilyLead.setGuestCount(250);
        emilyLead.setVenueName("Dallas Garden Hall");
        emilyLead.setNotes("Wants Chiavari chairs, round tables, white linens, bistro string lights, and dance floor for 250 guests.");
        emilyLead.setStatus(LeadStatus.QUALIFIED);
        emilyLead.setAssignedTo("Sarah Miller");
        leadService.createLead(tenantId, emilyLead);

        // Seed Demo Lead 2: TechCorp Annual Gala
        LeadDTO techLead = new LeadDTO();
        techLead.setFirstName("Alex");
        techLead.setLastName("Morgan");
        techLead.setCompanyName("TechCorp Inc");
        techLead.setEmail("events@techcorp-demo.com");
        techLead.setPhone("+1 555-019-2044");
        techLead.setSource(LeadSource.PARTNER);
        techLead.setEventType(EventType.CORPORATE);
        techLead.setEventDate(LocalDate.of(2026, 9, 22));
        techLead.setGuestCount(500);
        techLead.setVenueName("Austin Convention Center");
        techLead.setNotes("Annual employee gala requiring stage platform, audio system, 50 cocktail tables.");
        techLead.setStatus(LeadStatus.NEW);
        techLead.setAssignedTo("James Taylor");
        leadService.createLead(tenantId, techLead);
    }
}
