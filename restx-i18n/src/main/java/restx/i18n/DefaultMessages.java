package restx.i18n;

import com.google.common.base.Optional;
import com.google.common.collect.Ordering;
import com.google.common.io.Resources;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import restx.common.MoreResources;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Date: 25/1/14
 * Time: 14:37
 */
public class DefaultMessages extends AbstractMessages implements Messages {
    private final String baseName;
    private final Charset charset;

    public DefaultMessages(String baseName) {
        this(baseName, StandardCharsets.UTF_8);
    }

    public DefaultMessages(String baseName, Charset charset) {
        this.baseName = baseName;
        this.charset = charset;
    }

    public String getBaseName() {
        return baseName;
    }

    public Charset getCharset() {
        return charset;
    }

    @Override
    public String getMessage(String key, MessageParams params, Locale locale) {
        return getTemplate(key, locale).execute(params.toMap());
    }

    @Override
    public Iterable<String> keys() {
        Optional<ResourceBundle> bundle = getBundle(Locale.ROOT);
        return bundle.isPresent() ? Ordering.natural().sortedCopy(bundle.get().keySet()) : Collections.<String>emptySet();
    }

    @Override
    public String getMessageTemplate(String key, Locale locale) {
        return getTemplateString(key, locale);
    }

    protected Template getTemplate(String key, Locale locale) {
        return Mustache.compiler().escapeHTML(false).compile(getTemplateString(key, locale));
    }

    protected String getTemplateString(String key, Locale locale) {
        Optional<ResourceBundle> bundle = getBundle(locale);
        if (!bundle.isPresent()) {
            return key + "-[" + locale + "]";
        }
        try {
            return bundle.get().getString(key);
        } catch (MissingResourceException e) {
            return key + "-[" + locale + "]";
        }
    }

    protected Optional<ResourceBundle> getBundle(Locale locale) {
        ResourceBundle.Control control = new ResourceBundle.Control() {
            @Override
            public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
                    throws IllegalAccessException, InstantiationException, IOException {
                String bundleName = toBundleName(baseName, locale);
                String resourceName = toResourceName(bundleName, "properties");

                try {
                    final URL resource = getResource(resourceName);
                    try (InputStreamReader input = Resources.newReaderSupplier(resource, charset).getInput()) {
                        return newResourceBundle(resource, input);
                    }
                } catch (IllegalArgumentException e) {
                    return null;
                }
            }

            @Override
            public long getTimeToLive(String baseName, Locale locale) {
                return DefaultMessages.this.getTimeToLive();
            }
        };

        try {
            return Optional.of(ResourceBundle.getBundle(baseName, locale, control));
        } catch (MissingResourceException e) {
            return Optional.absent();
        }
    }

    protected PropertyResourceBundle newResourceBundle(URL resource, InputStreamReader input) throws IOException {
        return new PropertyResourceBundle(input);
    }

    protected long getTimeToLive() {
        return ResourceBundle.Control.TTL_NO_EXPIRATION_CONTROL;
    }

    protected URL getResource(String resourceName) {
        return MoreResources.getResource(resourceName, false);
    }

    @Override
    public String toString() {
        return "DefaultMessages{" +
                "baseName='" + baseName + '\'' +
                ", charset=" + charset +
                '}';
    }
}
