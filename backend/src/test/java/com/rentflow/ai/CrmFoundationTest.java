package com.rentflow.ai;

import com.rentflow.ai.controller.CustomerController;
import com.rentflow.ai.controller.EventController;
import com.rentflow.ai.controller.LeadController;
import com.rentflow.ai.dto.*;
import com.rentflow.ai.model.*;
import com.rentflow.ai.repository.CustomerRepository;
import com.rentflow.ai.repository.EventRepository;
import com.rentflow.ai.repository.EventRequirementRepository;
import com.rentflow.ai.repository.LeadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CrmFoundationTest {

    @Autowired
    private LeadController leadController;

    @Autowired
    private CustomerController customerController;

    @Autowired
    private EventController eventController;

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventRequirementRepository requirementRepository;

    private final String TENANT_A = "tenant-aaa-111";
    private final String TENANT_B = "tenant-bbb-222";

    @BeforeEach
    void setUp() {
        requirementRepository.deleteAll();
        eventRepository.deleteAll();
        customerRepository.deleteAll();
        leadRepository.deleteAll();
    }

    @Test
    void test1_CreateLead() {
        LeadDTO dto = new LeadDTO();
        dto.setFirstName("Emily");
        dto.setLastName("Brown");
        dto.setEmail("emily.brown@example-demo.com");
        dto.setPhone("+1 555-010-1001");
        dto.setSource(LeadSource.WEBSITE);
        dto.setEventType(EventType.WEDDING);
        dto.setEventDate(LocalDate.of(2026, 9, 20));
        dto.setGuestCount(250);
        dto.setVenueName("Dallas Garden Hall");

        ResponseEntity<?> response = leadController.createLead(dto, TENANT_A, "SALES");

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody() instanceof LeadDTO);
        LeadDTO created = (LeadDTO) response.getBody();
        assertNotNull(created.getId());
        assertEquals("Emily", created.getFirstName());
        assertEquals(LeadStatus.NEW, created.getStatus());
    }

    @Test
    void test2_SearchLead() {
        LeadDTO dto = new LeadDTO();
        dto.setFirstName("Emily");
        dto.setLastName("Brown");
        dto.setEmail("emily.brown@example-demo.com");
        dto.setSource(LeadSource.WEBSITE);
        leadController.createLead(dto, TENANT_A, "SALES");

        ResponseEntity<?> response = leadController.getLeads(TENANT_A, "SALES", null, "Emily");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<LeadDTO> leads = (List<LeadDTO>) response.getBody();
        assertEquals(1, leads.size());
        assertEquals("Emily", leads.get(0).getFirstName());
    }

    @Test
    void test3_CreateCustomer() {
        CustomerDTO dto = new CustomerDTO();
        dto.setFirstName("Emily");
        dto.setLastName("Brown");
        dto.setEmail("emily.brown@example-demo.com");
        dto.setPhone("+1 555-010-1001");
        dto.setCustomerType(CustomerType.INDIVIDUAL);

        ResponseEntity<?> response = customerController.createCustomer(dto, TENANT_A, "SALES");

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        CustomerDTO created = (CustomerDTO) response.getBody();
        assertNotNull(created.getId());
        assertTrue(created.getCustomerNumber().startsWith("CUS-"));
    }

    @Test
    void test4_CreateEventForCustomer() {
        CustomerDTO cust = new CustomerDTO();
        cust.setFirstName("Emily");
        cust.setEmail("emily@example.com");
        CustomerDTO createdCust = (CustomerDTO) customerController.createCustomer(cust, TENANT_A, "SALES").getBody();

        EventDTO eventReq = new EventDTO();
        eventReq.setCustomerId(createdCust.getId());
        eventReq.setEventName("Emily's Wedding");
        eventReq.setEventType(EventType.WEDDING);
        eventReq.setEventDate(LocalDate.of(2026, 9, 20));
        eventReq.setGuestCount(250);
        eventReq.setVenueName("Dallas Garden Hall");

        ResponseEntity<?> response = eventController.createEvent(eventReq, TENANT_A, "SALES");

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        EventDTO createdEvent = (EventDTO) response.getBody();
        assertEquals(createdCust.getId(), createdEvent.getCustomerId());
        assertEquals("Emily's Wedding", createdEvent.getEventName());
    }

    @Test
    void test5_CreateEventRequirement() {
        CustomerDTO cust = new CustomerDTO();
        cust.setFirstName("Emily");
        cust.setEmail("emily@example.com");
        CustomerDTO createdCust = (CustomerDTO) customerController.createCustomer(cust, TENANT_A, "SALES").getBody();

        EventDTO eventReq = new EventDTO();
        eventReq.setCustomerId(createdCust.getId());
        eventReq.setEventName("Emily's Wedding");
        eventReq.setEventDate(LocalDate.of(2026, 9, 20));
        EventDTO createdEvent = (EventDTO) eventController.createEvent(eventReq, TENANT_A, "SALES").getBody();

        EventRequirementDTO req = new EventRequirementDTO();
        req.setDescription("Chiavari Chairs");
        req.setQuantity(250);
        req.setNotes("Gold chairs with cushions");

        ResponseEntity<?> response = eventController.addRequirement(createdEvent.getId(), req, TENANT_A, "SALES");

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        EventRequirementDTO createdReq = (EventRequirementDTO) response.getBody();
        assertEquals("Chiavari Chairs", createdReq.getDescription());
        assertEquals(250, createdReq.getQuantity());
    }

    @Test
    void test6_ConvertLead() {
        LeadDTO dto = new LeadDTO();
        dto.setFirstName("Emily");
        dto.setLastName("Brown");
        dto.setEmail("emily.brown@example-demo.com");
        dto.setPhone("+1 555-010-1001");
        dto.setSource(LeadSource.WEBSITE);
        dto.setEventType(EventType.WEDDING);
        dto.setEventDate(LocalDate.of(2026, 9, 20));
        dto.setGuestCount(250);
        dto.setVenueName("Dallas Garden Hall");
        LeadDTO createdLead = (LeadDTO) leadController.createLead(dto, TENANT_A, "SALES").getBody();

        ResponseEntity<?> convertResp = leadController.convertLead(createdLead.getId(), null, TENANT_A, "SALES");

        assertEquals(HttpStatus.OK, convertResp.getStatusCode());
        LeadConversionResult result = (LeadConversionResult) convertResp.getBody();
        assertNotNull(result.getCustomerId());
        assertNotNull(result.getEventId());
        assertEquals("SUCCESS", result.getStatus());

        // Check lead status updated to CONVERTED
        LeadDTO updatedLead = (LeadDTO) leadController.getLeadById(createdLead.getId(), TENANT_A, "SALES").getBody();
        assertEquals(LeadStatus.CONVERTED, updatedLead.getStatus());
    }

    @Test
    void test7_DuplicateEmail() {
        // Pre-create customer with email
        CustomerDTO cust = new CustomerDTO();
        cust.setFirstName("Emily");
        cust.setLastName("Brown");
        cust.setEmail("emily.brown@example-demo.com");
        customerController.createCustomer(cust, TENANT_A, "SALES");

        // Create lead with same email
        LeadDTO dto = new LeadDTO();
        dto.setFirstName("Emily");
        dto.setLastName("Brown");
        dto.setEmail("emily.brown@example-demo.com");
        LeadDTO createdLead = (LeadDTO) leadController.createLead(dto, TENANT_A, "SALES").getBody();

        // Attempt convert without force
        ResponseEntity<?> response = leadController.convertLead(createdLead.getId(), null, TENANT_A, "SALES");

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        LeadConversionResult result = (LeadConversionResult) response.getBody();
        assertTrue(result.isPossibleDuplicateFound());
        assertEquals("DUPLICATE_FOUND", result.getStatus());
    }

    @Test
    void test8_TenantIsolation() {
        CustomerDTO cust = new CustomerDTO();
        cust.setFirstName("TenantA Customer");
        cust.setEmail("custA@example.com");
        CustomerDTO createdCust = (CustomerDTO) customerController.createCustomer(cust, TENANT_A, "SALES").getBody();

        // Tenant B attempts to fetch Tenant A customer
        ResponseEntity<?> getResp = customerController.getCustomerById(createdCust.getId(), TENANT_B, "SALES");
        assertEquals(HttpStatus.NOT_FOUND, getResp.getStatusCode());
    }

    @Test
    void test9_SalesUserCanCreateLead() {
        LeadDTO dto = new LeadDTO();
        dto.setFirstName("Sales");
        dto.setEmail("saleslead@example.com");

        ResponseEntity<?> response = leadController.createLead(dto, TENANT_A, "SALES");
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void test10_WarehouseUserCannotEditCustomer() {
        CustomerDTO cust = new CustomerDTO();
        cust.setFirstName("Warehouse Test");
        cust.setEmail("wh@example.com");
        CustomerDTO createdCust = (CustomerDTO) customerController.createCustomer(cust, TENANT_A, "SALES").getBody();

        CustomerDTO updateReq = new CustomerDTO();
        updateReq.setFirstName("Hacked Name");

        ResponseEntity<?> response = customerController.updateCustomer(createdCust.getId(), updateReq, TENANT_A, "WAREHOUSE");
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void test11_CustomerCanOnlyAccessEvents() {
        // Verification of Customer Role endpoint access
        ResponseEntity<?> eventsResp = eventController.getEvents(TENANT_A, "CUSTOMER");
        assertEquals(HttpStatus.OK, eventsResp.getStatusCode());
    }
}
