package restx.validation;

import restx.factory.BoundlessComponentBox;
import restx.factory.Factory;
import restx.factory.Name;
import restx.factory.SingleNameFactoryMachine;

import javax.validation.Validation;
import javax.validation.Validator;

/**
 * User: xavierhanin
 * Date: 2/3/13
 * Time: 9:48 PM
 */
public class ValidatorFactory extends SingleNameFactoryMachine<Validator> {
    public static final String VALIDATOR_NAME = "validator";
    public static final Name<Validator> VALIDATOR = Name.of(Validator.class, VALIDATOR_NAME);

    public ValidatorFactory() {
        super(0, VALIDATOR, BoundlessComponentBox.FACTORY);
    }

    @Override
    protected Validator doNewComponent(Factory factory) {
        return Validation.buildDefaultValidatorFactory().getValidator();
    }
}
