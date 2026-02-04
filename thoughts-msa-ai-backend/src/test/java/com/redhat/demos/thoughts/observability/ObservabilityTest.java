package com.redhat.demos.thoughts.observability;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
public class ObservabilityTest {

    @Test
    public void testMetricsEndpoint() {
        given()
                .when()
                .get("/q/metrics")
                .then()
                .statusCode(200);
    }

    @Test
    public void testOpenApiEndpoint() {
        given()
                .when()
                .get("/q/openapi")
                .then()
                .statusCode(200)
                .body(containsString("openapi"))
                .body(containsString("paths"));
    }
}
