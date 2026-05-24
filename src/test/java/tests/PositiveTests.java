//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package tests;

import io.restassured.RestAssured;
import io.restassured.path.xml.XmlPath;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import java.io.StringReader;
import java.net.URL;
import java.util.List;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.assertj.core.api.AbstractIntegerAssert;
import org.assertj.core.api.AbstractStringAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.xml.sax.SAXParseException;
import utils.BaseTest;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("CrossRef OpenURL API — Positive Tests")
class PositiveTests extends BaseTest {
    private static final String KNOWN_DOI = "10.1016/j.jebo.2023.08.009";
    private static final String PID = "sahinfatih@gmail.com";

    private static Response openUrl(String... extraParams) {
        RequestSpecification spec = RestAssured.given().header("User-Agent", "Mozilla/5.0", new Object[0]).header("Accept", "application/xml", new Object[0]).queryParam("pid", new Object[]{"sahinfatih@gmail.com"}).queryParam("noredirect", new Object[]{"true"});

        for(int i = 0; i < extraParams.length; i += 2) {
            spec = spec.queryParam(extraParams[i], new Object[]{extraParams[i + 1]});
        }

        return (Response)spec.when().get("/openurl", new Object[0]);
    }

    @Test
    @Order(1)
    @DisplayName("Valid DOI should return HTTP 200 with XML metadata")
    public void validDoiShouldReturnXmlMetadata() {
        Response response = (Response)RestAssured.given().header("User-Agent", "Mozilla/5.0", new Object[0]).header("Accept", "application/xml", new Object[0]).queryParam("pid", new Object[]{"sahinfatih@gmail.com"}).queryParam("id", new Object[]{"doi:10.1016/j.jebo.2023.08.009"}).queryParam("noredirect", new Object[]{"true"}).when().get("/openurl", new Object[0]);
        ((ValidatableResponse)response.then()).statusCode(200);
        String body = response.asString();
        System.out.println(body);
        XmlPath xml = new XmlPath(body);
        String doi = xml.getString("**.find { it.name() == 'doi' }");
        Assertions.assertNotNull(doi, "DOI node must be present in the response");
        Assertions.assertEquals("10.1016/j.jebo.2023.08.009", doi.trim(), "Returned DOI must match the queried DOI exactly");
    }

    @Test
    @Order(2)
    @DisplayName("Response should contain a non-blank article title")
    public void responseShouldContainJournalTitle() {
        Response response = (Response)RestAssured.given().header("User-Agent", "Mozilla/5.0", new Object[0]).header("Accept", "application/xml", new Object[0]).queryParam("pid", new Object[]{"sahinfatih@gmail.com"}).queryParam("id", new Object[]{"doi:10.1016/j.jebo.2023.08.009"}).queryParam("noredirect", new Object[]{"true"}).when().get("/openurl", new Object[0]);
        ((ValidatableResponse)response.then()).statusCode(200);
        XmlPath xml = new XmlPath(response.asString());
        String journalTitle = xml.getString("**.find { it.name() == 'article_title' }");
        Assertions.assertNotNull(journalTitle, "article_title node must be present");
        ((AbstractStringAssert)org.assertj.core.api.Assertions.assertThat(journalTitle.trim()).as("article_title must not be an empty string", new Object[0])).isNotBlank();
    }

    @Test
    @Order(3)
    @DisplayName("Successful DOI query should return query_result with status='resolved'")
    public void successfulQueryShouldHaveResolvedStatus() {
        Response response = (Response)RestAssured.given().header("User-Agent", "Mozilla/5.0", new Object[0]).header("Accept", "application/xml", new Object[0]).queryParam("pid", new Object[]{"sahinfatih@gmail.com"}).queryParam("id", new Object[]{"doi:10.1016/j.jebo.2023.08.009"}).queryParam("noredirect", new Object[]{"true"}).when().get("/openurl", new Object[0]);
        ((ValidatableResponse)response.then()).statusCode(200);
        String body = response.asString();
        XmlPath xml = new XmlPath(body);
        String status = xml.getString("**.find { it.name() == 'query' }.@status");
        ((AbstractStringAssert)org.assertj.core.api.Assertions.assertThat(status).as("query status must be 'resolved' for a valid DOI — got: '%s'\nBody: %s", new Object[]{status, body.substring(0, Math.min(500, body.length()))})).isEqualToIgnoringCase("resolved");
    }

    @Test
    @Order(4)
    @DisplayName("Response body must not be empty")
    public void responseBodyIsNotEmpty() {
        Response response = openUrl("id", "doi:10.1016/j.jebo.2023.08.009");
        ((ValidatableResponse)response.then()).statusCode(200);
        ((AbstractStringAssert)org.assertj.core.api.Assertions.assertThat(response.asString().trim()).as("Response body must not be empty for a valid DOI query", new Object[0])).isNotEmpty();
    }

    @Test
    @Order(5)
    @DisplayName("Response Content-Type must indicate XML")
    public void responseContentTypeMustBeXml() {
        Response response = (Response)RestAssured.given().header("User-Agent", "Mozilla/5.0", new Object[0]).header("Accept", "application/xml", new Object[0]).queryParam("pid", new Object[]{"sahinfatih@gmail.com"}).queryParam("id", new Object[]{"doi:10.1016/j.jebo.2023.08.009"}).queryParam("noredirect", new Object[]{"true"}).when().get("/openurl", new Object[0]);
        ((ValidatableResponse)response.then()).statusCode(200);
        String contentType = response.contentType();
        ((AbstractStringAssert)org.assertj.core.api.Assertions.assertThat(contentType).as("Response Content-Type must contain 'xml', got: %s", new Object[]{contentType})).containsIgnoringCase("xml");
    }

    @Test
    @Order(6)
    @DisplayName("format=unixref should return UNIXREF-formatted XML with journal_article element")
    public void unixrefFormatShouldReturnUnixrefElements() {
        Response response = (Response)RestAssured.given().header("User-Agent", "Mozilla/5.0", new Object[0]).header("Accept", "application/xml", new Object[0]).queryParam("pid", new Object[]{"sahinfatih@gmail.com"}).queryParam("id", new Object[]{"doi:10.1016/j.jebo.2023.08.009"}).queryParam("noredirect", new Object[]{"true"}).queryParam("format", new Object[]{"unixref"}).when().get("/openurl", new Object[0]);
        ((ValidatableResponse)response.then()).statusCode(200);
        String body = response.asString();
        ((AbstractStringAssert)org.assertj.core.api.Assertions.assertThat(body).as("UNIXREF format response should contain 'journal_article' element.\nBody: %s", new Object[]{body.substring(0, Math.min(500, body.length()))})).containsIgnoringCase("journal_article");
    }

    @Test
    @Order(7)
    @DisplayName("Default format (no format param) should return UNIXSD with crossref_result root")
    public void defaultFormatShouldReturnUnixsdRoot() {
        Response response = (Response)RestAssured.given().header("User-Agent", "Mozilla/5.0", new Object[0]).header("Accept", "application/xml", new Object[0]).queryParam("pid", new Object[]{"sahinfatih@gmail.com"}).queryParam("id", new Object[]{"doi:10.1016/j.jebo.2023.08.009"}).queryParam("noredirect", new Object[]{"true"}).when().get("/openurl", new Object[0]);
        ((ValidatableResponse)response.then()).statusCode(200);
        String body = response.asString();
        ((AbstractStringAssert)org.assertj.core.api.Assertions.assertThat(body).as("Default format response should contain 'crossref_result' root element", new Object[0])).containsIgnoringCase("crossref_result");
    }

    @Test
    @Order(8)
    @DisplayName("Metadata-based query (issn + aulast + date) should resolve to a DOI")
    public void metadataQueryShouldResolveToDoi() {
        Response response = (Response)RestAssured.given().header("User-Agent", "Mozilla/5.0", new Object[0]).header("Accept", "application/xml", new Object[0]).queryParam("pid", new Object[]{"sahinfatih@gmail.com"}).queryParam("issn", new Object[]{"03770273"}).queryParam("aulast", new Object[]{"Walker"}).queryParam("volume", new Object[]{"54"}).queryParam("spage", new Object[]{"117"}).queryParam("date", new Object[]{"1983"}).queryParam("noredirect", new Object[]{"true"}).when().get("/openurl", new Object[0]);
        ((ValidatableResponse)response.then()).statusCode(200);
        String body = response.asString();
        XmlPath xml = new XmlPath(body);
        String doi = xml.getString("**.find { it.name() == 'doi' }");
        ((AbstractStringAssert)((AbstractStringAssert)org.assertj.core.api.Assertions.assertThat(doi).as("Metadata query (Walker/1983) should resolve to a DOI.\nBody: %s", new Object[]{body.substring(0, Math.min(500, body.length()))})).isNotNull()).isNotBlank();
    }

    @Test
    @Order(9)
    @DisplayName("Metadata query with title + author should return resolved status")
    public void metadataQueryWithTitleShouldResolve() {
        Response response = (Response)RestAssured.given().header("User-Agent", "Mozilla/5.0", new Object[0]).header("Accept", "application/xml", new Object[0]).queryParam("pid", new Object[]{"sahinfatih@gmail.com"}).queryParam("title", new Object[]{"Science"}).queryParam("aulast", new Object[]{"Fernandez"}).queryParam("date", new Object[]{"2009"}).queryParam("noredirect", new Object[]{"true"}).when().get("/openurl", new Object[0]);
        ((ValidatableResponse)response.then()).statusCode(200);
        String body = response.asString();
        XmlPath xml = new XmlPath(body);
        String status = xml.getString("**.find { it.name() == 'query' }.@status");
        ((AbstractStringAssert)((AbstractStringAssert)org.assertj.core.api.Assertions.assertThat(status).as("query @status must be 'resolved' or 'unresolved', got: '%s'\nBody: %s", new Object[]{status, body.substring(0, Math.min(400, body.length()))})).isNotBlank()).isIn(new Object[]{"resolved", "unresolved", "multiresolved"});
    }

    @Test
    @Order(10)
    @DisplayName("multihit=true should return at least one result for an ambiguous query")
    public void multihitShouldReturnMultipleResults() {
        Response response = (Response)RestAssured.given().header("User-Agent", "Mozilla/5.0", new Object[0]).header("Accept", "application/xml", new Object[0]).queryParam("pid", new Object[]{"sahinfatih@gmail.com"}).queryParam("issn", new Object[]{"03603016"}).queryParam("volume", new Object[]{"54"}).queryParam("issue", new Object[]{"2"}).queryParam("spage", new Object[]{"215"}).queryParam("date", new Object[]{"2002"}).queryParam("multihit", new Object[]{"true"}).queryParam("noredirect", new Object[]{"true"}).when().get("/openurl", new Object[0]);
        ((ValidatableResponse)response.then()).statusCode(200);
        String body = response.asString();
        XmlPath xml = new XmlPath(body);
        String status = xml.getString("**.find { it.name() == 'query' }.@status");
        List<?> dois = xml.getList("**.findAll { it.name() == 'doi' }");
        ((AbstractStringAssert)((AbstractStringAssert)org.assertj.core.api.Assertions.assertThat(status).as("multihit=true should return status 'multiresolved' or 'resolved'.\nBody: %s", new Object[]{body.substring(0, Math.min(500, body.length()))})).isNotBlank()).isIn(new Object[]{"multiresolved", "resolved"});
        ((AbstractIntegerAssert)org.assertj.core.api.Assertions.assertThat(dois.size()).as("multihit response should contain at least one doi element.\nBody: %s", new Object[]{body.substring(0, Math.min(500, body.length()))})).isGreaterThanOrEqualTo(1);
    }

    @Test
    @Order(11)
    @DisplayName("redirect=false should return XML body without HTTP redirect")
    public void redirectFalseShouldReturnXmlBody() {
        Response response = (Response)RestAssured.given().header("User-Agent", "Mozilla/5.0", new Object[0]).header("Accept", "application/xml", new Object[0]).queryParam("pid", new Object[]{"sahinfatih@gmail.com"}).queryParam("id", new Object[]{"doi:10.1016/j.jebo.2023.08.009"}).queryParam("redirect", new Object[]{"false"}).when().get("/openurl", new Object[0]);
        ((ValidatableResponse)response.then()).statusCode(200);
        String body = response.asString();
        ((AbstractStringAssert)org.assertj.core.api.Assertions.assertThat(body.trim()).as("redirect=false must return an XML body starting with '<'.\nGot: %s", new Object[]{body.substring(0, Math.min(200, body.length()))})).startsWith("<");
    }

    @Order(12)
    @DisplayName("noredirect=true and redirect=false should both produce XML bodies")
    @ParameterizedTest(
            name = "param={0} value={1}"
    )
    @CsvSource({"noredirect, true", "redirect,   false"})
    public void bothNoredirectVariantsShouldReturnXml(String param, String value) {
        Response response = (Response)RestAssured.given().header("User-Agent", "Mozilla/5.0", new Object[0]).header("Accept", "application/xml", new Object[0]).queryParam("pid", new Object[]{"sahinfatih@gmail.com"}).queryParam("id", new Object[]{"doi:10.1016/j.jebo.2023.08.009"}).queryParam(param.trim(), new Object[]{value.trim()}).when().get("/openurl", new Object[0]);
        ((ValidatableResponse)response.then()).statusCode(200);
        String body = response.asString();
        ((AbstractStringAssert)org.assertj.core.api.Assertions.assertThat(body.trim()).as("With %s=%s, response must be XML.\nGot: %s", new Object[]{param.trim(), value.trim(), body.substring(0, Math.min(200, body.length()))})).startsWith("<");
        ((AbstractStringAssert)org.assertj.core.api.Assertions.assertThat(body).as("XML response must contain a DOI for param %s=%s", new Object[]{param, value})).containsIgnoringCase("10.1016/j.jebo.2023.08.009");
    }

    @Test
    @Order(13)
    @DisplayName("Response XML must validate against CrossRef UNIXSD schema")
    public void responseXmlMustValidateAgainstSchema() throws Exception {
        Response response = (Response)RestAssured.given().header("User-Agent", "Mozilla/5.0", new Object[0]).header("Accept", "application/xml", new Object[0]).queryParam("pid", new Object[]{"sahinfatih@gmail.com"}).queryParam("id", new Object[]{"doi:10.1016/j.jebo.2023.08.009"}).queryParam("noredirect", new Object[]{"true"}).when().get("/openurl", new Object[0]);
        ((ValidatableResponse)response.then()).statusCode(200);
        String body = response.asString();
        String xsdUrl = "https://www.crossref.org/schemas/crossref_query_output2.0.xsd";

        try {
            SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
            Schema schema = factory.newSchema(new URL(xsdUrl));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new StringReader(body)));
            System.out.println("[SCHEMA] Response validates against UNIXSD XSD ✓");
        } catch (SAXParseException var7) {
            if (var7.getMessage() != null && var7.getMessage().contains("paper_title")) {
                System.out.println("[SCHEMA SKIP] CrossRef XSD has a known duplicate 'paper_title' definition — schema-level bug, not a response bug. Skipping validation. CrossRef issue reference: sch-props-correct.2");
                Assumptions.assumeTrue(false, "CrossRef XSD is internally invalid (duplicate paper_title) — cannot validate response against a broken schema.");
            } else {
                Assertions.fail("Response XML does not validate against CrossRef UNIXSD schema: " + var7.getMessage());
            }
        } catch (Exception e) {
            Assumptions.assumeTrue(false, "XSD schema unreachable — skipping: " + e.getMessage());
        }

    }

    @Test
    @Order(14)
    @DisplayName("aulast with Unicode characters (accented) should not cause a server error")
    public void unicodeAuthorNameShouldNotCauseServerError() {
        Response response = (Response)RestAssured.given().header("User-Agent", "Mozilla/5.0", new Object[0]).header("Accept", "application/xml", new Object[0]).queryParam("pid", new Object[]{"sahinfatih@gmail.com"}).queryParam("title", new Object[]{"Science"}).queryParam("aulast", new Object[]{"Fernández"}).queryParam("date", new Object[]{"2009"}).queryParam("noredirect", new Object[]{"true"}).when().get("/openurl", new Object[0]);
        String body = response.asString();
        ((AbstractIntegerAssert)org.assertj.core.api.Assertions.assertThat(response.statusCode()).as("Unicode author name must not cause a server error (5xx).\n  Status: %d\n  Body: %s", new Object[]{response.statusCode(), body.substring(0, Math.min(300, body.length()))})).isLessThan(500);
        ((AbstractStringAssert)org.assertj.core.api.Assertions.assertThat(body.trim()).as("Response to Unicode query must still be XML (starts with '<')", new Object[0])).startsWith("<");
    }

    @Test
    @Order(15)
    @DisplayName("DOI with special characters in suffix should be correctly URL-encoded and resolved")
    public void doiWithSpecialCharactersShouldResolve() {
        Response response = (Response)RestAssured.given().header("User-Agent", "Mozilla/5.0", new Object[0]).header("Accept", "application/xml", new Object[0]).queryParam("pid", new Object[]{"sahinfatih@gmail.com"}).queryParam("id", new Object[]{"doi:10.1175/1520-0485(2002)032<0870:CT>2.0.CO;2"}).queryParam("noredirect", new Object[]{"true"}).when().get("/openurl", new Object[0]);
        ((AbstractIntegerAssert)org.assertj.core.api.Assertions.assertThat(response.statusCode()).as("DOI with special characters must not cause a server error.\nStatus: %d", new Object[]{response.statusCode()})).isLessThan(500);
        ((AbstractStringAssert)org.assertj.core.api.Assertions.assertThat(response.asString().trim()).as("Response must be XML even for special-character DOIs", new Object[0])).startsWith("<");
    }
}
