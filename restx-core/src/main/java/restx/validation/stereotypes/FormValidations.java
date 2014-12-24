package restx.validation.stereotypes;

import javax.validation.groups.Default;

/**
 * @author fcamblor
 */
public interface FormValidations {
    public static interface Create extends Default{}
    public static interface Update extends Default{}
}
