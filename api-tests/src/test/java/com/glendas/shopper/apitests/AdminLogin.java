package com.glendas.shopper.apitests;

import static io.restassured.RestAssured.given;

import java.util.Map;

import io.restassured.http.ContentType;

/**
 * The admin's credentials and a shared login token for tests.
 *
 * <p>Credentials come from {@code -DADMIN_USERNAME} and {@code -DADMIN_PASSWORD} (the plain password
 * matching the backend's ADMIN_PASSWORD_BCRYPT). CI passes CI-only values. Locally, pass your own:
 * {@code .\mvnw.cmd test "-DADMIN_USERNAME=glenda" "-DADMIN_PASSWORD=..."}
 *
 * <p>The token is fetched once and reused, so tests don't log in hundreds of times. Tests that are
 * <i>about</i> logging in (e.g. logout) log in themselves, so they never break the shared token.
 */
final class AdminLogin {

    private static String sharedToken;

    private AdminLogin() {
    }

    static String username() {
        return required("ADMIN_USERNAME");
    }

    static String password() {
        return required("ADMIN_PASSWORD");
    }

    /** The shared admin token, logging in the first time it's needed. */
    static synchronized String token() {
        if (sharedToken == null) {
            sharedToken = freshToken();
        }
        return sharedToken;
    }

    /** A brand-new session, for tests that end their session (e.g. logout). */
    static String freshToken() {
        return given()
            .baseUri(ApiTestBase.BASE_URL)
            .contentType(ContentType.JSON)
            .body(Map.of("username", username(), "password", password()))
        .when()
            .post("/v1/auth/login")
        .then()
            .statusCode(200)
            .extract().path("token");
    }

    private static String required(String name) {
        String value = System.getProperty(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " is not set. Run the API tests with \"-D" + name
                + "=...\" (see api-tests/README.md).");
        }
        return value;
    }
}
