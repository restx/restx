package restx.types.optional;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Optional;

public class GuavaOptionalTypeDefinition extends OptionalTypeDefinition.ClassBasedOptionalTypeDefinition {

    private static final Function<String, String> FROM_NULLABLE_EXPRESSION_TRANSFORMER =
            expression -> "Optional.fromNullable(" + expression + ")";

    public GuavaOptionalTypeDefinition() {
        super(Optional.class, Functions.identity(), FROM_NULLABLE_EXPRESSION_TRANSFORMER);
    }
}

