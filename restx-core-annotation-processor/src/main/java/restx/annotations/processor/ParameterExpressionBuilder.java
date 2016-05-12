package restx.annotations.processor;

import com.google.common.base.Optional;
import restx.endpoint.EndpointParameterKind;

import java.util.List;
import java.util.Set;

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

    private static enum IterableInterface {
        ITERABLE(Iterable.class.getCanonicalName()) {
            @Override
            String generateEmptyIterableExpr() {
                return "EMPTY_ITERABLE_SUPPLIER";
            }
        },
        LIST(List.class.getCanonicalName()) {
            @Override
            String generateEmptyIterableExpr() {
                return "EMPTY_LIST_SUPPLIER";
            }
        },
        SET(Set.class.getCanonicalName()) {
            @Override
            String generateEmptyIterableExpr() {
                return "EMPTY_SET_SUPPLIER";
            }
        };

        private String fqcn;
        private IterableInterface(String fqcn) {
            this.fqcn = fqcn;
        }

        abstract String generateEmptyIterableExpr();

        static Optional<IterableInterface> fromType(String fqcn) {
            for(IterableInterface iterableInterface : values()){
                if(fqcn.startsWith(iterableInterface.fqcn)) {
                    return Optional.of(iterableInterface);
                }
            }

            return Optional.absent();
        }
    }

    public ParameterExpressionBuilder surroundWithCheckValid(
            RestxAnnotationProcessor.ResourceMethodParameter parameter) {

        boolean isOptionalType = parameter.guavaOptional || parameter.java8Optional;
        // If we don't have any optional type, we should check for non nullity *before* calling checkValid()
        if(!isOptionalType) {
            // In case we're on an iterable interface, parameterExpr will always return a non-null value
            // so we don't need to add checkNotNull() check on this
            Optional<IterableInterface> iterableInterface = IterableInterface.fromType(parameter.type);
            if(iterableInterface.isPresent()) {
            } else {
                // If not an iterable type, ensuring target value is set
                this.parameterExpr = String.format(
                        "checkNotNull(%s, \"%s param <%s> is required\")",
                        this.parameterExpr,
                        this.kind,
                        parameter.name
                );
            }
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

        // We should check if target type is an iterable interface : in that case, we should
        // instantiate an empty iterable instead of null value if data is missing in request
        Optional<IterableInterface> iterableInterface = IterableInterface.fromType(parameter.type);

        return new ParameterExpressionBuilder(String.format(
            "%smapQueryObjectFromRequest(%s.class, \"%s\", request, match, EndpointParameterKind.%s%s)",
            TypeHelper.isParameterizedType(parameter.type)?"("+parameter.type+")":"",
            TypeHelper.rawTypeFrom(parameter.type),
            parameter.reqParamName,
            kind.name(),
            iterableInterface.isPresent()?", "+iterableInterface.get().generateEmptyIterableExpr():""
        ), kind.name());
    }
}
