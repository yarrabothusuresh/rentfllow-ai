package com.rentflow.aisales.service;

import com.rentflow.aisales.dto.AiSettingsDTO;
import com.rentflow.aisales.model.AiTenantSettings;
import com.rentflow.aisales.repository.AiTenantSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AiSalesSettingsService {

    private final AiTenantSettingsRepository settingsRepository;

    public AiSalesSettingsService(AiTenantSettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    public AiTenantSettings getSettings(String tenantId) {
        return settingsRepository.findByTenantId(tenantId).orElseGet(() -> {
            AiTenantSettings s = new AiTenantSettings();
            s.setTenantId(tenantId);
            s.setAiEnabled(true);
            s.setAiProvider("mock");
            s.setAiModel("mock-sales-v1");
            s.setCustomerAiEnabled(true);
            s.setInternalSalesAssistantEnabled(true);
            s.setHumanQuoteApprovalRequired(true);
            s.setDailyRequestLimit(500);
            s.setMaxConversationMessages(20);
            s.setTargetGrossMarginPct(30.0);
            s.setLowMarginThresholdPct(20.0);
            return settingsRepository.save(s);
        });
    }

    public AiSettingsDTO getSettingsDTO(String tenantId) {
        AiTenantSettings s = getSettings(tenantId);
        AiSettingsDTO dto = new AiSettingsDTO();
        dto.setAiEnabled(s.isAiEnabled());
        dto.setAiProvider(s.getAiProvider());
        dto.setAiModel(s.getAiModel());
        dto.setCustomerAiEnabled(s.isCustomerAiEnabled());
        dto.setInternalSalesAssistantEnabled(s.isInternalSalesAssistantEnabled());
        dto.setHumanQuoteApprovalRequired(s.isHumanQuoteApprovalRequired());
        dto.setDailyRequestLimit(s.getDailyRequestLimit());
        dto.setMaxConversationMessages(s.getMaxConversationMessages());
        dto.setTargetGrossMarginPct(s.getTargetGrossMarginPct());
        dto.setLowMarginThresholdPct(s.getLowMarginThresholdPct());
        dto.setApiKeyMasked(s.getApiKeyMasked());
        return dto;
    }

    @Transactional
    public AiSettingsDTO updateSettings(String tenantId, AiSettingsDTO dto) {
        AiTenantSettings s = getSettings(tenantId);
        s.setAiEnabled(dto.isAiEnabled());
        if (dto.getAiProvider() != null) s.setAiProvider(dto.getAiProvider());
        if (dto.getAiModel() != null) s.setAiModel(dto.getAiModel());
        s.setCustomerAiEnabled(dto.isCustomerAiEnabled());
        s.setInternalSalesAssistantEnabled(dto.isInternalSalesAssistantEnabled());
        s.setHumanQuoteApprovalRequired(dto.isHumanQuoteApprovalRequired());
        if (dto.getDailyRequestLimit() > 0) s.setDailyRequestLimit(dto.getDailyRequestLimit());
        if (dto.getMaxConversationMessages() > 0) s.setMaxConversationMessages(dto.getMaxConversationMessages());
        if (dto.getTargetGrossMarginPct() > 0) s.setTargetGrossMarginPct(dto.getTargetGrossMarginPct());
        if (dto.getLowMarginThresholdPct() > 0) s.setLowMarginThresholdPct(dto.getLowMarginThresholdPct());
        settingsRepository.save(s);
        return getSettingsDTO(tenantId);
    }
}
