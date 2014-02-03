package restx.tests.json;

import com.google.common.io.CharStreams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Date: 3/2/14
 * Time: 22:10
 */
public class URLJsonSource implements JsonSource {
    private final String name;
    private final Charset cs;
    private final URL url;

    public URLJsonSource(URL url, Charset cs) {
        this.name = url.toString();
        this.cs = cs;
        this.url = url;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String content() {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(url.openStream(), cs))) {
            try {
                return CharStreams.toString(r);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
