package restx.annotations.processor;

import com.google.common.base.Optional;
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

        boolean isOptionalType = parameter.guavaOptional || parameter.java8Optional;
        // If we don't have any optional type, we should check for non nullity *before* calling checkValid()
        if(!isOptionalType) {
            this.parameterExpr = String.format(
                    "checkNotNull(%s, \"%s param <%s> is required\")",
                    this.parameterExpr,
                    this.kind,
                    parameter.name
            );
        }

        Optional<String> validationGroupsExpr = parameter.joinedValidationGroupFQNExpression();
        this.parameterExpr = String.format(
                "checkValid(validator, %s%s)",
                this.parameterExpr,
                validationGroupsExpr.isPresent()?","+validationGroupsExpr.get():""
        );

        if(parameter.guavaOptional) {
            this.parameterExpr = String.format(
                    "Optional.fromNullable(%s)",
                    this.parameterExpr
            );
        } else if(parameter.java8Optional) {
            this.parameterExpr = String.format(
                    "java.util.Optional.ofNullable(%s)",
                    this.parameterExpr
            );
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
            "mapQueryObjectFromRequest(ParamDef.of(%s, \"%s\"), request, match, EndpointParameterKind.%s)",
            TypeHelper.getTypeReferenceExpressionFor(parameter.type),
            parameter.reqParamName,
            kind.name()
        ), kind.name());
    }
}
