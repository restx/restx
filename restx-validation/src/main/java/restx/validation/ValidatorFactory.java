package restx.validation;

import org.hibernate.validator.HibernateValidator;
import restx.factory.Module;
import restx.factory.Name;
import restx.factory.Provides;

import javax.inject.Named;
import javax.validation.Validation;
import javax.validation.Validator;

/**
 * User: xavierhanin
 * Date: 2/3/13
 * Time: 9:48 PM
 */
@Module
public class ValidatorFactory {
    public static final String VALIDATOR_NAME = "validator";
    public static final Name<Validator> VALIDATOR = Name.of(Validator.class, VALIDATOR_NAME);

    @Provides @Named(VALIDATOR_NAME)
    public Validator validator() {
        return Validation.byProvider(HibernateValidator.class).configure()
                        .ignoreXmlConfiguration().buildValidatorFactory().getValidator();
    }
}
