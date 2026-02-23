package com.redhat.demos.evaluation.integration;

import com.redhat.demos.evaluation.model.EvaluationVector;
import com.redhat.demos.evaluation.model.ThoughtEvaluation;
import com.redhat.demos.evaluation.model.ThoughtStatus;
import com.redhat.demos.evaluation.model.VectorType;
import com.redhat.demos.evaluation.service.EvaluationService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for threshold configuration loading from application.properties/ConfigMap.
 * Tests that the similarity threshold is properly loaded and affects evaluation results.
 */
@QuarkusTest
public class ThresholdConfigurationTest {

    @Inject
    EvaluationService evaluationService;

    @ConfigProperty(name = "evaluation.similarity.threshold", defaultValue = "0.85")
    double configuredThreshold;

    @BeforeEach
    @Transactional
    public void setup() {
        // Clean up test data
        ThoughtEvaluation.deleteAll();
        EvaluationVector.deleteAll();

        // Seed test vectors
        seedTestVectors();
    }

    @Test
    public void testDefaultThresholdIsLoaded() {
        // Assert: Verify default threshold of 0.85 is loaded
        assertEquals(0.85, configuredThreshold, 0.001,
            "Default threshold should be 0.85");
    }

    @Test
    @Transactional
    public void testThresholdAffectsEvaluationResult() {
        // Arrange: Create content that will have moderate similarity
        UUID thoughtId = UUID.randomUUID();
        String content = "This content has some negative aspects but also positive ones";

        // Act: Evaluate the thought
        ThoughtEvaluation evaluation = evaluationService.evaluateThought(thoughtId, content);

        // Assert: Verify evaluation metadata contains the threshold
        assertNotNull(evaluation.metadata);
        assertTrue(evaluation.metadata.contains("threshold"),
            "Metadata should contain threshold configuration");
        assertTrue(evaluation.metadata.contains("0.85"),
            "Metadata should contain threshold value 0.85");
    }

    @Test
    @Transactional
    public void testEvaluationUsesConfiguredThreshold() {
        // Arrange: Create clearly positive content
        UUID thoughtId = UUID.randomUUID();
        String positiveContent = "I am grateful for all the wonderful opportunities and positive experiences";

        // Act: Evaluate the thought
        ThoughtEvaluation evaluation = evaluationService.evaluateThought(thoughtId, positiveContent);

        // Assert: Verify evaluation respects threshold
        assertNotNull(evaluation);
        assertNotNull(evaluation.similarityScore);

        // If similarity is below threshold, should be APPROVED
        if (evaluation.similarityScore.doubleValue() <= configuredThreshold) {
            assertEquals(ThoughtStatus.APPROVED, evaluation.status,
                "Thoughts below threshold should be APPROVED");
        } else {
            assertEquals(ThoughtStatus.REJECTED, evaluation.status,
                "Thoughts above threshold should be REJECTED");
        }
    }

    @Test
    @Transactional
    public void testMetadataIncludesConfigurationDetails() {
        // Arrange
        UUID thoughtId = UUID.randomUUID();
        String content = "Test content for metadata verification";

        // Act: Evaluate the thought
        ThoughtEvaluation evaluation = evaluationService.evaluateThought(thoughtId, content);

        // Assert: Verify metadata structure
        assertNotNull(evaluation.metadata);
        assertTrue(evaluation.metadata.contains("correlationId"),
            "Metadata should include correlation ID");
        assertTrue(evaluation.metadata.contains("threshold"),
            "Metadata should include threshold value");
        assertTrue(evaluation.metadata.contains("evaluationTimestamp"),
            "Metadata should include timestamp");
        assertTrue(evaluation.metadata.contains("modelName"),
            "Metadata should include model name");
    }

    private void seedTestVectors() {
        // Create negative vector
        EvaluationVector negativeVector = new EvaluationVector();
        negativeVector.vectorType = VectorType.NEGATIVE;
        negativeVector.label = "Test negative vector for threshold testing";
        negativeVector.vectorData = createNegativeVectorJson();
        negativeVector.persist();

        // Create positive vector
        EvaluationVector positiveVector = new EvaluationVector();
        positiveVector.vectorType = VectorType.POSITIVE;
        positiveVector.label = "Test positive vector for threshold testing";
        positiveVector.vectorData = createPositiveVectorJson();
        positiveVector.persist();
    }

    private String createNegativeVectorJson() {
        StringBuilder json = new StringBuilder("{\"embedding\": [");
        for (int i = 0; i < 384; i++) {
            if (i > 0) json.append(", ");
            json.append(-0.09f + (Math.random() * 0.02f));
        }
        json.append("]}");
        return json.toString();
    }

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
