package com.rentflow.portal.service;

import com.rentflow.ai.dto.AvailabilityResultDTO;
import com.rentflow.ai.model.Product;
import com.rentflow.ai.model.ProductCategory;
import com.rentflow.ai.repository.ProductCategoryRepository;
import com.rentflow.ai.repository.ProductRepository;
import com.rentflow.ai.service.AvailabilityService;
import com.rentflow.portal.dto.PublicAvailabilityResponseDTO;
import com.rentflow.portal.dto.PublicProductDTO;
import com.rentflow.portal.model.TenantStorefrontConfig;
import com.rentflow.portal.repository.TenantStorefrontConfigRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PublicCatalogService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final AvailabilityService availabilityService;
    private final TenantStorefrontConfigRepository storefrontRepository;

    public PublicCatalogService(ProductRepository productRepository,
                                ProductCategoryRepository categoryRepository,
                                AvailabilityService availabilityService,
                                TenantStorefrontConfigRepository storefrontRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.availabilityService = availabilityService;
        this.storefrontRepository = storefrontRepository;
    }

    public TenantStorefrontConfig getStorefrontConfig(String tenantSlug) {
        return storefrontRepository.findByTenantSlug(tenantSlug)
                .orElseGet(() -> {
                    TenantStorefrontConfig defaultConfig = new TenantStorefrontConfig();
                    defaultConfig.setTenantId("99999999-9999-9999-9999-999999999999");
                    defaultConfig.setTenantSlug(tenantSlug);
                    defaultConfig.setCompanyName("RentFlow Event Rentals");
                    defaultConfig.setPrimaryEmail("info@rentflow.ai");
                    defaultConfig.setPrimaryPhone("(555) 019-2831");
                    defaultConfig.setTermsAndConditions("Standard event equipment rental agreement.");
                    defaultConfig.setCurrency("USD");
                    return defaultConfig;
                });
    }

    public List<PublicProductDTO> getPublicCatalog(String tenantId, String category, BigDecimal minPrice, BigDecimal maxPrice, String search, String sortBy) {
        List<Product> products = productRepository.findByTenantId(tenantId);

        if (category != null && !category.isBlank()) {
            products = products.stream()
                    .filter(p -> p.getCategoryId() != null && categoryRepository.findById(p.getCategoryId())
                            .map(c -> category.equalsIgnoreCase(c.getName()))
                            .orElse(false))
                    .collect(Collectors.toList());
        }

        if (minPrice != null) {
            products = products.stream()
                    .filter(p -> p.getRentalPrice() != null && p.getRentalPrice().compareTo(minPrice) >= 0)
                    .collect(Collectors.toList());
        }

        if (maxPrice != null) {
            products = products.stream()
                    .filter(p -> p.getRentalPrice() != null && p.getRentalPrice().compareTo(maxPrice) <= 0)
                    .collect(Collectors.toList());
        }

        if (search != null && !search.isBlank()) {
            String term = search.toLowerCase();
            products = products.stream()
                    .filter(p -> (p.getName() != null && p.getName().toLowerCase().contains(term)) ||
                                 (p.getDescription() != null && p.getDescription().toLowerCase().contains(term)) ||
                                 (p.getSku() != null && p.getSku().toLowerCase().contains(term)))
                    .collect(Collectors.toList());
        }

        if ("PRICE_ASC".equalsIgnoreCase(sortBy)) {
            products.sort(Comparator.comparing(p -> p.getRentalPrice() != null ? p.getRentalPrice() : BigDecimal.ZERO));
        } else if ("PRICE_DESC".equalsIgnoreCase(sortBy)) {
            products.sort((p1, p2) -> (p2.getRentalPrice() != null ? p2.getRentalPrice() : BigDecimal.ZERO)
                    .compareTo(p1.getRentalPrice() != null ? p1.getRentalPrice() : BigDecimal.ZERO));
        } else if ("NAME".equalsIgnoreCase(sortBy)) {
            products.sort(Comparator.comparing(p -> p.getName() != null ? p.getName() : ""));
        }

        return products.stream().map(this::mapToPublicDTO).collect(Collectors.toList());
    }

    public PublicProductDTO getPublicProductDetail(String tenantId, UUID productId) {
        Product product = productRepository.findByTenantIdAndId(tenantId, productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + productId));
        return mapToPublicDTO(product);
    }

    public PublicAvailabilityResponseDTO checkAvailability(String tenantId, UUID productId, LocalDate startDate, LocalDate endDate, int quantity) {
        LocalDateTime start = (startDate != null ? startDate : LocalDate.now()).atTime(8, 0);
        LocalDateTime end = (endDate != null ? endDate : (startDate != null ? startDate.plusDays(2) : LocalDate.now().plusDays(2))).atTime(20, 0);

        AvailabilityResultDTO result = availabilityService.checkAvailability(tenantId, productId, Math.max(1, quantity), start, end);

        return new PublicAvailabilityResponseDTO(
                result.getProductId(),
                result.getRequestedQuantity(),
                result.getAvailableQuantity(),
                result.isAvailable()
        );
    }

    private PublicProductDTO mapToPublicDTO(Product p) {
        PublicProductDTO dto = new PublicProductDTO();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setSku(p.getSku());
        if (p.getCategoryId() != null) {
            categoryRepository.findById(p.getCategoryId()).ifPresent(c -> dto.setCategoryName(c.getName()));
        }
        dto.setDescription(p.getDescription());
        dto.setRentalPrice(p.getRentalPrice() != null ? p.getRentalPrice() : BigDecimal.ZERO);
        dto.setImageUrl(p.getImageUrl());
        int netAvail = p.getQuantityOwned() - p.getQuantityInMaintenance() - p.getQuantityDamaged() - p.getQuantityLost();
        dto.setAvailableQuantity(Math.max(0, netAvail));
        dto.setAvailable(dto.getAvailableQuantity() > 0);
        return dto;
    }
}
