package restx.annotations.processor;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Date: 23/10/13
 * Time: 10:27
 */
public class TypeHelperTest {
    @Test
    public void should_produce_type_expression() throws Exception {
        assertThat(TypeHelper.getTypeExpressionFor("java.lang.String"))
                .isEqualTo("java.lang.String.class");
        assertThat(TypeHelper.getTypeExpressionFor("java.util.List<java.lang.String>"))
                .isEqualTo("Types.newParameterizedType(java.util.List.class, java.lang.String.class)");
        assertThat(TypeHelper.getTypeExpressionFor("java.util.Map<java.lang.String, java.lang.Integer>"))
                .isEqualTo("Types.newParameterizedType(java.util.Map.class, java.lang.String.class, java.lang.Integer.class)");
        assertThat(TypeHelper.getTypeExpressionFor("java.util.List<java.util.List<java.lang.String>>"))
                .isEqualTo("Types.newParameterizedType(java.util.List.class, Types.newParameterizedType(java.util.List.class, java.lang.String.class))");
        assertThat(TypeHelper.getTypeExpressionFor("java.util.List<java.util.Map<java.lang.String, java.lang.Integer>>"))
                .isEqualTo("Types.newParameterizedType(java.util.List.class, Types.newParameterizedType(java.util.Map.class, java.lang.String.class, java.lang.Integer.class))");
        assertThat(TypeHelper.getTypeExpressionFor("java.util.List<java.util.Map<java.util.Set<java.lang.String>, java.lang.Integer>>"))
                .isEqualTo("Types.newParameterizedType(java.util.List.class, Types.newParameterizedType(java.util.Map.class, Types.newParameterizedType(java.util.Set.class, java.lang.String.class), java.lang.Integer.class))");
    }
}
