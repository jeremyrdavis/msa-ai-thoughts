package com.redhat.demos.evaluation.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.demos.evaluation.consumer.ThoughtEvaluationConsumer;
import com.redhat.demos.evaluation.dto.ThoughtEvent;
import com.redhat.demos.evaluation.model.EvaluationVector;
import com.redhat.demos.evaluation.model.ThoughtEvaluation;
import com.redhat.demos.evaluation.model.ThoughtStatus;
import com.redhat.demos.evaluation.service.EvaluationService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Integration test for error handling and consumer resilience.
 * Tests that malformed events and service failures are handled gracefully.
 */
@QuarkusTest
public class ErrorHandlingIntegrationTest {

    @Inject
    ThoughtEvaluationConsumer consumer;

    @InjectMock
    EvaluationService evaluationService;

    @Inject
    ObjectMapper objectMapper;

    @BeforeEach
    @Transactional
    public void setup() {
        // Clean up test data
        ThoughtEvaluation.deleteAll();
        EvaluationVector.deleteAll();
    }

    @Test
    public void testConsumerContinuesProcessingAfterError() throws Exception {
        // Arrange: Create multiple events, one will fail
        UUID thoughtId1 = UUID.randomUUID();
        UUID thoughtId2 = UUID.randomUUID();
        UUID thoughtId3 = UUID.randomUUID();

        ThoughtEvent event1 = createThoughtEvent(thoughtId1, "First thought");
        ThoughtEvent event2 = createThoughtEvent(thoughtId2, "Second thought");
        ThoughtEvent event3 = createThoughtEvent(thoughtId3, "Third thought");

        ThoughtEvaluation mockEval = createMockEvaluation(thoughtId1);

        // Mock service to succeed on first, fail on second, succeed on third
        when(evaluationService.evaluateThought(eq(thoughtId1), any())).thenReturn(mockEval);
        when(evaluationService.evaluateThought(eq(thoughtId2), any()))
            .thenThrow(new RuntimeException("Evaluation failed"));
        when(evaluationService.evaluateThought(eq(thoughtId3), any())).thenReturn(mockEval);

        // Act: Consume all events
        assertDoesNotThrow(() -> consumer.consumeThoughtEvent(objectMapper.writeValueAsString(event1)));
        assertDoesNotThrow(() -> consumer.consumeThoughtEvent(objectMapper.writeValueAsString(event2)));
        assertDoesNotThrow(() -> consumer.consumeThoughtEvent(objectMapper.writeValueAsString(event3)));

        // Assert: Verify all three events were attempted to be processed
        verify(evaluationService, times(1)).evaluateThought(thoughtId1, "First thought");
        verify(evaluationService, times(1)).evaluateThought(thoughtId2, "Second thought");
        verify(evaluationService, times(1)).evaluateThought(thoughtId3, "Third thought");
    }

    @Test
    public void testMalformedJsonHandledGracefully() {
        // Arrange: Create malformed JSON
        String malformedJson1 = "{invalid json}";
        String malformedJson2 = "{\"thoughtId\": \"not-a-uuid\"}";
        String malformedJson3 = "";

        // Act & Assert: Consumer should not throw exceptions
        assertDoesNotThrow(() -> consumer.consumeThoughtEvent(malformedJson1));
        assertDoesNotThrow(() -> consumer.consumeThoughtEvent(malformedJson2));
        assertDoesNotThrow(() -> consumer.consumeThoughtEvent(malformedJson3));

        // Verify evaluation service was never called
        verify(evaluationService, never()).evaluateThought(any(), any());
    }

    @Test
    public void testNullAndEmptyContentHandledGracefully() throws Exception {
        // Arrange: Create events with null/empty content
        UUID thoughtId1 = UUID.randomUUID();
        UUID thoughtId2 = UUID.randomUUID();
        UUID thoughtId3 = UUID.randomUUID();

        ThoughtEvent event1 = createThoughtEvent(thoughtId1, null);
        ThoughtEvent event2 = createThoughtEvent(thoughtId2, "");
        ThoughtEvent event3 = createThoughtEvent(thoughtId3, "   ");

        String json1 = objectMapper.writeValueAsString(event1);
        String json2 = objectMapper.writeValueAsString(event2);
        String json3 = objectMapper.writeValueAsString(event3);

        // Act: Consume events with invalid content
        assertDoesNotThrow(() -> consumer.consumeThoughtEvent(json1));
        assertDoesNotThrow(() -> consumer.consumeThoughtEvent(json2));
        assertDoesNotThrow(() -> consumer.consumeThoughtEvent(json3));

        // Assert: Evaluation service should not be called for invalid content
        verify(evaluationService, never()).evaluateThought(any(), any());
    }

    @Test
    public void testMissingRequiredFieldsHandledGracefully() throws Exception {
        // Arrange: Create events with missing required fields
        ThoughtEvent eventNoId = new ThoughtEvent();
        eventNoId.setThoughtContent("Content without ID");
        eventNoId.setEventType("CREATED");

        ThoughtEvent eventNoContent = new ThoughtEvent();
        eventNoContent.setThoughtId(UUID.randomUUID());
        eventNoContent.setEventType("CREATED");

        String json1 = objectMapper.writeValueAsString(eventNoId);
        String json2 = objectMapper.writeValueAsString(eventNoContent);

        // Act: Consume events with missing fields
        assertDoesNotThrow(() -> consumer.consumeThoughtEvent(json1));
        assertDoesNotThrow(() -> consumer.consumeThoughtEvent(json2));

        // Assert: Evaluation service should not be called
        verify(evaluationService, never()).evaluateThought(any(), any());
    }

    @Test
    public void testConsumerRecoveryAfterMultipleFailures() throws Exception {
        // Arrange: Create sequence of failing and succeeding events
        UUID thoughtId1 = UUID.randomUUID();
        UUID thoughtId2 = UUID.randomUUID();
        UUID thoughtId3 = UUID.randomUUID();
        UUID thoughtId4 = UUID.randomUUID();

        ThoughtEvent event1 = createThoughtEvent(thoughtId1, "Thought 1");
        ThoughtEvent event2 = createThoughtEvent(thoughtId2, "Thought 2");
        ThoughtEvent event3 = createThoughtEvent(thoughtId3, "Thought 3");
        ThoughtEvent event4 = createThoughtEvent(thoughtId4, "Thought 4");

        ThoughtEvaluation mockEval = createMockEvaluation(thoughtId1);

        // Mock service to fail on first two, succeed on last two
        when(evaluationService.evaluateThought(eq(thoughtId1), any()))
            .thenThrow(new RuntimeException("Fail 1"));
        when(evaluationService.evaluateThought(eq(thoughtId2), any()))
            .thenThrow(new RuntimeException("Fail 2"));
        when(evaluationService.evaluateThought(eq(thoughtId3), any())).thenReturn(mockEval);
        when(evaluationService.evaluateThought(eq(thoughtId4), any())).thenReturn(mockEval);

        // Act: Process all events
        consumer.consumeThoughtEvent(objectMapper.writeValueAsString(event1));
        consumer.consumeThoughtEvent(objectMapper.writeValueAsString(event2));
        consumer.consumeThoughtEvent(objectMapper.writeValueAsString(event3));
        consumer.consumeThoughtEvent(objectMapper.writeValueAsString(event4));

        // Assert: Verify consumer recovered and processed all events
        verify(evaluationService, times(4)).evaluateThought(any(UUID.class), anyString());
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

    private ThoughtEvaluation createMockEvaluation(UUID thoughtId) {
        ThoughtEvaluation evaluation = new ThoughtEvaluation();
        evaluation.id = UUID.randomUUID();
        evaluation.thoughtId = thoughtId;
        evaluation.status = ThoughtStatus.APPROVED;
        evaluation.similarityScore = new BigDecimal("0.75");
        evaluation.metadata = "{}";
        return evaluation;
    }
}
