package com.rentflow.ai.service;

import com.rentflow.ai.dto.CustomerDTO;
import com.rentflow.ai.model.Customer;
import com.rentflow.ai.model.CustomerStatus;
import com.rentflow.ai.model.CustomerType;
import com.rentflow.ai.repository.CustomerRepository;
import com.rentflow.ai.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final EventRepository eventRepository;

    public CustomerService(CustomerRepository customerRepository, EventRepository eventRepository) {
        this.customerRepository = customerRepository;
        this.eventRepository = eventRepository;
    }

    public List<CustomerDTO> getCustomers(String tenantId) {
        return customerRepository.findByTenantId(tenantId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<CustomerDTO> getCustomerById(String tenantId, UUID id) {
        return customerRepository.findByTenantIdAndId(tenantId, id)
                .map(this::toDTO);
    }

    public Optional<CustomerDTO> findByEmail(String tenantId, String email) {
        return customerRepository.findFirstByTenantIdAndEmailIgnoreCase(tenantId, email)
                .map(this::toDTO);
    }

    public List<CustomerDTO> searchCustomers(String tenantId, String query) {
        if (query == null || query.trim().isEmpty()) {
            return getCustomers(tenantId);
        }
        return customerRepository.searchCustomers(tenantId, query.trim()).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public CustomerDTO createCustomer(String tenantId, CustomerDTO dto) {
        Customer customer = new Customer();
        customer.setTenantId(tenantId);
        customer.setCustomerNumber(generateCustomerNumber(tenantId));
        customer.setFirstName(dto.getFirstName());
        customer.setLastName(dto.getLastName());
        customer.setCompanyName(dto.getCompanyName());
        customer.setEmail(dto.getEmail());
        customer.setPhone(dto.getPhone());
        customer.setAlternatePhone(dto.getAlternatePhone());
        customer.setCustomerType(dto.getCustomerType() != null ? dto.getCustomerType() : CustomerType.INDIVIDUAL);
        customer.setBillingAddress(dto.getBillingAddress());
        customer.setShippingAddress(dto.getShippingAddress());
        customer.setCity(dto.getCity());
        customer.setState(dto.getState());
        customer.setZipCode(dto.getZipCode());
        customer.setCountry(dto.getCountry() != null ? dto.getCountry() : "USA");
        customer.setNotes(dto.getNotes());
        customer.setStatus(dto.getStatus() != null ? dto.getStatus() : CustomerStatus.ACTIVE);

        Customer saved = customerRepository.save(customer);
        return toDTO(saved);
    }

    @Transactional
    public Optional<CustomerDTO> updateCustomer(String tenantId, UUID id, CustomerDTO dto) {
        return customerRepository.findByTenantIdAndId(tenantId, id).map(existing -> {
            if (dto.getFirstName() != null) existing.setFirstName(dto.getFirstName());
            if (dto.getLastName() != null) existing.setLastName(dto.getLastName());
            if (dto.getCompanyName() != null) existing.setCompanyName(dto.getCompanyName());
            if (dto.getEmail() != null) existing.setEmail(dto.getEmail());
            if (dto.getPhone() != null) existing.setPhone(dto.getPhone());
            if (dto.getAlternatePhone() != null) existing.setAlternatePhone(dto.getAlternatePhone());
            if (dto.getCustomerType() != null) existing.setCustomerType(dto.getCustomerType());
            if (dto.getBillingAddress() != null) existing.setBillingAddress(dto.getBillingAddress());
            if (dto.getShippingAddress() != null) existing.setShippingAddress(dto.getShippingAddress());
            if (dto.getCity() != null) existing.setCity(dto.getCity());
            if (dto.getState() != null) existing.setState(dto.getState());
            if (dto.getZipCode() != null) existing.setZipCode(dto.getZipCode());
            if (dto.getCountry() != null) existing.setCountry(dto.getCountry());
            if (dto.getNotes() != null) existing.setNotes(dto.getNotes());
            if (dto.getStatus() != null) existing.setStatus(dto.getStatus());

            Customer saved = customerRepository.save(existing);
            return toDTO(saved);
        });
    }

    public String generateCustomerNumber(String tenantId) {
        long count = customerRepository.countByTenantId(tenantId);
        return String.format("CUS-%06d", count + 1);
    }

    public CustomerDTO toDTO(Customer customer) {
        CustomerDTO dto = new CustomerDTO();
        dto.setId(customer.getId());
        dto.setTenantId(customer.getTenantId());
        dto.setCustomerNumber(customer.getCustomerNumber());
        dto.setFirstName(customer.getFirstName());
        dto.setLastName(customer.getLastName());
        dto.setCompanyName(customer.getCompanyName());
        dto.setEmail(customer.getEmail());
        dto.setPhone(customer.getPhone());
        dto.setAlternatePhone(customer.getAlternatePhone());
        dto.setCustomerType(customer.getCustomerType());
        dto.setBillingAddress(customer.getBillingAddress());
        dto.setShippingAddress(customer.getShippingAddress());
        dto.setCity(customer.getCity());
        dto.setState(customer.getState());
        dto.setZipCode(customer.getZipCode());
        dto.setCountry(customer.getCountry());
        dto.setNotes(customer.getNotes());
        dto.setStatus(customer.getStatus());
        dto.setCreatedAt(customer.getCreatedAt());
        dto.setUpdatedAt(customer.getUpdatedAt());

        // Count events for this customer
        int eventsCount = eventRepository.findByTenantIdAndCustomerId(customer.getTenantId(), customer.getId()).size();
        dto.setEventsCount(eventsCount);

        return dto;
    }
}
