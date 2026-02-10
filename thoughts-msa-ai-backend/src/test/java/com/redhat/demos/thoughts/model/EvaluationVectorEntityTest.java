package com.redhat.demos.thoughts.model;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class EvaluationVectorEntityTest {

    @Inject
    EntityManager entityManager;

    @Inject
    Validator validator;

    @Test
    @Transactional
    public void testEvaluationVectorPersistence() {
        EvaluationVector vector = new EvaluationVector();
        vector.vectorData = "{\"embedding\": [0.1, 0.2, 0.3]}";
        vector.vectorType = VectorType.POSITIVE;
        vector.label = "Test positive vector";
        vector.persist();

        assertNotNull(vector.id);
        assertNotNull(vector.createdAt);
        assertEquals(VectorType.POSITIVE, vector.vectorType);
        assertEquals("Test positive vector", vector.label);
    }

    @Test
    @Transactional
    public void testVectorDataNotNull() {
        EvaluationVector vector = new EvaluationVector();
        vector.vectorType = VectorType.NEGATIVE;
        vector.label = "Test label";

        Set<ConstraintViolation<EvaluationVector>> violations = validator.validate(vector);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Vector data cannot be null")));
    }

    @Test
    @Transactional
    public void testVectorTypeEnumPersistence() {
        EvaluationVector vector = new EvaluationVector();
        vector.vectorData = "{\"embedding\": [0.5, 0.6, 0.7]}";
        vector.vectorType = VectorType.NEGATIVE;
        vector.label = "Test negative vector";
        vector.persist();

        entityManager.flush();
        entityManager.clear();

        EvaluationVector retrieved = EvaluationVector.findById(vector.id);
        assertNotNull(retrieved);
        assertEquals(VectorType.NEGATIVE, retrieved.vectorType);
    }

    @Test
    @Transactional
    public void testLabelValidation() {
        EvaluationVector vector = new EvaluationVector();
        vector.vectorData = "{\"embedding\": [0.1, 0.2]}";
        vector.vectorType = VectorType.POSITIVE;

        Set<ConstraintViolation<EvaluationVector>> violations = validator.validate(vector);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Label cannot be null")));
    }
}
