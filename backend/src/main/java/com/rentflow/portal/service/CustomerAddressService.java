package com.rentflow.portal.service;

import com.rentflow.portal.dto.CustomerAddressDTO;
import com.rentflow.portal.model.CustomerAddress;
import com.rentflow.portal.repository.CustomerAddressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class CustomerAddressService {

    private final CustomerAddressRepository addressRepository;

    public CustomerAddressService(CustomerAddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    @Transactional(readOnly = true)
    public List<CustomerAddressDTO> getCustomerAddresses(String tenantId, UUID customerId) {
        return addressRepository.findByTenantIdAndCustomerId(tenantId, customerId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CustomerAddressDTO getAddressDetail(String tenantId, UUID customerId, UUID addressId) {
        CustomerAddress address = addressRepository.findByTenantIdAndCustomerIdAndId(tenantId, customerId, addressId)
                .orElseThrow(() -> new SecurityException("Access Denied: Address not found or unauthorized."));
        return mapToDTO(address);
    }

    public CustomerAddressDTO createAddress(String tenantId, UUID customerId, CustomerAddressDTO dto) {
        CustomerAddress address = new CustomerAddress();
        address.setTenantId(tenantId);
        address.setCustomerId(customerId);
        address.setAddressType(dto.getAddressType() != null ? dto.getAddressType() : com.rentflow.portal.model.AddressType.HOME);
        address.setAddressLine1(dto.getAddressLine1());
        address.setAddressLine2(dto.getAddressLine2());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setZipCode(dto.getZipCode());
        address.setCountry(dto.getCountry() != null ? dto.getCountry() : "USA");
        address.setDefault(dto.isDefault());
        address.setDeliveryInstructions(dto.getDeliveryInstructions());
        address.setContactPerson(dto.getContactPerson());
        address.setPhone(dto.getPhone());

        // Handle setting default
        if (dto.isDefault()) {
            clearPreviousDefaults(tenantId, customerId);
        }

        CustomerAddress saved = addressRepository.save(address);
        return mapToDTO(saved);
    }

    public CustomerAddressDTO updateAddress(String tenantId, UUID customerId, UUID addressId, CustomerAddressDTO dto) {
        CustomerAddress address = addressRepository.findByTenantIdAndCustomerIdAndId(tenantId, customerId, addressId)
                .orElseThrow(() -> new SecurityException("Access Denied: Address not found or unauthorized."));

        if (dto.getAddressType() != null) address.setAddressType(dto.getAddressType());
        if (dto.getAddressLine1() != null) address.setAddressLine1(dto.getAddressLine1());
        if (dto.getAddressLine2() != null) address.setAddressLine2(dto.getAddressLine2());
        if (dto.getCity() != null) address.setCity(dto.getCity());
        if (dto.getState() != null) address.setState(dto.getState());
        if (dto.getZipCode() != null) address.setZipCode(dto.getZipCode());
        if (dto.getCountry() != null) address.setCountry(dto.getCountry());
        if (dto.getDeliveryInstructions() != null) address.setDeliveryInstructions(dto.getDeliveryInstructions());
        if (dto.getContactPerson() != null) address.setContactPerson(dto.getContactPerson());
        if (dto.getPhone() != null) address.setPhone(dto.getPhone());

        if (dto.isDefault() && !address.isDefault()) {
            clearPreviousDefaults(tenantId, customerId);
            address.setDefault(true);
        }

        CustomerAddress saved = addressRepository.save(address);
        return mapToDTO(saved);
    }

    public boolean deleteAddress(String tenantId, UUID customerId, UUID addressId) {
        CustomerAddress address = addressRepository.findByTenantIdAndCustomerIdAndId(tenantId, customerId, addressId)
                .orElseThrow(() -> new SecurityException("Access Denied: Address not found or unauthorized."));
        addressRepository.delete(address);
        return true;
    }

    private void clearPreviousDefaults(String tenantId, UUID customerId) {
        addressRepository.findByTenantIdAndCustomerIdAndIsDefaultTrue(tenantId, customerId)
                .ifPresent(existingDefault -> {
                    existingDefault.setDefault(false);
                    addressRepository.save(existingDefault);
                });
    }

    private CustomerAddressDTO mapToDTO(CustomerAddress a) {
        CustomerAddressDTO dto = new CustomerAddressDTO();
        dto.setId(a.getId());
        dto.setCustomerId(a.getCustomerId());
        dto.setAddressType(a.getAddressType());
        dto.setAddressLine1(a.getAddressLine1());
        dto.setAddressLine2(a.getAddressLine2());
        dto.setCity(a.getCity());
        dto.setState(a.getState());
        dto.setZipCode(a.getZipCode());
        dto.setCountry(a.getCountry());
        dto.setDefault(a.isDefault());
        dto.setDeliveryInstructions(a.getDeliveryInstructions());
        dto.setContactPerson(a.getContactPerson());
        dto.setPhone(a.getPhone());
        dto.setCreatedAt(a.getCreatedAt());
        return dto;
    }
}
