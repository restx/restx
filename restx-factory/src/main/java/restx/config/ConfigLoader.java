package restx.config;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import restx.common.ConfigElement;
import restx.common.RestxConfig;
import restx.common.StdRestxConfig;

import static com.google.common.io.Files.asCharSource;

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
     * The loader will first try to load config from a file which location is provided by a system
     * property, the name of the property being the name returned by #locationKeyForResource.
     *
     * Then it will try to load an env specific resource named [resource].[env].properties.
     *
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

                loadAllFromResource(elements, resource);

                return StdRestxConfig.of(elements);
            }
        };
    }

    /**
     * Provides a ConfigSupplier loading config from a file.
     *
     * The loader will first try to load an env specific file named [path].[env].properties.
     *
     * Then it will try to load a file named [path].properties.
     *
     * Then it will try to load a file named [path] (without extension).
     *
     * @param path the path of the file to load config from.
     *
     * @return a ConfigSupplier ready to load corresponding file.
     */
    public ConfigSupplier fromFile(final String path) {
        return new ConfigSupplier() {
            @Override
            public RestxConfig get() {
                List<ConfigElement> elements = new ArrayList<>();

                loadAllFromFile(elements, path);

                return StdRestxConfig.of(elements);
            }
        };
    }

    /**
     * Gives the name of the system property that can be used to provide a file location
     * to load to override settings for a particular resource.
     *
     * By defaut the name is equal to the resource name where slashes `/` are replaced by dots `.`,
     * with `.location` suffix.
     *
     * Eg.
     * `myapp/settings` becomes `myapp.settings.location`
     *
     * @param resource the resource for which the system property name should be provided.
     *
     * @return the system property name.
     */
    public String locationKeyForResource(String resource) {
        return resource.replace('/', '.') + ".location";
    }

    protected void loadAllFromResource(List<ConfigElement> elements, String resource) {
        String locationKey = locationKeyForResource(resource);
        String location = System.getProperty(locationKey);
        if (location != null) {
            Path path = Paths.get(location).toAbsolutePath();
            logger.info("loading {} settings from {}", resource, path);
            loadFileInto(path, elements);
        } else {
            logger.debug("system property `{}` is not set, no file to load to override settings from {}",
                    locationKey, resource);
        }

        loadResourceInto(resource + "." + env + ".properties", elements);
        loadResourceInto(resource + ".properties", elements);
    }

    protected void loadAllFromFile(List<ConfigElement> elements, String path) {
        if (!path.endsWith(".properties")) {
            loadFileInto(Paths.get(path + "." + env + ".properties"), elements);
            loadFileInto(Paths.get(path + ".properties"), elements);
        }

        loadFileInto(Paths.get(path), elements);
    }

    protected void loadFileInto(Path path, List<ConfigElement> elements) {
        File file = path.toFile().getAbsoluteFile();
        if (!file.exists()) {
            logger.debug("no settings loaded from {}: file not available", file);
            return;
        }

        if (!file.isFile()) {
            logger.warn("no settings loaded from {}: this is not a file", file);
            return;
        }

        try {
            Iterable<ConfigElement> loadedElements = StdRestxConfig.parse("file://" + file,
                    asCharSource(file, Charsets.UTF_8)).elements();
            Iterables.addAll(elements, loadedElements);
            logger.debug("loaded {} elements from {}", Iterables.size(loadedElements), file);
        } catch (IOException e) {
            logger.warn("can't load " + file + ": " + e.getMessage(), e);
        }

    }

    protected void loadResourceInto(String name, List<ConfigElement> elements) {
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
