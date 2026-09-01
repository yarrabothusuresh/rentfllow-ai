package com.rentflow.integration.dto;

import com.rentflow.integration.model.IntegrationProvider;

public class ConnectIntegrationDTO {
    private IntegrationProvider provider;
    private String name;
    private String configuration;
    private String apiKeyOrSecret;

    public ConnectIntegrationDTO() {}

    public IntegrationProvider getProvider() { return provider; }
    public void setProvider(IntegrationProvider provider) { this.provider = provider; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getConfiguration() { return configuration; }
    public void setConfiguration(String configuration) { this.configuration = configuration; }

    public String getApiKeyOrSecret() { return apiKeyOrSecret; }
    public void setApiKeyOrSecret(String apiKeyOrSecret) { this.apiKeyOrSecret = apiKeyOrSecret; }
}
