package com.rentflow.portal.service;

import com.rentflow.ai.model.Booking;
import com.rentflow.ai.repository.BookingRepository;
import com.rentflow.portal.dto.CreateMessageRequestDTO;
import com.rentflow.portal.dto.CustomerConversationDTO;
import com.rentflow.portal.model.ConversationStatus;
import com.rentflow.portal.model.CustomerConversation;
import com.rentflow.portal.model.CustomerMessage;
import com.rentflow.portal.repository.CustomerConversationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class CustomerMessagingService {

    private final CustomerConversationRepository conversationRepository;
    private final BookingRepository bookingRepository;

    public CustomerMessagingService(CustomerConversationRepository conversationRepository,
                                      BookingRepository bookingRepository) {
        this.conversationRepository = conversationRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional(readOnly = true)
    public List<CustomerConversationDTO> getCustomerConversations(String tenantId, UUID customerId) {
        return conversationRepository.findByTenantIdAndCustomerIdOrderByUpdatedAtDesc(tenantId, customerId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CustomerConversationDTO getConversationDetail(String tenantId, UUID customerId, UUID conversationId) {
        CustomerConversation conv = conversationRepository.findByTenantIdAndCustomerIdAndId(tenantId, customerId, conversationId)
                .orElseThrow(() -> new SecurityException("Access Denied: Conversation not found or unauthorized."));
        return mapToDTO(conv);
    }

    public CustomerConversationDTO createOrReplyMessage(String tenantId, UUID customerId, CreateMessageRequestDTO dto, String senderType) {
        CustomerConversation conv;

        if (dto.getConversationId() != null) {
            conv = conversationRepository.findByTenantIdAndCustomerIdAndId(tenantId, customerId, dto.getConversationId())
                    .orElseThrow(() -> new SecurityException("Access Denied: Conversation not found or unauthorized."));
        } else if (dto.getBookingId() != null) {
            conv = conversationRepository.findByTenantIdAndBookingId(tenantId, dto.getBookingId())
                    .orElseGet(() -> {
                        Booking booking = bookingRepository.findById(dto.getBookingId())
                                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + dto.getBookingId()));
                        CustomerConversation c = new CustomerConversation();
                        c.setTenantId(tenantId);
                        c.setCustomerId(customerId);
                        c.setBookingId(booking.getId());
                        c.setSubject(dto.getSubject() != null ? dto.getSubject() : "Question about " + booking.getBookingNumber());
                        c.setStatus(ConversationStatus.OPEN);
                        return conversationRepository.save(c);
                    });
        } else {
            conv = new CustomerConversation();
            conv.setTenantId(tenantId);
            conv.setCustomerId(customerId);
            conv.setQuoteId(dto.getQuoteId());
            conv.setSubject(dto.getSubject() != null ? dto.getSubject() : "Customer Question");
            conv.setStatus(ConversationStatus.OPEN);
            conv = conversationRepository.save(conv);
        }

        CustomerMessage msg = new CustomerMessage();
        msg.setConversation(conv);
        msg.setSenderType(senderType != null ? senderType : "CUSTOMER");
        msg.setSenderId(customerId != null ? customerId.toString() : "System");
        msg.setMessage(dto.getMessage());

        conv.getMessages().add(msg);
        conv.setStatus("CUSTOMER".equalsIgnoreCase(senderType) ? ConversationStatus.WAITING_FOR_STAFF : ConversationStatus.WAITING_FOR_CUSTOMER);
        conv.setUpdatedAt(LocalDateTime.now());

        CustomerConversation saved = conversationRepository.save(conv);
        return mapToDTO(saved);
    }

    private CustomerConversationDTO mapToDTO(CustomerConversation c) {
        CustomerConversationDTO dto = new CustomerConversationDTO();
        dto.setId(c.getId());
        dto.setCustomerId(c.getCustomerId());
        dto.setBookingId(c.getBookingId());
        if (c.getBookingId() != null) {
            bookingRepository.findById(c.getBookingId()).ifPresent(b -> dto.setBookingNumber(b.getBookingNumber()));
        }
        dto.setQuoteId(c.getQuoteId());
        dto.setSubject(c.getSubject());
        dto.setStatus(c.getStatus());
        dto.setCreatedAt(c.getCreatedAt());
        dto.setUpdatedAt(c.getUpdatedAt());

        if (c.getMessages() != null) {
            dto.setMessages(c.getMessages().stream().map(m -> {
                CustomerConversationDTO.MessageDTO mdto = new CustomerConversationDTO.MessageDTO();
                mdto.setId(m.getId());
                mdto.setSenderType(m.getSenderType());
                mdto.setSenderId(m.getSenderId());
                mdto.setMessage(m.getMessage());
                mdto.setCreatedAt(m.getCreatedAt());
                return mdto;
            }).collect(Collectors.toList()));
        }

        return dto;
    }
}
