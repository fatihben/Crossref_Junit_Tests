package tests;

import io.restassured.path.xml.XmlPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OpenURL API — Production Resilient Negative Test Suite")
public class NegativeTests {

    private static final String BASE_URL = "https://api.crossref.org/openurl";
    private static final String VALID_PID = "sahinfatih@gmail.com";
    private static final String INVALID_DOI = "doi:10.1016/invalid.doi.999999";
    private static final String VALID_DOI = "doi:10.1007/s10696-011-9101-8";

    @Test
    @DisplayName("NC-01: Invalid DOI handling — Accepts structural error or standard resource rejection")
    void invalidDoiShouldReturnErrorMessage() {
        Response response = given()
                .header("User-Agent", "Mozilla/5.0")
                .header("Accept", "application/xml")
                .queryParam("pid", VALID_PID)
                .queryParam("id", INVALID_DOI)
                .queryParam("noredirect", "true")
                .when()
                .get(BASE_URL);

        // Canlı sistem geçersiz DOI'lerde 200 (XML error), 403 (WAF) veya 404 (Route error) dönebilir.
        assertThat(response.statusCode())
                .as("API must reject or gracefully handle the invalid DOI lookup")
                .isIn(200, 403, 404);

        if (response.getStatusCode() == 200) {
            XmlPath xml = new XmlPath(response.asString());
            String queryStatus = xml.getString("**.find { it.name() == 'query' }.@status");
            assertThat(queryStatus).containsIgnoringCase("id");
        }
    }

    @Test
    @DisplayName("NC-02: Missing 'id' parameter should result in client-side anomaly or resource error")
    void missingIdParameterTest() {
        Response response = given()
                .queryParam("pid", VALID_PID)
                .when()
                .get(BASE_URL);

        assertThat(response.statusCode())
                .as("Missing mandatory 'id' parameter must cause a resource resolution failure (>= 400)")
                .isGreaterThanOrEqualTo(400);
    }

    @Test
    @DisplayName("NC-03: Missing 'pid' parameter should fail authentication or route validation")
    void missingPidParameterTest() {
        Response response = given()
                .queryParam("id", VALID_DOI)
                .queryParam("noredirect", "true")
                .when()
                .get(BASE_URL);

        // Canlı sistem 404 Route Not Found veya 403 Authentication hatası fırlatıyor.
        assertThat(response.statusCode())
                .as("Missing credential/pid must trigger a secure rejection (403 or 404)")
                .isIn(403, 404);

        String body = response.asString();
        assertThat(body)
                .as("Response must indicate an auth or route matching anomaly")
                .matches(b -> b.contains("route-not-found")
                        || b.contains("Authentication required")
                        || b.contains("not recognized"));
    }

    @Test
    @DisplayName("NC-04: Unsupported Accept Header should be cleanly rejected")
    void unsupportedAcceptHeaderShouldReturn406() {
        Response response = given()
                .header("User-Agent", "Mozilla/5.0")
                .header("Accept", "application/pdf")
                .queryParam("pid", VALID_PID)
                .queryParam("id", VALID_DOI)
                .when()
                .get(BASE_URL);

        // API geçersiz medya tiplerini 406, 403 veya 404 (Route Dropped) ile güvenli bir şekilde reddeder.
        assertThat(response.statusCode())
                .as("Content negotiation failure: Request should be rejected with 406, 403, or 404")
                .isIn(406, 403, 404);
    }

    @Test
    @DisplayName("NC-05: Empty value for mandatory parameters should be handled without server crashes")
    void emptyMandatoryParameterValueShouldNotCause500() {
        Response response = given()
                .queryParam("pid", "")
                .queryParam("id", VALID_DOI)
                .when()
                .get(BASE_URL);

        assertThat(response.statusCode())
                .as("An empty parameter should never trigger a fatal 500 Internal Server Error")
                .isLessThan(500);
    }
}