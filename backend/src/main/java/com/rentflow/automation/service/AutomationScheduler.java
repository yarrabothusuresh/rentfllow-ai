package com.rentflow.automation.service;

import com.rentflow.tenant.Tenant;
import com.rentflow.tenant.TenantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class AutomationScheduler {

    private static final Logger log = LoggerFactory.getLogger(AutomationScheduler.class);

    @Autowired
    private SignalDetectionService signalDetectionService;

    @Autowired
    private TenantRepository tenantRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        log.info("Running initial startup proactive business signal detection...");
        runPeriodicScan();
    }

    @Scheduled(fixedDelay = 300000) // Every 5 minutes
    public void runPeriodicScan() {
        Set<String> tenantIds = new HashSet<>();
        try {
            List<Tenant> tenants = tenantRepository.findAll();
            for (Tenant t : tenants) {
                if (t.getId() != null) {
                    tenantIds.add(t.getId().toString());
                }
            }
        } catch (Exception e) {
            log.warn("Failed to fetch tenants from TenantRepository: {}", e.getMessage());
        }

        // Add standard tenant defaults if empty
        if (tenantIds.isEmpty()) {
            tenantIds.add("tenant-1");
            tenantIds.add("default");
        } else {
            tenantIds.add("tenant-1");
        }

        for (String tenantId : tenantIds) {
            try {
                signalDetectionService.runDetection(tenantId);
            } catch (Exception e) {
                log.error("Scheduled signal scan failed for tenant {}: {}", tenantId, e.getMessage(), e);
            }
        }
    }
}
