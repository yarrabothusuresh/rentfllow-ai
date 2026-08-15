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

@Component
public class CrmDataInitializer implements CommandLineRunner {

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
            // Seed Demo Lead 1: Emily Brown (Primary Demo Lead for Day 7)
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

            // Seed Demo Lead 3: Sarah Jenkins
            LeadDTO sarahLead = new LeadDTO();
            sarahLead.setFirstName("Sarah");
            sarahLead.setLastName("Jenkins");
            sarahLead.setEmail("sjenkins@example-demo.com");
            sarahLead.setPhone("+1 555-083-3912");
            sarahLead.setSource(LeadSource.REFERRAL);
            sarahLead.setEventType(EventType.PRIVATE_PARTY);
            sarahLead.setEventDate(LocalDate.of(2026, 9, 19));
            sarahLead.setGuestCount(150);
            sarahLead.setVenueName("Fort Worth Botanic Garden");
            sarahLead.setNotes("Outdoor anniversary reception.");
            sarahLead.setStatus(LeadStatus.QUOTE_REQUESTED);
            sarahLead.setAssignedTo("Sarah Miller");
            leadService.createLead(tenantId, sarahLead);

            // Seed Demo Lead 4: Festival Committee
            LeadDTO festLead = new LeadDTO();
            festLead.setFirstName("Marcus");
            festLead.setLastName("Vance");
            festLead.setCompanyName("Austin Festival Committee");
            festLead.setEmail("info@austinfestival-demo.org");
            festLead.setPhone("+1 555-099-1122");
            festLead.setSource(LeadSource.WALK_IN);
            festLead.setEventType(EventType.FESTIVAL);
            festLead.setEventDate(LocalDate.of(2026, 9, 25));
            festLead.setGuestCount(1000);
            festLead.setVenueName("Zilker Park, Austin TX");
            festLead.setNotes("40x60 Frame tent and outdoor staging.");
            festLead.setStatus(LeadStatus.NEGOTIATION);
            festLead.setAssignedTo("James Taylor");
            leadService.createLead(tenantId, festLead);

            // Seed existing Customer + Event for demonstration of pre-converted customer
            CustomerDTO existingCust = new CustomerDTO();
            existingCust.setFirstName("Fairview");
            existingCust.setLastName("Hall Manager");
            existingCust.setCompanyName("Fairview Event Hall");
            existingCust.setEmail("contact@fairviewhall-demo.com");
            existingCust.setPhone("+1 555-042-2811");
            existingCust.setCustomerType(CustomerType.VENUE);
            existingCust.setBillingAddress("100 Fairview Blvd");
            existingCust.setCity("Arlington");
            existingCust.setState("TX");
            existingCust.setZipCode("76010");
            existingCust.setCountry("USA");
            existingCust.setStatus(CustomerStatus.ACTIVE);
            CustomerDTO createdCust = customerService.createCustomer(tenantId, existingCust);

            // Seed Event for Fairview Hall
            EventDTO event1 = new EventDTO();
            event1.setCustomerId(createdCust.getId());
            event1.setEventName("Fairview Autumn Gala");
            event1.setEventType(EventType.CORPORATE);
            event1.setEventDate(LocalDate.of(2026, 9, 21));
            event1.setStartTime("18:00");
            event1.setEndTime("23:00");
            event1.setGuestCount(120);
            event1.setVenueName("Fairview Event Hall");
            event1.setVenueAddress("100 Fairview Blvd, Arlington TX");
            event1.setCity("Arlington");
            event1.setState("TX");
            event1.setStatus(EventStatus.BOOKED);
            EventDTO createdEvent = eventService.createEvent(tenantId, event1);

            // Seed Event Requirements
            EventRequirementDTO req1 = new EventRequirementDTO();
            req1.setDescription("Chiavari Chairs");
            req1.setQuantity(120);
            req1.setNotes("Black chiavari chairs");
            eventService.addRequirement(tenantId, createdEvent.getId(), req1);

            EventRequirementDTO req2 = new EventRequirementDTO();
            req2.setDescription("Round Tables");
            req2.setQuantity(12);
            req2.setNotes("60-inch round tables");
            eventService.addRequirement(tenantId, createdEvent.getId(), req2);
    }
}
