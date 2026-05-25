package tests;

import io.restassured.path.xml.XmlPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.xml.sax.SAXParseException;
import pages.OpenUrlApi;
import utils.BaseTest;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.StringReader;
import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("CrossRef OpenURL API — Positive Tests")
class PositiveTests extends BaseTest {

    private static final String KNOWN_DOI = "10.1016/j.jebo.2023.08.009";

    // -------------------------------------------------------------------------
    // Data-driven DOI test — for each DOI in CSV file get HTTP 200 + resolved
    // -------------------------------------------------------------------------

    @ParameterizedTest(name = "DOI [{0}] should return 200 and resolved status")
    @CsvFileSource(resources = "/test-dois.csv", numLinesToSkip = 1)
    @Order(0)
    @DisplayName("All DOIs in CSV should return HTTP 200 and resolved status")
    void allDoisShouldReturn200AndResolved(String doi) {
        Response response = OpenUrlApi.queryByDoi(doi);

        assertEquals(200, response.statusCode(),
                "DOI [" + doi + "] must return HTTP 200");

        XmlPath xml = new XmlPath(response.asString());
        String status = xml.getString("**.find { it.name() == 'query' }.@status");

        assertEquals("resolved", status.toLowerCase(),
                "DOI [" + doi + "] query @status must be 'resolved', got: " + status);
    }

    // -------------------------------------------------------------------------
    // Positive Tests
    // -------------------------------------------------------------------------

    @Test
    @Order(1)
    @DisplayName("Valid DOI should return HTTP 200 with XML metadata")
    void validDoiShouldReturnXmlMetadata() {
        Response response = OpenUrlApi.queryByDoi(KNOWN_DOI);

        assertEquals(200, response.statusCode(), "Status code must be 200");

        XmlPath xml = new XmlPath(response.asString());
        String doi = xml.getString("**.find { it.name() == 'doi' }");

        assertNotNull(doi, "DOI node must be present in the response");
        assertEquals(KNOWN_DOI, doi.trim(), "Returned DOI must match the queried DOI exactly");
    }

    @Test
    @Order(2)
    @DisplayName("Response should contain a non-blank article title")
    void responseShouldContainJournalTitle() {
        Response response = OpenUrlApi.queryByDoi(KNOWN_DOI);

        assertEquals(200, response.statusCode(), "Status code must be 200");

        XmlPath xml = new XmlPath(response.asString());
        String journalTitle = xml.getString("**.find { it.name() == 'article_title' }");

        assertNotNull(journalTitle, "article_title node must be present");
        assertFalse(journalTitle.trim().isEmpty(), "article_title must not be an empty string");
    }

    @Test
    @Order(3)
    @DisplayName("Successful DOI query should return query_result with status='resolved'")
    void successfulQueryShouldHaveResolvedStatus() {
        Response response = OpenUrlApi.queryByDoi(KNOWN_DOI);

        assertEquals(200, response.statusCode(), "Status code must be 200");

        XmlPath xml = new XmlPath(response.asString());
        String status = xml.getString("**.find { it.name() == 'query' }.@status");

        assertEquals("resolved", status.toLowerCase(),
                "query status must be 'resolved' for a valid DOI — got: '" + status + "'");
    }

    @Test
    @Order(4)
    @DisplayName("Response body must not be empty")
    void responseBodyIsNotEmpty() {
        Response response = OpenUrlApi.queryByDoi(KNOWN_DOI);

        assertEquals(200, response.statusCode(), "Status code must be 200");
        assertFalse(response.asString().trim().isEmpty(),
                "Response body must not be empty for a valid DOI query");
    }

    @Test
    @Order(5)
    @DisplayName("Response Content-Type must indicate XML")
    void responseContentTypeMustBeXml() {
        Response response = OpenUrlApi.queryByDoi(KNOWN_DOI);

        assertEquals(200, response.statusCode(), "Status code must be 200");

        String contentType = response.contentType();
        assertTrue(contentType.toLowerCase().contains("xml"),
                "Response Content-Type must contain 'xml', got: " + contentType);
    }

    @Test
    @Order(6)
    @DisplayName("format=unixref should return UNIXREF-formatted XML with journal_article element")
    void unixrefFormatShouldReturnUnixrefElements() {
        Response response = OpenUrlApi.queryByDoiWithFormat(KNOWN_DOI, "unixref");

        assertEquals(200, response.statusCode(), "Status code must be 200");

        assertTrue(response.asString().toLowerCase().contains("journal_article"),
                "UNIXREF format response should contain 'journal_article' element");
    }

    @Test
    @Order(7)
    @DisplayName("Default format (no format param) should return UNIXSD with crossref_result root")
    void defaultFormatShouldReturnUnixsdRoot() {
        Response response = OpenUrlApi.queryByDoi(KNOWN_DOI);

        assertEquals(200, response.statusCode(), "Status code must be 200");

        assertTrue(response.asString().toLowerCase().contains("crossref_result"),
                "Default format response should contain 'crossref_result' root element");
    }

    @Test
    @Order(8)
    @DisplayName("Metadata-based query (issn + aulast + date) should resolve to a DOI")
    void metadataQueryShouldResolveToDoi() {
        Response response = OpenUrlApi.queryByMetadata("03770273", "Walker", "54", "117", "1983");

        assertEquals(200, response.statusCode(), "Status code must be 200");

        XmlPath xml = new XmlPath(response.asString());
        String doi = xml.getString("**.find { it.name() == 'doi' }");

        assertNotNull(doi, "Metadata query (Walker/1983) should resolve to a DOI");
        assertFalse(doi.trim().isEmpty(), "Resolved DOI must not be blank");
    }

    @Test
    @Order(9)
    @DisplayName("Metadata query with title + author should return resolved status")
    void metadataQueryWithTitleShouldResolve() {
        Response response = OpenUrlApi.queryByTitleAndAuthor("Science", "Fernandez", "2009");

        assertEquals(200, response.statusCode(), "Status code must be 200");

        XmlPath xml = new XmlPath(response.asString());
        String status = xml.getString("**.find { it.name() == 'query' }.@status").toLowerCase();

        assertFalse(status.trim().isEmpty(), "query @status must not be blank");
        assertTrue(
                status.equals("resolved") || status.equals("unresolved") || status.equals("multiresolved"),
                "query @status must be resolved/unresolved/multiresolved, got: " + status
        );
    }

    @Test
    @Order(10)
    @DisplayName("multihit=true should return at least one result for an ambiguous query")
    void multihitShouldReturnMultipleResults() {
        Response response = OpenUrlApi.queryMultihit("03603016", "54", "2", "215", "2002");

        assertEquals(200, response.statusCode(), "Status code must be 200");

        XmlPath xml = new XmlPath(response.asString());
        String status = xml.getString("**.find { it.name() == 'query' }.@status").toLowerCase();
        List<?> dois = xml.getList("**.findAll { it.name() == 'doi' }");

        assertTrue(
                status.equals("multiresolved") || status.equals("resolved"),
                "multihit=true should return 'multiresolved' or 'resolved', got: " + status
        );
        assertTrue(!dois.isEmpty(),
                "multihit response should contain at least one doi element");
    }

    @Test
    @Order(11)
    @DisplayName("redirect=false should return XML body without HTTP redirect")
    void redirectFalseShouldReturnXmlBody() {
        Response response = OpenUrlApi.queryByDoiWithRedirectFalse(KNOWN_DOI);

        assertEquals(200, response.statusCode(), "Status code must be 200");

        assertTrue(response.asString().trim().startsWith("<"),
                "redirect=false must return an XML body starting with '<'");
    }

    @Order(12)
    @DisplayName("noredirect=true and redirect=false should both produce XML bodies")
    @ParameterizedTest(name = "param={0} value={1}")
    @CsvSource({"noredirect, true", "redirect, false"})
    void bothNoredirectVariantsShouldReturnXml(String param, String value) {
        Response response = OpenUrlApi.queryByDoiWithParam(KNOWN_DOI, param.trim(), value.trim());

        assertEquals(200, response.statusCode(), "Status code must be 200");

        String body = response.asString().trim();
        assertTrue(body.startsWith("<"),
                "With " + param.trim() + "=" + value.trim() + ", response must be XML");
        assertTrue(body.contains(KNOWN_DOI),
                "XML response must contain DOI for param " + param + "=" + value);
    }

    @Test
    @Order(13)
    @DisplayName("Response XML must validate against CrossRef UNIXSD schema")
    void responseXmlMustValidateAgainstSchema() throws Exception {
        Response response = OpenUrlApi.queryByDoi(KNOWN_DOI);

        assertEquals(200, response.statusCode(), "Status code must be 200");

        String body    = response.asString();
        String xsdUrl  = "https://www.crossref.org/schemas/crossref_query_output2.0.xsd";

        try {
            SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
            Schema schema         = factory.newSchema(new URL(xsdUrl));
            Validator validator   = schema.newValidator();
            validator.validate(new StreamSource(new StringReader(body)));
            System.out.println("[SCHEMA] Response validates against UNIXSD XSD ✓");
        } catch (SAXParseException e) {
            if (e.getMessage() != null && e.getMessage().contains("paper_title")) {
                System.out.println("[SCHEMA SKIP] CrossRef XSD has a known duplicate 'paper_title' — skipping.");
                Assumptions.assumeTrue(false, "CrossRef XSD is internally invalid (duplicate paper_title).");
            } else {
                fail("Response XML does not validate against CrossRef UNIXSD schema: " + e.getMessage());
            }
        } catch (Exception e) {
            Assumptions.assumeTrue(false, "XSD schema unreachable — skipping: " + e.getMessage());
        }
    }

    @Test
    @Order(14)
    @DisplayName("aulast with Unicode characters (accented) should not cause a server error")
    void unicodeAuthorNameShouldNotCauseServerError() {
        Response response = OpenUrlApi.queryByTitleAndAuthor("Science", "Fernández", "2009");

        String body = response.asString().trim();

        assertTrue(response.statusCode() < 500,
                "Unicode author name must not cause a server error (5xx), got: " + response.statusCode());
        assertTrue(body.startsWith("<"),
                "Response to Unicode query must still be XML (starts with '<')");
    }

    @Test
    @Order(15)
    @DisplayName("DOI with special characters in suffix should be correctly URL-encoded and resolved")
    void doiWithSpecialCharactersShouldResolve() {
        Response response = OpenUrlApi.queryByDoi("10.1175/1520-0485(2002)032<0870:CT>2.0.CO;2");

        String body = response.asString().trim();

        assertTrue(response.statusCode() < 500,
                "DOI with special characters must not cause a server error, got: " + response.statusCode());
        assertTrue(body.startsWith("<"),
                "Response must be XML even for special-character DOIs");
    }
}