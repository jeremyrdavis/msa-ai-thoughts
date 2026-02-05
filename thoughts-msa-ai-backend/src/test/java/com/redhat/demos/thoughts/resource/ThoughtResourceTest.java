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

    @Test
    public void testCreateThoughtWithDefaultStatus() {
        String thoughtContent = "Testing default status in POST response";

        given()
                .contentType(ContentType.JSON)
                .body("{\"content\": \"" + thoughtContent + "\"}")
                .when()
                .post("/thoughts")
                .then()
                .statusCode(201)
                .body("status", equalTo("IN_REVIEW"));
    }

    @Test
    public void testGetThoughtIncludesStatus() {
        Thought thought = createTestThought("Testing status in GET response");

        given()
                .when()
                .get("/thoughts/" + thought.id)
                .then()
                .statusCode(200)
                .body("status", notNullValue());
    }

    @Test
    public void testListThoughtsIncludesStatus() {
        createTestThought("Testing status in list response");

        given()
                .when()
                .get("/thoughts")
                .then()
                .statusCode(200)
                .body("[0].status", notNullValue());
    }

    @Test
    public void testUpdateThoughtStatus() {
        Thought thought = createTestThought("Testing status update via PUT");

        given()
                .contentType(ContentType.JSON)
                .body("{\"content\": \"" + thought.content + "\", \"status\": \"APPROVED\"}")
                .when()
                .put("/thoughts/" + thought.id)
                .then()
                .statusCode(200)
                .body("status", equalTo("APPROVED"));
    }

    // Author field tests
    @Test
    public void testCreateThoughtWithAuthorFields() {
        String thoughtContent = "This is a wonderful positive thought that inspires me";
        String author = "Marcus Aurelius";
        String authorBio = "Roman Emperor and Stoic philosopher";

        given()
                .contentType(ContentType.JSON)
                .body("{\"content\": \"" + thoughtContent + "\", \"author\": \"" + author + "\", \"authorBio\": \"" + authorBio + "\"}")
                .when()
                .post("/thoughts")
                .then()
                .statusCode(201)
                .body("content", equalTo(thoughtContent))
                .body("author", equalTo(author))
                .body("authorBio", equalTo(authorBio));
    }

    @Test
    public void testCreateThoughtWithoutAuthorAppliesDefaults() {
        String thoughtContent = "This is a wonderful positive thought that inspires me";

        given()
                .contentType(ContentType.JSON)
                .body("{\"content\": \"" + thoughtContent + "\"}")
                .when()
                .post("/thoughts")
                .then()
                .statusCode(201)
                .body("author", equalTo("Unknown"))
                .body("authorBio", equalTo("Unknown"));
    }

    @Test
    public void testUpdateThoughtAuthorFields() {
        Thought thought = createTestThought("Original positive thought content");
        String updatedAuthor = "Seneca";
        String updatedAuthorBio = "Roman Stoic philosopher";

        given()
                .contentType(ContentType.JSON)
                .body("{\"content\": \"" + thought.content + "\", \"author\": \"" + updatedAuthor + "\", \"authorBio\": \"" + updatedAuthorBio + "\"}")
                .when()
                .put("/thoughts/" + thought.id)
                .then()
                .statusCode(200)
                .body("author", equalTo(updatedAuthor))
                .body("authorBio", equalTo(updatedAuthorBio));
    }

    @Test
    public void testGetThoughtIncludesAuthorFields() {
        Thought thought = createTestThought("Testing author fields in GET response");

        given()
                .when()
                .get("/thoughts/" + thought.id)
                .then()
                .statusCode(200)
                .body("author", notNullValue())
                .body("authorBio", notNullValue());
    }

    @Test
    public void testListThoughtsIncludesAuthorFields() {
        createTestThought("Testing author fields in list response");

        given()
                .when()
                .get("/thoughts")
                .then()
                .statusCode(200)
                .body("[0].author", notNullValue())
                .body("[0].authorBio", notNullValue());
    }

    @Test
    public void testRandomThoughtIncludesAuthorFields() {
        createTestThought("Random thought with author fields test");

        given()
                .when()
                .get("/thoughts/random")
                .then()
                .statusCode(200)
                .body("author", notNullValue())
                .body("authorBio", notNullValue());
    }

    @Test
    public void testAuthorFieldExceeding200CharactersFails() {
        String thoughtContent = "This is a wonderful positive thought that inspires me";
        String longAuthor = "A".repeat(201);

        given()
                .contentType(ContentType.JSON)
                .body("{\"content\": \"" + thoughtContent + "\", \"author\": \"" + longAuthor + "\"}")
                .when()
                .post("/thoughts")
                .then()
                .statusCode(400);
    }

    @Test
    public void testAuthorBioFieldExceeding200CharactersFails() {
        String thoughtContent = "This is a wonderful positive thought that inspires me";
        String longAuthorBio = "B".repeat(201);

        given()
                .contentType(ContentType.JSON)
                .body("{\"content\": \"" + thoughtContent + "\", \"authorBio\": \"" + longAuthorBio + "\"}")
                .when()
                .post("/thoughts")
                .then()
                .statusCode(400);
    }

    @Transactional
    protected Thought createTestThought(String content) {
        Thought thought = new Thought();
        thought.content = content;
        thought.persist();
        return thought;
    }
}
