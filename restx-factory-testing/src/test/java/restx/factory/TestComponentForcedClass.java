package restx.factory;

/**
 * @author apeyrard
 */
@Component(asClass = TestGreeting.class)
public class TestComponentForcedClass implements TestGreeting {
	@Override
	public String greet() {
		return "hello";
	}
}
