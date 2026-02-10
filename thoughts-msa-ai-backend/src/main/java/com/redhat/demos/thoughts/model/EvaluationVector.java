package com.redhat.demos.thoughts.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "evaluation_vectors")
public class EvaluationVector extends PanacheEntityBase {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    public UUID id;

    @NotNull(message = "Vector data cannot be null")
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "vector_data", nullable = false, columnDefinition = "jsonb")
    public String vectorData;

    @NotNull(message = "Vector type cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "vector_type", nullable = false)
    public VectorType vectorType;

    @NotNull(message = "Label cannot be null")
    @Size(min = 1, max = 255, message = "Label must be between 1 and 255 characters")
    @Column(name = "label", nullable = false, length = 255)
    public String label;

    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
