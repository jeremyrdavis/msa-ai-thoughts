package com.redhat.demos.thoughts.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.redhat.demos.thoughts.model.ThoughtStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class EvaluationDTO {

    @JsonProperty("id")
    public UUID id;

    @JsonProperty("thoughtId")
    public UUID thoughtId;

    @JsonProperty("thoughtContent")
    public String thoughtContent;

    @JsonProperty("status")
    public ThoughtStatus status;

    @JsonProperty("similarityScore")
    public BigDecimal similarityScore;

    @JsonProperty("evaluatedAt")
    public LocalDateTime evaluatedAt;

    public EvaluationDTO() {
    }

    public EvaluationDTO(UUID id, UUID thoughtId, String thoughtContent, ThoughtStatus status, BigDecimal similarityScore, LocalDateTime evaluatedAt) {
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
