package com.rentflow.aisales.controller;

import com.rentflow.aisales.dto.AiSettingsDTO;
import com.rentflow.aisales.service.AiSalesSettingsService;
import com.rentflow.security.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai-sales/settings")
public class AiSalesSettingsController {

    private final AiSalesSettingsService settingsService;

    public AiSalesSettingsController(AiSalesSettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    public ResponseEntity<AiSettingsDTO> getSettings() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(settingsService.getSettingsDTO(tenantId));
    }

    @PutMapping
    public ResponseEntity<AiSettingsDTO> updateSettings(@RequestBody AiSettingsDTO dto) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(settingsService.updateSettings(tenantId, dto));
    }
}
