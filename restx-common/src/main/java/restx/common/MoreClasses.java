package restx.common;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Provides static utility methods to deal with {@link Class}
 *
 * @author apeyrard
 */
public class MoreClasses {
	private MoreClasses() {}

	/**
	 * Gets all inherited classes for a specified class (in a flat structure). Its super classes, and its interfaces.
	 *
	 * This process is recursive, if class A inherit from B which also inherit from C and D, the result will
	 * be (B, C, D).
	 *
	 * @param clazz the clazz to analyse
	 * @return the list of inherited classes
	 */
	public static Set<Class> getInheritedClasses(Class clazz) {
		Set<Class> inheritedClasses = Sets.newHashSet();

		// add super class, and add recursively their inherited classes
		Class superClass = clazz.getSuperclass();
		if (superClass != null) {
			inheritedClasses.add(superClass);
			inheritedClasses.addAll(getInheritedClasses(superClass));
		}

		// add all interfaces, and recursively add their inherited classes
		Class[] interfaces = clazz.getInterfaces();
		for (Class anInterface : interfaces) {
			inheritedClasses.add(anInterface);
			inheritedClasses.addAll(getInheritedClasses(anInterface));
		}

		return inheritedClasses;
	}
}
