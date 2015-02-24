package restx.annotations.processor;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Date: 23/10/13
 * Time: 10:26
 */
public class TypeHelper {
    private static Pattern PARAMETERIZED_TYPE_PATTERN = Pattern.compile("([^\\<]+)\\<(.+)\\>");
    private static Pattern guavaOptionalPattern = Pattern.compile("\\Q" + Optional.class.getName() + "<\\E(.+)>");
    private static Pattern java8OptionalPattern = Pattern.compile("\\Qjava.util.Optional<\\E(.+)>");
    private static Set<String> RAW_TYPES_STR = Sets.newHashSet("byte", "short", "int", "long", "float", "double", "boolean", "char");

    static String getTypeExpressionFor(String type) {
        Matcher matcher = PARAMETERIZED_TYPE_PATTERN.matcher(type);
        if (matcher.matches()) {
            String rawType = matcher.group(1);

            return "Types.newParameterizedType(" + rawType + ".class, " + getTypeExpressionFor(matcher.group(2)) + ")";
        } else {
            if (type.contains(",")) {
                List<String> pTypes = new ArrayList<>();
                for (String pType : Splitter.on(",").trimResults().split(type)) {
                    pTypes.add(getTypeExpressionFor(pType));
                }
                return Joiner.on(", ").join(pTypes);
            } else {
                return type + ".class";
            }

        }
    }

    static String getTypeReferenceExpressionFor(String type) {
        return RAW_TYPES_STR.contains(type) ? type+".class" : "new TypeReference<"+type+">(){}";
    }

    public static class OptionalMatchingType {
        public enum Type { GUAVA, JAVA8, NONE };
        private final Type optionalType;
        private final String underlyingType;

        protected OptionalMatchingType(Type optionalType, String underlyingType) {
            this.optionalType = optionalType;
            this.underlyingType = underlyingType;
        }

        public static OptionalMatchingType guava(String underlyingType) {
            return new OptionalMatchingType(Type.GUAVA, underlyingType);
        }
        public static OptionalMatchingType java8(String underlyingType) {
            return new OptionalMatchingType(Type.JAVA8, underlyingType);
        }
        public static OptionalMatchingType none(String underlyingType) {
            return new OptionalMatchingType(Type.NONE, underlyingType);
        }

        public Type getOptionalType() {
            return optionalType;
        }

        public String getUnderlyingType() {
            return underlyingType;
        }
    }

    public static OptionalMatchingType optionalMatchingTypeOf(String type) {
        Matcher guavaOptionalMatcher = guavaOptionalPattern.matcher(type);
        if (guavaOptionalMatcher.matches()) {
            return OptionalMatchingType.guava(guavaOptionalMatcher.group(1));
        }

        Matcher java8OptionalMatcher = java8OptionalPattern.matcher(type);
        if(java8OptionalMatcher.matches()) {
            return OptionalMatchingType.java8(java8OptionalMatcher.group(1));
        }

        return OptionalMatchingType.none(type);
    }

}
