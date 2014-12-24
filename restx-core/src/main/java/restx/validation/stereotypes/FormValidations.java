package restx.validation.stereotypes;

import javax.validation.groups.Default;

/**
 * @author fcamblor
 */
public interface FormValidations {
    public static final String DefaultFQN = "javax.validation.groups.Default";
    public static final String CreateFQN = "restx.validation.stereotypes.FormValidations.Create";
    public static interface Create extends Default{}
    public static final String UpdateFQN = "restx.validation.stereotypes.FormValidations.Update";
    public static interface Update extends Default{}
}
