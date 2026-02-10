package com.redhat.demos.evaluation.service;

import com.redhat.demos.evaluation.model.EvaluationVector;
import com.redhat.demos.evaluation.model.ThoughtEvaluation;
import com.redhat.demos.evaluation.model.ThoughtStatus;
import com.redhat.demos.evaluation.model.VectorType;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class EvaluationServiceTest {

    @Inject
    EvaluationService evaluationService;

    @BeforeEach
    @Transactional
    public void seedVectors() {
        // Clear existing vectors
        EvaluationVector.deleteAll();

        // Add a negative vector (high negative values similar to seeded data)
        EvaluationVector negativeVector = new EvaluationVector();
        negativeVector.vectorType = VectorType.NEGATIVE;
        negativeVector.label = "Test negative vector";
        negativeVector.vectorData = createNegativeVectorJson();
        negativeVector.persist();

        // Add a positive vector
        EvaluationVector positiveVector = new EvaluationVector();
        positiveVector.vectorType = VectorType.POSITIVE;
        positiveVector.label = "Test positive vector";
        positiveVector.vectorData = createPositiveVectorJson();
        positiveVector.persist();
    }

    @Test
    @Transactional
    public void testEvaluateThought_ApprovedWhenBelowThreshold() {
        UUID thoughtId = UUID.randomUUID();
        String positiveContent = "I am grateful for this wonderful day and all the opportunities ahead";

        ThoughtEvaluation result = evaluationService.evaluateThought(thoughtId, positiveContent);

        assertNotNull(result);
        assertEquals(thoughtId, result.thoughtId);
        assertEquals(ThoughtStatus.APPROVED, result.status);
        assertNotNull(result.similarityScore);
        assertTrue(result.similarityScore.doubleValue() <= 0.85);
    }

    @Test
    @Transactional
    public void testEvaluateThought_RejectedWhenAboveThreshold() {
        UUID thoughtId = UUID.randomUUID();
        String negativeContent = "I hate everyone and everything is terrible and awful";

        ThoughtEvaluation result = evaluationService.evaluateThought(thoughtId, negativeContent);

        assertNotNull(result);
        assertEquals(thoughtId, result.thoughtId);
        assertEquals(ThoughtStatus.REJECTED, result.status);
        assertNotNull(result.similarityScore);
        assertTrue(result.similarityScore.doubleValue() > 0.85);
    }

    @Test
    public void testVectorSimilarityCalculation() {
        VectorSimilarityService similarityService = new VectorSimilarityService();

        float[] vector1 = {1.0f, 0.0f, 0.0f};
        float[] vector2 = {1.0f, 0.0f, 0.0f};

        double similarity = similarityService.calculateCosineSimilarity(vector1, vector2);

        assertEquals(1.0, similarity, 0.001);
    }

    @Test
    public void testVectorSimilarity_Orthogonal() {
        VectorSimilarityService similarityService = new VectorSimilarityService();

        float[] vector1 = {1.0f, 0.0f, 0.0f};
        float[] vector2 = {0.0f, 1.0f, 0.0f};

        double similarity = similarityService.calculateCosineSimilarity(vector1, vector2);

        assertEquals(0.0, similarity, 0.001);
    }

    @Test
    @Transactional
    public void testEvaluationPersistence() {
        UUID thoughtId = UUID.randomUUID();
        String content = "This is a test thought for persistence";

        ThoughtEvaluation result = evaluationService.evaluateThought(thoughtId, content);

        assertNotNull(result.id);
        assertNotNull(result.evaluatedAt);
        assertNotNull(result.metadata);
        assertTrue(result.metadata.contains("correlationId"));
        assertTrue(result.metadata.contains("threshold"));
    }

    /**
     * Creates a JSON representation of a negative vector.
     * This matches the pattern used by the mock embedding model for negative content.
     */
    private String createNegativeVectorJson() {
        StringBuilder json = new StringBuilder("{\"embedding\": [");
        for (int i = 0; i < 384; i++) {
            if (i > 0) json.append(", ");
            json.append(-0.09f + (Math.random() * 0.02f));
        }
        json.append("]}");
        return json.toString();
    }

    /**
     * Creates a JSON representation of a positive vector.
     */
    private String createPositiveVectorJson() {
        StringBuilder json = new StringBuilder("{\"embedding\": [");
        for (int i = 0; i < 384; i++) {
            if (i > 0) json.append(", ");
            json.append(Math.random() * 0.2f - 0.1f);
        }
        json.append("]}");
        return json.toString();
    }
}
