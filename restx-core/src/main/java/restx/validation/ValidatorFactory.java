package restx.validation;

import org.hibernate.validator.HibernateValidator;
import restx.factory.*;

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
        super(0, new NoDepsMachineEngine<Validator>(VALIDATOR, BoundlessComponentBox.FACTORY) {
            @Override
            public Validator doNewComponent(SatisfiedBOM satisfiedBOM) {
                return Validation.byProvider(HibernateValidator.class).configure()
                                .ignoreXmlConfiguration().buildValidatorFactory().getValidator();
            }
        });
    }
}
