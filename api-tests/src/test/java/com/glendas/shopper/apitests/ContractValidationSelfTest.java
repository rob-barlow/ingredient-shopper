package com.glendas.shopper.apitests;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.atlassian.oai.validator.model.Request;
import com.atlassian.oai.validator.model.SimpleResponse;
import com.atlassian.oai.validator.report.ValidationReport;

/**
 * Tests the <b>safety net itself</b>: proves the contract validation really catches drift. If these
 * failed, every other test's "matches the contract" guarantee would be worthless.
 *
 * <p>No backend needed: hand-made responses are checked against the contract directly.
 */
class ContractValidationSelfTest extends ApiTestBase {

    private static ValidationReport validateHealthResponse(int status, String json) {
        var response = SimpleResponse.Builder.status(status)
            .withContentType("application/json")
            .withBody(json)
            .build();
        return CONTRACT.validateResponse("/actuator/health", Request.Method.GET, response);
    }

    @Test
    void aResponseMatchingTheContract_passes() {
        assertThat(validateHealthResponse(200, """
            {"status":"UP","groups":["liveness","readiness"]}""").getMessages()).isEmpty();
    }

    @Test
    void aValueOutsideTheContractsEnum_fails() {
        assertThat(validateHealthResponse(200, """
            {"status":"SIDEWAYS"}""").hasErrors()).isTrue();
    }

    @Test
    void aMissingRequiredField_fails() {
        assertThat(validateHealthResponse(200, """
            {"groups":[]}""").hasErrors()).isTrue();
    }

    @Test
    void aStatusCodeTheContractDoesntList_fails() {
        assertThat(validateHealthResponse(418, """
            {"status":"UP"}""").hasErrors()).isTrue();
    }

    @Test
    void aContentTypeTheContractDoesntList_fails() {
        // e.g. Spring Actuator's own type, which it uses if the client doesn't ask for JSON
        var response = SimpleResponse.Builder.status(200)
            .withContentType("application/vnd.spring-boot.actuator.v3+json")
            .withBody("{\"status\":\"UP\"}")
            .build();
        assertThat(CONTRACT.validateResponse("/actuator/health", Request.Method.GET, response)
            .hasErrors()).isTrue();
    }

    @Test
    void aPathThatIsntInTheContract_fails() {
        var response = SimpleResponse.Builder.ok().withContentType("application/json").withBody("{}").build();
        assertThat(CONTRACT.validateResponse("/v1/not-in-the-contract", Request.Method.GET, response)
            .hasErrors()).isTrue();
    }
}
