package restx.description;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import restx.common.MoreAnnotations;
import restx.http.HttpStatus;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

/**
 * User: xavierhanin
 * Date: 2/7/13
 * Time: 10:54 AM
 */
public class OperationDescription {
    public String httpMethod;
    public String nickname;
    public String responseClass;
    public String sourceLocation = "";
    public String inEntitySchemaKey = "";
    @JsonIgnore
    public Type inEntityType;
    public String outEntitySchemaKey = "";
    @JsonIgnore
    public Type outEntityType;
    public String summary = "";
    public String notes = "";
    public HttpStatus.Descriptor successStatus;
    public List<OperationParameterDescription> parameters = Lists.newArrayList();
    public List<ErrorResponseDescription> errorResponses = Lists.newArrayList();
    public List<OperationReference> relatedOperations = Lists.newArrayList();
    @JsonIgnore
    public ImmutableList<? extends Annotation> annotations;

    public static class Matcher implements Predicate<OperationDescription> {
        public Predicate<String> httpMethodMatcher = Predicates.alwaysTrue();
        public Predicate<Type> inEntityTypeMatcher = Predicates.alwaysTrue();
        public Predicate<Type> outEntityTypeMatcher = Predicates.alwaysTrue();
        public Predicate<HttpStatus.Descriptor> successStatusMatcher = Predicates.alwaysTrue();
        public Predicate<ImmutableList<ErrorResponseDescription>> errorResponsesMatcher = Predicates.alwaysTrue();
        public Predicate<ImmutableList<? extends Annotation>> annotationsMatcher = Predicates.alwaysTrue();

        public Matcher withHttpMethodMatcher(Predicate<String> httpMethodMatcher) {
            this.httpMethodMatcher = httpMethodMatcher;
            return this;
        }

        public Matcher withInEntityTypeMatcher(Predicate<Type> inEntityTypeMatcher) {
            this.inEntityTypeMatcher = inEntityTypeMatcher;
            return this;
        }

        public Matcher withOutEntityTypeMatcher(Predicate<Type> outEntityTypeMatcher) {
            this.outEntityTypeMatcher = outEntityTypeMatcher;
            return this;
        }

        public Matcher withSuccessStatusMatcher(Predicate<HttpStatus.Descriptor> successStatusMatcher) {
            this.successStatusMatcher = successStatusMatcher;
            return this;
        }

        public Matcher withErrorResponsesMatcher(Predicate<ImmutableList<ErrorResponseDescription>> errorResponsesMatcher) {
            this.errorResponsesMatcher = errorResponsesMatcher;
            return this;
        }

        public Matcher withAnnotationsMatcher(Predicate<ImmutableList<? extends Annotation>> annotationsMatcher) {
            this.annotationsMatcher = annotationsMatcher;
            return this;
        }

        public Matcher havingAnyAnnotations(final Class<? extends Annotation>... annotationTypes) {
            return this.withAnnotationsMatcher(new Predicate<ImmutableList<? extends Annotation>>() {
                @Override
                public boolean apply(ImmutableList<? extends Annotation> annotations) {
                    if(annotations == null) {
                        return false;
                    }
                    return !FluentIterable.from(annotations)
                            .transform(MoreAnnotations.EXTRACT_ANNOTATION_TYPE)
                            .filter(Predicates.in(Arrays.asList(annotationTypes)))
                            .isEmpty();
                }
            });
        }

        public Matcher havingAllAnnotations(final Class<? extends Annotation>... annotationTypes) {
            return this.withAnnotationsMatcher(new Predicate<ImmutableList<? extends Annotation>>() {
                @Override
                public boolean apply(ImmutableList<? extends Annotation> annotations) {
                    if(annotations == null) {
                        return false;
                    }
                    return FluentIterable.from(annotations)
                            .transform(MoreAnnotations.EXTRACT_ANNOTATION_TYPE)
                            .filter(Predicates.in(Arrays.asList(annotationTypes)))
                            .size() == annotationTypes.length;
                }
            });
        }

        @Override
        public boolean apply(OperationDescription description) {
            return httpMethodMatcher.apply(description.httpMethod)
                && inEntityTypeMatcher.apply(description.inEntityType)
                && outEntityTypeMatcher.apply(description.outEntityType)
                && successStatusMatcher.apply(description.successStatus)
                && errorResponsesMatcher.apply(ImmutableList.copyOf(description.errorResponses))
                && annotationsMatcher.apply(description.annotations);
        }
    }

    public Optional<OperationParameterDescription> findBodyParameter() {
        for (OperationParameterDescription parameter : parameters) {
            if (parameter.paramType == OperationParameterDescription.ParamType.body) {
                return Optional.of(parameter);
            }
        }

        return Optional.absent();
    }

    public <T extends Annotation> Optional<T> findAnnotation(final Class<T> clazz) {
        for(Annotation annotation: annotations) {
            if(clazz.isAssignableFrom(annotation.getClass())) {
                return Optional.fromNullable((T) annotation);
            }
        }
        return Optional.absent();
    }
}
