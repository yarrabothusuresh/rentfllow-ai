package com.rentflow.notification.service;

import com.rentflow.notification.dto.NotificationPreferenceDTO;
import com.rentflow.notification.model.NotificationPreference;
import com.rentflow.notification.model.NotificationType;
import com.rentflow.notification.repository.NotificationPreferenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NotificationPreferenceService {

    private final NotificationPreferenceRepository preferenceRepository;

    public NotificationPreferenceService(NotificationPreferenceRepository preferenceRepository) {
        this.preferenceRepository = preferenceRepository;
    }

    @Transactional(readOnly = true)
    public List<NotificationPreferenceDTO> getCustomerPreferences(String tenantId, UUID customerId) {
        List<NotificationPreference> existing = preferenceRepository.findByTenantIdAndCustomerId(tenantId, customerId);
        return ensureAllTypesPresent(tenantId, customerId, null, existing);
    }

    @Transactional(readOnly = true)
    public List<NotificationPreferenceDTO> getUserPreferences(String tenantId, UUID userId) {
        List<NotificationPreference> existing = preferenceRepository.findByTenantIdAndUserId(tenantId, userId);
        return ensureAllTypesPresent(tenantId, null, userId, existing);
    }

    @Transactional
    public List<NotificationPreferenceDTO> updateCustomerPreferences(String tenantId, UUID customerId, List<NotificationPreferenceDTO> dtos) {
        List<NotificationPreferenceDTO> result = new ArrayList<>();
        for (NotificationPreferenceDTO dto : dtos) {
            Optional<NotificationPreference> opt = preferenceRepository.findByTenantIdAndCustomerIdAndNotificationType(
                    tenantId, customerId, dto.getNotificationType());
            NotificationPreference pref = opt.orElseGet(() -> new NotificationPreference(
                    tenantId, customerId, null, dto.getNotificationType(), true, true, true));
            pref.setEmailEnabled(dto.isEmailEnabled());
            pref.setSmsEnabled(dto.isSmsEnabled());
            pref.setInAppEnabled(dto.isInAppEnabled());
            NotificationPreference saved = preferenceRepository.save(pref);
            result.add(mapToDTO(saved));
        }
        return result;
    }

    @Transactional
    public List<NotificationPreferenceDTO> updateUserPreferences(String tenantId, UUID userId, List<NotificationPreferenceDTO> dtos) {
        List<NotificationPreferenceDTO> result = new ArrayList<>();
        for (NotificationPreferenceDTO dto : dtos) {
            Optional<NotificationPreference> opt = preferenceRepository.findByTenantIdAndUserIdAndNotificationType(
                    tenantId, userId, dto.getNotificationType());
            NotificationPreference pref = opt.orElseGet(() -> new NotificationPreference(
                    tenantId, null, userId, dto.getNotificationType(), true, true, true));
            pref.setEmailEnabled(dto.isEmailEnabled());
            pref.setSmsEnabled(dto.isSmsEnabled());
            pref.setInAppEnabled(dto.isInAppEnabled());
            NotificationPreference saved = preferenceRepository.save(pref);
            result.add(mapToDTO(saved));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public boolean isChannelEnabled(String tenantId, UUID customerId, UUID userId, NotificationType type, String channel) {
        Optional<NotificationPreference> opt = Optional.empty();
        if (customerId != null) {
            opt = preferenceRepository.findByTenantIdAndCustomerIdAndNotificationType(tenantId, customerId, type);
        } else if (userId != null) {
            opt = preferenceRepository.findByTenantIdAndUserIdAndNotificationType(tenantId, userId, type);
        }

        if (opt.isEmpty()) {
            // Default settings if preference record is not created yet
            return true;
        }

        NotificationPreference pref = opt.get();
        if ("EMAIL".equalsIgnoreCase(channel)) return pref.isEmailEnabled();
        if ("SMS".equalsIgnoreCase(channel)) return pref.isSmsEnabled();
        if ("IN_APP".equalsIgnoreCase(channel)) return pref.isInAppEnabled();
        return true;
    }

    private List<NotificationPreferenceDTO> ensureAllTypesPresent(String tenantId, UUID customerId, UUID userId, List<NotificationPreference> existing) {
        List<NotificationPreferenceDTO> dtos = existing.stream().map(this::mapToDTO).collect(Collectors.toList());
        for (NotificationType type : NotificationType.values()) {
            boolean present = dtos.stream().anyMatch(d -> d.getNotificationType() == type);
            if (!present) {
                NotificationPreference defaultPref = new NotificationPreference(tenantId, customerId, userId, type, true, true, true);
                dtos.add(mapToDTO(defaultPref));
            }
        }
        return dtos;
    }

    private NotificationPreferenceDTO mapToDTO(NotificationPreference entity) {
        return new NotificationPreferenceDTO(
                entity.getId(),
                entity.getTenantId(),
                entity.getCustomerId(),
                entity.getUserId(),
                entity.getNotificationType(),
                entity.isEmailEnabled(),
                entity.isSmsEnabled(),
                entity.isInAppEnabled()
        );
    }
}
