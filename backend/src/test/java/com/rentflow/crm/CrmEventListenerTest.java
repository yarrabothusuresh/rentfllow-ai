package com.rentflow.crm;

import com.rentflow.crm.listener.CrmEventListener;
import com.rentflow.crm.model.Lead;
import com.rentflow.crm.model.LeadSource;
import com.rentflow.crm.model.LeadStage;
import com.rentflow.crm.repository.LeadRepository;
import com.rentflow.notification.event.CustomerRequestCreatedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class CrmEventListenerTest {

    @Autowired
    private CrmEventListener eventListener;

    @Autowired
    private LeadRepository leadRepository;

    private final String tenantId = "tenant-crm-events";

    @Test
    public void testStorefrontRequestCreatesLeadIdempotently() {
        UUID reqId = UUID.randomUUID();
        CustomerRequestCreatedEvent event = new CustomerRequestCreatedEvent(
                tenantId, reqId, UUID.randomUUID(), "George Miller", "EVENT_INQUIRY", "Summer Festival Gala", "Need 50 tables"
        );

        // First event submission
        eventListener.onCustomerRequestCreated(event);

        Optional<Lead> leadOpt = leadRepository.findByTenantIdAndRentalRequestId(tenantId, reqId);
        assertTrue(leadOpt.isPresent());
        Lead lead = leadOpt.get();
        assertEquals(LeadSource.STOREFRONT_REQUEST, lead.getSource());
        assertEquals(LeadStage.NEW, lead.getStage());
        assertEquals("Summer Festival Gala", lead.getEventName());

        // Duplicate event submission must NOT create duplicate lead
        eventListener.onCustomerRequestCreated(event);
        long count = leadRepository.findByTenantId(tenantId).stream()
                .filter(l -> reqId.equals(l.getRentalRequestId()))
                .count();
        assertEquals(1, count);
    }
}
