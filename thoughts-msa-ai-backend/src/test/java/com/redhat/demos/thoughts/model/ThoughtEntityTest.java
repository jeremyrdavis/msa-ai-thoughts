package com.redhat.demos.thoughts.model;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class ThoughtEntityTest {

    @Inject
    EntityManager entityManager;

    @Inject
    Validator validator;

    @Test
    @Transactional
    public void testThoughtPersistence() {
        Thought thought = new Thought();
        thought.content = "This is a positive thought that makes me happy and grateful";
        thought.persist();

        assertNotNull(thought.id);
        assertNotNull(thought.createdAt);
        assertNotNull(thought.updatedAt);
        assertEquals(0, thought.thumbsUp);
        assertEquals(0, thought.thumbsDown);
    }

    @Test
    @Transactional
    public void testThoughtUuidGeneration() {
        Thought thought = new Thought();
        thought.content = "UUID should be auto-generated for this thought";
        thought.persist();

        assertNotNull(thought.id);
        assertTrue(thought.id.toString().matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
    }

    @Test
    @Transactional
    public void testThoughtTimestamps() throws InterruptedException {
        Thought thought = new Thought();
        thought.content = "Testing timestamps on this positive thought";
        thought.persist();

        LocalDateTime createdTime = thought.createdAt;
        assertNotNull(createdTime);
        assertEquals(createdTime, thought.updatedAt);

        Thread.sleep(100);

        thought.content = "Updated content for timestamp testing";
        entityManager.merge(thought);
        entityManager.flush();

        assertTrue(thought.updatedAt.isAfter(createdTime));
    }

    @Test
    public void testContentValidation() {
        Thought thought = new Thought();
        thought.content = "";

        Set<ConstraintViolation<Thought>> violations = validator.validate(thought);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("cannot be blank")));
    }

    @Test
    public void testContentSizeValidation() {
        Thought thought = new Thought();
        thought.content = "Short";

        Set<ConstraintViolation<Thought>> violations = validator.validate(thought);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("between 10 and 500")));
    }

    @Test
    @Transactional
    public void testFindRandom() {
        Thought.deleteAll();

        Thought thought1 = new Thought();
        thought1.content = "First positive thought for random selection test";
        thought1.persist();

        Thought thought2 = new Thought();
        thought2.content = "Second positive thought for random selection test";
        thought2.persist();

        Optional<Thought> randomThought = Thought.findRandom();
        assertTrue(randomThought.isPresent());
        assertTrue(randomThought.get().id.equals(thought1.id) || randomThought.get().id.equals(thought2.id));
    }

    @Test
    @Transactional
    public void testFindRandomEmptyDatabase() {
        Thought.deleteAll();

        Optional<Thought> randomThought = Thought.findRandom();
        assertFalse(randomThought.isPresent());
    }
}
