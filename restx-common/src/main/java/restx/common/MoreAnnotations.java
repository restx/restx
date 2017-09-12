package restx.common;

import com.google.common.base.Function;

import java.lang.annotation.Annotation;

public class MoreAnnotations {
    public static final Function<Annotation, Class<? extends Annotation>> EXTRACT_ANNOTATION_TYPE = new Function<Annotation, Class<? extends Annotation>>() {
        @Override
        public Class<? extends Annotation> apply(Annotation input) {
            return input.annotationType();
        }
    };

}
