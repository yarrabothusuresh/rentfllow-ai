package com.rentflow.idempotency;

import com.rentflow.crm.listener.CrmEventListener;
import com.rentflow.crm.model.Lead;
import com.rentflow.crm.repository.LeadRepository;
import com.rentflow.notification.event.CustomerRequestCreatedEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class LeadCreationIdempotencyTest {

    @Autowired
    private CrmEventListener crmEventListener;

    @Autowired
    private LeadRepository leadRepository;

    private String tenantId;

    @BeforeEach
    public void setUp() {
        tenantId = "tenant-crm-" + UUID.randomUUID();
    }

    @AfterEach
    public void tearDown() {
        if (tenantId != null) {
            leadRepository.deleteAll(leadRepository.findByTenantId(tenantId));
        }
    }

    @Test
    @DisplayName("43. CRM Event Replay: Delivering the same rental-request event 5 times creates exactly 1 lead")
    public void testEventReplay_CreatesSingleLead() {
        UUID rentalRequestId = UUID.randomUUID();
        CustomerRequestCreatedEvent event = new CustomerRequestCreatedEvent(
                tenantId,
                rentalRequestId,
                null,
                "Sarah Connor",
                "RENTAL_REQUEST",
                "Corporate Gala Inquiry",
                "Need 50 chairs and 10 tables"
        );

        for (int i = 0; i < 5; i++) {
            crmEventListener.onCustomerRequestCreated(event);
        }

        List<Lead> leads = leadRepository.findByTenantId(tenantId);
        assertEquals(1, leads.size(), "Duplicate events must not create duplicate CRM leads");
        assertEquals(rentalRequestId, leads.get(0).getRentalRequestId());
    }

    @Test
    @DisplayName("43b. Concurrent Event Delivery: Simultaneous delivery of same rental request event produces exactly 1 lead")
    public void testConcurrentEventDelivery_ProducesSingleLead() throws Exception {
        UUID rentalRequestId = UUID.randomUUID();
        int threads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threads);
        AtomicInteger completed = new AtomicInteger(0);

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    CustomerRequestCreatedEvent event = new CustomerRequestCreatedEvent(
                            tenantId,
                            rentalRequestId,
                            null,
                            "Sarah Connor",
                            "RENTAL_REQUEST",
                            "Concurrent Gala",
                            "Urgent inquiry"
                    );
                    crmEventListener.onCustomerRequestCreated(event);
                    completed.incrementAndGet();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        assertTrue(finishLatch.await(10, TimeUnit.SECONDS));
        executor.shutdown();

        assertEquals(threads, completed.get());
        List<Lead> leads = leadRepository.findByTenantId(tenantId);
        assertEquals(1, leads.size(), "Even under high concurrency, exactly 1 lead must be created");
    }
}
