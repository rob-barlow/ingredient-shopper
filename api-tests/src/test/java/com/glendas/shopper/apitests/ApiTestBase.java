package com.glendas.shopper.apitests;

import static io.restassured.RestAssured.given;

import java.nio.file.Path;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.report.LevelResolver;
import com.atlassian.oai.validator.report.ValidationReport;
import com.atlassian.oai.validator.restassured.OpenApiValidationFilter;

import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.swagger.v3.parser.core.models.ParseOptions;

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

    private static final String CONTRACT_URL = Path.of(CONTRACT_FILE).toAbsolutePath().toUri().toString();

    /**
     * How the contract is read. {@code resolveCombinators} merges {@code allOf} schemas (e.g. NewProduct =
     * ProductInput + startingStock) into one before validating. Without it, each part is checked on its
     * own and complains about the other part's fields.
     */
    private static ParseOptions parseOptions() {
        var options = new ParseOptions();
        options.setResolve(true);
        options.setResolveFully(true);
        options.setResolveCombinators(true);
        return options;
    }

    /** The contract, loaded once for all tests. Checks requests and responses. */
    protected static final OpenApiInteractionValidator CONTRACT = OpenApiInteractionValidator
        .createForSpecificationUrl(CONTRACT_URL)
        .withParseOptions(parseOptions())
        .build();

    /** The same contract, but request problems are ignored. Responses are still fully checked. */
    private static final OpenApiInteractionValidator CONTRACT_RESPONSES_ONLY = OpenApiInteractionValidator
        .createForSpecificationUrl(CONTRACT_URL)
        .withParseOptions(parseOptions())
        .withLevelResolver(LevelResolver.create()
            .withLevel("validation.request", ValidationReport.Level.IGNORE)
            .build())
        .build();

    private static final OpenApiValidationFilter CONTRACT_CHECK = new OpenApiValidationFilter(CONTRACT);
    private static final OpenApiValidationFilter RESPONSE_CHECK = new OpenApiValidationFilter(CONTRACT_RESPONSES_ONLY);

    /** Start every request with this: {@code api().when().get("/v1/...").then()...} */
    protected static RequestSpecification api() {
        return given()
            .baseUri(BASE_URL)
            .accept(ContentType.JSON)
            .filter(CONTRACT_CHECK);
    }

    /**
     * For tests that <b>deliberately</b> send a request the contract forbids (e.g. empty required
     * fields, to prove the backend rejects them). The response must still match the contract.
     * Using this anywhere else would hide real mistakes in a test, so the name says what it's for.
     */
    protected static RequestSpecification apiSendingInvalidRequest() {
        return given()
            .baseUri(BASE_URL)
            .accept(ContentType.JSON)
            .filter(RESPONSE_CHECK);
    }

    /** {@link #api()}, as the logged-in admin. */
    protected static RequestSpecification asAdmin() {
        return withToken(AdminLogin.token());
    }

    /**
     * {@link #api()} with {@code Authorization: Bearer <token>}. Set as a plain header, not REST Assured's
     * {@code auth().oauth2()}, which adds the header too late for the contract check to see it.
     */
    protected static RequestSpecification withToken(String token) {
        return api().header("Authorization", "Bearer " + token);
    }
}
