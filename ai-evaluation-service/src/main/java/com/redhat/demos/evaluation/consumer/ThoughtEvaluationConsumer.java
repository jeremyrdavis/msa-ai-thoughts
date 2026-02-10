package com.redhat.demos.evaluation.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.demos.evaluation.dto.ThoughtEvent;
import com.redhat.demos.evaluation.service.EvaluationService;
import io.smallrye.reactive.messaging.annotations.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

import java.util.UUID;

/**
 * Kafka consumer for thought events.
 * Listens to the thoughts-events topic and processes thought-created events.
 * Follows the pattern from ThoughtEventService (producer) in reverse.
 */
@ApplicationScoped
public class ThoughtEvaluationConsumer {

    private static final Logger LOG = Logger.getLogger(ThoughtEvaluationConsumer.class);
    private static final String EVENT_TYPE_CREATED = "CREATED";

    @Inject
    EvaluationService evaluationService;

    @Inject
    ObjectMapper objectMapper;

    /**
     * Consumes thought events from Kafka and processes thought-created events only.
     * Events are received as JSON strings and deserialized to ThoughtEvent objects.
     *
     * @param eventJson the raw JSON event string from Kafka
     */
    @Incoming("thoughts-events")
    @Blocking
    public void consumeThoughtEvent(String eventJson) {
        UUID correlationId = UUID.randomUUID();

        try {
            LOG.debugf("[%s] Received event: %s", correlationId, eventJson);

            // Deserialize the event
            ThoughtEvent event = deserializeEvent(eventJson);

            if (event == null) {
                LOG.warnf("[%s] Failed to deserialize event, skipping", correlationId);
                return;
            }

            // Filter for thought-created events only
            if (!isCreatedEvent(event)) {
                LOG.debugf("[%s] Ignoring non-created event type: %s for thought: %s",
                    correlationId, event.getEventType(), event.getThoughtId());
                return;
            }

            // Extract thought ID and content
            UUID thoughtId = event.getThoughtId();
            String thoughtContent = event.getThoughtContent();

            if (thoughtId == null || thoughtContent == null || thoughtContent.trim().isEmpty()) {
                LOG.warnf("[%s] Event missing required fields (thoughtId or content), skipping", correlationId);
                return;
            }

            LOG.infof("[%s] Processing thought-created event for thought: %s", correlationId, thoughtId);

            // Delegate to evaluation service
            evaluationService.evaluateThought(thoughtId, thoughtContent);

            LOG.infof("[%s] Successfully processed thought: %s", correlationId, thoughtId);

        } catch (Exception e) {
            // Log error but don't rethrow - consumer should continue processing subsequent messages
            LOG.errorf(e, "[%s] Error processing thought event: %s", correlationId, e.getMessage());
        }
    }

    /**
     * Deserializes the JSON event string into a ThoughtEvent object.
     * Handles malformed JSON gracefully by returning null.
     *
     * @param eventJson the JSON string to deserialize
     * @return the deserialized ThoughtEvent or null if deserialization fails
     */
    private ThoughtEvent deserializeEvent(String eventJson) {
        try {
            return objectMapper.readValue(eventJson, ThoughtEvent.class);
        } catch (Exception e) {
            LOG.warnf("Failed to deserialize event JSON: %s - Error: %s", eventJson, e.getMessage());
            return null;
        }
    }

    /**
     * Checks if the event is a thought-created event.
     * The event type might be in the event metadata or inferred from the presence of createdAt field.
     *
     * @param event the event to check
     * @return true if this is a created event, false otherwise
     */
    private boolean isCreatedEvent(ThoughtEvent event) {
        // Check explicit event type if present
        if (event.getEventType() != null) {
            return EVENT_TYPE_CREATED.equalsIgnoreCase(event.getEventType());
        }

        // Since ThoughtEventService doesn't include explicit eventType in the payload,
        // we treat all events as potential created events and rely on the topic/channel filtering
        // In a real scenario, we might check if createdAt equals updatedAt or use message headers
        return true;
    }
}
