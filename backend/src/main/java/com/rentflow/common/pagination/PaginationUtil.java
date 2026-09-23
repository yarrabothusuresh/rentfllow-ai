package com.rentflow.common.pagination;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

/**
 * Enterprise pagination and sorting utility for RentFlow AI.
 * Enforces bounded page sizes, allowlists for sort fields, and deterministic secondary sorting.
 */
public final class PaginationUtil {

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    private PaginationUtil() {
        // Utility class
    }

    /**
     * Creates and validates a Pageable instance.
     *
     * @param page              0-indexed page number (must be >= 0)
     * @param size              page size (must be > 0 and <= MAX_PAGE_SIZE)
     * @param sortBy            property to sort by (must be in allowedSortFields or null/blank)
     * @param direction         direction ('ASC' or 'DESC', default 'DESC')
     * @param allowedSortFields set of allowable entity properties for sorting
     * @param defaultSortField  fallback property to sort by if sortBy is empty
     * @return validated Pageable with deterministic secondary sort on 'id DESC'
     */
    public static Pageable createPageRequest(
            int page,
            int size,
            String sortBy,
            String direction,
            Set<String> allowedSortFields) {
        return createPageRequest(page, size, sortBy, direction, allowedSortFields, "createdAt");
    }

    public static Pageable createPageRequest(
            int page,
            int size,
            String sortBy,
            String direction,
            Set<String> allowedSortFields,
            String defaultSortField) {

        if (page < 0) {
            throw new IllegalArgumentException("Page index must not be negative: " + page);
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Page size must be greater than zero: " + size);
        }
        if (size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Page size cannot exceed maximum limit of " + MAX_PAGE_SIZE + ": " + size);
        }

        String effectiveSortBy = (sortBy != null && !sortBy.isBlank()) ? sortBy.trim() : defaultSortField;

        if (allowedSortFields != null && !allowedSortFields.isEmpty() && !allowedSortFields.contains(effectiveSortBy)) {
            throw new IllegalArgumentException("Invalid sort property: '" + effectiveSortBy + "'. Allowed sort fields are: " + allowedSortFields);
        }

        Sort.Direction sortDirection = Sort.Direction.DESC;
        if (direction != null && !direction.isBlank()) {
            try {
                sortDirection = Sort.Direction.fromString(direction.trim());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid sort direction: '" + direction + "'. Must be ASC or DESC.");
            }
        }

        Sort primarySort = Sort.by(sortDirection, effectiveSortBy);
        // Append secondary deterministic sort by id DESC unless primary sort is already id
        Sort finalSort = "id".equalsIgnoreCase(effectiveSortBy)
                ? primarySort
                : primarySort.and(Sort.by(Sort.Direction.DESC, "id"));

        return PageRequest.of(page, size, finalSort);
    }

    /**
     * Sanitizes user search input to prevent expensive pathological queries (preserving Day 33 max length rule).
     */
    public static String sanitizeSearchQuery(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }
        String trimmed = query.trim();
        return trimmed.length() > 200 ? trimmed.substring(0, 200) : trimmed;
    }
}
