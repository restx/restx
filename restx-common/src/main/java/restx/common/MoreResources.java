package restx.common;

import com.google.common.io.Resources;
import org.reflections.util.ClasspathHelper;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

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
}
