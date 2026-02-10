package com.redhat.demos.thoughts.model;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class ThoughtEvaluationEntityTest {

    @Inject
    EntityManager entityManager;

    @Inject
    Validator validator;

    @Test
    @Transactional
    public void testThoughtEvaluationPersistence() {
        // Create a thought first
        Thought thought = new Thought();
        thought.content = "This is a test thought for evaluation";
        thought.persist();

        ThoughtEvaluation evaluation = new ThoughtEvaluation();
        evaluation.thoughtId = thought.id;
        evaluation.status = ThoughtStatus.APPROVED;
        evaluation.similarityScore = new BigDecimal("0.7500");
        evaluation.metadata = "{\"model\": \"test-model\"}";
        evaluation.persist();

        assertNotNull(evaluation.id);
        assertNotNull(evaluation.evaluatedAt);
        assertEquals(ThoughtStatus.APPROVED, evaluation.status);
        assertEquals(0, evaluation.similarityScore.compareTo(new BigDecimal("0.7500")));
    }

    @Test
    @Transactional
    public void testThoughtEvaluationRelationship() {
        // Create a thought first
        Thought thought = new Thought();
        thought.content = "This is another test thought for relationship testing";
        thought.persist();

        ThoughtEvaluation evaluation = new ThoughtEvaluation();
        evaluation.thoughtId = thought.id;
        evaluation.status = ThoughtStatus.REJECTED;
        evaluation.similarityScore = new BigDecimal("0.9200");
        evaluation.persist();

        entityManager.flush();
        entityManager.clear();

        ThoughtEvaluation retrieved = ThoughtEvaluation.findById(evaluation.id);
        assertNotNull(retrieved);
        assertEquals(thought.id, retrieved.thoughtId);
        assertNotNull(retrieved.thought);
        assertEquals(thought.content, retrieved.thought.content);
    }

    @Test
    @Transactional
    public void testThoughtIdNotNull() {
        ThoughtEvaluation evaluation = new ThoughtEvaluation();
        evaluation.status = ThoughtStatus.APPROVED;
        evaluation.similarityScore = new BigDecimal("0.5000");

        Set<ConstraintViolation<ThoughtEvaluation>> violations = validator.validate(evaluation);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Thought ID cannot be null")));
    }

    @Test
    @Transactional
    public void testStatusEnumPersistence() {
        // Create a thought first
        Thought thought = new Thought();
        thought.content = "Testing status enum persistence in evaluation";
        thought.persist();

        ThoughtEvaluation evaluation = new ThoughtEvaluation();
        evaluation.thoughtId = thought.id;
        evaluation.status = ThoughtStatus.REJECTED;
        evaluation.similarityScore = new BigDecimal("0.8800");
        evaluation.persist();

        entityManager.flush();
        entityManager.clear();

        ThoughtEvaluation retrieved = ThoughtEvaluation.findById(evaluation.id);
        assertNotNull(retrieved);
        assertEquals(ThoughtStatus.REJECTED, retrieved.status);
    }
}
