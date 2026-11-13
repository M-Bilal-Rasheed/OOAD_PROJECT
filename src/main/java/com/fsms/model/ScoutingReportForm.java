package com.fsms.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// ============================================================
// ScoutingReportForm — form-backing object for creating/editing a
// ScoutingReport. Keeps validation and HTML binding separate from
// the JPA entity, so a form can never set the author or report id.
// ============================================================
public class ScoutingReportForm {

    @NotNull(message = "Please choose a player")
    private Long playerId;

    @NotBlank(message = "Strengths are required")
    @Size(max = ScoutingReport.TEXT_LIMIT, message = "Strengths must be 2000 characters or fewer")
    private String strengths;

    @NotBlank(message = "Weaknesses are required")
    @Size(max = ScoutingReport.TEXT_LIMIT, message = "Weaknesses must be 2000 characters or fewer")
    private String weaknesses;

    @NotBlank(message = "Potential is required")
    @Size(max = ScoutingReport.TEXT_LIMIT, message = "Potential must be 2000 characters or fewer")
    private String potential;

    @NotNull(message = "Potential rating is required")
    @Min(value = 1, message = "Potential rating must be between 1 and 10")
    @Max(value = 10, message = "Potential rating must be between 1 and 10")
    private Integer potentialRating = 5;

    @NotBlank(message = "Performance observations are required")
    @Size(max = ScoutingReport.TEXT_LIMIT, message = "Observations must be 2000 characters or fewer")
    private String performanceObservations;

    @NotNull(message = "Please choose an overall recommendation")
    private Recommendation recommendation;

    @NotBlank(message = "Please explain your recommendation")
    @Size(max = ScoutingReport.TEXT_LIMIT, message = "Summary must be 2000 characters or fewer")
    private String recommendationSummary;

    public ScoutingReportForm() {
    }

    // Builds a form pre-filled from an existing report (used by the edit page)
    public static ScoutingReportForm fromReport(ScoutingReport r) {
        ScoutingReportForm f = new ScoutingReportForm();
        f.setPlayerId(r.getPlayer().getId());
        f.setStrengths(r.getStrengths());
        f.setWeaknesses(r.getWeaknesses());
        f.setPotential(r.getPotential());
        f.setPotentialRating(r.getPotentialRating());
        f.setPerformanceObservations(r.getPerformanceObservations());
        f.setRecommendation(r.getRecommendation());
        f.setRecommendationSummary(r.getRecommendationSummary());
        return f;
    }

    // Copies the editable fields onto a report entity (trimmed)
    public void applyTo(ScoutingReport r) {
        r.setStrengths(strengths.trim());
        r.setWeaknesses(weaknesses.trim());
        r.setPotential(potential.trim());
        r.setPotentialRating(potentialRating);
        r.setPerformanceObservations(performanceObservations.trim());
        r.setRecommendation(recommendation);
        r.setRecommendationSummary(recommendationSummary.trim());
    }

    public Long getPlayerId() { return playerId; }
    public void setPlayerId(Long playerId) { this.playerId = playerId; }

    public String getStrengths() { return strengths; }
    public void setStrengths(String strengths) { this.strengths = strengths; }

    public String getWeaknesses() { return weaknesses; }
    public void setWeaknesses(String weaknesses) { this.weaknesses = weaknesses; }

    public String getPotential() { return potential; }
    public void setPotential(String potential) { this.potential = potential; }

    public Integer getPotentialRating() { return potentialRating; }
    public void setPotentialRating(Integer potentialRating) { this.potentialRating = potentialRating; }

    public String getPerformanceObservations() { return performanceObservations; }
    public void setPerformanceObservations(String performanceObservations) {
        this.performanceObservations = performanceObservations;
    }

    public Recommendation getRecommendation() { return recommendation; }
    public void setRecommendation(Recommendation recommendation) { this.recommendation = recommendation; }

    public String getRecommendationSummary() { return recommendationSummary; }
    public void setRecommendationSummary(String recommendationSummary) {
        this.recommendationSummary = recommendationSummary;
    }
}
