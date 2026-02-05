package com.redhat.demos.thoughts.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Entity
@Table(name = "thoughts")
public class Thought extends PanacheEntityBase {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    public UUID id;

    @NotBlank(message = "Thought content cannot be blank")
    @Size(min = 10, max = 500, message = "Thought content must be between 10 and 500 characters")
    @Column(name = "content", nullable = false, length = 500)
    public String content;

    @Column(name = "thumbs_up", nullable = false)
    public int thumbsUp = 0;

    @Column(name = "thumbs_down", nullable = false)
    public int thumbsDown = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    public ThoughtStatus status;

    @Size(max = 200, message = "Author must be no more than 200 characters")
    @Column(name = "author", nullable = false, length = 200)
    public String author;

    @Size(max = 200, message = "Author bio must be no more than 200 characters")
    @Column(name = "author_bio", nullable = false, length = 200)
    public String authorBio;

    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    public LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = ThoughtStatus.IN_REVIEW;
        }
        if (this.author == null || this.author.trim().isEmpty()) {
            this.author = "Unknown";
        }
        if (this.authorBio == null || this.authorBio.trim().isEmpty()) {
            this.authorBio = "Unknown";
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public static Optional<Thought> findRandom() {
        long count = count();
        if (count == 0) {
            return Optional.empty();
        }

        int randomIndex = (int) (Math.random() * count);
        return Optional.ofNullable(findAll().page(randomIndex, 1).firstResult());
    }
}
