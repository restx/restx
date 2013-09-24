package restx.common;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.factory.Component;

import javax.inject.Named;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * User: xavierhanin
 * Date: 9/24/13
 * Time: 11:54 PM
 */
@Component
public class ConfigLoader {
    private final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);

    private final String env;

    public ConfigLoader(@Named("env") Optional<String> env) {
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
                Iterables.addAll(elements, StdRestxConfig.parse("classpath:" + name,
                        Resources.newReaderSupplier(r, Charsets.UTF_8)).elements());
            } catch (IOException e) {
                logger.warn("can't load " + name + ": " + e.getMessage(), e);
            }
        }
    }
}
