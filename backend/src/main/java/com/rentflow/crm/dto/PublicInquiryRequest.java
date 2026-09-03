package com.rentflow.crm.dto;

import com.rentflow.ai.model.EventType;
import java.time.LocalDate;

public class PublicInquiryRequest {
    private String name;
    private String company;
    private String email;
    private String phone;
    private EventType eventType;
    private LocalDate eventDate;
    private String message;
    private String honeypot;

    public PublicInquiryRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public EventType getEventType() { return eventType; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }

    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getHoneypot() { return honeypot; }
    public void setHoneypot(String honeypot) { this.honeypot = honeypot; }
}
