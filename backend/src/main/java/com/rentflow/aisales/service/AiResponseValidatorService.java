package com.rentflow.aisales.service;

import com.rentflow.aisales.dto.AiSalesChatResponseDTO;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class AiResponseValidatorService {

    private static final Pattern INTERNAL_COST_PATTERN = Pattern.compile(
        "(?i)\\b(our cost is|internal cost|purchase cost|supplier price|wholesale price|gross margin|profit margin is|operating cost)\\b"
    );

    private static final Pattern SECRET_PATTERN = Pattern.compile(
        "\\b(whsec_[a-zA-Z0-9]{16,}|rf_live_[a-zA-Z0-9]{16,}|sk-[a-zA-Z0-9]{20,})\\b"
    );

    public void validateCustomerResponse(AiSalesChatResponseDTO response) {
        if (response.getReplyText() == null) return;

        // 1. Check for internal cost leak
        if (INTERNAL_COST_PATTERN.matcher(response.getReplyText()).find()) {
            response.setReplyText("I have prepared your rental options and pricing estimate! A sales specialist is reviewing logistics and will follow up shortly.");
        }

        // 2. Check for secret leak
        if (SECRET_PATTERN.matcher(response.getReplyText()).find()) {
            response.setReplyText("I'd be glad to help you finalize this rental inquiry. Please let me know if you have any questions!");
        }

        // 3. Ensure suggested replies exist
        if (response.getSuggestedReplies() == null || response.getSuggestedReplies().isEmpty()) {
            response.setSuggestedReplies(java.util.List.of("Check availability", "Show chair options", "Talk to a person"));
        }
    }
}
