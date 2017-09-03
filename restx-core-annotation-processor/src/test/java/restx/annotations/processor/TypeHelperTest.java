package restx.annotations.processor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Date: 23/10/13
 * Time: 10:27
 */
@RunWith(Parameterized.class)
public class TypeHelperTest {
    private final String stringedType;
    private final String expectedTypeExpression;

    @Parameterized.Parameters(name="{0}")
    public static Iterable<Object[]> data(){
        return Arrays.asList(new Object[][]{
                {"java.lang.String", "java.lang.String.class"},
                {"java.util.List<java.lang.String>", "Types.newParameterizedType(java.util.List.class, java.lang.String.class)" },
                {"java.util.Map<java.lang.String, java.lang.Integer>", "Types.newParameterizedType(java.util.Map.class, java.lang.String.class, java.lang.Integer.class)" },
                {"java.util.List<java.util.List<java.lang.String>>", "Types.newParameterizedType(java.util.List.class, Types.newParameterizedType(java.util.List.class, java.lang.String.class))" },
                {"java.util.List<java.util.Map<java.lang.String, java.lang.Integer>>", "Types.newParameterizedType(java.util.List.class, Types.newParameterizedType(java.util.Map.class, java.lang.String.class, java.lang.Integer.class))" },
                {"java.util.List<java.util.Map<java.util.Set<java.lang.String>, java.lang.Integer>>", "Types.newParameterizedType(java.util.List.class, Types.newParameterizedType(java.util.Map.class, Types.newParameterizedType(java.util.Set.class, java.lang.String.class), java.lang.Integer.class))" },
                {"java.util.Map<java.lang.Boolean, java.util.Collection<java.lang.String>>", "Types.newParameterizedType(java.util.Map.class, java.lang.Boolean.class, Types.newParameterizedType(java.util.Collection.class, java.lang.String.class))" },
                {"java.util.Map<java.lang.Boolean, java.util.Map<foo.bar.Tuple<java.lang.String,foo.bar.Tuple<java.lang.Integer,java.lang.Float>,java.lang.String>,java.lang.String>>",
                        "Types.newParameterizedType(java.util.Map.class, java.lang.Boolean.class, Types.newParameterizedType(java.util.Map.class, Types.newParameterizedType(foo.bar.Tuple.class, java.lang.String.class, Types.newParameterizedType(foo.bar.Tuple.class, java.lang.Integer.class, java.lang.Float.class), java.lang.String.class), java.lang.String.class))" }
        });
    }

    public TypeHelperTest(String stringedType, String expectedTypeExpression) {
        this.stringedType = stringedType;
        this.expectedTypeExpression = expectedTypeExpression;
    }

    @Test
    public void should_produce_type_expression() throws Exception {
        assertThat(TypeHelper.getTypeExpressionFor(this.stringedType)).isEqualTo(this.expectedTypeExpression);
    }
}
