package com.rentflow.integration.dto;

public class GeneratedApiKeyResponseDTO {
    private ExternalApiKeyDTO apiKey;
    private String rawApiKey; // Only returned once on generation

    public GeneratedApiKeyResponseDTO() {}

    public GeneratedApiKeyResponseDTO(ExternalApiKeyDTO apiKey, String rawApiKey) {
        this.apiKey = apiKey;
        this.rawApiKey = rawApiKey;
    }

    public ExternalApiKeyDTO getApiKey() { return apiKey; }
    public void setApiKey(ExternalApiKeyDTO apiKey) { this.apiKey = apiKey; }

    public String getRawApiKey() { return rawApiKey; }
    public void setRawApiKey(String rawApiKey) { this.rawApiKey = rawApiKey; }
}
