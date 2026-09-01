package com.rentflow.integration.dto;

import java.time.LocalDateTime;
import java.util.List;

public class CreateApiKeyDTO {
    private String name;
    private List<String> scopes;
    private Integer rateLimitPerMinute;
    private LocalDateTime expiresAt;

    public CreateApiKeyDTO() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<String> getScopes() { return scopes; }
    public void setScopes(List<String> scopes) { this.scopes = scopes; }

    public Integer getRateLimitPerMinute() { return rateLimitPerMinute; }
    public void setRateLimitPerMinute(Integer rateLimitPerMinute) { this.rateLimitPerMinute = rateLimitPerMinute; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}
