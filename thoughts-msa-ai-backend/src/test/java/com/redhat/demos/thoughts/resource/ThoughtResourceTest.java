package com.redhat.demos.thoughts.resource;

import com.redhat.demos.thoughts.model.Thought;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
public class ThoughtResourceTest {

    @BeforeEach
    @Transactional
    public void setup() {
        Thought.deleteAll();
    }

    @Test
    public void testCreateThought() {
        String thoughtContent = "This is a wonderful positive thought that inspires me";

        given()
                .contentType(ContentType.JSON)
                .body("{\"content\": \"" + thoughtContent + "\"}")
                .when()
                .post("/thoughts")
                .then()
                .statusCode(201)
                .body("content", equalTo(thoughtContent))
                .body("id", notNullValue())
                .body("thumbsUp", equalTo(0))
                .body("thumbsDown", equalTo(0));
    }

    @Test
    public void testGetThought() {
        Thought thought = createTestThought("A positive thought for retrieval test");

        given()
                .when()
                .get("/thoughts/" + thought.id)
                .then()
                .statusCode(200)
                .body("content", equalTo(thought.content))
                .body("id", equalTo(thought.id.toString()));
    }

    @Test
    public void testGetThoughtNotFound() {
        given()
                .when()
                .get("/thoughts/00000000-0000-0000-0000-000000000000")
                .then()
                .statusCode(404);
    }

    @Test
    public void testValidationError() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"content\": \"\"}")
                .when()
                .post("/thoughts")
                .then()
                .statusCode(400);
    }

    @Test
    public void testListThoughts() {
        createTestThought("First positive thought for listing");
        createTestThought("Second positive thought for listing");

        given()
                .when()
                .get("/thoughts")
                .then()
                .statusCode(200)
                .body("size()", equalTo(2));
    }

    @Test
    public void testUpdateThought() {
        Thought thought = createTestThought("Original positive thought content");
        String updatedContent = "Updated positive thought with new inspiration";

        given()
                .contentType(ContentType.JSON)
                .body("{\"content\": \"" + updatedContent + "\"}")
                .when()
                .put("/thoughts/" + thought.id)
                .then()
                .statusCode(200)
                .body("content", equalTo(updatedContent));
    }

    @Test
    public void testDeleteThought() {
        Thought thought = createTestThought("A thought to be deleted");

        given()
                .when()
                .delete("/thoughts/" + thought.id)
                .then()
                .statusCode(204);

        given()
                .when()
                .get("/thoughts/" + thought.id)
                .then()
                .statusCode(404);
    }

    @Test
    public void testRandomThought() {
        createTestThought("Random thought selection test one");
        createTestThought("Random thought selection test two");

        given()
                .when()
                .get("/thoughts/random")
                .then()
                .statusCode(200)
                .body("content", notNullValue());
    }

    @Transactional
    protected Thought createTestThought(String content) {
        Thought thought = new Thought();
        thought.content = content;
        thought.persist();
        return thought;
    }
}
