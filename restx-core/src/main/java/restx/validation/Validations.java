package restx.validation;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.groups.Default;
import java.util.Set;

/**
 * User: xavierhanin
 * Date: 2/3/13
 * Time: 9:57 PM
 */
public class Validations {
    public static <T> T checkValid(Optional<Validator> validator, T o, Class... groups) {
        if(validator.isPresent()) {
            if(groups == null || groups.length==0) {
                groups = new Class[]{ Default.class };
            }

            Set<ConstraintViolation<T>> violations = validator.get().validate(o, groups);
            if (!violations.isEmpty()) {
                throw new IllegalArgumentException(Joiner.on(", ").join(violations));
            }
        }

        return o;
    }
}
