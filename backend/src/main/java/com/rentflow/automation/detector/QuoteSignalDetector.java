package com.rentflow.automation.detector;

import com.rentflow.ai.model.Customer;
import com.rentflow.ai.model.Quote;
import com.rentflow.ai.model.QuoteStatus;
import com.rentflow.ai.repository.CustomerRepository;
import com.rentflow.ai.repository.QuoteRepository;
import com.rentflow.automation.model.AutomationActionType;
import com.rentflow.automation.model.BusinessSignalCategory;
import com.rentflow.automation.model.BusinessSignalSeverity;
import com.rentflow.automation.model.BusinessSignalType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Component
public class QuoteSignalDetector implements BusinessSignalDetector {

    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Override
    public List<BusinessSignalType> getSupportedTypes() {
        return List.of(
            BusinessSignalType.QUOTE_EXPIRING_SOON,
            BusinessSignalType.QUOTE_EXPIRED_PENDING_ACTION,
            BusinessSignalType.QUOTE_MARGIN_LOW,
            BusinessSignalType.QUOTE_AWAITING_CUSTOMER_RESPONSE
        );
    }

    @Override
    public List<DetectedSignal> detect(String tenantId) {
        List<DetectedSignal> signals = new ArrayList<>();
        List<Quote> quotes = quoteRepository.findByTenantId(tenantId);
        LocalDate today = LocalDate.now();

        for (Quote q : quotes) {
            if (q.getStatus() == QuoteStatus.CANCELLED || q.getStatus() == QuoteStatus.ACCEPTED || q.getStatus() == QuoteStatus.REJECTED || q.getStatus() == QuoteStatus.DECLINED) {
                continue;
            }

            Customer customer = q.getCustomerId() != null ? customerRepository.findById(q.getCustomerId()).orElse(null) : null;
            String custName = customer != null ? (customer.getFirstName() + " " + customer.getLastName()).trim() : "Unknown Customer";
            String custEmail = customer != null ? customer.getEmail() : "";

            // 1. QUOTE_EXPIRING_SOON
            if (q.getValidUntil() != null) {
                long daysUntilExpiry = ChronoUnit.DAYS.between(today, q.getValidUntil());
                if (daysUntilExpiry >= 0 && daysUntilExpiry <= 2 && (q.getStatus() == QuoteStatus.SENT || q.getStatus() == QuoteStatus.VIEWED || q.getStatus() == QuoteStatus.DRAFT)) {
                    Map<String, Object> evidence = new LinkedHashMap<>();
                    evidence.put("quoteNumber", q.getQuoteNumber());
                    evidence.put("customerName", custName);
                    evidence.put("customerEmail", custEmail);
                    evidence.put("validUntil", q.getValidUntil().toString());
                    evidence.put("daysUntilExpiry", daysUntilExpiry);
                    evidence.put("totalAmount", q.getTotalAmount() != null ? q.getTotalAmount().toString() : "0.00");
                    evidence.put("status", q.getStatus().name());

                    Map<String, Object> payload = new LinkedHashMap<>();
                    payload.put("quoteId", q.getId().toString());
                    payload.put("quoteNumber", q.getQuoteNumber());
                    payload.put("customerName", custName);
                    payload.put("customerEmail", custEmail);
                    payload.put("totalAmount", q.getTotalAmount() != null ? q.getTotalAmount().toString() : "0.00");
                    payload.put("validUntil", q.getValidUntil().toString());

                    signals.add(new DetectedSignal(
                        BusinessSignalType.QUOTE_EXPIRING_SOON,
                        BusinessSignalCategory.SALES,
                        "QUOTE",
                        q.getId().toString(),
                        q.getQuoteNumber(),
                        daysUntilExpiry == 0 ? BusinessSignalSeverity.HIGH : BusinessSignalSeverity.MEDIUM,
                        tenantId + ":QUOTE_EXPIRING_SOON:" + q.getId(),
                        evidence,
                        AutomationActionType.SEND_QUOTE_REMINDER,
                        payload,
                        "Quote " + q.getQuoteNumber() + " is expiring in " + daysUntilExpiry + " day(s)",
                        "Quote " + q.getQuoteNumber() + " for " + custName + " worth $" + q.getTotalAmount() + " expires on " + q.getValidUntil() + ".",
                        "Quotes expiring without follow-up represent lost sales conversion opportunities."
                    ));
                }

                // 2. QUOTE_EXPIRED_PENDING_ACTION
                if (daysUntilExpiry < 0 && (q.getStatus() == QuoteStatus.SENT || q.getStatus() == QuoteStatus.VIEWED)) {
                    Map<String, Object> evidence = new LinkedHashMap<>();
                    evidence.put("quoteNumber", q.getQuoteNumber());
                    evidence.put("customerName", custName);
                    evidence.put("validUntil", q.getValidUntil().toString());
                    evidence.put("expiredDaysAgo", Math.abs(daysUntilExpiry));
                    evidence.put("totalAmount", q.getTotalAmount() != null ? q.getTotalAmount().toString() : "0.00");

                    Map<String, Object> payload = new LinkedHashMap<>();
                    payload.put("taskTitle", "Review expired quote " + q.getQuoteNumber());
                    payload.put("entityType", "QUOTE");
                    payload.put("entityId", q.getId().toString());
                    payload.put("priority", "HIGH");
                    payload.put("assignedRole", "ROLE_SALES");

                    signals.add(new DetectedSignal(
                        BusinessSignalType.QUOTE_EXPIRED_PENDING_ACTION,
                        BusinessSignalCategory.SALES,
                        "QUOTE",
                        q.getId().toString(),
                        q.getQuoteNumber(),
                        BusinessSignalSeverity.HIGH,
                        tenantId + ":QUOTE_EXPIRED_PENDING_ACTION:" + q.getId(),
                        evidence,
                        AutomationActionType.CREATE_INTERNAL_TASK,
                        payload,
                        "Quote " + q.getQuoteNumber() + " has expired with no resolution",
                        "Quote " + q.getQuoteNumber() + " ($" + q.getTotalAmount() + ") expired " + Math.abs(daysUntilExpiry) + " days ago. Status is still " + q.getStatus() + ".",
                        "Stale expired quotes lock potential inventory commitments and require manual sales follow-up or closure."
                    ));
                }
            }

            // 3. QUOTE_MARGIN_LOW
            if (q.getSubtotal() != null && q.getSubtotal().compareTo(BigDecimal.ZERO) > 0 && q.getDiscountAmount() != null) {
                BigDecimal discountPct = q.getDiscountAmount().multiply(new BigDecimal(100)).divide(q.getSubtotal(), 2, RoundingMode.HALF_UP);
                if (discountPct.compareTo(new BigDecimal("20.00")) >= 0) {
                    Map<String, Object> evidence = new LinkedHashMap<>();
                    evidence.put("quoteNumber", q.getQuoteNumber());
                    evidence.put("customerName", custName);
                    evidence.put("subtotal", q.getSubtotal().toString());
                    evidence.put("discountAmount", q.getDiscountAmount().toString());
                    evidence.put("discountPercentage", discountPct.toString());
                    evidence.put("thresholdPercentage", "20.00");

                    Map<String, Object> payload = new LinkedHashMap<>();
                    payload.put("taskTitle", "Margin Review Required: " + q.getQuoteNumber() + " has " + discountPct + "% discount");
                    payload.put("entityType", "QUOTE");
                    payload.put("entityId", q.getId().toString());
                    payload.put("priority", "HIGH");
                    payload.put("assignedRole", "ROLE_SALES");

                    signals.add(new DetectedSignal(
                        BusinessSignalType.QUOTE_MARGIN_LOW,
                        BusinessSignalCategory.PROFITABILITY,
                        "QUOTE",
                        q.getId().toString(),
                        q.getQuoteNumber(),
                        BusinessSignalSeverity.HIGH,
                        tenantId + ":QUOTE_MARGIN_LOW:" + q.getId(),
                        evidence,
                        AutomationActionType.CREATE_INTERNAL_TASK,
                        payload,
                        "Low margin alert on Quote " + q.getQuoteNumber(),
                        "Quote " + q.getQuoteNumber() + " includes a heavy discount of " + discountPct + "% ($" + q.getDiscountAmount() + ") on subtotal $" + q.getSubtotal() + ".",
                        "Deep discounts erode profitability below target business thresholds and require management sign-off."
                    ));
                }
            }
        }
        return signals;
    }
}
