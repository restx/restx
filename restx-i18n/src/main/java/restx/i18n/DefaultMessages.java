package restx.i18n;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Ordering;
import com.google.common.io.Resources;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import restx.common.MoreResources;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Date: 25/1/14
 * Time: 14:37
 */
public class DefaultMessages extends AbstractMessages implements Messages {
    private final String baseName;
    private final Charset charset;
    private final LoadingCache<Locale, Iterable<String>> cachedKeysByLocale = CacheBuilder.newBuilder().build(new CacheLoader<Locale, Iterable<String>>() {
        @Override
        public Iterable<String> load(Locale locale) throws Exception {
            Optional<ResourceBundle> bundle = getBundle(locale);
            return bundle.isPresent() ? Ordering.natural().sortedCopy(bundle.get().keySet()) : Collections.<String>emptySet();
        }
    });

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
    public Iterable<String> keys(Locale locale) {
        return cachedKeysByLocale.getUnchecked(locale);
    }

    protected void invalidateCachedKeysFor(Locale locale) {
        cachedKeysByLocale.invalidate(locale);
    }

    @Override
    public Iterable<String> keys() {
        return keys(Locale.ROOT);
    }

    @Override
    public Iterable<Entry<String, String>> entries(Locale locale) {
        Optional<ResourceBundle> rootBundle = getBundle(Locale.ROOT);
        Optional<ResourceBundle> bundle = getBundle(locale);
        if (rootBundle.isPresent()) {
            Set<String> keys = rootBundle.get().keySet();
            List<Entry<String, String>> entries = new ArrayList<>(keys.size());
            for (String key : Ordering.natural().sortedCopy(keys)) {
                entries.add(new SimpleEntry<>(key, getTemplateString(key, locale, bundle)));
            }
            return entries;
        } else {
            return Collections.emptySet();
        }
    }

    @Override
    public String getMessageTemplate(String key, Locale locale) {
        return getTemplateString(key, locale);
    }

    protected Template getTemplate(String key, Locale locale) {
        return Mustache.compiler().escapeHTML(false).compile(getTemplateString(key, locale));
    }

    protected String getTemplateString(String key, Locale locale) {
        return getTemplateString(key, locale, getBundle(locale));
    }

    private String getTemplateString(String key, Locale locale, Optional<ResourceBundle> bundle) {
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
                    try (Reader input = Resources.asCharSource(resource, charset).openStream()) {
                        return newResourceBundle(resource, input);
                    }
                } catch (IllegalArgumentException e) {
                    return null;
                }
            }

            @Override
            public List<String> getFormats(String baseName) {
                return ResourceBundle.Control.FORMAT_PROPERTIES;
            }

            @Override
            public Locale getFallbackLocale(String baseName, Locale locale) {
                if (baseName == null || locale == null) {
                    throw new NullPointerException();
                }
                return null;
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

    protected PropertyResourceBundle newResourceBundle(URL resource, Reader input) throws IOException {
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
