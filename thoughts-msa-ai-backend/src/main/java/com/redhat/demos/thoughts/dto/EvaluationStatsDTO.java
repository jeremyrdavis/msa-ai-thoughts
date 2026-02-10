package com.redhat.demos.thoughts.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class EvaluationStatsDTO {

    @JsonProperty("totalEvaluated")
    public long totalEvaluated;

    @JsonProperty("approvedCount")
    public long approvedCount;

    @JsonProperty("rejectedCount")
    public long rejectedCount;

    @JsonProperty("averageSimilarityScore")
    public BigDecimal averageSimilarityScore;

    @JsonProperty("approvedPercentage")
    public BigDecimal approvedPercentage;

    @JsonProperty("rejectedPercentage")
    public BigDecimal rejectedPercentage;

    public EvaluationStatsDTO() {
    }

    public EvaluationStatsDTO(long totalEvaluated, long approvedCount, long rejectedCount, BigDecimal averageSimilarityScore) {
        this.totalEvaluated = totalEvaluated;
        this.approvedCount = approvedCount;
        this.rejectedCount = rejectedCount;
        this.averageSimilarityScore = averageSimilarityScore != null
                ? averageSimilarityScore.setScale(4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        if (totalEvaluated > 0) {
            this.approvedPercentage = BigDecimal.valueOf(approvedCount)
                    .divide(BigDecimal.valueOf(totalEvaluated), 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            this.rejectedPercentage = BigDecimal.valueOf(rejectedCount)
                    .divide(BigDecimal.valueOf(totalEvaluated), 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        } else {
            this.approvedPercentage = BigDecimal.ZERO;
            this.rejectedPercentage = BigDecimal.ZERO;
        }
    }
}
