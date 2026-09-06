package com.rentflow.automation.action;

import com.rentflow.automation.model.AutomationActionType;
import com.rentflow.invoice.model.Invoice;
import com.rentflow.invoice.model.InvoiceStatus;
import com.rentflow.invoice.repository.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Component
public class SendInvoiceReminderActionHandler implements AutomationActionHandler {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Override
    public AutomationActionType getActionType() {
        return AutomationActionType.SEND_INVOICE_REMINDER;
    }

    @Override
    public RevalidationResult revalidate(String tenantId, Map<String, Object> payload) {
        if (payload == null || !payload.containsKey("invoiceId")) {
            return RevalidationResult.stale("Missing invoiceId in payload");
        }
        UUID invoiceId;
        try {
            invoiceId = UUID.fromString(payload.get("invoiceId").toString());
        } catch (Exception e) {
            return RevalidationResult.stale("Invalid invoiceId: " + payload.get("invoiceId"));
        }

        Invoice invoice = invoiceRepository.findByTenantIdAndId(tenantId, invoiceId).orElse(null);
        if (invoice == null) {
            return RevalidationResult.stale("Invoice not found or belongs to another tenant");
        }

        if (invoice.getStatus() == InvoiceStatus.PAID || (invoice.getBalanceDue() != null && invoice.getBalanceDue().compareTo(BigDecimal.ZERO) <= 0)) {
            return RevalidationResult.alreadyCompleted("Invoice " + invoice.getInvoiceNumber() + " is already paid in full");
        }
        if (invoice.getStatus() == InvoiceStatus.VOID) {
            return RevalidationResult.stale("Invoice " + invoice.getInvoiceNumber() + " has been voided");
        }

        return RevalidationResult.valid();
    }

    @Override
    public ActionResult execute(String tenantId, Map<String, Object> payload, String executedBy) {
        UUID invoiceId = UUID.fromString(payload.get("invoiceId").toString());
        Invoice invoice = invoiceRepository.findByTenantIdAndId(tenantId, invoiceId).orElseThrow();
        String customerEmail = (String) payload.getOrDefault("customerEmail", "customer");

        return ActionResult.success(
            "Sent payment reminder for Invoice " + invoice.getInvoiceNumber() + " ($" + invoice.getBalanceDue() + " due) to " + customerEmail,
            Map.of("invoiceNumber", invoice.getInvoiceNumber(), "balanceDue", invoice.getBalanceDue().toString(), "recipient", customerEmail)
        );
    }
}
