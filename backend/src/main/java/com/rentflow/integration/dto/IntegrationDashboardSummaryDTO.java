package com.rentflow.integration.dto;

public class IntegrationDashboardSummaryDTO {
    private long connectedIntegrations;
    private long webhookEndpoints;
    private long eventsToday;
    private long successfulDeliveriesToday;
    private long failedDeliveriesToday;
    private long pendingRetries;
    private long deadLetterCount;
    private double successRate;

    public IntegrationDashboardSummaryDTO() {}

    public long getConnectedIntegrations() { return connectedIntegrations; }
    public void setConnectedIntegrations(long connectedIntegrations) { this.connectedIntegrations = connectedIntegrations; }

    public long getWebhookEndpoints() { return webhookEndpoints; }
    public void setWebhookEndpoints(long webhookEndpoints) { this.webhookEndpoints = webhookEndpoints; }

    public long getEventsToday() { return eventsToday; }
    public void setEventsToday(long eventsToday) { this.eventsToday = eventsToday; }

    public long getSuccessfulDeliveriesToday() { return successfulDeliveriesToday; }
    public void setSuccessfulDeliveriesToday(long successfulDeliveriesToday) { this.successfulDeliveriesToday = successfulDeliveriesToday; }

    public long getFailedDeliveriesToday() { return failedDeliveriesToday; }
    public void setFailedDeliveriesToday(long failedDeliveriesToday) { this.failedDeliveriesToday = failedDeliveriesToday; }

    public long getPendingRetries() { return pendingRetries; }
    public void setPendingRetries(long pendingRetries) { this.pendingRetries = pendingRetries; }

    public long getDeadLetterCount() { return deadLetterCount; }
    public void setDeadLetterCount(long deadLetterCount) { this.deadLetterCount = deadLetterCount; }

    public double getSuccessRate() { return successRate; }
    public void setSuccessRate(double successRate) { this.successRate = successRate; }

    public long getActiveConnectionsCount() { return connectedIntegrations; }
    public long getTotalOutboxEvents() { return eventsToday; }
    public long getPendingOutboxEvents() { return pendingRetries; }
    public long getFailedOutboxEvents() { return deadLetterCount; }
    public java.util.List<?> getRecentSyncJobs() { return java.util.Collections.emptyList(); }
}
