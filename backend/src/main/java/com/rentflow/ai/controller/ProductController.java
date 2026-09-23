package com.rentflow.ai.controller;

import com.rentflow.ai.dto.ProductDTO;
import com.rentflow.ai.model.ProductStatus;
import com.rentflow.ai.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.rentflow.common.pagination.PaginationUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final com.rentflow.security.CurrentUserService currentUserService;

    private static final Set<String> PRODUCT_SORT_FIELDS = Set.of(
            "id", "sku", "name", "productType", "status", "rentalPrice", "replacementCost",
            "quantityOwned", "createdAt", "updatedAt"
    );

    public ProductController(ProductService productService, com.rentflow.security.CurrentUserService currentUserService) {
        this.productService = productService;
        this.currentUserService = currentUserService;
    }

    private String resolveTenantId(String ignoredHeader) {
        return currentUserService.requireTenantId();
    }

    private String resolveRole(String ignoredHeader) {
        return currentUserService.requireRole();
    }

    @GetMapping
    public ResponseEntity<?> getProducts(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(name = "direction", defaultValue = "desc") String direction,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        String tenantId = resolveTenantId(tenantHeader);
        String role = resolveRole(roleHeader);
        Pageable pageable = PaginationUtil.createPageRequest(page, size, sortBy, direction, PRODUCT_SORT_FIELDS);
        return ResponseEntity.ok(productService.getProducts(tenantId, role, pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchProducts(
            @RequestParam("query") String query,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(name = "direction", defaultValue = "desc") String direction,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        if (query == null || query.isBlank()) {
            return ResponseEntity.ok(Page.empty());
        }
        String sanitizedQuery = query.trim();
        if (sanitizedQuery.length() > 200) {
            sanitizedQuery = sanitizedQuery.substring(0, 200);
        }

        String tenantId = resolveTenantId(tenantHeader);
        String role = resolveRole(roleHeader);
        Pageable pageable = PaginationUtil.createPageRequest(page, size, sortBy, direction, PRODUCT_SORT_FIELDS);
        return ResponseEntity.ok(productService.searchProducts(tenantId, sanitizedQuery, role, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(
            @PathVariable("id") UUID id,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        String tenantId = resolveTenantId(tenantHeader);
        String role = resolveRole(roleHeader);
        return productService.getProductById(tenantId, id, role)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    public ResponseEntity<?> createProduct(
            @Valid @RequestBody ProductDTO dto,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        String role = resolveRole(roleHeader);
        if ("CUSTOMER".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Customers cannot create products."));
        }

        String tenantId = resolveTenantId(tenantHeader);
        ProductDTO created = productService.createProduct(tenantId, dto, role);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(
            @PathVariable("id") UUID id,
            @Valid @RequestBody ProductDTO dto,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        String role = resolveRole(roleHeader);
        if ("CUSTOMER".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Customers cannot update products."));
        }

        String tenantId = resolveTenantId(tenantHeader);
        return productService.updateProduct(tenantId, id, dto, role)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable("id") UUID id,
            @RequestParam("status") ProductStatus status,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        String role = resolveRole(roleHeader);
        if ("CUSTOMER".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Customers cannot update product status."));
        }

        String tenantId = resolveTenantId(tenantHeader);
        return productService.updateStatus(tenantId, id, status, role)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(
            @PathVariable("id") UUID id,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        String role = resolveRole(roleHeader);
        if (!List.of("OWNER", "ADMIN").contains(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Only Owner or Admin can delete products."));
        }

        String tenantId = resolveTenantId(tenantHeader);
        boolean deleted = productService.deleteProduct(tenantId, id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
