package com.rentflow.claims.dto;

public class ClaimRecommendationDTO {
    private String recommendation; // REPAIR, REPLACEMENT, WAIVE
    private double confidence;
    private String reason;

    public ClaimRecommendationDTO() {}

    public ClaimRecommendationDTO(String recommendation, double confidence, String reason) {
        this.recommendation = recommendation;
        this.confidence = confidence;
        this.reason = reason;
    }

    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }

    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
