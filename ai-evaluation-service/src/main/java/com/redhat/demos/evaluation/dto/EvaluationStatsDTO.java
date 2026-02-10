package com.redhat.demos.evaluation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class EvaluationStatsDTO {

    @JsonProperty("totalEvaluated")
    private long totalEvaluated;

    @JsonProperty("approvedCount")
    private long approvedCount;

    @JsonProperty("rejectedCount")
    private long rejectedCount;

    @JsonProperty("averageSimilarityScore")
    private BigDecimal averageSimilarityScore;

    public EvaluationStatsDTO() {
    }

    public EvaluationStatsDTO(long totalEvaluated, long approvedCount, long rejectedCount,
                              BigDecimal averageSimilarityScore) {
        this.totalEvaluated = totalEvaluated;
        this.approvedCount = approvedCount;
        this.rejectedCount = rejectedCount;
        this.averageSimilarityScore = averageSimilarityScore;
    }

    public long getTotalEvaluated() {
        return totalEvaluated;
    }

    public void setTotalEvaluated(long totalEvaluated) {
        this.totalEvaluated = totalEvaluated;
    }

    public long getApprovedCount() {
        return approvedCount;
    }

    public void setApprovedCount(long approvedCount) {
        this.approvedCount = approvedCount;
    }

    public long getRejectedCount() {
        return rejectedCount;
    }

    public void setRejectedCount(long rejectedCount) {
        this.rejectedCount = rejectedCount;
    }

    public BigDecimal getAverageSimilarityScore() {
        return averageSimilarityScore;
    }

    public void setAverageSimilarityScore(BigDecimal averageSimilarityScore) {
        this.averageSimilarityScore = averageSimilarityScore;
    }
}
