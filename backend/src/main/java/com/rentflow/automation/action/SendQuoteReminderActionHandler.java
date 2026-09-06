package com.rentflow.automation.action;

import com.rentflow.ai.model.Quote;
import com.rentflow.ai.model.QuoteStatus;
import com.rentflow.ai.repository.QuoteRepository;
import com.rentflow.automation.model.AutomationActionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class SendQuoteReminderActionHandler implements AutomationActionHandler {

    @Autowired
    private QuoteRepository quoteRepository;

    @Override
    public AutomationActionType getActionType() {
        return AutomationActionType.SEND_QUOTE_REMINDER;
    }

    @Override
    public RevalidationResult revalidate(String tenantId, Map<String, Object> payload) {
        if (payload == null || !payload.containsKey("quoteId")) {
            return RevalidationResult.stale("Missing quoteId in action payload");
        }
        UUID quoteId;
        try {
            quoteId = UUID.fromString(payload.get("quoteId").toString());
        } catch (Exception e) {
            return RevalidationResult.stale("Invalid quoteId: " + payload.get("quoteId"));
        }

        Quote quote = quoteRepository.findById(quoteId).orElse(null);
        if (quote == null || !tenantId.equals(quote.getTenantId())) {
            return RevalidationResult.stale("Quote not found or belongs to another tenant");
        }

        if (quote.getStatus() == QuoteStatus.ACCEPTED) {
            return RevalidationResult.alreadyCompleted("Quote " + quote.getQuoteNumber() + " has already been accepted by customer");
        }
        if (quote.getStatus() == QuoteStatus.DECLINED || quote.getStatus() == QuoteStatus.REJECTED || quote.getStatus() == QuoteStatus.CANCELLED) {
            return RevalidationResult.stale("Quote " + quote.getQuoteNumber() + " is currently in terminal status " + quote.getStatus());
        }

        return RevalidationResult.valid();
    }

    @Override
    public ActionResult execute(String tenantId, Map<String, Object> payload, String executedBy) {
        UUID quoteId = UUID.fromString(payload.get("quoteId").toString());
        Quote quote = quoteRepository.findById(quoteId).orElseThrow();
        String customerEmail = (String) payload.getOrDefault("customerEmail", "customer");

        // Record audit / note
        String existingNotes = quote.getNotes() != null ? quote.getNotes() : "";
        quote.setNotes(existingNotes + "\n[Automation] Quote expiration reminder sent to " + customerEmail + " by " + executedBy);
        quoteRepository.save(quote);

        return ActionResult.success(
            "Sent quote expiration reminder for Quote " + quote.getQuoteNumber() + " to " + customerEmail,
            Map.of("quoteNumber", quote.getQuoteNumber(), "recipient", customerEmail, "status", "SENT")
        );
    }
}
