package restx.tests;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import restx.specs.RestxSpec;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static java.lang.String.format;

/**
 * A runner which can be used to run a set of specs as JUnit tests.
 *
 * Example of use:
 *
 * <code>
 * @RunWith(RestxSpecTestsRunner.class)
 * @FindSpecsIn("specs/city")
 * public class CitySpecTest { }
 * </code>
 *
 * or
 *
 * <code>
 * @RunWith(RestxSpecTestsRunner.class)
 * public class CitySpecTest extends RestxSpecTests {
 *     public CitySpecTest() {
 *         super(new RestxSpecRule(), RestxSpecTests.findSpecsIn("specs/city"));
 *     }
 * }
 * </code>
 */
public class RestxSpecTestsRunner extends ParentRunner<RestxSpec> {

    private final RestxSpecTests tests;

    /**
     * Constructs a new {@code RestxSpecTestsRunner} that will run {@code @TestClass}
     */
    public RestxSpecTestsRunner(Class<?> testClass) throws InitializationError, IllegalAccessException,
                            InvocationTargetException, InstantiationException, NoSuchFieldException, IOException {
        super(testClass);
        FindSpecsIn findSpecsIn = getTestClass().getJavaClass().getAnnotation(FindSpecsIn.class);
        if (findSpecsIn != null) {
            tests = new RestxSpecTests(new RestxSpecRule(), RestxSpecTests.findSpecsIn(findSpecsIn.value()));
        } else {
            Object o = getTestClass().getOnlyConstructor().newInstance();
            if (!(o instanceof RestxSpecTests)) {
                throw new IllegalArgumentException(
                        format("Test class %s must either be annotated with FindSoecsIn" +
                        " or extend RestxSpecTests " +
                        "to be run with RestxSpecTestsRunner.", 
                                getTestClass().getJavaClass().getName()));
            }
            tests = (RestxSpecTests) o;
        }
    }

    @Override
    protected List<RestxSpec> getChildren() {
        return tests.getSpecs();
    }

    @Override
    protected Description describeChild(RestxSpec child) {
        return Description.createTestDescription(getTestClass().getJavaClass(), child.getTitle());
    }

    @Override
    protected List<TestRule> classRules() {
        List<TestRule> rules = super.classRules();
        rules.add(tests.getRule());
        return rules;
    }

    @Override
    protected void runChild(final RestxSpec restxSpec, RunNotifier notifier) {
        Description description = describeChild(restxSpec);
        runLeaf(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                tests.getRule().runTest(restxSpec);
            }
        }, description, notifier);
    }
}
