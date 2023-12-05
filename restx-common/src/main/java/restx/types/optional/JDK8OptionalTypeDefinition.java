package restx.types.optional;

import com.google.common.base.Function;
import java.util.Optional;

public class JDK8OptionalTypeDefinition extends OptionalTypeDefinition.ClassBasedOptionalTypeDefinition {

    private static final Function<String, String> JDK8_OPTIONAL_EXPRESSION_TO_GUAVA_OPTIONAL_CODE_EXPRESSION = new Function<String, String>() {
        @Override
        public String apply(String currentOptionalCodeExpression) {
            return "Optional.fromNullable(" + currentOptionalCodeExpression + ".orElse(null))";
        }
    };
    private static final Function<String, String> FROM_NULLABLE_EXPRESSION_TRANSFORMER = new Function<String, String>() {
        @Override
        public String apply(String expression) {
            return "java.util.Optional.ofNullable(" + expression + ")";
        }
    };

    public JDK8OptionalTypeDefinition() {
        super(Optional.class, JDK8_OPTIONAL_EXPRESSION_TO_GUAVA_OPTIONAL_CODE_EXPRESSION, FROM_NULLABLE_EXPRESSION_TRANSFORMER);
    }
}
