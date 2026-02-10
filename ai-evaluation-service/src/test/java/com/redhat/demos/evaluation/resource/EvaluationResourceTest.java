package com.redhat.demos.evaluation.resource;

import com.redhat.demos.evaluation.model.ThoughtEvaluation;
import com.redhat.demos.evaluation.model.ThoughtStatus;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

@QuarkusTest
public class EvaluationResourceTest {

    @BeforeEach
    @Transactional
    public void setup() {
        // Clean up test data
        ThoughtEvaluation.deleteAll();
    }

    @Test
    public void testGetEvaluationsWithPagination() {
        // Create test evaluations
        createTestEvaluation(ThoughtStatus.APPROVED, new BigDecimal("0.75"));
        createTestEvaluation(ThoughtStatus.REJECTED, new BigDecimal("0.92"));
        createTestEvaluation(ThoughtStatus.APPROVED, new BigDecimal("0.65"));

        given()
                .queryParam("page", 0)
                .queryParam("size", 2)
                .when()
                .get("/evaluations")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", is(2));
    }

    @Test
    public void testGetEvaluationByThoughtId() {
        // Create test evaluation
        UUID thoughtId = UUID.randomUUID();
        createTestEvaluationWithThoughtId(thoughtId, ThoughtStatus.APPROVED, new BigDecimal("0.75"));

        given()
                .pathParam("thoughtId", thoughtId.toString())
                .when()
                .get("/evaluations/thought/{thoughtId}")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("thoughtId", is(thoughtId.toString()))
                .body("status", is("APPROVED"))
                .body("similarityScore", is(0.75f));
    }

    @Test
    public void testGetEvaluationByThoughtIdNotFound() {
        UUID nonExistentThoughtId = UUID.randomUUID();

        given()
                .pathParam("thoughtId", nonExistentThoughtId.toString())
                .when()
                .get("/evaluations/thought/{thoughtId}")
                .then()
                .statusCode(404);
    }

    @Test
    public void testGetEvaluationStats() {
        // Create test evaluations
        createTestEvaluation(ThoughtStatus.APPROVED, new BigDecimal("0.75"));
        createTestEvaluation(ThoughtStatus.APPROVED, new BigDecimal("0.65"));
        createTestEvaluation(ThoughtStatus.REJECTED, new BigDecimal("0.92"));
        createTestEvaluation(ThoughtStatus.REJECTED, new BigDecimal("0.88"));

        given()
                .when()
                .get("/evaluations/stats")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("totalEvaluated", is(4))
                .body("approvedCount", is(2))
                .body("rejectedCount", is(2))
                .body("averageSimilarityScore", notNullValue());
    }

    @Test
    public void testGetEvaluationsPaginationDefaults() {
        // Create more than default page size evaluations
        for (int i = 0; i < 25; i++) {
            createTestEvaluation(ThoughtStatus.APPROVED, new BigDecimal("0.75"));
        }

        given()
                .when()
                .get("/evaluations")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", is(20)); // Default page size
    }

    @Test
    public void testGetEvaluationsEmptyList() {
        given()
                .when()
                .get("/evaluations")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", is(0));
    }

    @Test
    public void testGetStatsWithNoEvaluations() {
        given()
                .when()
                .get("/evaluations/stats")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("totalEvaluated", is(0))
                .body("approvedCount", is(0))
                .body("rejectedCount", is(0));
    }

    @Transactional
    void createTestEvaluation(ThoughtStatus status, BigDecimal score) {
        ThoughtEvaluation evaluation = new ThoughtEvaluation();
        evaluation.thoughtId = UUID.randomUUID();
        evaluation.status = status;
        evaluation.similarityScore = score;
        evaluation.metadata = "{}";
        evaluation.persist();
    }

    @Transactional
    void createTestEvaluationWithThoughtId(UUID thoughtId, ThoughtStatus status, BigDecimal score) {
        ThoughtEvaluation evaluation = new ThoughtEvaluation();
        evaluation.thoughtId = thoughtId;
        evaluation.status = status;
        evaluation.similarityScore = score;
        evaluation.metadata = "{}";
        evaluation.persist();
    }
}
