package restx.validation;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
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
    public static final String VALIDATOR_NAME = "hibernate.validator";
    public static final String IGNORE_XML_CONFIGURATION_NAME = "hibernate.validator.ignore.xml.configuration";
    public static final Name<Validator> VALIDATOR = Name.of(Validator.class, VALIDATOR_NAME);

    @Provides @Named(VALIDATOR_NAME)
    public Validator validator(@Named(IGNORE_XML_CONFIGURATION_NAME) Boolean ignoreXmlConfiguration) {
        HibernateValidatorConfiguration config = Validation.byProvider(HibernateValidator.class).configure();
        if(ignoreXmlConfiguration) {
            config.ignoreXmlConfiguration();
        }
        return config.buildValidatorFactory().getValidator();
    }

    // Perf improvement to greatly fasten startup time
    @Provides @Named(IGNORE_XML_CONFIGURATION_NAME)
    public Boolean ignoreXmlConfigurationFlag(){
        return Boolean.TRUE;
    }
}
