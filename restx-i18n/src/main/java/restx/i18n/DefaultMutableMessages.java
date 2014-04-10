package restx.i18n;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import restx.common.MoreResources;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Date: 25/1/14
 * Time: 15:31
 */
public class DefaultMutableMessages extends DefaultMessages implements MutableMessages {
    public DefaultMutableMessages(String baseName) {
        super(baseName);
    }

    public DefaultMutableMessages(String baseName, Charset charset) {
        super(baseName, charset);
    }

    @Override
    public MutableMessages setMessageTemplate(String key, String messageTemplate, Locale locale) throws IOException {
        Optional<ResourceBundle> b = getBundle(locale);
        if (!b.isPresent()) {
            throw new IllegalStateException("bundle not found: " + getBaseName() + " - " + locale);
        }
        MutablePropertyResourceBundle bundle = (MutablePropertyResourceBundle) b.get();
        bundle.setMessageTemplate(key, messageTemplate);
        return this;
    }

    protected PropertyResourceBundle newResourceBundle(final URL resource, InputStreamReader input) throws IOException {
        return new MutablePropertyResourceBundle(input, resource, getCharset());
    }

    protected long getTimeToLive() {
        return ResourceBundle.Control.TTL_DONT_CACHE;
    }

    protected URL getResource(String resourceName) {
        return MoreResources.getResource(resourceName, true);
    }

    private static class MutablePropertyResourceBundle extends PropertyResourceBundle {
        private final URL resource;
        private final Charset charset;

        public MutablePropertyResourceBundle(InputStreamReader input, URL resource, Charset charset) throws IOException {
            super(input);
            this.resource = resource;
            this.charset = charset;
        }

        public void setMessageTemplate(String key, String value) throws IOException {
            if (!resource.getProtocol().equals("file")) {
                throw new IllegalStateException(
                        "can't set message when resource bundle is not loaded from a file." +
                                " It was loaded from " + resource);
            }
            File f;
            try {
                f = new File(resource.toURI());
            } catch(URISyntaxException e) {
                f = new File(resource.getPath());
            }
            updateProperties(f, charset, ImmutableMap.of(key, value));
        }
    }


    private static void updateProperties(File file, Charset charset, ImmutableMap<String, String> properties) throws IOException {
        Map<String, String> props = new LinkedHashMap<>(properties);
        String s = Files.toString(file, charset);
        StringBuilder out = new StringBuilder();

        boolean updated = false;
        String[] lines = s.split("(?m)$");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.indexOf('=') != -1) {
                String p = line.substring(0, line.indexOf('='));
                if (props.containsKey(p.trim())) {
                    updated = true;
                    out.append(p).append("=").append(properties.get(p.trim()));
                    props.remove(p.trim());
                } else {
                    out.append(line);
                }
            } else {
                out.append(line);
            }
        }
        if (!props.isEmpty()) {
            updated = true;
            for (Map.Entry<String, String> entry : props.entrySet()) {
                out.append("\n").append(entry.getKey()).append("=").append(entry.getValue());
            }
            out.append("\n");
        }

        if (updated) {
            Files.write(out.toString(), file, charset);
        }
    }
}
