package utils;

import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

/**
 * Central request factory.
 * All tests create request from this class
 * Header, auth ve common  parameters are managed from single point
 */
public class RequestBuilder {

    private RequestBuilder() {
        // utility class — instantiation engellendi
    }

    /**
     * Base spec responds for standard XML request
     * pid ve User-Agent as compulsory are set here.
     */
    public static RequestSpecification baseRequest() {
        return given()
                .header("User-Agent", "Mozilla/5.0")
                .header("Accept", "application/xml")
                .queryParam("pid", BaseTest.PID);
    }

    /**
     * Send request without pid (for negative testing)
     */
    public static RequestSpecification requestWithoutPid() {
        return given()
                .header("User-Agent", "Mozilla/5.0")
                .header("Accept", "application/xml");
    }

    /**
     * Send request with special Accept header (for Negative tests).
     */
    public static RequestSpecification requestWithAccept(String acceptHeader) {
        return given()
                .header("User-Agent", "Mozilla/5.0")
                .header("Accept", acceptHeader)
                .queryParam("pid", BaseTest.PID);
    }
}