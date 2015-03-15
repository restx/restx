package restx.classloader;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper methods to manage cold classes.
 *
 * @author apeyrard
 */
public class ColdClasses {
	private static final Logger logger = LoggerFactory.getLogger(ColdClasses.class);

	private ColdClasses() {}

	/**
	 * Extracts the cold classes from a string containing a list of cold classes.
	 * <p>
	 * The {@code coldClasses} parameter contains a list of fqcn separated by ':' character.
	 *
	 * @param classLoader the classloader to load cold classes
	 * @param coldClasses all cold classes in a string
	 * @return a set of cold classes
	 */
	public static ImmutableSet<Class<?>> extractFromString(ClassLoader classLoader, String coldClasses) {
		ImmutableSet.Builder<Class<?>> classes = ImmutableSet.builder();
		for (String fqcn : Splitter.on(':').split(coldClasses)) {
			try {
				classes.add(classLoader.loadClass(fqcn));
			} catch (ClassNotFoundException e) {
				logger.warn("invalid cold class {}: unable to find it from supplied classloader", fqcn);
			}
		}
		return classes.build();
	}
}
