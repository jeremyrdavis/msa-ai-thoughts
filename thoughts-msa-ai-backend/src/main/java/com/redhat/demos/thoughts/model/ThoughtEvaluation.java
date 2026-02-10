package com.redhat.demos.thoughts.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "thought_evaluations")
public class ThoughtEvaluation extends PanacheEntityBase {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    public UUID id;

    @NotNull(message = "Thought ID cannot be null")
    @Column(name = "thought_id", nullable = false)
    public UUID thoughtId;

    @NotNull(message = "Status cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    public ThoughtStatus status;

    @NotNull(message = "Similarity score cannot be null")
    @Column(name = "similarity_score", nullable = false, precision = 5, scale = 4)
    public BigDecimal similarityScore;

    @Column(name = "evaluated_at", nullable = false, updatable = false)
    public LocalDateTime evaluatedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    public String metadata;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thought_id", insertable = false, updatable = false)
    public Thought thought;

    @PrePersist
    public void prePersist() {
        this.evaluatedAt = LocalDateTime.now();
    }
}
