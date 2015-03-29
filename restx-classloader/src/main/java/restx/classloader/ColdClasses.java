package restx.classloader;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

/**
 * Helper methods to manage cold classes.
 *
 * @author apeyrard
 */
public class ColdClasses {
	private static final Logger logger = LoggerFactory.getLogger(ColdClasses.class);

	public static final String COLD_CLASSES_FILE_PATH = "META-INF/cold-classes.list";

	private ColdClasses() {}

	/**
	 * Extracts the cold classes from a string containing a list of cold classes.
	 * <p>
	 * The {@code coldClasses} parameter contains a list of fqcn separated by ',' character.
	 *
	 * @param classLoader the classloader to load cold classes
	 * @param coldClasses all cold classes in a string
	 * @return a set of cold classes
	 */
	public static ImmutableSet<Class<?>> extractFromString(ClassLoader classLoader, String coldClasses) {
		ImmutableSet.Builder<Class<?>> classes = ImmutableSet.builder();
		for (String fqcn : Splitter.on(',').trimResults().split(coldClasses)) {
			try {
				classes.add(classLoader.loadClass(fqcn));
			} catch (ClassNotFoundException e) {
				logger.warn("invalid cold class {}: unable to find it from supplied classloader", fqcn);
			}
		}
		return classes.build();
	}

	/**
	 * Extracts the cold classes from resources file.
	 *
	 * @param classLoader the classloader to load cold classes and resources
	 * @return the list of cold classes
	 * @throws java.io.IOException if an I/O error occurs
	 */
	public static ImmutableSet<Class<?>> extractFromResources(final ClassLoader classLoader) throws IOException {
		ImmutableSet.Builder<Class<?>> classes = ImmutableSet.builder();

		Enumeration<URL> resources = classLoader.getResources(COLD_CLASSES_FILE_PATH);
		while (resources.hasMoreElements()) {
			for (String fqcn : Resources.readLines(resources.nextElement(), Charsets.UTF_8)) {
				try {
					classes.add(classLoader.loadClass(fqcn));
				} catch (ClassNotFoundException e) {
					logger.warn("invalid cold class {}: unable to find it from supplied classloader", fqcn);
				}
			}
		}

		return classes.build();
	}
}
