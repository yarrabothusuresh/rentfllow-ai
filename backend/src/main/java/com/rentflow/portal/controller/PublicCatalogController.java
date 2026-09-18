package com.rentflow.portal.controller;

import com.rentflow.portal.dto.PublicAvailabilityResponseDTO;
import com.rentflow.portal.dto.PublicProductDTO;
import com.rentflow.portal.model.TenantStorefrontConfig;
import com.rentflow.portal.service.PublicCatalogService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/public")
public class PublicCatalogController {

    private final PublicCatalogService catalogService;

    public PublicCatalogController(PublicCatalogService catalogService) {
        this.catalogService = catalogService;
    }

    private String resolveTenantId(String tenantHeader) {
        return (tenantHeader != null && !tenantHeader.isBlank()) ? tenantHeader : "99999999-9999-9999-9999-999999999999";
    }

    @GetMapping("/storefront/{tenantSlug}")
    public ResponseEntity<TenantStorefrontConfig> getStorefrontConfig(@PathVariable String tenantSlug) {
        return ResponseEntity.ok(catalogService.getStorefrontConfig(tenantSlug));
    }

    @GetMapping("/catalog")
    public ResponseEntity<List<PublicProductDTO>> getCatalog(
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sortBy) {
        String tenantId = resolveTenantId(tenantHeader);
        return ResponseEntity.ok(catalogService.getPublicCatalog(tenantId, category, minPrice, maxPrice, search, sortBy));
    }

    @GetMapping("/catalog/{id}")
    public ResponseEntity<PublicProductDTO> getProductDetail(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader) {
        String tenantId = resolveTenantId(tenantHeader);
        return ResponseEntity.ok(catalogService.getPublicProductDetail(tenantId, id));
    }

    @GetMapping("/catalog/{id}/availability")
    public ResponseEntity<PublicAvailabilityResponseDTO> checkAvailability(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "1") int quantity) {
        String tenantId = resolveTenantId(tenantHeader);
        return ResponseEntity.ok(catalogService.checkAvailability(tenantId, id, startDate, endDate, quantity));
    }
}
