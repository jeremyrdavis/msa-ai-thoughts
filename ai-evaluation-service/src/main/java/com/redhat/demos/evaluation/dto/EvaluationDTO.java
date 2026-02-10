package com.redhat.demos.evaluation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.redhat.demos.evaluation.model.ThoughtStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class EvaluationDTO {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("thoughtId")
    private UUID thoughtId;

    @JsonProperty("thoughtContent")
    private String thoughtContent;

    @JsonProperty("status")
    private ThoughtStatus status;

    @JsonProperty("similarityScore")
    private BigDecimal similarityScore;

    @JsonProperty("evaluatedAt")
    private LocalDateTime evaluatedAt;

    public EvaluationDTO() {
    }

    public EvaluationDTO(UUID id, UUID thoughtId, String thoughtContent, ThoughtStatus status,
                         BigDecimal similarityScore, LocalDateTime evaluatedAt) {
        this.id = id;
        this.thoughtId = thoughtId;
        this.thoughtContent = thoughtContent;
        this.status = status;
        this.similarityScore = similarityScore;
        this.evaluatedAt = evaluatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getThoughtId() {
        return thoughtId;
    }

    public void setThoughtId(UUID thoughtId) {
        this.thoughtId = thoughtId;
    }

    public String getThoughtContent() {
        return thoughtContent;
    }

    public void setThoughtContent(String thoughtContent) {
        this.thoughtContent = thoughtContent;
    }

    public ThoughtStatus getStatus() {
        return status;
    }

    public void setStatus(ThoughtStatus status) {
        this.status = status;
    }

    public BigDecimal getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(BigDecimal similarityScore) {
        this.similarityScore = similarityScore;
    }

    public LocalDateTime getEvaluatedAt() {
        return evaluatedAt;
    }

    public void setEvaluatedAt(LocalDateTime evaluatedAt) {
        this.evaluatedAt = evaluatedAt;
    }

    public static class Builder {
        private UUID id;
        private UUID thoughtId;
        private String thoughtContent;
        private ThoughtStatus status;
        private BigDecimal similarityScore;
        private LocalDateTime evaluatedAt;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder thoughtId(UUID thoughtId) {
            this.thoughtId = thoughtId;
            return this;
        }

        public Builder thoughtContent(String thoughtContent) {
            this.thoughtContent = thoughtContent;
            return this;
        }

        public Builder status(ThoughtStatus status) {
            this.status = status;
            return this;
        }

        public Builder similarityScore(BigDecimal similarityScore) {
            this.similarityScore = similarityScore;
            return this;
        }

        public Builder evaluatedAt(LocalDateTime evaluatedAt) {
            this.evaluatedAt = evaluatedAt;
            return this;
        }

        public EvaluationDTO build() {
            return new EvaluationDTO(id, thoughtId, thoughtContent, status, similarityScore, evaluatedAt);
        }
    }
}
