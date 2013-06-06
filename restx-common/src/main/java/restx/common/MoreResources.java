package restx.common;

import com.google.common.io.Resources;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;

/**
 * User: xavierhanin
 * Date: 6/2/13
 * Time: 9:13 PM
 */
public class MoreResources {
    public static URL getResource(String resourceName, boolean searchInSources) {
        URL resource = null;
        if (searchInSources) {
            // in DEV mode we try to load resource from sources if we find them
            Set<URL> urls = ClasspathHelper.forResource(resourceName);
            for (URL url : urls) {
                if (url.getProtocol().equals("file")) {
                    for (String classesLocation : asList("target/classes/", "bin/")) {
                        if (url.getFile().endsWith(classesLocation)) {
                            for (String sourcesLocation : asList("src/main/resources/", "src/")) {
                                File file = new File(url.getFile().replace(classesLocation, ""),
                                                        sourcesLocation + resourceName);
                                if (file.exists()) {
                                    try {
                                        resource = file.toURI().toURL();
                                    } catch (MalformedURLException e) {
                                        // ignored
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        if (resource == null) {
            resource = Resources.getResource(resourceName);
        }
        return resource;
    }

    public static Map<String, URL> findResources(String packageName, final Pattern p, boolean searchInSources) {
        final Map<String, URL> resourcesUrls = new LinkedHashMap<>();

        if (searchInSources) {
            // here we try to find resources from sources
            Set<Path> sourceRoots = findSourceRoots();
            for (final Path sourceRoot : sourceRoots) {
                try {
                    Files.walkFileTree(sourceRoot, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                            if (p.matcher(file.getFileName().toString()).matches()) {
                                try {
                                    String resource = sourceRoot.relativize(file).toString();
                                    if (!resourcesUrls.containsKey(resource)) {
                                        resourcesUrls.put(resource, file.toUri().toURL());
                                    }
                                } catch (MalformedURLException e) {
                                    // ignore
                                }
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    });
                } catch (IOException e) {
                    // ignore
                }
            }
        }

        // now we search for real resources, but avoid duplicates, especially for the one found in sources
        for (String r : new ConfigurationBuilder()
                        .setUrls(ClasspathHelper.forPackage(packageName))
                        .setScanners(new ResourcesScanner())
                        .build()
                        .getResources(p)) {
            if (!resourcesUrls.containsKey(r)) {
                resourcesUrls.put(r, Resources.getResource(r));
            }
        }
        return resourcesUrls;
    }

    private static Set<Path> findSourceRoots() {
        Set<Path> sourceRoots = new LinkedHashSet<>();
        Set<URL> urls = ClasspathHelper.forClassLoader();
        for (URL url : urls) {
            if (url.getProtocol().equals("file")) {
                for (String classesLocation : asList("target/classes/", "bin/")) {
                    if (url.getFile().endsWith(classesLocation)) {
                        for (String sourcesLocation : asList("src/main/resources/", "src/")) {
                            final File sourceRoot = new File(url.getFile().replace(classesLocation, ""),
                                    sourcesLocation);
                            if (sourceRoot.exists()) {
                                sourceRoots.add(sourceRoot.toPath());
                                break;
                            }
                        }
                    }
                }
            }
        }
        return sourceRoots;
    }
}
