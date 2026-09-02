package com.rentflow.aisales.service;

import com.rentflow.aisales.dto.RentalInquiryDTO;
import com.rentflow.aisales.model.RentalInquiry;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AiInquiryExtractionService {

    public List<String> determineMissingFields(RentalInquiry inquiry) {
        List<String> missing = new ArrayList<>();
        if (inquiry.getEventType() == null) missing.add("eventType");
        if (inquiry.getGuestCount() == null || inquiry.getGuestCount() <= 0) missing.add("guestCount");
        if (inquiry.getEventDate() == null) missing.add("eventDate");
        if (inquiry.isDeliveryRequired() && (inquiry.getDeliveryCity() == null || inquiry.getDeliveryCity().trim().isEmpty())) {
            missing.add("deliveryCity");
        }
        if (inquiry.getTablePreference() == null) missing.add("tablePreference");
        return missing;
    }

    public RentalInquiryDTO mapToDTO(RentalInquiry inquiry) {
        RentalInquiryDTO dto = new RentalInquiryDTO();
        dto.setId(inquiry.getId());
        dto.setConversationId(inquiry.getConversationId());
        dto.setCustomerId(inquiry.getCustomerId());
        dto.setEventType(inquiry.getEventType());
        dto.setEventName(inquiry.getEventName());
        dto.setEventDate(inquiry.getEventDate());
        dto.setRentalStart(inquiry.getRentalStart());
        dto.setRentalEnd(inquiry.getRentalEnd());
        dto.setDeliveryAddress(inquiry.getDeliveryAddress());
        dto.setDeliveryCity(inquiry.getDeliveryCity());
        dto.setDeliveryTime(inquiry.getDeliveryTime());
        dto.setDeliveryRequired(inquiry.isDeliveryRequired());
        dto.setGuestCount(inquiry.getGuestCount());
        dto.setTablePreference(inquiry.getTablePreference());
        dto.setChairPreference(inquiry.getChairPreference());
        dto.setBudget(inquiry.getBudget());
        dto.setComplete(inquiry.isComplete());
        dto.setNotes(inquiry.getNotes());
        dto.setMissingFields(determineMissingFields(inquiry));
        return dto;
    }
}
