package pages;

import io.restassured.response.Response;
import utils.RequestBuilder;

/**
 * CrossRef OpenURL API — Page Object Model katmanı.
 *
 * Test sınıfları doğrudan HTTP çağrısı yapmaz.
 * Tüm endpoint çağrıları bu sınıf üzerinden yapılır.
 * Böylece endpoint veya parametre değiştiğinde sadece bu sınıf güncellenir.
 */
public class OpenUrlApi {

    private static final String ENDPOINT   = "/openurl";
    private static final String NO_REDIRECT = "true";

    private OpenUrlApi() {
        // utility class — instantiation engellendi
    }

    // -------------------------------------------------------------------------
    // DOI bazlı sorgular
    // -------------------------------------------------------------------------

    /**
     * Geçerli bir DOI ile standart OpenURL sorgusu yapar.
     */
    public static Response queryByDoi(String doi) {
        return RequestBuilder.baseRequest()
                .queryParam("id", "doi:" + doi)
                .queryParam("noredirect", NO_REDIRECT)
                .when()
                .get(ENDPOINT);
    }

    /**
     * Belirli bir format ile DOI sorgusu yapar (unixref, unixsd vb.).
     */
    public static Response queryByDoiWithFormat(String doi, String format) {
        return RequestBuilder.baseRequest()
                .queryParam("id", "doi:" + doi)
                .queryParam("noredirect", NO_REDIRECT)
                .queryParam("format", format)
                .when()
                .get(ENDPOINT);
    }

    /**
     * redirect=false parametresi ile DOI sorgusu yapar.
     */
    public static Response queryByDoiWithRedirectFalse(String doi) {
        return RequestBuilder.baseRequest()
                .queryParam("id", "doi:" + doi)
                .queryParam("redirect", "false")
                .when()
                .get(ENDPOINT);
    }

    /**
     * Belirtilen redirect parametresi ile DOI sorgusu yapar.
     * (noredirect=true veya redirect=false gibi varyantlar için)
     */
    public static Response queryByDoiWithParam(String doi, String paramName, String paramValue) {
        return RequestBuilder.baseRequest()
                .queryParam("id", "doi:" + doi)
                .queryParam(paramName, paramValue)
                .when()
                .get(ENDPOINT);
    }

    // -------------------------------------------------------------------------
    // Metadata bazlı sorgular
    // -------------------------------------------------------------------------

    /**
     * ISSN + yazar + cilt + sayfa + tarih ile metadata sorgusu yapar.
     */
    public static Response queryByMetadata(String issn, String aulast,
                                           String volume, String spage, String date) {
        return RequestBuilder.baseRequest()
                .queryParam("issn", issn)
                .queryParam("aulast", aulast)
                .queryParam("volume", volume)
                .queryParam("spage", spage)
                .queryParam("date", date)
                .queryParam("noredirect", NO_REDIRECT)
                .when()
                .get(ENDPOINT);
    }

    /**
     * Dergi adı + yazar + tarih ile metadata sorgusu yapar.
     */
    public static Response queryByTitleAndAuthor(String title, String aulast, String date) {
        return RequestBuilder.baseRequest()
                .queryParam("title", title)
                .queryParam("aulast", aulast)
                .queryParam("date", date)
                .queryParam("noredirect", NO_REDIRECT)
                .when()
                .get(ENDPOINT);
    }

    /**
     * multihit=true ile belirsiz sorgu yapar (birden fazla sonuç beklendiğinde).
     */
    public static Response queryMultihit(String issn, String volume,
                                         String issue, String spage, String date) {
        return RequestBuilder.baseRequest()
                .queryParam("issn", issn)
                .queryParam("volume", volume)
                .queryParam("issue", issue)
                .queryParam("spage", spage)
                .queryParam("date", date)
                .queryParam("multihit", "true")
                .queryParam("noredirect", NO_REDIRECT)
                .when()
                .get(ENDPOINT);
    }

    // -------------------------------------------------------------------------
    // Negatif test senaryoları
    // -------------------------------------------------------------------------

    /**
     * pid parametresi olmadan istek gönderir (auth negatif testi).
     */
    public static Response queryWithoutPid(String doi) {
        return RequestBuilder.requestWithoutPid()
                .queryParam("id", "doi:" + doi)
                .queryParam("noredirect", NO_REDIRECT)
                .when()
                .get(ENDPOINT);
    }

    /**
     * id parametresi olmadan istek gönderir (zorunlu parametre negatif testi).
     */
    public static Response queryWithoutId() {
        return RequestBuilder.baseRequest()
                .queryParam("noredirect", NO_REDIRECT)
                .when()
                .get(ENDPOINT);
    }

    /**
     * Geçersiz DOI ile istek gönderir.
     */
    public static Response queryWithInvalidDoi(String invalidDoi) {
        return RequestBuilder.baseRequest()
                .queryParam("id", invalidDoi)
                .queryParam("noredirect", NO_REDIRECT)
                .when()
                .get(ENDPOINT);
    }

    /**
     * Boş pid ile istek gönderir.
     */
    public static Response queryWithEmptyPid(String doi) {
        return RequestBuilder.requestWithoutPid()
                .queryParam("pid", "")
                .queryParam("id", "doi:" + doi)
                .when()
                .get(ENDPOINT);
    }

    /**
     * Desteklenmeyen Accept header ile istek gönderir.
     */
    public static Response queryWithUnsupportedAccept(String doi, String acceptHeader) {
        return RequestBuilder.requestWithAccept(acceptHeader)
                .queryParam("id", "doi:" + doi)
                .queryParam("noredirect", NO_REDIRECT)
                .when()
                .get(ENDPOINT);
    }
}