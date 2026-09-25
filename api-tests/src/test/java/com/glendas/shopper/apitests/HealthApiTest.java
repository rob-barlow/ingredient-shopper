package com.glendas.shopper.apitests;

import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

/**
 * The health check (constitution Article V). The first real API test: it needs a running backend
 * at BASE_URL, and its response is validated against the contract like every other test.
 */
class HealthApiTest extends ApiTestBase {

    @Test
    void health_isUpIncludingTheDatabase() {
        api()
        .when()
            .get("/actuator/health")
        .then()
            .statusCode(200)
            .body("status", equalTo("UP"));
    }
}
