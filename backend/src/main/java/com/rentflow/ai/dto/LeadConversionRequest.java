package com.rentflow.ai.dto;

import java.util.UUID;

public class LeadConversionRequest {
    private UUID useExistingCustomerId;
    private boolean forceNewCustomer;

    public LeadConversionRequest() {}

    public UUID getUseExistingCustomerId() {
        return useExistingCustomerId;
    }

    public void setUseExistingCustomerId(UUID useExistingCustomerId) {
        this.useExistingCustomerId = useExistingCustomerId;
    }

    public boolean isForceNewCustomer() {
        return forceNewCustomer;
    }

    public void setForceNewCustomer(boolean forceNewCustomer) {
        this.forceNewCustomer = forceNewCustomer;
    }
}
