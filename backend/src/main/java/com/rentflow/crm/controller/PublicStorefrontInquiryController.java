package com.rentflow.crm.controller;

import com.rentflow.crm.dto.LeadCreateRequest;
import com.rentflow.crm.dto.PublicInquiryRequest;
import com.rentflow.crm.model.LeadPriority;
import com.rentflow.crm.model.LeadSource;
import com.rentflow.crm.service.LeadService;
import com.rentflow.portal.model.TenantStorefrontConfig;
import com.rentflow.portal.repository.TenantStorefrontConfigRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/public/storefront")
public class PublicStorefrontInquiryController {

    private final LeadService leadService;
    private final TenantStorefrontConfigRepository storefrontConfigRepository;

    public PublicStorefrontInquiryController(LeadService leadService,
                                            TenantStorefrontConfigRepository storefrontConfigRepository) {
        this.leadService = leadService;
        this.storefrontConfigRepository = storefrontConfigRepository;
    }

    private String resolveTenantId(String tenantSlug) {
        if (tenantSlug == null || tenantSlug.isBlank()) {
            return "tenant-evergreen";
        }
        Optional<TenantStorefrontConfig> config = storefrontConfigRepository.findByTenantSlug(tenantSlug);
        if (config.isPresent()) {
            return config.get().getTenantId();
        }
        return "tenant-" + tenantSlug;
    }

    @PostMapping("/{tenantSlug}/inquiries")
    public ResponseEntity<Map<String, Object>> submitPublicInquiry(
            @PathVariable String tenantSlug,
            @RequestBody PublicInquiryRequest req) {

        // Honeypot spam check
        if (req.getHoneypot() != null && !req.getHoneypot().trim().isEmpty()) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Inquiry received."));
        }

        if (req.getEmail() == null || !req.getEmail().contains("@")) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Valid email address is required."));
        }

        String tenantId = resolveTenantId(tenantSlug);

        // Sanitize name
        String name = req.getName() != null ? req.getName().trim() : "Website Visitor";
        String firstName = name;
        String lastName = "";
        if (name.contains(" ")) {
            int idx = name.indexOf(" ");
            firstName = name.substring(0, idx);
            lastName = name.substring(idx + 1).trim();
        }

        LeadCreateRequest leadReq = new LeadCreateRequest();
        leadReq.setFirstName(firstName);
        leadReq.setLastName(lastName);
        leadReq.setCompanyName(req.getCompany());
        leadReq.setEmail(req.getEmail().trim());
        leadReq.setPhone(req.getPhone());
        leadReq.setSource(LeadSource.WEBSITE_INQUIRY);
        leadReq.setPriority(LeadPriority.NORMAL);
        leadReq.setEventType(req.getEventType());
        leadReq.setEventDate(req.getEventDate());
        leadReq.setEventName(req.getEventType() != null ? req.getEventType() + " Inquiry" : "Website Rental Inquiry");
        leadReq.setCustomerNotes(req.getMessage());

        leadService.createLead(tenantId, leadReq, "PUBLIC_INQUIRY");

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "We received your rental inquiry. Our team will review it and follow up promptly."
        ));
    }
}
