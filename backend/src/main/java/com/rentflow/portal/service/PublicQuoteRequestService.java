package com.rentflow.portal.service;

import com.rentflow.ai.dto.QuoteDTO;
import com.rentflow.ai.dto.QuoteItemDTO;
import com.rentflow.ai.model.Customer;
import com.rentflow.ai.model.CustomerType;
import com.rentflow.ai.repository.CustomerRepository;
import com.rentflow.ai.service.QuoteService;
import com.rentflow.portal.dto.PublicQuoteRequestDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PublicQuoteRequestService {

    private final QuoteService quoteService;
    private final CustomerRepository customerRepository;

    public PublicQuoteRequestService(QuoteService quoteService, CustomerRepository customerRepository) {
        this.quoteService = quoteService;
        this.customerRepository = customerRepository;
    }

    public QuoteDTO submitPublicQuoteRequest(String tenantId, UUID customerId, PublicQuoteRequestDTO request) {
        // Resolve or Create Customer if guest
        UUID targetCustomerId = customerId;
        if (targetCustomerId == null) {
            Customer guest = new Customer();
            guest.setTenantId(tenantId);
            guest.setCustomerNumber("CUS-GUEST-" + UUID.randomUUID().toString().substring(0, 6));
            guest.setFirstName(request.getGuestName() != null && !request.getGuestName().isBlank() ? request.getGuestName() : "Guest");
            guest.setLastName("Customer");
            guest.setEmail(request.getGuestEmail() != null && !request.getGuestEmail().isBlank() ? request.getGuestEmail() : "guest@rentflow.ai");
            guest.setPhone(request.getGuestPhone());
            guest.setCompanyName(request.getGuestCompany());
            guest.setCustomerType(CustomerType.INDIVIDUAL);
            if (request.getDeliveryAddressText() != null) {
                guest.setShippingAddress(request.getDeliveryAddressText());
            }
            Customer savedGuest = customerRepository.save(guest);
            targetCustomerId = savedGuest.getId();
        }

        QuoteDTO qDto = new QuoteDTO();
        qDto.setTenantId(tenantId);
        qDto.setCustomerId(targetCustomerId);

        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : LocalDate.now().plusDays(1);
        LocalDate endDate = request.getEndDate() != null ? request.getEndDate() : startDate.plusDays(2);

        qDto.setRentalStartDateTime(startDate.atTime(9, 0));
        qDto.setRentalEndDateTime(endDate.atTime(18, 0));
        qDto.setNotes("Event: " + (request.getEventName() != null ? request.getEventName() : "Rental Event") +
                      (request.getNotes() != null ? " • " + request.getNotes() : ""));

        List<QuoteItemDTO> items = new ArrayList<>();
        if (request.getItems() != null) {
            for (PublicQuoteRequestDTO.QuoteItemRequestDTO reqItem : request.getItems()) {
                QuoteItemDTO item = new QuoteItemDTO();
                item.setProductId(reqItem.getProductId());
                item.setQuantity(Math.max(1, reqItem.getQuantity()));
                item.setRentalDays(Math.max(1, (int) java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate)));
                items.add(item);
            }
        }
        qDto.setItems(items);

        // Delegate to existing QuoteService (using CUSTOMER role)
        return quoteService.createQuote(tenantId, qDto, "CUSTOMER");
    }
}
