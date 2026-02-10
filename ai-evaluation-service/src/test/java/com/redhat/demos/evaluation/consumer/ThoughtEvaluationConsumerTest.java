package com.redhat.demos.evaluation.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.demos.evaluation.dto.ThoughtEvent;
import com.redhat.demos.evaluation.service.EvaluationService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for ThoughtEvaluationConsumer.
 * Focused on testing event consumption, filtering, and error handling.
 */
@QuarkusTest
class ThoughtEvaluationConsumerTest {

    @Inject
    ThoughtEvaluationConsumer consumer;

    @InjectMock
    EvaluationService evaluationService;

    @Inject
    ObjectMapper objectMapper;

    @Test
    void testConsumeValidThoughtCreatedEvent() throws Exception {
        // Arrange
        UUID thoughtId = UUID.randomUUID();
        String thoughtContent = "This is a positive thought about the future";

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

        // Act
        consumer.consumeThoughtEvent(eventJson);

        // Assert
        ArgumentCaptor<UUID> idCaptor = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);

        verify(evaluationService, times(1)).evaluateThought(idCaptor.capture(), contentCaptor.capture());
        assertEquals(thoughtId, idCaptor.getValue());
        assertEquals(thoughtContent, contentCaptor.getValue());
    }

    @Test
    void testConsumeEventWithMalformedJson() {
        // Arrange
        String malformedJson = "{invalid json}";

        // Act
        consumer.consumeThoughtEvent(malformedJson);

        // Assert - consumer should handle gracefully without calling evaluation service
        verify(evaluationService, never()).evaluateThought(any(), any());
    }

    @Test
    void testConsumeEventWithMissingThoughtId() throws Exception {
        // Arrange
        ThoughtEvent event = new ThoughtEvent();
        event.setThoughtContent("Some content");
        event.setEventType("CREATED");

        String eventJson = objectMapper.writeValueAsString(event);

        // Act
        consumer.consumeThoughtEvent(eventJson);

        // Assert - should not call evaluation service when thoughtId is missing
        verify(evaluationService, never()).evaluateThought(any(), any());
    }

    @Test
    void testConsumeEventWithMissingContent() throws Exception {
        // Arrange
        UUID thoughtId = UUID.randomUUID();
        ThoughtEvent event = new ThoughtEvent();
        event.setThoughtId(thoughtId);
        event.setEventType("CREATED");

        String eventJson = objectMapper.writeValueAsString(event);

        // Act
        consumer.consumeThoughtEvent(eventJson);

        // Assert - should not call evaluation service when content is missing
        verify(evaluationService, never()).evaluateThought(any(), any());
    }

    @Test
    void testConsumeEventWithEmptyContent() throws Exception {
        // Arrange
        UUID thoughtId = UUID.randomUUID();
        ThoughtEvent event = new ThoughtEvent();
        event.setThoughtId(thoughtId);
        event.setThoughtContent("   ");  // Empty/whitespace only
        event.setEventType("CREATED");

        String eventJson = objectMapper.writeValueAsString(event);

        // Act
        consumer.consumeThoughtEvent(eventJson);

        // Assert - should not call evaluation service when content is empty
        verify(evaluationService, never()).evaluateThought(any(), any());
    }

    @Test
    void testConsumeEventWithEvaluationServiceException() throws Exception {
        // Arrange
        UUID thoughtId = UUID.randomUUID();
        String thoughtContent = "Test content";

        ThoughtEvent event = new ThoughtEvent();
        event.setThoughtId(thoughtId);
        event.setThoughtContent(thoughtContent);
        event.setEventType("CREATED");

        String eventJson = objectMapper.writeValueAsString(event);

        // Mock evaluation service to throw exception
        doThrow(new RuntimeException("Evaluation failed")).when(evaluationService)
            .evaluateThought(any(), any());

        // Act - should not rethrow exception
        assertDoesNotThrow(() -> consumer.consumeThoughtEvent(eventJson));

        // Assert - evaluation service was called despite exception
        verify(evaluationService, times(1)).evaluateThought(thoughtId, thoughtContent);
    }

    @Test
    void testExtractThoughtIdAndContentCorrectly() throws Exception {
        // Arrange
        UUID expectedId = UUID.randomUUID();
        String expectedContent = "Specific thought content for extraction test";

        ThoughtEvent event = new ThoughtEvent();
        event.setThoughtId(expectedId);
        event.setThoughtContent(expectedContent);
        event.setAuthor("Author Name");
        event.setAuthorBio("Bio");
        event.setEventType("CREATED");

        String eventJson = objectMapper.writeValueAsString(event);

        // Act
        consumer.consumeThoughtEvent(eventJson);

        // Assert
        ArgumentCaptor<UUID> idCaptor = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);

        verify(evaluationService, times(1)).evaluateThought(idCaptor.capture(), contentCaptor.capture());
        assertEquals(expectedId, idCaptor.getValue());
        assertEquals(expectedContent, contentCaptor.getValue());
    }

    @Test
    void testConsumeMultipleEventsSequentially() throws Exception {
        // Arrange
        UUID thoughtId1 = UUID.randomUUID();
        UUID thoughtId2 = UUID.randomUUID();

        ThoughtEvent event1 = new ThoughtEvent();
        event1.setThoughtId(thoughtId1);
        event1.setThoughtContent("First thought");
        event1.setEventType("CREATED");

        ThoughtEvent event2 = new ThoughtEvent();
        event2.setThoughtId(thoughtId2);
        event2.setThoughtContent("Second thought");
        event2.setEventType("CREATED");

        String eventJson1 = objectMapper.writeValueAsString(event1);
        String eventJson2 = objectMapper.writeValueAsString(event2);

        // Act
        consumer.consumeThoughtEvent(eventJson1);
        consumer.consumeThoughtEvent(eventJson2);

        // Assert
        verify(evaluationService, times(2)).evaluateThought(any(UUID.class), anyString());
    }
}
