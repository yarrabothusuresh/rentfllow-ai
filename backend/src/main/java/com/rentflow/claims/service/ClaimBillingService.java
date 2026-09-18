package com.rentflow.claims.service;

import com.rentflow.claims.model.DamageClaim;
import com.rentflow.claims.model.DamageClaimItem;
import com.rentflow.invoice.model.Invoice;
import com.rentflow.invoice.model.InvoiceItem;
import com.rentflow.invoice.model.InvoiceStatus;
import com.rentflow.invoice.repository.InvoiceItemRepository;
import com.rentflow.invoice.repository.InvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ClaimBillingService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;

    public ClaimBillingService(InvoiceRepository invoiceRepository,
                               InvoiceItemRepository invoiceItemRepository) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceItemRepository = invoiceItemRepository;
    }

    @Transactional
    public Optional<Invoice> createDraftChargeForClaim(DamageClaim claim, List<DamageClaimItem> items) {
        if (claim == null || claim.getApprovedTotalCost() == null || claim.getApprovedTotalCost().compareTo(BigDecimal.ZERO) <= 0) {
            return Optional.empty();
        }

        // Find existing booking invoice or create draft
        Optional<Invoice> existingInvoice = invoiceRepository.findByTenantIdAndBookingId(claim.getTenantId(), claim.getBookingId());
        Invoice invoice;
        if (existingInvoice.isPresent()) {
            invoice = existingInvoice.get();
        } else {
            invoice = new Invoice();
            invoice.setTenantId(claim.getTenantId());
            invoice.setBookingId(claim.getBookingId());
            invoice.setCustomerId(claim.getCustomerId());
            invoice.setInvoiceNumber("INV-CLM-" + claim.getClaimNumber().replace("CLM-", ""));
            invoice.setStatus(InvoiceStatus.DRAFT);
            invoice.setIssueDate(LocalDate.now());
            invoice.setDueDate(LocalDate.now().plusDays(14));
            invoice.setSubtotal(BigDecimal.ZERO);
            invoice.setTax(BigDecimal.ZERO);
            invoice.setTotalAmount(BigDecimal.ZERO);
            invoice.setAmountPaid(BigDecimal.ZERO);
            invoice.setBalanceDue(BigDecimal.ZERO);
            invoice = invoiceRepository.save(invoice);
        }

        for (DamageClaimItem item : items) {
            if (item.getApprovedCost() != null && item.getApprovedCost().compareTo(BigDecimal.ZERO) > 0) {
                InvoiceItem line = new InvoiceItem();
                line.setInvoiceId(invoice.getId());
                line.setProductId(item.getProductId());
                line.setDescription("Damage Claim " + claim.getClaimNumber() + " - " + item.getProductNameSnapshot() + " (" + item.getClaimType() + ")");
                line.setQuantity(item.getQuantity());
                line.setUnitPrice(item.getApprovedCost().setScale(2, RoundingMode.HALF_UP));
                line.setLineTotal(item.getApprovedCost().setScale(2, RoundingMode.HALF_UP));
                invoiceItemRepository.save(line);
            }
        }

        BigDecimal currentSub = invoice.getSubtotal() != null ? invoice.getSubtotal() : BigDecimal.ZERO;
        BigDecimal claimCost = claim.getApprovedTotalCost() != null ? claim.getApprovedTotalCost() : BigDecimal.ZERO;
        BigDecimal newSubtotal = currentSub.add(claimCost).setScale(2, RoundingMode.HALF_UP);
        invoice.setSubtotal(newSubtotal);

        BigDecimal currentTax = invoice.getTax() != null ? invoice.getTax() : BigDecimal.ZERO;
        BigDecimal newTotal = newSubtotal.add(currentTax).setScale(2, RoundingMode.HALF_UP);
        invoice.setTotalAmount(newTotal);

        BigDecimal currentPaid = invoice.getAmountPaid() != null ? invoice.getAmountPaid() : BigDecimal.ZERO;
        invoice.setBalanceDue(newTotal.subtract(currentPaid).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP));
        Invoice saved = invoiceRepository.save(invoice);

        return Optional.of(saved);
    }
}
