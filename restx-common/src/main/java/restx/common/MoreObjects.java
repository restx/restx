package restx.common;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

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

	public static String toString(Type type) {
		if (type == null) {
			return "null";
		}
        String typeString;
        /*
            in jse8 another case should be done, using type.getTypeName() if
            type is an instanceof ParameterizedType
         */
        if (type instanceof Class) {
            typeString = ((Class) type).getName();
        } else {
            typeString = String.valueOf(type);
        }

		return  typeString + "[" + toString(Types.getRawType(type).getClassLoader()) + "]";
	}

    public static String toString(ClassLoader classLoader) {
        if (classLoader == null) {
            return "";
        }

        if (classLoader.getClass().getName().equals("sun.misc.Launcher$AppClassLoader")) {
            return "AppClassLoader";
        } else {
            String classloaderStr = classLoader.toString();
            // some classloaders have very detailed toString implementation and usually have new lines inside in
            // that cases (eg Tomcat WebappClassLoader). This breaks the layout when used in RESTX cases,
            // so we keep only the data before the new line
            classloaderStr = cutAfter(classloaderStr, '\r');
            classloaderStr = cutAfter(classloaderStr, '\n');
            return classloaderStr;
        }
    }

    private static String cutAfter(String str, char c) {
        int index = str.indexOf(c);
        if (index != -1) {
            return str.substring(0, index);
        } else {
            return str;
        }
    }
}
