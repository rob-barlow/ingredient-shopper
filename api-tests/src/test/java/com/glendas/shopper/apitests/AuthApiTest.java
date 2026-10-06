package com.glendas.shopper.apitests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyOrNullString;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

/** Admin login, logout and the admin-only guard: 004 AC-2, AC-3, AC-4, AC-7, AC-8 (API side). */
class AuthApiTest extends ApiTestBase {

    private static ExtractableResponse<Response> login(String username, String password) {
        return api()
            .contentType(ContentType.JSON)
            .body(Map.of("username", username, "password", password))
        .when()
            .post("/v1/auth/login")
        .then()
            .extract();
    }

    @Test
    void ac004_2_correctCredentialsGiveAWorkingToken() {
        var response = login(AdminLogin.username(), AdminLogin.password());
        assertThat(response.statusCode()).isEqualTo(200);
        String token = response.path("token");
        assertThat(token).isNotBlank();

        withToken(token)
        .when()
            .get("/v1/auth/me")
        .then()
            .statusCode(200)
            .body("username", equalTo(AdminLogin.username()));
    }

    @Test
    void ac004_3_wrongUsernameOrPasswordGiveTheSameMessage() {
        var wrongUsername = login("not-" + AdminLogin.username(), AdminLogin.password());
        var wrongPassword = login(AdminLogin.username(), "not-the-password");

        assertThat(wrongUsername.statusCode()).isEqualTo(401);
        assertThat(wrongPassword.statusCode()).isEqualTo(401);
        assertThat(wrongUsername.<String>path("detail")).isEqualTo("Wrong username or password");
        // Identical responses: nothing reveals which part was wrong.
        assertThat(wrongUsername.body().asString()).isEqualTo(wrongPassword.body().asString());
    }

    @Test
    void ac004_3_thereIsNoLockout() {
        for (int attempt = 0; attempt < 5; attempt++) {
            assertThat(login(AdminLogin.username(), "wrong-" + attempt).statusCode()).isEqualTo(401);
        }
        assertThat(login(AdminLogin.username(), AdminLogin.password()).statusCode()).isEqualTo(200);
    }

    @Test
    void ac004_4_emptyFieldsAreRejected() {
        apiSendingInvalidRequest()     // empty fields break the contract on purpose
            .contentType(ContentType.JSON)
            .body(Map.of("username", "", "password", ""))
        .when()
            .post("/v1/auth/login")
        .then()
            .statusCode(422)
            .body("errors.field", hasItems("username", "password"));
    }

    @Test
    void ac004_7_logoutKillsTheToken() {
        String token = AdminLogin.freshToken();   // its own session, so the shared one survives

        // Logout returns 204 with no body, so ask for "anything" (*/*), as the real client does by sending
        // no Accept header. Accept: application/json would get a 406, because there's no JSON to send.
        withToken(token).accept(ContentType.ANY).when().post("/v1/auth/logout").then().statusCode(204);

        withToken(token).when().get("/v1/auth/me").then().statusCode(401);
    }

    @Test
    void ac004_8_noTokenIsRefused() {
        apiSendingInvalidRequest()   // no token on a bearerAuth operation: breaks the contract on purpose
            .when().get("/v1/auth/me")
            .then().statusCode(401).body("title", not(emptyOrNullString()));
    }

    @Test
    void ac004_8_madeUpTokenIsRefused() {
        withToken("made-up-" + UUID.randomUUID()).when().get("/v1/auth/me")
            .then().statusCode(401);
    }

    @Test
    void ac004_8_guestCannotReachAdminEndpoints() {
        // A perfectly valid product, but no token: refused before any product code runs.
        var product = Map.of(
            "name", "Guest attempt " + UUID.randomUUID().toString().substring(0, 8),
            "description", "Should never be created",
            "categoryId", 1, "unitAmount", 1, "unitMeasure", "each",
            "pricePence", 100, "imageUrl", "https://example.com/x.jpg", "startingStock", 1);

        apiSendingInvalidRequest()   // no token: breaks the contract on purpose
            .contentType(ContentType.JSON)
            .body(product)
        .when()
            .post("/v1/admin/products")
        .then()
            .statusCode(401);
    }
}
