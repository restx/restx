package restx.server;

import com.google.common.base.Optional;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * User: xavierhanin
 * Date: 2/20/13
 * Time: 1:57 PM
 */
public class HTTPTest {
    @Test
    public void should_extract_charset() throws Exception {
        assertEquals(Optional.of("UTF-8"), HTTP.charsetFromContentType("text/html; charset=UTF-8"));
        assertEquals(Optional.of("ISO-8859-1"), HTTP.charsetFromContentType("application/json; charset=ISO-8859-1"));
        assertEquals(Optional.absent(), HTTP.charsetFromContentType("application/json"));
    }
}
