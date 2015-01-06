package restx.config;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import restx.common.ConfigElement;
import restx.common.RestxConfig;
import restx.common.StdRestxConfig;

/**
 * User: xavierhanin
 * Date: 9/24/13
 * Time: 11:54 PM
 */
public class ConfigLoader {
    private final static Logger logger = LoggerFactory.getLogger(ConfigLoader.class);

    private final String env;

    public ConfigLoader(Optional<String> env) {
        this.env = env.or("default");
    }

    /**
     * Provides a ConfigSupplier loading config from a classpath resource.
     *
     * The name of the resource provided must not contain the extension, which must be .properties
     *
     * The loader will first try to load an env specific resource named [resource].[env].properties.
     * Then it will load a resource named [resource].properties.
     *
     * @param resource the path of the resource to load, without extension.
     *
     * @return a ConfigSupplier ready to load corresponding resource.
     */
    public ConfigSupplier fromResource(final String resource) {
        return new ConfigSupplier() {
            @Override
            public RestxConfig get() {
                List<ConfigElement> elements = new ArrayList<>();
                loadResourceInto(resource + "." + env + ".properties", elements);
                loadResourceInto(resource + ".properties", elements);
                return StdRestxConfig.of(elements);
            }
        };
    }

    private void loadResourceInto(String name, List<ConfigElement> elements) {
        URL r;
        r = Thread.currentThread().getContextClassLoader().getResource(name);
        if (r != null) {
            try {
                Iterable<ConfigElement> loadedElements = StdRestxConfig.parse("classpath:" + name,
                        Resources.asCharSource(r, Charsets.UTF_8)).elements();
                Iterables.addAll(elements, loadedElements);
                logger.debug("loaded {} elements from {}", Iterables.size(loadedElements), name);
            } catch (IOException e) {
                logger.warn("can't load " + name + ": " + e.getMessage(), e);
            }
        } else {
            logger.debug("no settings loaded from {}: resource not available", name);
        }
    }
}
