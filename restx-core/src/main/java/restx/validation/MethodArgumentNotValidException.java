package restx.validation;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;

import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.metadata.ConstraintDescriptor;
import java.lang.annotation.ElementType;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

/**
 * Created by Thomas Zayouna on 07/10/15.
 * Exception wrapping bean validation violations
 */
public class MethodArgumentNotValidException extends IllegalArgumentException {

    public static class ViolationContent {
        private final String message;
        private final Path propertyPath;
        private final Class rootBeanClass;

        public ViolationContent(String message, Path propertyPath, Class rootBeanClass) {
            this.message = message;
            this.propertyPath = propertyPath;
            this.rootBeanClass = rootBeanClass;
        }

        public String getMessage() {
            return message;
        }

        public Path getPropertyPath() {
            return propertyPath;
        }

        public Class getRootBeanClass() {
            return rootBeanClass;
        }
    }

    public static Function<ConstraintViolation, ViolationContent> VIOLATION_CONTENT_EXTRACTOR = new Function<ConstraintViolation, ViolationContent>() {
        @Override
        public ViolationContent apply(ConstraintViolation input) {
            return new ViolationContent(input.getMessage(), input.getPropertyPath(), input.getRootBeanClass());
        }
    };

    private final Set<ViolationContent> violations;

    public <T> MethodArgumentNotValidException(Set<ConstraintViolation<T>> violations) {
        // Kept for backward compat for restx <= 0.34
        super(Joiner.on(",").join(violations));
        this.violations = newHashSet(Collections2.transform(violations, VIOLATION_CONTENT_EXTRACTOR));
    }

    public Set<ViolationContent> getViolations() {
        return violations;
    }
}
