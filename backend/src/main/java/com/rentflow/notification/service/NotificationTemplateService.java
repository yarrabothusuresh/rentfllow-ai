package com.rentflow.notification.service;

import com.rentflow.notification.dto.NotificationTemplateDTO;
import com.rentflow.notification.model.NotificationChannel;
import com.rentflow.notification.model.NotificationTemplate;
import com.rentflow.notification.model.NotificationType;
import com.rentflow.notification.repository.NotificationTemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NotificationTemplateService {

    private final NotificationTemplateRepository templateRepository;
    private final SafeTemplateRenderer templateRenderer;

    public NotificationTemplateService(NotificationTemplateRepository templateRepository, SafeTemplateRenderer templateRenderer) {
        this.templateRepository = templateRepository;
        this.templateRenderer = templateRenderer;
    }

    @Transactional(readOnly = true)
    public List<NotificationTemplateDTO> getTemplates(String tenantId) {
        return templateRepository.findByTenantId(tenantId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public NotificationTemplateDTO getTemplateById(UUID id, String tenantId) {
        NotificationTemplate template = templateRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found or unauthorized"));
        return mapToDTO(template);
    }

    @Transactional
    public NotificationTemplateDTO createTemplate(String tenantId, NotificationTemplateDTO dto) {
        NotificationTemplate template = new NotificationTemplate(
                tenantId,
                dto.getName(),
                dto.getNotificationType(),
                dto.getChannel(),
                dto.getSubject(),
                dto.getBody()
        );
        template.setActive(dto.isActive());
        NotificationTemplate saved = templateRepository.save(template);
        return mapToDTO(saved);
    }

    @Transactional
    public NotificationTemplateDTO updateTemplate(UUID id, String tenantId, NotificationTemplateDTO dto) {
        NotificationTemplate template = templateRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found or unauthorized"));
        template.setName(dto.getName());
        template.setNotificationType(dto.getNotificationType());
        template.setChannel(dto.getChannel());
        template.setSubject(dto.getSubject());
        template.setBody(dto.getBody());
        template.setActive(dto.isActive());
        NotificationTemplate saved = templateRepository.save(template);
        return mapToDTO(saved);
    }

    @Transactional(readOnly = true)
    public String renderPreview(String templateBody, Map<String, Object> sampleVariables) {
        return templateRenderer.render(templateBody, sampleVariables);
    }

    @Transactional(readOnly = true)
    public Optional<NotificationTemplate> findActiveTemplate(String tenantId, NotificationType type, NotificationChannel channel) {
        return templateRepository.findByTenantIdAndNotificationTypeAndChannelAndActiveTrue(tenantId, type, channel);
    }

    public NotificationTemplateDTO mapToDTO(NotificationTemplate entity) {
        NotificationTemplateDTO dto = new NotificationTemplateDTO();
        dto.setId(entity.getId());
        dto.setTenantId(entity.getTenantId());
        dto.setName(entity.getName());
        dto.setNotificationType(entity.getNotificationType());
        dto.setChannel(entity.getChannel());
        dto.setSubject(entity.getSubject());
        dto.setBody(entity.getBody());
        dto.setActive(entity.isActive());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}
