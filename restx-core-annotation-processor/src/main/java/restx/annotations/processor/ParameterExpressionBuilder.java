package restx.annotations.processor;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Optional;
import restx.common.AggregateType;
import restx.endpoint.EndpointParameterKind;

import java.util.HashMap;
import java.util.Map;

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

    private static final Map<AggregateType, Function<String, String>> EMPTY_AGGREGATE_FUNCTIONS = new HashMap<AggregateType, Function<String, String>>(){{
        // These (Function) casts are ugly, I know, but read https://github.com/google/guava/issues/1927
        put(AggregateType.ITERABLE, (Function) Functions.constant("EMPTY_ITERABLE_SUPPLIER"));
        put(AggregateType.LIST, (Function) Functions.constant("EMPTY_LIST_SUPPLIER"));
        put(AggregateType.SET, (Function) Functions.constant("EMPTY_SET_SUPPLIER"));
        put(AggregateType.COLLECTION, (Function) Functions.constant("EMPTY_COLLECTION_SUPPLIER"));
        put(AggregateType.ARRAY, new Function<String, String>() {
            @Override
            public String apply(String fqcn) {
                return "Suppliers.ofInstance(new "+fqcn+"{})";
            }
        });
    }};

    public ParameterExpressionBuilder surroundWithCheckValid(
            RestxAnnotationProcessor.ResourceMethodParameter parameter) {

        boolean isOptionalType = parameter.guavaOptional || parameter.java8Optional || parameter.annotationNullable;
        // If we don't have any optional type, we should check for non nullity *before* calling checkValid()
        if(!isOptionalType) {
            // In case we're on an aggregate interface, parameterExpr will always return a non-null value
            // (see createFromMapQueryObjectFromRequest() method) so we don't need to add checkNotNull() check on this
            if (!AggregateType.isAggregate(parameter.type)) {
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
        Optional<AggregateType> aggregateType = AggregateType.fromType(parameter.type);
        String emptySupplierParam = "";
        if(aggregateType.isPresent()) {
            Function<String, String> fqcnToEmptyAggregateTransformer = EMPTY_AGGREGATE_FUNCTIONS.get(aggregateType.get());
            if(fqcnToEmptyAggregateTransformer == null) {
                throw new IllegalStateException("Missing EMPTY_AGGREGATE_FUNCTIONS entry for aggregate type "+aggregateType.get().name());
            }
            emptySupplierParam = ", "+fqcnToEmptyAggregateTransformer.apply(parameter.type);
        }

        return new ParameterExpressionBuilder(String.format(
            "%smapQueryObjectFromRequest(%s.class, \"%s\", request, match, EndpointParameterKind.%s%s)",
            TypeHelper.isParameterizedType(parameter.type)?"("+parameter.type+")":"",
            TypeHelper.rawTypeFrom(parameter.type),
            parameter.reqParamName,
            kind.name(),
            emptySupplierParam
        ), kind.name());
    }
}
