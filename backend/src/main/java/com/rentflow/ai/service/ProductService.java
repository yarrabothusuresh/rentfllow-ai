package com.rentflow.ai.service;

import com.rentflow.ai.dto.ProductDTO;
import com.rentflow.ai.model.Product;
import com.rentflow.ai.model.ProductStatus;
import com.rentflow.ai.model.ProductType;
import com.rentflow.ai.repository.ProductCategoryRepository;
import com.rentflow.ai.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, ProductCategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<ProductDTO> getProducts(String tenantId, String userRole) {
        return productRepository.findByTenantId(tenantId).stream()
                .map(p -> mapToDTO(p, userRole))
                .collect(Collectors.toList());
    }

    public Optional<ProductDTO> getProductById(String tenantId, UUID id, String userRole) {
        return productRepository.findByTenantIdAndId(tenantId, id)
                .map(p -> mapToDTO(p, userRole));
    }

    public List<ProductDTO> searchProducts(String tenantId, String query, String userRole) {
        return productRepository.searchProducts(tenantId, query).stream()
                .map(p -> mapToDTO(p, userRole))
                .collect(Collectors.toList());
    }

    public ProductDTO createProduct(String tenantId, ProductDTO dto, String userRole) {
        Product p = new Product();
        p.setTenantId(tenantId);
        p.setSku(dto.getSku());
        p.setName(dto.getName());
        p.setDescription(dto.getDescription());
        p.setCategoryId(dto.getCategoryId());
        p.setProductType(dto.getProductType() != null ? dto.getProductType() : ProductType.RENTAL_ITEM);
        p.setStatus(dto.getStatus() != null ? dto.getStatus() : ProductStatus.ACTIVE);
        p.setRentalPrice(dto.getRentalPrice());
        p.setReplacementCost(dto.getReplacementCost());
        p.setQuantityOwned(dto.getQuantityOwned());
        p.setQuantityInMaintenance(dto.getQuantityInMaintenance());
        p.setQuantityDamaged(dto.getQuantityDamaged());
        p.setQuantityLost(dto.getQuantityLost());
        p.setImageUrl(dto.getImageUrl());

        Product saved = productRepository.save(p);
        return mapToDTO(saved, userRole);
    }

    public Optional<ProductDTO> updateProduct(String tenantId, UUID id, ProductDTO dto, String userRole) {
        return productRepository.findByTenantIdAndId(tenantId, id)
                .map(existing -> {
                    if (dto.getSku() != null) existing.setSku(dto.getSku());
                    if (dto.getName() != null) existing.setName(dto.getName());
                    if (dto.getDescription() != null) existing.setDescription(dto.getDescription());
                    existing.setCategoryId(dto.getCategoryId());
                    if (dto.getProductType() != null) existing.setProductType(dto.getProductType());
                    if (dto.getStatus() != null) existing.setStatus(dto.getStatus());
                    if (dto.getRentalPrice() != null) existing.setRentalPrice(dto.getRentalPrice());
                    if (dto.getReplacementCost() != null) existing.setReplacementCost(dto.getReplacementCost());
                    existing.setImageUrl(dto.getImageUrl());

                    return mapToDTO(productRepository.save(existing), userRole);
                });
    }

    public Optional<ProductDTO> updateStatus(String tenantId, UUID id, ProductStatus status, String userRole) {
        return productRepository.findByTenantIdAndId(tenantId, id)
                .map(existing -> {
                    existing.setStatus(status);
                    return mapToDTO(productRepository.save(existing), userRole);
                });
    }

    public boolean deleteProduct(String tenantId, UUID id) {
        return productRepository.findByTenantIdAndId(tenantId, id)
                .map(p -> {
                    productRepository.delete(p);
                    return true;
                }).orElse(false);
    }

    public ProductDTO mapToDTO(Product p, String userRole) {
        ProductDTO dto = new ProductDTO();
        dto.setId(p.getId());
        dto.setTenantId(p.getTenantId());
        dto.setSku(p.getSku());
        dto.setName(p.getName());
        dto.setDescription(p.getDescription());
        dto.setCategoryId(p.getCategoryId());
        dto.setProductType(p.getProductType());
        dto.setStatus(p.getStatus());
        dto.setRentalPrice(p.getRentalPrice());

        // Field Protection: Hide replacementCost for CUSTOMER role
        if ("CUSTOMER".equalsIgnoreCase(userRole)) {
            dto.setReplacementCost(null);
        } else {
            dto.setReplacementCost(p.getReplacementCost());
        }

        dto.setQuantityOwned(p.getQuantityOwned());
        dto.setQuantityInMaintenance(p.getQuantityInMaintenance());
        dto.setQuantityDamaged(p.getQuantityDamaged());
        dto.setQuantityLost(p.getQuantityLost());
        dto.setImageUrl(p.getImageUrl());
        dto.setCreatedAt(p.getCreatedAt());
        dto.setUpdatedAt(p.getUpdatedAt());

        // Category Name
        if (p.getCategoryId() != null) {
            categoryRepository.findById(p.getCategoryId())
                    .ifPresent(cat -> dto.setCategoryName(cat.getName()));
        }

        // Base calculated availability without reservation context
        int baseAvailable = p.getQuantityOwned() - p.getQuantityInMaintenance() - p.getQuantityDamaged() - p.getQuantityLost();
        dto.setAvailableQuantity(Math.max(0, baseAvailable));

        // Health indicator
        if (p.getQuantityOwned() == 0) {
            dto.setHealth("CRITICAL");
        } else {
            double availPct = (double) baseAvailable / p.getQuantityOwned();
            if (availPct < 0.2) {
                dto.setHealth("CRITICAL");
            } else if (availPct < 0.5) {
                dto.setHealth("WARNING");
            } else {
                dto.setHealth("GOOD");
            }
        }

        return dto;
    }
}
