package com.rentflow.crm.dto;

import java.util.UUID;

public class CustomerMatchDTO {
    private UUID customerId;
    private String customerNumber;
    private String name;
    private String email;
    private String phone;
    private String companyName;
    private String matchType;

    public CustomerMatchDTO() {}

    public CustomerMatchDTO(UUID customerId, String customerNumber, String name, String email, String phone, String companyName, String matchType) {
        this.customerId = customerId;
        this.customerNumber = customerNumber;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.companyName = companyName;
        this.matchType = matchType;
    }

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public String getCustomerNumber() { return customerNumber; }
    public void setCustomerNumber(String customerNumber) { this.customerNumber = customerNumber; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getMatchType() { return matchType; }
    public void setMatchType(String matchType) { this.matchType = matchType; }
}
