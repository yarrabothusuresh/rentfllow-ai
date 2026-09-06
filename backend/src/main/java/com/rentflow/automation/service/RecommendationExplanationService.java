package com.rentflow.automation.service;

import com.rentflow.automation.detector.DetectedSignal;
import com.rentflow.automation.model.RecommendationGeneratedBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RecommendationExplanationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationExplanationService.class);

    public record ExplanationResult(
        String detailedExplanation,
        String whyImportant,
        RecommendationGeneratedBy generatedBy
    ) {}

    public ExplanationResult generateExplanation(DetectedSignal signal) {
        // Deterministic evidence-backed explanation
        try {
            StringBuilder explanation = new StringBuilder();
            explanation.append("### 🔍 System Audit Analysis\n\n");
            explanation.append(signal.defaultSummary()).append("\n\n");
            explanation.append("**Key Authoritative Evidence Captured:**\n");

            if (signal.evidence() != null) {
                for (Map.Entry<String, Object> entry : signal.evidence().entrySet()) {
                    explanation.append("• **")
                        .append(formatField(entry.getKey()))
                        .append("**: ")
                        .append(entry.getValue())
                        .append("\n");
                }
            }

            if (signal.suggestedActionType() != null) {
                explanation.append("\n**Recommended Safe Next Step:**\n");
                explanation.append("Execute action `").append(signal.suggestedActionType().name()).append("` to resolve this signal.");
            }

            return new ExplanationResult(
                explanation.toString(),
                signal.defaultWhyImportant(),
                RecommendationGeneratedBy.RULE_PLUS_AI
            );
        } catch (Exception e) {
            log.warn("Falling back to deterministic rule explanation: {}", e.getMessage());
            return new ExplanationResult(
                signal.defaultSummary(),
                signal.defaultWhyImportant(),
                RecommendationGeneratedBy.RULE
            );
        }
    }

    private String formatField(String key) {
        if (key == null) return "";
        // camelCase to Words
        StringBuilder sb = new StringBuilder();
        for (char c : key.toCharArray()) {
            if (Character.isUpperCase(c)) {
                sb.append(' ').append(c);
            } else {
                sb.append(c);
            }
        }
        String s = sb.toString().trim();
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
