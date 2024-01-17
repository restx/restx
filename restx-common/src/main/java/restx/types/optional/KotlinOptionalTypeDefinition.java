package restx.types.optional;

import com.google.common.base.Function;
import com.google.common.base.Functions;

import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.stream.Stream;

public class KotlinOptionalTypeDefinition implements OptionalTypeDefinition {
    private static final String annotation = "org.jetbrains.annotations.Nullable";

    private static final Function<String, String> KOTLIN_OPTIONAL_EXPRESSION_TO_GUAVA_OPTIONAL_CODE_EXPRESSION =
            currentOptionalCodeExpression -> "Optional.fromNullable(" + currentOptionalCodeExpression + ")";

    @Override
    public Matcher matches(final TypeMirror type, final List<String> additionalAnnotationClasses) {
        final Stream<String> fieldAnnotations = type.getAnnotationMirrors().stream()
                .map(annotation -> annotation.getAnnotationType().toString());

        if (Stream.concat(fieldAnnotations, additionalAnnotationClasses.stream()).anyMatch(annotation::equals)) {
            return Matcher.optionalOf(type.toString(), KOTLIN_OPTIONAL_EXPRESSION_TO_GUAVA_OPTIONAL_CODE_EXPRESSION, Functions.identity());
        } else {
            return Matcher.notOptional(type.toString());
        }
    }
}
