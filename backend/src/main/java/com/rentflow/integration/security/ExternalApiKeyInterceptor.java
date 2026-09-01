package com.rentflow.integration.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentflow.integration.dto.ExternalApiErrorDTO;
import com.rentflow.integration.model.ExternalApiKey;
import com.rentflow.integration.service.ExternalApiKeyService;
import com.rentflow.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;
import java.util.UUID;

@Component
public class ExternalApiKeyInterceptor implements HandlerInterceptor {

    public static final String ATTR_API_KEY = "EXTERNAL_API_KEY";
    private final ExternalApiKeyService apiKeyService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ExternalApiKeyInterceptor(ExternalApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Only intercept requests for external API
        String path = request.getRequestURI();
        if (!path.startsWith("/api/v1/external")) {
            return true;
        }

        String requestId = "req_" + UUID.randomUUID().toString().substring(0, 8);
        request.setAttribute("REQUEST_ID", requestId);

        // 1. Extract API Key
        String rawKey = request.getHeader("X-API-Key");
        if (rawKey == null || rawKey.isBlank()) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer rf_live_")) {
                rawKey = authHeader.substring(7);
            }
        }

        if (rawKey == null || rawKey.isBlank()) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHORIZED", "Missing or invalid API key. Pass via X-API-Key or Authorization: Bearer <key>", requestId);
            return false;
        }

        // 2. Authenticate Key
        Optional<ExternalApiKey> keyOpt = apiKeyService.authenticateApiKey(rawKey);
        if (keyOpt.isEmpty()) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHORIZED", "Invalid, revoked or expired API key", requestId);
            return false;
        }

        ExternalApiKey key = keyOpt.get();
        request.setAttribute(ATTR_API_KEY, key);
        SecurityUtils.setTestTenantId(key.getTenantId());

        // 3. Check Scope
        if (handler instanceof HandlerMethod handlerMethod) {
            ExternalApiScope scopeAnnotation = handlerMethod.getMethodAnnotation(ExternalApiScope.class);
            if (scopeAnnotation == null) {
                scopeAnnotation = handlerMethod.getBeanType().getAnnotation(ExternalApiScope.class);
            }

            if (scopeAnnotation != null) {
                String requiredScope = scopeAnnotation.value();
                if (!apiKeyService.hasScope(key, requiredScope)) {
                    sendError(response, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", "API key lacks required scope: [" + requiredScope + "]", requestId);
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        String path = request.getRequestURI();
        if (path.startsWith("/api/v1/external")) {
            SecurityUtils.clearTestTenantId();
        }
    }

    private void sendError(HttpServletResponse response, int status, String code, String message, String requestId) throws Exception {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        ExternalApiErrorDTO error = new ExternalApiErrorDTO(code, message, requestId);
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}
