package restx.build;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URL;
import java.util.regex.Pattern;

/**
 * User: xavierhanin
 * Date: 4/15/13
 * Time: 12:32 PM
 */
public class ModuleFragment {
    private final String url;
    private boolean lazyContentLoaded = false;
    private String lazyContent = null;

    public ModuleFragment(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void write(ModuleDescriptor md, Writer w) throws IOException {
        String content = getLazilyContent();
        if(content != null) {
            w.write(content);
        }
    }

    public boolean resolvedContent() {
        try {
            String content = getLazilyContent();
            return content != null;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean matches(Pattern pattern) throws IOException {
        String content = getLazilyContent();
        return pattern.matcher(content).matches();
    }

    public String getLazilyContent() throws IOException {
        if(!lazyContentLoaded) {
            try {
                if (url.startsWith("classpath://")) {
                    String fragmentPath = url.substring("classpath://".length());
                    try(InputStream stream = getClass().getResourceAsStream(fragmentPath)) {
                        if (stream == null) {
                            throw new IllegalArgumentException("classpath fragment not found: '" + fragmentPath + "'" +
                                    ". Check your classpath.");
                        }
                        lazyContent = RestxBuildHelper.toString(stream);
                    }
                } else {
                    URL fragmentUrl = new URL(url);
                    try (InputStream stream = fragmentUrl.openStream()) {
                        lazyContent = RestxBuildHelper.toString(stream);
                    }
                }
            } finally {
                lazyContentLoaded = true;
            }
        }
        return lazyContent;
    }
}
