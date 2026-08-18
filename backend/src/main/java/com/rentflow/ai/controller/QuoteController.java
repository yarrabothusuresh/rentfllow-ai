package com.rentflow.ai.controller;

import com.rentflow.ai.dto.*;
import com.rentflow.ai.mock.DemoDataRepository;
import com.rentflow.ai.model.QuoteStatus;
import com.rentflow.ai.service.QuoteCalculationService;
import com.rentflow.ai.service.QuoteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/quotes")
@CrossOrigin(originPatterns = "*")
public class QuoteController {

    private final QuoteService quoteService;
    private final QuoteCalculationService calculationService;

    public QuoteController(QuoteService quoteService, QuoteCalculationService calculationService) {
        this.quoteService = quoteService;
        this.calculationService = calculationService;
    }

    private String resolveTenantId(String tenantHeader) {
        return (tenantHeader != null && !tenantHeader.isBlank())
                ? tenantHeader : DemoDataRepository.EVERGREEN_TENANT_ID;
    }

    private String resolveRole(String roleHeader) {
        return (roleHeader != null && !roleHeader.isBlank())
                ? roleHeader.toUpperCase() : "OWNER";
    }

    @PostMapping
    public ResponseEntity<?> createQuote(
            @RequestBody QuoteDTO dto,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        try {
            String role = resolveRole(roleHeader);
            if ("CUSTOMER".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Customers cannot create quotes directly."));
            }
            if (dto.getCustomerId() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Customer is required."));
            }
            if (dto.getEventId() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Event is required."));
            }

            String tenantId = resolveTenantId(tenantHeader);
            QuoteDTO created = quoteService.createQuote(tenantId, dto, role);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage() != null ? e.getMessage() : e.toString()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getQuotes(
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        try {
            String tenantId = resolveTenantId(tenantHeader);
            String role = resolveRole(roleHeader);
            return ResponseEntity.ok(quoteService.getQuotes(tenantId, role));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage() != null ? e.getMessage() : e.toString()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getQuoteById(
            @PathVariable("id") UUID id,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        try {
            String tenantId = resolveTenantId(tenantHeader);
            String role = resolveRole(roleHeader);
            return quoteService.getQuoteById(tenantId, id, role)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage() != null ? e.getMessage() : e.toString()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateQuote(
            @PathVariable("id") UUID id,
            @RequestBody QuoteDTO dto,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        try {
            String role = resolveRole(roleHeader);
            if ("CUSTOMER".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Customers cannot update quotes."));
            }

            String tenantId = resolveTenantId(tenantHeader);
            return quoteService.updateQuote(tenantId, id, dto, role)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage() != null ? e.getMessage() : e.toString()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteQuote(
            @PathVariable("id") UUID id,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        try {
            String role = resolveRole(roleHeader);
            if (!("OWNER".equals(role) || "ADMIN".equals(role))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Only Owner or Admin can delete quotes."));
            }

            String tenantId = resolveTenantId(tenantHeader);
            boolean deleted = quoteService.deleteQuote(tenantId, id);
            if (deleted) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage() != null ? e.getMessage() : e.toString()));
        }
    }

    @PostMapping("/{id}/calculate")
    public ResponseEntity<?> calculateQuote(
            @PathVariable("id") UUID id,
            @RequestBody(required = false) QuoteCalculationRequest req,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader) {

        try {
            if (req != null) {
                return ResponseEntity.ok(calculationService.calculate(req));
            }

            String tenantId = resolveTenantId(tenantHeader);
            QuoteDTO q = quoteService.getQuoteById(tenantId, id, "OWNER")
                    .orElseThrow(() -> new IllegalArgumentException("Quote not found"));

            QuoteCalculationRequest calcReq = new QuoteCalculationRequest();
            calcReq.setItems(q.getItems());
            calcReq.setDeliveryFee(q.getDeliveryFee());
            calcReq.setPickupFee(q.getPickupFee());
            calcReq.setSetupFee(q.getSetupFee());
            calcReq.setBreakdownFee(q.getBreakdownFee());
            calcReq.setServiceFee(q.getServiceFee());
            calcReq.setTaxRate(q.getTaxRate());
            calcReq.setDepositPercentage(q.getDepositPercentage());
            calcReq.setDiscountValue(q.getDiscountAmount());

            return ResponseEntity.ok(calculationService.calculate(calcReq));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage() != null ? e.getMessage() : e.toString()));
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable("id") UUID id,
            @RequestParam("status") QuoteStatus status,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        try {
            String tenantId = resolveTenantId(tenantHeader);
            String role = resolveRole(roleHeader);
            return quoteService.updateStatus(tenantId, id, status, role)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage() != null ? e.getMessage() : e.toString()));
        }
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<?> acceptQuote(
            @PathVariable("id") UUID id,
            @RequestBody(required = false) Map<String, Object> body,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        try {
            String tenantId = resolveTenantId(tenantHeader);
            String role = resolveRole(roleHeader);
            return quoteService.updateStatus(tenantId, id, QuoteStatus.ACCEPTED, role)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage() != null ? e.getMessage() : e.toString()));
        }
    }


    @PostMapping("/{id}/duplicate")
    public ResponseEntity<?> duplicateQuote(
            @PathVariable("id") UUID id,
            @RequestBody(required = false) QuoteDuplicateRequest dupReq,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        try {
            String tenantId = resolveTenantId(tenantHeader);
            String role = resolveRole(roleHeader);
            return quoteService.duplicateQuote(tenantId, id, dupReq, role)
                    .map(dto -> ResponseEntity.status(HttpStatus.CREATED).body(dto))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage() != null ? e.getMessage() : e.toString()));
        }
    }

    // --- QUOTE ITEM ENDPOINTS ---

    @PostMapping("/{quoteId}/items")
    public ResponseEntity<?> addQuoteItem(
            @PathVariable("quoteId") UUID quoteId,
            @RequestBody QuoteItemDTO itemDto,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        try {
            String tenantId = resolveTenantId(tenantHeader);
            String role = resolveRole(roleHeader);
            QuoteDTO updated = quoteService.addQuoteItem(tenantId, quoteId, itemDto, role);
            return ResponseEntity.status(HttpStatus.CREATED).body(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage() != null ? e.getMessage() : e.toString()));
        }
    }

    @PutMapping("/{quoteId}/items/{itemId}")
    public ResponseEntity<?> updateQuoteItem(
            @PathVariable("quoteId") UUID quoteId,
            @PathVariable("itemId") UUID itemId,
            @RequestBody QuoteItemDTO itemDto,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        try {
            String tenantId = resolveTenantId(tenantHeader);
            String role = resolveRole(roleHeader);
            QuoteDTO updated = quoteService.updateQuoteItem(tenantId, quoteId, itemId, itemDto, role);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage() != null ? e.getMessage() : e.toString()));
        }
    }

    @DeleteMapping("/{quoteId}/items/{itemId}")
    public ResponseEntity<?> deleteQuoteItem(
            @PathVariable("quoteId") UUID quoteId,
            @PathVariable("itemId") UUID itemId,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        try {
            String tenantId = resolveTenantId(tenantHeader);
            String role = resolveRole(roleHeader);
            QuoteDTO updated = quoteService.deleteQuoteItem(tenantId, quoteId, itemId, role);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage() != null ? e.getMessage() : e.toString()));
        }
    }
}
