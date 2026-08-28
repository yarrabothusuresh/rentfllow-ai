package com.rentflow.portal.dto;

import com.rentflow.ai.dto.BookingDTO;
import com.rentflow.ai.dto.CustomerDTO;
import com.rentflow.ai.dto.QuoteDTO;
import com.rentflow.claims.dto.DamageClaimDTO;
import com.rentflow.invoice.dto.InvoiceDTO;
import com.rentflow.payment.dto.PaymentDTO;
import com.rentflow.portal.model.CustomerActivity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Customer360DTO {

    private CustomerDTO customerInfo;
    private List<CustomerAddressDTO> addresses = new ArrayList<>();
    private List<BookingDTO> upcomingBookings = new ArrayList<>();
    private List<BookingDTO> activeRentals = new ArrayList<>();
    private List<BookingDTO> pastBookings = new ArrayList<>();
    private List<QuoteDTO> quotes = new ArrayList<>();
    private List<InvoiceDTO> invoices = new ArrayList<>();
    private List<PaymentDTO> payments = new ArrayList<>();
    private List<DamageClaimDTO> damageClaims = new ArrayList<>();
    private List<CustomerConversationDTO> messages = new ArrayList<>();
    private List<CustomerActivity> activityTimeline = new ArrayList<>();

    private BigDecimal totalLifetimeValue = BigDecimal.ZERO;
    private BigDecimal currentOutstandingBalance = BigDecimal.ZERO;

    public Customer360DTO() {}

    public CustomerDTO getCustomerInfo() { return customerInfo; }
    public void setCustomerInfo(CustomerDTO customerInfo) { this.customerInfo = customerInfo; }

    public List<CustomerAddressDTO> getAddresses() { return addresses; }
    public void setAddresses(List<CustomerAddressDTO> addresses) { this.addresses = addresses; }

    public List<BookingDTO> getUpcomingBookings() { return upcomingBookings; }
    public void setUpcomingBookings(List<BookingDTO> upcomingBookings) { this.upcomingBookings = upcomingBookings; }

    public List<BookingDTO> getActiveRentals() { return activeRentals; }
    public void setActiveRentals(List<BookingDTO> activeRentals) { this.activeRentals = activeRentals; }

    public List<BookingDTO> getPastBookings() { return pastBookings; }
    public void setPastBookings(List<BookingDTO> pastBookings) { this.pastBookings = pastBookings; }

    public List<QuoteDTO> getQuotes() { return quotes; }
    public void setQuotes(List<QuoteDTO> quotes) { this.quotes = quotes; }

    public List<InvoiceDTO> getInvoices() { return invoices; }
    public void setInvoices(List<InvoiceDTO> invoices) { this.invoices = invoices; }

    public List<PaymentDTO> getPayments() { return payments; }
    public void setPayments(List<PaymentDTO> payments) { this.payments = payments; }

    public List<DamageClaimDTO> getDamageClaims() { return damageClaims; }
    public void setDamageClaims(List<DamageClaimDTO> damageClaims) { this.damageClaims = damageClaims; }

    public List<CustomerConversationDTO> getMessages() { return messages; }
    public void setMessages(List<CustomerConversationDTO> messages) { this.messages = messages; }

    public List<CustomerActivity> getActivityTimeline() { return activityTimeline; }
    public void setActivityTimeline(List<CustomerActivity> activityTimeline) { this.activityTimeline = activityTimeline; }

    public BigDecimal getTotalLifetimeValue() { return totalLifetimeValue; }
    public void setTotalLifetimeValue(BigDecimal totalLifetimeValue) { this.totalLifetimeValue = totalLifetimeValue; }

    public BigDecimal getCurrentOutstandingBalance() { return currentOutstandingBalance; }
    public void setCurrentOutstandingBalance(BigDecimal currentOutstandingBalance) { this.currentOutstandingBalance = currentOutstandingBalance; }
}
