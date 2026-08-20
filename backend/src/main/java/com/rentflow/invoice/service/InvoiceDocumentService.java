package com.rentflow.invoice.service;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.UUID;

@Service
public class InvoiceDocumentService {

    /**
     * Placeholder method preparing future PDF document rendering.
     */
    public Map<String, Object> generateInvoicePdf(UUID invoiceId) {
        return Map.of(
            "invoiceId", invoiceId.toString(),
            "status", "PDF_GENERATION_NOT_IMPLEMENTED_DAY_12",
            "message", "PDF rendering infrastructure will be integrated in a future release."
        );
    }
}
