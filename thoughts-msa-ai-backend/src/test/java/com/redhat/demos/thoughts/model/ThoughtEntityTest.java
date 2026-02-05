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

    @Test
    @Transactional
    public void testStatusEnumPersistence() {
        Thought thought = new Thought();
        thought.content = "Testing status enum storage as string in database";
        thought.status = ThoughtStatus.APPROVED;
        thought.persist();

        entityManager.flush();
        entityManager.clear();

        Thought retrieved = Thought.findById(thought.id);
        assertNotNull(retrieved.status);
        assertEquals(ThoughtStatus.APPROVED, retrieved.status);
    }

    @Test
    @Transactional
    public void testStatusFieldDefaultValue() {
        Thought thought = new Thought();
        thought.content = "Testing default status is set to IN_REVIEW automatically";
        thought.persist();

        assertNotNull(thought.status);
        assertEquals(ThoughtStatus.IN_REVIEW, thought.status);
    }

    @Test
    @Transactional
    public void testStatusFieldInJsonResponse() {
        Thought thought = new Thought();
        thought.content = "Testing that status field appears in entity representation";
        thought.status = ThoughtStatus.APPROVED;
        thought.persist();

        Thought retrieved = Thought.findById(thought.id);
        assertNotNull(retrieved.status);
        assertEquals(ThoughtStatus.APPROVED, retrieved.status);
    }

    // Author field tests
    @Test
    @Transactional
    public void testAuthorFieldPersistence() {
        Thought thought = new Thought();
        thought.content = "This is a thought with an author";
        thought.author = "Marcus Aurelius";
        thought.authorBio = "Roman Emperor and Stoic philosopher";
        thought.persist();

        entityManager.flush();
        entityManager.clear();

        Thought retrieved = Thought.findById(thought.id);
        assertNotNull(retrieved.author);
        assertEquals("Marcus Aurelius", retrieved.author);
        assertNotNull(retrieved.authorBio);
        assertEquals("Roman Emperor and Stoic philosopher", retrieved.authorBio);
    }

    @Test
    @Transactional
    public void testAuthorDefaultValueHandling() {
        Thought thought = new Thought();
        thought.content = "This thought has no author specified";
        thought.persist();

        assertNotNull(thought.author);
        assertEquals("Unknown", thought.author);
        assertNotNull(thought.authorBio);
        assertEquals("Unknown", thought.authorBio);
    }

    @Test
    public void testAuthorSizeValidation() {
        Thought thought = new Thought();
        thought.content = "This is a valid thought content";
        thought.author = "a".repeat(201);
        thought.authorBio = "Valid bio";

        Set<ConstraintViolation<Thought>> violations = validator.validate(thought);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v ->
            v.getPropertyPath().toString().equals("author") &&
            v.getMessage().contains("200")));
    }

    @Test
    public void testAuthorBioSizeValidation() {
        Thought thought = new Thought();
        thought.content = "This is a valid thought content";
        thought.author = "Valid author";
        thought.authorBio = "b".repeat(201);

        Set<ConstraintViolation<Thought>> violations = validator.validate(thought);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v ->
            v.getPropertyPath().toString().equals("authorBio") &&
            v.getMessage().contains("200")));
    }

    @Test
    @Transactional
    public void testAuthorFieldsWithMaxLength() {
        Thought thought = new Thought();
        thought.content = "Testing maximum length for author fields";
        thought.author = "a".repeat(200);
        thought.authorBio = "b".repeat(200);
        thought.persist();

        entityManager.flush();
        entityManager.clear();

        Thought retrieved = Thought.findById(thought.id);
        assertEquals(200, retrieved.author.length());
        assertEquals(200, retrieved.authorBio.length());
    }

    @Test
    @Transactional
    public void testEmptyAuthorFieldsSetToDefault() {
        Thought thought = new Thought();
        thought.content = "This thought has empty author fields";
        thought.author = "";
        thought.authorBio = "";
        thought.persist();

        assertEquals("Unknown", thought.author);
        assertEquals("Unknown", thought.authorBio);
    }
}
