package com.redhat.demos.thoughts.integration;

import com.redhat.demos.thoughts.model.Thought;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for author/authorBio workflows across API and database layers.
 * These tests verify end-to-end functionality from HTTP request to database persistence.
 */
@QuarkusTest
public class AuthorWorkflowIntegrationTest {

    @BeforeEach
    @Transactional
    public void setup() {
        Thought.deleteAll();
    }

    @Test
    public void testCompleteThoughtCreationWithAuthorWorkflow() {
        // Step 1: Create thought with author via API
        String thoughtId = given()
                .contentType(ContentType.JSON)
                .body("{\"content\": \"A wonderful thought about persistence and determination\", \"author\": \"Marcus Aurelius\", \"authorBio\": \"Roman Emperor and Stoic philosopher\"}")
                .when()
                .post("/thoughts")
                .then()
                .statusCode(201)
                .body("content", equalTo("A wonderful thought about persistence and determination"))
                .body("author", equalTo("Marcus Aurelius"))
                .body("authorBio", equalTo("Roman Emperor and Stoic philosopher"))
                .extract()
                .path("id");

        // Step 2: Retrieve thought via API and verify author fields
        given()
                .when()
                .get("/thoughts/" + thoughtId)
                .then()
                .statusCode(200)
                .body("author", equalTo("Marcus Aurelius"))
                .body("authorBio", equalTo("Roman Emperor and Stoic philosopher"));

        // Step 3: Verify thought persisted to database with correct author fields
        Thought persistedThought = Thought.findById(java.util.UUID.fromString(thoughtId));
        assertNotNull(persistedThought);
        assertEquals("Marcus Aurelius", persistedThought.author);
        assertEquals("Roman Emperor and Stoic philosopher", persistedThought.authorBio);
    }

    @Test
    public void testCompleteThoughtCreationWithoutAuthorWorkflow() {
        // Step 1: Create thought without author via API
        String thoughtId = given()
                .contentType(ContentType.JSON)
                .body("{\"content\": \"A thought without explicit author attribution\"}")
                .when()
                .post("/thoughts")
                .then()
                .statusCode(201)
                .body("author", equalTo("Unknown"))
                .body("authorBio", equalTo("Unknown"))
                .extract()
                .path("id");

        // Step 2: Retrieve thought and verify defaults were applied
        given()
                .when()
                .get("/thoughts/" + thoughtId)
                .then()
                .statusCode(200)
                .body("author", equalTo("Unknown"))
                .body("authorBio", equalTo("Unknown"));

        // Step 3: Verify database has "Unknown" defaults
        Thought persistedThought = Thought.findById(java.util.UUID.fromString(thoughtId));
        assertNotNull(persistedThought);
        assertEquals("Unknown", persistedThought.author);
        assertEquals("Unknown", persistedThought.authorBio);
    }

    @Test
    public void testCompleteThoughtUpdateWorkflow() {
        // Step 1: Create initial thought
        String thoughtId = given()
                .contentType(ContentType.JSON)
                .body("{\"content\": \"Initial thought about change and growth\", \"author\": \"Original Author\", \"authorBio\": \"Original bio\"}")
                .when()
                .post("/thoughts")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // Step 2: Update author fields via API
        given()
                .contentType(ContentType.JSON)
                .body("{\"content\": \"Initial thought about change and growth\", \"author\": \"Epictetus\", \"authorBio\": \"Greek Stoic philosopher\"}")
                .when()
                .put("/thoughts/" + thoughtId)
                .then()
                .statusCode(200)
                .body("author", equalTo("Epictetus"))
                .body("authorBio", equalTo("Greek Stoic philosopher"));

        // Step 3: Verify update persisted to database
        Thought updatedThought = Thought.findById(java.util.UUID.fromString(thoughtId));
        assertNotNull(updatedThought);
        assertEquals("Epictetus", updatedThought.author);
        assertEquals("Greek Stoic philosopher", updatedThought.authorBio);
    }

    @Test
    public void testRandomThoughtReturnsAuthorFields() {
        // Create multiple thoughts with different authors
        given()
                .contentType(ContentType.JSON)
                .body("{\"content\": \"First thought with author for random test\", \"author\": \"Seneca\", \"authorBio\": \"Roman philosopher\"}")
                .when()
                .post("/thoughts")
                .then()
                .statusCode(201);

        given()
                .contentType(ContentType.JSON)
                .body("{\"content\": \"Second thought with author for random test\", \"author\": \"Marcus Aurelius\", \"authorBio\": \"Roman Emperor\"}")
                .when()
                .post("/thoughts")
                .then()
                .statusCode(201);

        // Retrieve random thought and verify author fields are included
        given()
                .when()
                .get("/thoughts/random")
                .then()
                .statusCode(200)
                .body("author", notNullValue())
                .body("authorBio", notNullValue())
                .body("author", anyOf(equalTo("Seneca"), equalTo("Marcus Aurelius")));
    }

    @Test
    public void testListThoughtsIncludesAllAuthorFields() {
        // Create thoughts with varying author information
        given()
                .contentType(ContentType.JSON)
                .body("{\"content\": \"Thought one with specific author information\", \"author\": \"Epictetus\", \"authorBio\": \"Stoic teacher\"}")
                .when()
                .post("/thoughts")
                .then()
                .statusCode(201);

        given()
                .contentType(ContentType.JSON)
                .body("{\"content\": \"Thought two with default author information\"}")
                .when()
                .post("/thoughts")
                .then()
                .statusCode(201);

        // Verify list endpoint returns author fields for all thoughts
        given()
                .when()
                .get("/thoughts")
                .then()
                .statusCode(200)
                .body("size()", equalTo(2))
                .body("[0].author", notNullValue())
                .body("[0].authorBio", notNullValue())
                .body("[1].author", notNullValue())
                .body("[1].authorBio", notNullValue());
    }
}
