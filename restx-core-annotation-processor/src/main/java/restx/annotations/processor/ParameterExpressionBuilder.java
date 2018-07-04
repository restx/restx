package restx.annotations.processor;

import com.google.common.base.Optional;
import restx.types.Types;
import restx.endpoint.EndpointParameterKind;

/**
 * @author fcamblor
 * Will build endpoint parameter generated expression during annotation processor
 * Important note : calls ordering is not important, the only important thing is to call
 * build() call
 */
public class ParameterExpressionBuilder {
    String parameterExpr;
    String kind;

    private ParameterExpressionBuilder(String initialExpr, String kind) {
        this.parameterExpr = initialExpr;
        this.kind = kind;
    }

    public ParameterExpressionBuilder surroundWithCheckValid(
            RestxAnnotationProcessor.ResourceMethodParameter parameter) {

        // If we don't have any optional type, we should check for non nullity *before* calling checkValid()
        if(!parameter.optionalTypeMatcher.isOptionalType()) {
            // In case we're on an aggregate interface, parameterExpr will always return a non-null value
            // (see createFromMapQueryObjectFromRequest() method) so we don't need to add checkNotNull() check on this
            if(Types.isAggregateType(parameter.type)) {
            } else {
                // If not an iterable type, ensuring target value is set
                this.parameterExpr = String.format(
                        "checkNotNull(%s, \"%s param <%s> is required\")",
                        this.parameterExpr,
                        this.kind,
                        parameter.reqParamName
                );
            }
        }

        Optional<String> validationGroupsExpr = parameter.joinedValidationGroupFQNExpression();
        this.parameterExpr = String.format(
                "checkValid(validator, %s%s)",
                this.parameterExpr,
                validationGroupsExpr.isPresent()?","+validationGroupsExpr.get():""
        );

        if(parameter.optionalTypeMatcher.isOptionalType()) {
            this.parameterExpr = parameter.optionalTypeMatcher.getFromNullableExpressionTransformer().apply(this.parameterExpr);
        }

        return this;
    }

    public String getParameterExpr() {
        return parameterExpr;
    }

    public static ParameterExpressionBuilder createFromExpr(String expr, String kind) {
        return new ParameterExpressionBuilder(expr, kind);
    }

    public static ParameterExpressionBuilder createFromMapQueryObjectFromRequest(
            RestxAnnotationProcessor.ResourceMethodParameter parameter,
            EndpointParameterKind kind){

        return new ParameterExpressionBuilder(String.format(
            "%smapQueryObjectFromRequest(%s.class, \"%s\", request, match, EndpointParameterKind.%s)",
            Types.isParameterizedType(parameter.type)?"("+parameter.type+")":"",
            Types.rawTypeFrom(parameter.type),
            parameter.reqParamName,
            kind.name()
        ), kind.name());
    }
}
