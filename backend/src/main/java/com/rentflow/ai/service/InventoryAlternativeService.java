package com.rentflow.ai.service;

import com.rentflow.ai.dto.InventoryConflictDTO.AlternativeProductDTO;
import com.rentflow.ai.model.Product;
import com.rentflow.ai.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class InventoryAlternativeService {

    private final ProductRepository productRepository;
    private final AvailabilityService availabilityService;

    public InventoryAlternativeService(ProductRepository productRepository,
                                       AvailabilityService availabilityService) {
        this.productRepository = productRepository;
        this.availabilityService = availabilityService;
    }

    /**
     * Find alternative products for a given product ID during a specific date range.
     * Alternatives are other products in the same category or with similar SKU prefixes.
     */
    public List<AlternativeProductDTO> findAlternatives(String tenantId, UUID productId, LocalDateTime start, LocalDateTime end, int requiredQuantity) {
        Product currentProduct = productRepository.findByTenantIdAndId(tenantId, productId).orElse(null);
        if (currentProduct == null) {
            return List.of();
        }

        List<Product> categoryProducts = productRepository.findByTenantId(tenantId).stream()
                .filter(p -> !p.getId().equals(productId))
                .filter(p -> (currentProduct.getCategoryId() != null && currentProduct.getCategoryId().equals(p.getCategoryId())) ||
                             (currentProduct.getSku() != null && p.getSku() != null &&
                              currentProduct.getSku().substring(0, Math.min(3, currentProduct.getSku().length()))
                                      .equalsIgnoreCase(p.getSku().substring(0, Math.min(3, p.getSku().length())))))
                .collect(Collectors.toList());

        List<AlternativeProductDTO> alternatives = new ArrayList<>();
        for (Product alt : categoryProducts) {
            var availResult = availabilityService.checkAvailability(tenantId, alt.getId(), 1, start, end);
            if (availResult.getAvailableQuantity() > 0) {
                alternatives.add(new AlternativeProductDTO(
                        alt.getId(), alt.getName(), alt.getSku(), availResult.getAvailableQuantity()
                ));
            }
        }
        return alternatives;
    }
}
