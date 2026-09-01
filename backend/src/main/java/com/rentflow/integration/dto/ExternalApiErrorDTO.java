package com.rentflow.integration.dto;

import java.time.LocalDateTime;

public class ExternalApiErrorDTO {
    private String code;
    private String message;
    private String requestId;
    private LocalDateTime timestamp;

    public ExternalApiErrorDTO() {}

    public ExternalApiErrorDTO(String code, String message, String requestId) {
        this.code = code;
        this.message = message;
        this.requestId = requestId;
        this.timestamp = LocalDateTime.now();
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
