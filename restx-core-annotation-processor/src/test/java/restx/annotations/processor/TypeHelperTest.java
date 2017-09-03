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
    private final String expectedTypeDescription;

    @Parameterized.Parameters(name="{0}")
    public static Iterable<Object[]> data(){
        return Arrays.asList(new Object[][]{
                {"java.lang.String",
                        "java.lang.String.class", "string"},
                {"java.lang.Integer",
                        "java.lang.Integer.class", "int"},
                {"java.lang.Float",
                        "java.lang.Float.class", "float"},
                {"java.lang.Iterable<java.lang.String>",
                        "Types.newParameterizedType(java.lang.Iterable.class, java.lang.String.class)", "LIST[string]" },
                {"java.util.List<java.lang.String>",
                        "Types.newParameterizedType(java.util.List.class, java.lang.String.class)", "LIST[string]" },
                {"java.util.Map<java.lang.String, java.lang.Integer>",
                        "Types.newParameterizedType(java.util.Map.class, java.lang.String.class, java.lang.Integer.class)",
                        "MAP[string, int]" },
                {"java.util.List<java.util.List<java.lang.String>>",
                        "Types.newParameterizedType(java.util.List.class, Types.newParameterizedType(java.util.List.class, java.lang.String.class))",
                        "LIST[LIST[string]]"},
                {"java.util.List<java.util.Map<java.lang.String, java.lang.Integer>>",
                        "Types.newParameterizedType(java.util.List.class, Types.newParameterizedType(java.util.Map.class, java.lang.String.class, java.lang.Integer.class))",
                        "LIST[MAP[string, int]]"},
                {"java.util.List<java.util.Map<java.util.Set<java.lang.String>, java.lang.Integer>>",
                        "Types.newParameterizedType(java.util.List.class, Types.newParameterizedType(java.util.Map.class, Types.newParameterizedType(java.util.Set.class, java.lang.String.class), java.lang.Integer.class))",
                        "LIST[MAP[Set<string>, int]]"},
                {"java.util.Map<java.lang.Boolean, java.util.Collection<java.lang.String>>",
                        "Types.newParameterizedType(java.util.Map.class, java.lang.Boolean.class, Types.newParameterizedType(java.util.Collection.class, java.lang.String.class))",
                        "MAP[boolean, Collection<string>]"},
                {"java.util.Map<java.lang.Boolean, java.util.Map<foo.bar.Tuple<java.lang.String,foo.bar.Tuple<java.lang.Integer,java.lang.Float>,java.lang.String>,java.lang.String>>",
                        "Types.newParameterizedType(java.util.Map.class, java.lang.Boolean.class, Types.newParameterizedType(java.util.Map.class, Types.newParameterizedType(foo.bar.Tuple.class, java.lang.String.class, Types.newParameterizedType(foo.bar.Tuple.class, java.lang.Integer.class, java.lang.Float.class), java.lang.String.class), java.lang.String.class))",
                        "MAP[boolean, MAP[Tuple<string, Tuple<int, float>, string>, string]]" }
        });
    }

    public TypeHelperTest(String stringedType, String expectedTypeExpression, String expectedTypeDescription) {
        this.stringedType = stringedType;
        this.expectedTypeExpression = expectedTypeExpression;
        this.expectedTypeDescription = expectedTypeDescription;
    }

    @Test
    public void should_produce_type_expression() throws Exception {
        assertThat(TypeHelper.getTypeExpressionFor(this.stringedType)).isEqualTo(this.expectedTypeExpression);
    }

    @Test
    public void should_produce_type_description() throws Exception {
        assertThat(TypeHelper.toTypeDescription(this.stringedType)).isEqualTo(this.expectedTypeDescription);
    }
}
