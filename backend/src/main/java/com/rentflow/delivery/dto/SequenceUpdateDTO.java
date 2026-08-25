package com.rentflow.delivery.dto;

import java.util.List;
import java.util.UUID;

public class SequenceUpdateDTO {
    private List<UUID> deliveryIds;

    public List<UUID> getDeliveryIds() { return deliveryIds; }
    public void setDeliveryIds(List<UUID> deliveryIds) { this.deliveryIds = deliveryIds; }
}
