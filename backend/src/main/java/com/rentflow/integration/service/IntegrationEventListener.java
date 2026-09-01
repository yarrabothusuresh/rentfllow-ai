package com.rentflow.integration.service;

import com.rentflow.notification.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class IntegrationEventListener {

    private static final Logger log = LoggerFactory.getLogger(IntegrationEventListener.class);
    private final IntegrationEventService eventService;

    public IntegrationEventListener(IntegrationEventService eventService) {
        this.eventService = eventService;
    }

    @EventListener
    public void handleBookingConfirmed(BookingConfirmedEvent event) {
        log.info("[IntegrationEventListener] Received BookingConfirmedEvent: {}", event.getBookingNumber());
        Map<String, Object> data = new HashMap<>();
        data.put("bookingId", event.getBookingId());
        data.put("bookingNumber", event.getBookingNumber());
        data.put("customerId", event.getCustomerId());
        data.put("customerName", event.getCustomerName());
        data.put("eventName", event.getEventName());
        data.put("eventDate", event.getEventDate());

        eventService.publishEvent(
            event.getTenantId(),
            "booking.created",
            "BOOKING",
            event.getBookingId().toString(),
            data
        );
    }

    @EventListener
    public void handleBookingCancelled(BookingCancelledEvent event) {
        log.info("[IntegrationEventListener] Received BookingCancelledEvent: {}", event.getBookingNumber());
        Map<String, Object> data = new HashMap<>();
        data.put("bookingId", event.getBookingId());
        data.put("bookingNumber", event.getBookingNumber());
        data.put("reason", event.getReason());

        eventService.publishEvent(
            event.getTenantId(),
            "booking.cancelled",
            "BOOKING",
            event.getBookingId().toString(),
            data
        );
    }

    @EventListener
    public void handleInvoiceCreated(InvoiceCreatedEvent event) {
        log.info("[IntegrationEventListener] Received InvoiceCreatedEvent: {}", event.getInvoiceNumber());
        Map<String, Object> data = new HashMap<>();
        data.put("invoiceId", event.getInvoiceId());
        data.put("invoiceNumber", event.getInvoiceNumber());
        data.put("customerId", event.getCustomerId());
        data.put("totalAmount", event.getTotalAmount());
        data.put("dueDate", event.getDueDate());

        eventService.publishEvent(
            event.getTenantId(),
            "invoice.created",
            "INVOICE",
            event.getInvoiceId().toString(),
            data
        );
    }

    @EventListener
    public void handleInvoiceSent(InvoiceSentEvent event) {
        log.info("[IntegrationEventListener] Received InvoiceSentEvent: {}", event.getInvoiceNumber());
        Map<String, Object> data = new HashMap<>();
        data.put("invoiceId", event.getInvoiceId());
        data.put("invoiceNumber", event.getInvoiceNumber());
        data.put("customerId", event.getCustomerId());
        data.put("totalAmount", event.getTotalAmount());

        eventService.publishEvent(
            event.getTenantId(),
            "invoice.sent",
            "INVOICE",
            event.getInvoiceId().toString(),
            data
        );
    }

    @EventListener
    public void handlePaymentReceived(PaymentReceivedEvent event) {
        log.info("[IntegrationEventListener] Received PaymentReceivedEvent: paymentId={}, amount={}", event.getPaymentId(), event.getAmount());
        Map<String, Object> data = new HashMap<>();
        data.put("paymentId", event.getPaymentId());
        data.put("bookingNumber", event.getBookingNumber());
        data.put("customerId", event.getCustomerId());
        data.put("customerName", event.getCustomerName());
        data.put("amount", event.getAmount());
        data.put("balanceDue", event.getBalanceDue());

        eventService.publishEvent(
            event.getTenantId(),
            "payment.received",
            "PAYMENT",
            event.getPaymentId().toString(),
            data
        );
    }

    @EventListener
    public void handleQuoteAccepted(QuoteAcceptedEvent event) {
        log.info("[IntegrationEventListener] Received QuoteAcceptedEvent: {}", event.getQuoteNumber());
        Map<String, Object> data = new HashMap<>();
        data.put("quoteId", event.getQuoteId());
        data.put("quoteNumber", event.getQuoteNumber());
        data.put("customerName", event.getCustomerName());
        data.put("totalAmount", event.getTotalAmount());

        eventService.publishEvent(
            event.getTenantId(),
            "quote.approved",
            "QUOTE",
            event.getQuoteId().toString(),
            data
        );
    }

    @EventListener
    public void handleQuoteSent(QuoteSentEvent event) {
        log.info("[IntegrationEventListener] Received QuoteSentEvent: {}", event.getQuoteNumber());
        Map<String, Object> data = new HashMap<>();
        data.put("quoteId", event.getQuoteId());
        data.put("quoteNumber", event.getQuoteNumber());
        data.put("customerId", event.getCustomerId());
        data.put("customerName", event.getCustomerName());
        data.put("totalAmount", event.getTotalAmount());

        eventService.publishEvent(
            event.getTenantId(),
            "quote.sent",
            "QUOTE",
            event.getQuoteId().toString(),
            data
        );
    }
}
