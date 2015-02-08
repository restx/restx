package restx.factory.conditional.components;

import javax.inject.Named;
import restx.factory.Component;
import restx.factory.When;

/**
 * @author apeyrard
 */
@Component @Named("conditional")
@When(name = "conditional.component", value = "allowed")
public class TestConditionalComponent {
}
