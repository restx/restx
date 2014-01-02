package restx;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.*;

/**
 * User: xavierhanin
 * Date: 4/2/13
 * Time: 9:39 PM
 */
public class StdRequestTest {
    @Test
    public void should_build_restx_uri() throws Exception {
        StdRequest request = StdRequest.builder().setBaseUri("http://localhost:8080/api").setRestxPath("/message")
                .setQueryParams(ImmutableMap.<String, ImmutableList<String>>of("q", ImmutableList.of("val1", "val2"))).build();
        assertThat(request.getBaseUri()).isEqualTo("http://localhost:8080/api");
        assertThat(request.getBaseApiPath()).isEqualTo("/api");
        assertThat(request.getRestxPath()).isEqualTo("/message");
        assertThat(request.getRestxUri()).isEqualTo("/message?q=val1&q=val2");
    }

    @Test
    public void should_build_restx_uri_from_full_path() throws Exception {
        StdRequest request = StdRequest.builder().setBaseUri("http://localhost:8080/api")
                .setFullPath("/message?q=val1&q=val2")
                .build();
        assertThat(request.getBaseUri()).isEqualTo("http://localhost:8080/api");
        assertThat(request.getRestxPath()).isEqualTo("/message");
        assertThat(request.getRestxUri()).isEqualTo("/message?q=val1&q=val2");
    }

    // For Accept-Language header, see spec http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.4
    @Test
    public void should_get_default_locale() throws Exception {
        StdRequest request = StdRequest.builder().setBaseUri("http://localhost:8080/api")
                .setRestxPath("/")
                .build();
        assertThat(request.getLocale()).isEqualTo(Locale.getDefault());
    }

    @Test
    public void should_get_locale_from_header() throws Exception {
        StdRequest request = StdRequest.builder().setBaseUri("http://localhost:8080/api")
                .setRestxPath("/")
                .setHeaders(ImmutableMap.of("Accept-Language", "fr-FR"))
                .build();
        assertThat(request.getLocale()).isEqualTo(Locale.forLanguageTag("fr-FR"));
    }

    @Test
    public void should_get_locale_from_header_with_list() throws Exception {
        StdRequest request = StdRequest.builder().setBaseUri("http://localhost:8080/api")
                .setRestxPath("/")
                .setHeaders(ImmutableMap.of("Accept-Language", "fr-FR,en-US"))
                .build();
        assertThat(request.getLocale()).isEqualTo(Locale.forLanguageTag("fr-FR"));
    }

    @Test
    public void should_get_locale_from_header_with_list_with_spaces() throws Exception {
        StdRequest request = StdRequest.builder().setBaseUri("http://localhost:8080/api")
                .setRestxPath("/")
                .setHeaders(ImmutableMap.of("Accept-Language", " fr-FR , en-US "))
                .build();
        assertThat(request.getLocale()).isEqualTo(Locale.forLanguageTag("fr-FR"));
    }

    @Test
    public void should_get_locale_from_header_complex_list_with_quality() throws Exception {
        StdRequest request = StdRequest.builder().setBaseUri("http://localhost:8080/api")
                .setRestxPath("/")
                .setHeaders(ImmutableMap.of("Accept-Language", "en-US,en;q=0.8,fr;q=0.6"))
                .build();
        assertThat(request.getLocale()).isEqualTo(Locale.forLanguageTag("en-US"));
    }

    @Test
    public void should_get_locale_from_header_complex_list_with_quality_and_spaces() throws Exception {
        StdRequest request = StdRequest.builder().setBaseUri("http://localhost:8080/api")
                .setRestxPath("/")
                .setHeaders(ImmutableMap.of("Accept-Language", "en-US,en;q=0.8,fr;q=0.6"))
                .build();
        assertThat(request.getLocale()).isEqualTo(Locale.forLanguageTag("en-US"));
    }
}
