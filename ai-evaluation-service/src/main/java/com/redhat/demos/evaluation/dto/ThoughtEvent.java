package com.redhat.demos.evaluation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for thought events consumed from Kafka.
 * Represents the event structure published by ThoughtEventService in thoughts-msa-ai-backend.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ThoughtEvent {

    @JsonProperty("id")
    private UUID thoughtId;

    @JsonProperty("content")
    private String thoughtContent;

    @JsonProperty("author")
    private String author;

    @JsonProperty("authorBio")
    private String authorBio;

    @JsonProperty("status")
    private String status;

    @JsonProperty("thumbsUp")
    private Integer thumbsUp;

    @JsonProperty("thumbsDown")
    private Integer thumbsDown;

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;

    // Event metadata - not part of Thought entity but useful for filtering
    private String eventType;
    private LocalDateTime timestamp;

    public ThoughtEvent() {
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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthorBio() {
        return authorBio;
    }

    public void setAuthorBio(String authorBio) {
        this.authorBio = authorBio;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getThumbsUp() {
        return thumbsUp;
    }

    public void setThumbsUp(Integer thumbsUp) {
        this.thumbsUp = thumbsUp;
    }

    public Integer getThumbsDown() {
        return thumbsDown;
    }

    public void setThumbsDown(Integer thumbsDown) {
        this.thumbsDown = thumbsDown;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "ThoughtEvent{" +
                "thoughtId=" + thoughtId +
                ", eventType='" + eventType + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
