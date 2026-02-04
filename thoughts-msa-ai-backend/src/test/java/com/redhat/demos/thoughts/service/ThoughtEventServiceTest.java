package com.redhat.demos.thoughts.service;

import com.redhat.demos.thoughts.model.Thought;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import io.smallrye.reactive.messaging.memory.InMemorySink;
import jakarta.enterprise.inject.Any;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class ThoughtEventServiceTest {

    @Inject
    @Any
    InMemoryConnector connector;

    @BeforeEach
    @Transactional
    public void setup() {
        Thought.deleteAll();
        connector.sink("thoughts-events").clear();
    }

    @Test
    public void testEventPublishedOnCreate() {
        InMemorySink<Thought> thoughtsSink = connector.sink("thoughts-events");

        given()
                .contentType(ContentType.JSON)
                .body("{\"content\": \"Event publishing test for create operation\"}")
                .when()
                .post("/thoughts")
                .then()
                .statusCode(201);

        assertEquals(1, thoughtsSink.received().size());
        Thought published = thoughtsSink.received().get(0).getPayload();
        assertEquals("Event publishing test for create operation", published.content);
    }

    @Test
    public void testEventPublishedOnUpdate() {
        Thought thought = createTestThought("Original content for update event test");
        InMemorySink<Thought> thoughtsSink = connector.sink("thoughts-events");
        thoughtsSink.clear();

        given()
                .contentType(ContentType.JSON)
                .body("{\"content\": \"Updated content for event publishing\"}")
                .when()
                .put("/thoughts/" + thought.id)
                .then()
                .statusCode(200);

        assertEquals(1, thoughtsSink.received().size());
        Thought published = thoughtsSink.received().get(0).getPayload();
        assertEquals("Updated content for event publishing", published.content);
    }

    @Test
    public void testEventPublishedOnDelete() {
        Thought thought = createTestThought("Content for delete event test");
        InMemorySink<Thought> thoughtsSink = connector.sink("thoughts-events");
        thoughtsSink.clear();

        given()
                .when()
                .delete("/thoughts/" + thought.id)
                .then()
                .statusCode(204);

        assertEquals(1, thoughtsSink.received().size());
    }

    @Transactional
    protected Thought createTestThought(String content) {
        Thought thought = new Thought();
        thought.content = content;
        thought.persist();
        return thought;
    }
}
