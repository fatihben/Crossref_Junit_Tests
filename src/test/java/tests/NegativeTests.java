package tests;

import io.restassured.path.xml.XmlPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import pages.OpenUrlApi;
import utils.BaseTest;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("CrossRef OpenURL API — Negative Tests")
public class NegativeTests extends BaseTest {

    private static final String INVALID_DOI = "10.1016/invalid.doi.999999";
    private static final String VALID_DOI   = "10.1007/s10696-011-9101-8";

    @Test
    @Order(1)
    @DisplayName("NC-01: Invalid DOI should return unresolved status in XML body")
    void invalidDoiShouldReturnUnresolvedStatus() {
        Response response = OpenUrlApi.queryWithInvalidDoi("doi:" + INVALID_DOI);

        // CrossRef always returns 200; errors are indicated inside the XML body
        assertEquals(200, response.statusCode(),
                "API must return 200 even for invalid DOI");

        String body = response.asString();
        assertTrue(body.trim().startsWith("<"),
                "Response must be XML even for invalid DOI");

        XmlPath xml = new XmlPath(body);
        String status = xml.getString("**.find { it.name() == 'query' }.@status");

        // An invalid DOI must never return 'resolved' status
        assertFalse("resolved".equalsIgnoreCase(status),
                "Invalid DOI must NOT return 'resolved' status, got: " + status);
    }

    @Test
    @Order(2)
    @DisplayName("NC-02: Missing 'id' parameter should return XML response without server crash")
    void missingIdParameterShouldReturnXmlWithoutCrash() {
        Response response = OpenUrlApi.queryWithoutId();

        // CrossRef returns 200 even without an id; the error is indicated in the XML body
        assertEquals(200, response.statusCode(),
                "API returns 200 even when id is missing");

        String body = response.asString();
        assertTrue(body.trim().startsWith("<"),
                "Response must still be XML when id is missing");

        assertFalse(body.isEmpty(),
                "Response body must not be empty");
    }

    @Test
    @Order(3)
    @DisplayName("NC-03: Missing 'pid' parameter should not cause server crash")
    void missingPidParameterShouldNotCrashServer() {
        Response response = OpenUrlApi.queryWithoutPid(VALID_DOI);

        assertTrue(response.statusCode() < 500,
                "Missing pid must not cause a server error (5xx), got: " + response.statusCode());

        assertFalse(response.asString().trim().isEmpty(),
                "Response body must not be empty");
    }

    @Test
    @Order(4)
    @DisplayName("NC-04: Unsupported Accept header should not cause server crash (no 5xx)")
    void unsupportedAcceptHeaderShouldNotCauseServerError() {
        Response response = OpenUrlApi.queryWithUnsupportedAccept(VALID_DOI, "application/pdf");

        assertTrue(response.statusCode() < 500,
                "Unsupported Accept header must not cause a 5xx server error, got: "
                        + response.statusCode());

        assertFalse(response.asString().trim().isEmpty(),
                "Response body must not be empty");
    }

    @Test
    @Order(5)
    @DisplayName("NC-05: Empty pid value should not cause a server error (5xx)")
    void emptyPidShouldNotCauseServerError() {
        Response response = OpenUrlApi.queryWithEmptyPid(VALID_DOI);

        assertTrue(response.statusCode() < 500,
                "Empty pid must never trigger a 500 Internal Server Error, got: "
                        + response.statusCode());

        assertFalse(response.asString().trim().isEmpty(),
                "Response body must not be empty");
    }
}