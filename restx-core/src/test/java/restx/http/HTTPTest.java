package restx.http;

import com.google.common.base.Optional;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * User: xavierhanin
 * Date: 2/20/13
 * Time: 1:57 PM
 */
public class HTTPTest {
    @Test
    public void should_extract_charset() throws Exception {
        assertThat(HTTP.charsetFromContentType("text/html; charset=UTF-8")).isEqualTo(Optional.of("UTF-8"));
        assertThat(HTTP.charsetFromContentType("application/json; charset=ISO-8859-1")).isEqualTo(Optional.of("ISO-8859-1"));
        assertThat(HTTP.charsetFromContentType("application/json")).isEqualTo(Optional.<String>absent());
    }

    @Test
    public void should_find_mime_type() throws Exception {
        assertThat(HTTP.getContentTypeFromExtension("test.js")).isEqualTo(Optional.of("application/x-javascript"));
        assertThat(HTTP.getContentTypeFromExtension("html")).isEqualTo(Optional.of("text/html"));
        assertThat(HTTP.getContentTypeFromExtension("css")).isEqualTo(Optional.of("text/css"));
    }
}
