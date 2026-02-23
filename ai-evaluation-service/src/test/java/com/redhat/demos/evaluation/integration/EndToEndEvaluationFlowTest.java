package com.redhat.demos.evaluation.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.demos.evaluation.consumer.ThoughtEvaluationConsumer;
import com.redhat.demos.evaluation.dto.ThoughtEvent;
import com.redhat.demos.evaluation.model.EvaluationVector;
import com.redhat.demos.evaluation.model.ThoughtEvaluation;
import com.redhat.demos.evaluation.model.ThoughtStatus;
import com.redhat.demos.evaluation.model.VectorType;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end integration test for the complete evaluation flow:
 * Kafka event consumption -> embedding generation -> similarity calculation -> database persistence
 */
@QuarkusTest
public class EndToEndEvaluationFlowTest {

    @Inject
    ThoughtEvaluationConsumer consumer;

    @Inject
    ObjectMapper objectMapper;

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
    @Transactional
    public void testCompleteFlowFromKafkaEventToDatabasePersistence() throws Exception {
        // Arrange: Create a thought-created event
        UUID thoughtId = UUID.randomUUID();
        String thoughtContent = "I am grateful for this beautiful day and all the wonderful opportunities";

        ThoughtEvent event = new ThoughtEvent();
        event.setThoughtId(thoughtId);
        event.setThoughtContent(thoughtContent);
        event.setAuthor("Test Author");
        event.setAuthorBio("Test Bio");
        event.setStatus("IN_REVIEW");
        event.setCreatedAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());
        event.setEventType("CREATED");

        String eventJson = objectMapper.writeValueAsString(event);

        // Act: Consume the Kafka event
        consumer.consumeThoughtEvent(eventJson);

        // Assert: Verify evaluation was persisted
        List<ThoughtEvaluation> evaluations = ThoughtEvaluation.find("thoughtId", thoughtId).list();
        assertFalse(evaluations.isEmpty(), "Evaluation should be persisted in database");

        ThoughtEvaluation evaluation = evaluations.get(0);
        assertEquals(thoughtId, evaluation.thoughtId);
        assertEquals(ThoughtStatus.APPROVED, evaluation.status);
        assertNotNull(evaluation.similarityScore);
        assertNotNull(evaluation.evaluatedAt);
        assertNotNull(evaluation.metadata);
        assertTrue(evaluation.metadata.contains("correlationId"));
    }

    @Test
    @Transactional
    public void testCompleteFlowWithNegativeContent() throws Exception {
        // Arrange: Create event with negative content
        UUID thoughtId = UUID.randomUUID();
        String thoughtContent = "I hate everything and everyone is terrible and awful";

        ThoughtEvent event = new ThoughtEvent();
        event.setThoughtId(thoughtId);
        event.setThoughtContent(thoughtContent);
        event.setAuthor("Test Author");
        event.setAuthorBio("Test Bio");
        event.setStatus("IN_REVIEW");
        event.setCreatedAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());
        event.setEventType("CREATED");

        String eventJson = objectMapper.writeValueAsString(event);

        // Act: Consume the Kafka event
        consumer.consumeThoughtEvent(eventJson);

        // Assert: Verify evaluation marked as REJECTED
        List<ThoughtEvaluation> evaluations = ThoughtEvaluation.find("thoughtId", thoughtId).list();
        assertFalse(evaluations.isEmpty(), "Evaluation should be persisted in database");

        ThoughtEvaluation evaluation = evaluations.get(0);
        assertEquals(thoughtId, evaluation.thoughtId);
        assertEquals(ThoughtStatus.REJECTED, evaluation.status);
        assertNotNull(evaluation.similarityScore);
        assertTrue(evaluation.similarityScore.doubleValue() > 0.85,
            "Negative content should have high similarity to negative vectors");
    }

    @Test
    @Transactional
    public void testMultipleEventsProcessedSequentially() throws Exception {
        // Arrange: Create multiple events
        UUID thoughtId1 = UUID.randomUUID();
        UUID thoughtId2 = UUID.randomUUID();
        UUID thoughtId3 = UUID.randomUUID();

        ThoughtEvent event1 = createThoughtEvent(thoughtId1, "Positive thought 1");
        ThoughtEvent event2 = createThoughtEvent(thoughtId2, "I hate this");
        ThoughtEvent event3 = createThoughtEvent(thoughtId3, "Positive thought 2");

        // Act: Consume all events
        consumer.consumeThoughtEvent(objectMapper.writeValueAsString(event1));
        consumer.consumeThoughtEvent(objectMapper.writeValueAsString(event2));
        consumer.consumeThoughtEvent(objectMapper.writeValueAsString(event3));

        // Assert: Verify all evaluations persisted
        assertEquals(3, ThoughtEvaluation.count(), "All three evaluations should be persisted");

        ThoughtEvaluation eval1 = ThoughtEvaluation.find("thoughtId", thoughtId1).firstResult();
        ThoughtEvaluation eval2 = ThoughtEvaluation.find("thoughtId", thoughtId2).firstResult();
        ThoughtEvaluation eval3 = ThoughtEvaluation.find("thoughtId", thoughtId3).firstResult();

        assertNotNull(eval1);
        assertNotNull(eval2);
        assertNotNull(eval3);

        assertEquals(ThoughtStatus.APPROVED, eval1.status);
        assertEquals(ThoughtStatus.REJECTED, eval2.status);
        assertEquals(ThoughtStatus.APPROVED, eval3.status);
    }

    private ThoughtEvent createThoughtEvent(UUID thoughtId, String content) {
        ThoughtEvent event = new ThoughtEvent();
        event.setThoughtId(thoughtId);
        event.setThoughtContent(content);
        event.setAuthor("Test Author");
        event.setAuthorBio("Test Bio");
        event.setStatus("IN_REVIEW");
        event.setCreatedAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());
        event.setEventType("CREATED");
        return event;
    }

    private void seedTestVectors() {
        // Create negative vector
        EvaluationVector negativeVector = new EvaluationVector();
        negativeVector.vectorType = VectorType.NEGATIVE;
        negativeVector.label = "Test negative vector";
        negativeVector.vectorData = createNegativeVectorJson();
        negativeVector.persist();

        // Create positive vector
        EvaluationVector positiveVector = new EvaluationVector();
        positiveVector.vectorType = VectorType.POSITIVE;
        positiveVector.label = "Test positive vector";
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
