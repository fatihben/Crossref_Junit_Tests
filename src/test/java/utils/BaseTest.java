package utils;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;

public class BaseTest {

    protected static final String BASE_URI = "https://www.crossref.org";
    protected static final String PID      = "sahinfatih@gmail.com";

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URI;
    }
}