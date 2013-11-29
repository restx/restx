package restx.common;

/**
 * Date: 29/11/13
 * Time: 11:13
 */
public class MoreObjects {
    public static String toString(Class clazz) {
        if (clazz == null) {
            return "null";
        }
        return clazz.getName() + "[" + toString(clazz.getClassLoader()) + "]";
    }

    public static String toString(ClassLoader classLoader) {
        if (classLoader == null) {
            return "";
        }

        if (classLoader.getClass().getName().equals("sun.misc.Launcher$AppClassLoader")) {
            return "AppClassLoader";
        } else {
            return classLoader.toString();
        }
    }
}
