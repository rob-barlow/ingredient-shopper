package com.glendas.shopper.apitests;

import static io.restassured.RestAssured.given;

import java.nio.file.Path;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.restassured.OpenApiValidationFilter;

import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

/**
 * Base class for every API test.
 *
 * <p>Tests call {@link #api()} instead of REST Assured's {@code given()}. That gives each request:
 * <ul>
 *   <li>the backend's address, from {@code -DBASE_URL} (default http://localhost:8081)</li>
 *   <li>an {@code Accept: application/json} header, as real clients send</li>
 *   <li><b>contract validation</b>: the request and the response are both checked against
 *       {@code contracts/openapi.yaml}. An undocumented status code, a missing required field, a
 *       wrong type or an unknown path all fail the test, even if the test's own assertions pass.</li>
 * </ul>
 *
 * <p><b>Rules</b> (ADR-016): tests know nothing about the backend's code or database. They set up
 * their data through the API (e.g. creating products as the admin), and they use unique names
 * (e.g. a random suffix) so they never depend on each other or on previous runs.
 */
public abstract class ApiTestBase {

    protected static final String BASE_URL = System.getProperty("BASE_URL", "http://localhost:8081");

    private static final String CONTRACT_FILE =
        System.getProperty("CONTRACT_FILE", "../contracts/openapi.yaml");

    /** The contract, loaded once for all tests. */
    protected static final OpenApiInteractionValidator CONTRACT = OpenApiInteractionValidator
        .createForSpecificationUrl(Path.of(CONTRACT_FILE).toAbsolutePath().toUri().toString())
        .build();

    private static final OpenApiValidationFilter CONTRACT_CHECK = new OpenApiValidationFilter(CONTRACT);

    /** Start every request with this: {@code api().when().get("/v1/...").then()...} */
    protected static RequestSpecification api() {
        return given()
            .baseUri(BASE_URL)
            .accept(ContentType.JSON)
            .filter(CONTRACT_CHECK);
    }
}
