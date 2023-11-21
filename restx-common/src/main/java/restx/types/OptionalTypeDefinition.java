package restx.types;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Optional;

import java.util.regex.Pattern;

public interface OptionalTypeDefinition {
    class Matcher {
        private final boolean isOptionalType;
        private final String underlyingType;
        private final Function<String, String> currentOptionalExpressionToGuavaOptionalCodeExpressionTransformer;
        private final Function<String, String> fromNullableExpressionTransformer;

        public Matcher(boolean isOptionalType, String underlyingType,
                       Function<String, String> currentOptionalExpressionToGuavaOptionalCodeExpressionTransformer,
                       Function<String, String> fromNullableExpressionTransformer) {

            this.isOptionalType = isOptionalType;
            this.underlyingType = underlyingType;
            this.currentOptionalExpressionToGuavaOptionalCodeExpressionTransformer = currentOptionalExpressionToGuavaOptionalCodeExpressionTransformer;
            this.fromNullableExpressionTransformer = fromNullableExpressionTransformer;
        }

        public boolean isOptionalType() {
            return isOptionalType;
        }

        public String getUnderlyingType() {
            return underlyingType;
        }

        public Function<String, String> getCurrentOptionalExpressionToGuavaOptionalCodeExpressionTransformer() {
            return currentOptionalExpressionToGuavaOptionalCodeExpressionTransformer;
        }

        public Function<String, String> getFromNullableExpressionTransformer() {
            return fromNullableExpressionTransformer;
        }

        public static Matcher notOptional(String underlyingType) {
            return new Matcher(false, underlyingType, null, null);
        }
        public static Matcher optionalOf(String underlyingType,
                                         Function<String, String> currentOptionalExpressionToGuavaOptionalCodeExpressionTransformer,
                                         Function<String, String> fromNullableExpressionTransformer) {
            return new Matcher(true, underlyingType, currentOptionalExpressionToGuavaOptionalCodeExpressionTransformer, fromNullableExpressionTransformer);
        }
    }

    Matcher matches(String type);

    abstract class ClassBasedOptionalTypeDefinition implements OptionalTypeDefinition {
        private final Pattern optionalRegex;
        private final Function<String, String> currentOptionalExpressionToGuavaOptionalCodeExpressionTransformer;
        private final Function<String, String> fromNullableExpressionTransformer;

        protected ClassBasedOptionalTypeDefinition(
                Class clazz,
                Function<String, String> currentOptionalExpressionToGuavaOptionalCodeExpressionTransformer,
                Function<String, String> fromNullableExpressionTransformer) {

            this.optionalRegex = Pattern.compile("\\Q" + clazz.getName() + "<\\E(.+)>");
            this.currentOptionalExpressionToGuavaOptionalCodeExpressionTransformer = currentOptionalExpressionToGuavaOptionalCodeExpressionTransformer;
            this.fromNullableExpressionTransformer = fromNullableExpressionTransformer;
        }

        public Matcher matches(String type) {
            java.util.regex.Matcher matcher = this.optionalRegex.matcher(type);
            if(matcher.matches()) {
                return Matcher.optionalOf(matcher.group(1), this.currentOptionalExpressionToGuavaOptionalCodeExpressionTransformer, this.fromNullableExpressionTransformer);
            } else {
                return Matcher.notOptional(type);
            }
        }
    }

    class GUAVA extends ClassBasedOptionalTypeDefinition {
        private static final Function<String, String> FROM_NULLABLE_EXPRESSION_TRANSFORMER = new Function<String, String>() {
            @Override
            public String apply(String expression) {
                return "Optional.fromNullable(" + expression + ")";
            }
        };

        public GUAVA() {
            super(Optional.class, Functions.<String>identity(), FROM_NULLABLE_EXPRESSION_TRANSFORMER);
        }
    }
}
