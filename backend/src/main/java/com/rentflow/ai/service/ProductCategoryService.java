package com.rentflow.ai.service;

import com.rentflow.ai.dto.ProductCategoryDTO;
import com.rentflow.ai.model.ProductCategory;
import com.rentflow.ai.repository.ProductCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductCategoryService {

    private final ProductCategoryRepository categoryRepository;

    public ProductCategoryService(ProductCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<ProductCategoryDTO> getCategories(String tenantId) {
        return categoryRepository.findByTenantId(tenantId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public Optional<ProductCategoryDTO> getCategoryById(String tenantId, UUID id) {
        return categoryRepository.findByTenantIdAndId(tenantId, id)
                .map(this::mapToDTO);
    }

    public ProductCategoryDTO createCategory(String tenantId, ProductCategoryDTO dto) {
        ProductCategory entity = new ProductCategory();
        entity.setTenantId(tenantId);
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setParentCategoryId(dto.getParentCategoryId());
        entity.setActive(dto.isActive());

        ProductCategory saved = categoryRepository.save(entity);
        return mapToDTO(saved);
    }

    public Optional<ProductCategoryDTO> updateCategory(String tenantId, UUID id, ProductCategoryDTO dto) {
        return categoryRepository.findByTenantIdAndId(tenantId, id)
                .map(existing -> {
                    if (dto.getName() != null) existing.setName(dto.getName());
                    if (dto.getDescription() != null) existing.setDescription(dto.getDescription());
                    existing.setParentCategoryId(dto.getParentCategoryId());
                    existing.setActive(dto.isActive());
                    return mapToDTO(categoryRepository.save(existing));
                });
    }

    public boolean deleteCategory(String tenantId, UUID id) {
        return categoryRepository.findByTenantIdAndId(tenantId, id)
                .map(cat -> {
                    categoryRepository.delete(cat);
                    return true;
                }).orElse(false);
    }

    private ProductCategoryDTO mapToDTO(ProductCategory cat) {
        ProductCategoryDTO dto = new ProductCategoryDTO();
        dto.setId(cat.getId());
        dto.setTenantId(cat.getTenantId());
        dto.setName(cat.getName());
        dto.setDescription(cat.getDescription());
        dto.setParentCategoryId(cat.getParentCategoryId());
        dto.setActive(cat.isActive());
        dto.setCreatedAt(cat.getCreatedAt());
        dto.setUpdatedAt(cat.getUpdatedAt());

        if (cat.getParentCategoryId() != null) {
            categoryRepository.findById(cat.getParentCategoryId())
                    .ifPresent(parent -> dto.setParentCategoryName(parent.getName()));
        }

        return dto;
    }
}
