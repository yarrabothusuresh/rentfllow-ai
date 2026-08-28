package com.rentflow.portal.ai;

import com.rentflow.ai.model.Product;
import com.rentflow.ai.repository.ProductRepository;
import com.rentflow.portal.dto.PublicProductDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DefaultProductRecommendationService implements ProductRecommendationService {

    private final ProductRepository productRepository;

    public DefaultProductRecommendationService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<PublicProductDTO> getRecommendations(String tenantId, List<UUID> selectedProductIds) {
        if (selectedProductIds == null || selectedProductIds.isEmpty()) {
            return productRepository.findByTenantId(tenantId).stream()
                    .limit(4)
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
        }

        // Rule-based placeholder: Recommend items in same tenant not already selected
        return productRepository.findByTenantId(tenantId).stream()
                .filter(p -> !selectedProductIds.contains(p.getId()))
                .limit(4)
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private PublicProductDTO mapToDTO(Product p) {
        PublicProductDTO dto = new PublicProductDTO();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setSku(p.getSku());
        dto.setDescription(p.getDescription());
        dto.setRentalPrice(p.getRentalPrice());
        dto.setImageUrl(p.getImageUrl());
        dto.setAvailable(p.getQuantityOwned() > 0);
        dto.setAvailableQuantity(p.getQuantityOwned() - p.getQuantityInMaintenance() - p.getQuantityDamaged() - p.getQuantityLost());
        return dto;
    }
}
