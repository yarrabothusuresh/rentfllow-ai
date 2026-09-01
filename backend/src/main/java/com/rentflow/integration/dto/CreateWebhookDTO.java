package com.rentflow.integration.dto;

import java.util.List;

public class CreateWebhookDTO {
    private String name;
    private String endpointUrl;
    private List<String> subscribedEvents;
    private String description;

    public CreateWebhookDTO() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEndpointUrl() { return endpointUrl; }
    public void setEndpointUrl(String endpointUrl) { this.endpointUrl = endpointUrl; }

    public List<String> getSubscribedEvents() { return subscribedEvents; }
    public void setSubscribedEvents(List<String> subscribedEvents) { this.subscribedEvents = subscribedEvents; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
