package com.redhat.demos.thoughts.exception;

import com.redhat.demos.thoughts.model.Thought;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThan;

@QuarkusTest
public class ExceptionMappersTest {

    @BeforeEach
    @Transactional
    public void setup() {
        Thought.deleteAll();
    }

    @Test
    public void testValidationExceptionForBlankContent() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"content\": \"\"}")
                .when()
                .post("/thoughts")
                .then()
                .statusCode(400)
                .body("message", equalTo("Validation failed"))
                .body("status", equalTo(400))
                .body("correlationId", notNullValue())
                .body("fieldErrors", notNullValue())
                .header("X-Correlation-ID", notNullValue());
    }

    @Test
    public void testValidationExceptionForShortContent() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"content\": \"Short\"}")
                .when()
                .post("/thoughts")
                .then()
                .statusCode(400)
                .body("message", equalTo("Validation failed"))
                .body("status", equalTo(400))
                .body("fieldErrors.size()", greaterThan(0));
    }

    @Test
    public void testNotFoundError() {
        given()
                .when()
                .get("/thoughts/00000000-0000-0000-0000-000000000000")
                .then()
                .statusCode(404);
    }

    @Test
    public void testValidationWithMultipleErrors() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"content\": null}")
                .when()
                .post("/thoughts")
                .then()
                .statusCode(400)
                .body("correlationId", notNullValue())
                .body("timestamp", notNullValue());
    }
}
