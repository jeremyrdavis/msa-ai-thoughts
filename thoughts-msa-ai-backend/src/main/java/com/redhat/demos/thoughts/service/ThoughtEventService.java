package com.redhat.demos.thoughts.service;

import com.redhat.demos.thoughts.model.Thought;
import io.smallrye.reactive.messaging.MutinyEmitter;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ThoughtEventService {

    private static final Logger LOG = Logger.getLogger(ThoughtEventService.class);

    @Channel("thoughts-events")
    MutinyEmitter<Thought> thoughtsEmitter;

    public void publishThoughtCreated(Thought thought) {
        publishEvent(thought, "CREATED");
    }

    public void publishThoughtUpdated(Thought thought) {
        publishEvent(thought, "UPDATED");
    }

    public void publishThoughtDeleted(Thought thought) {
        publishEvent(thought, "DELETED");
    }

    private void publishEvent(Thought thought, String eventType) {
        thoughtsEmitter.sendAndAwait(thought);
        LOG.infof("Published %s event for thought: %s", eventType, thought.id);
    }
}
