/**
 *
 */
package restx.jongo.specs.tests;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.junit.rules.TestRule;
import org.junit.runners.model.InitializationError;

import restx.tests.RestxSpecTestsRunner;

/**
 * A runner which can be used to run a set of specs as JUnit tests and use embed mongo with dynamic port.
 * (download specify version).
 *
 * Example of use:
 *
 * <code>
 * @RunWith(MongoRestxSpecTestsRunner.class)
 * @FindSpecsIn("specs/city")
 * public class CitySpecTest { }
 * </code>
 *
 * or
 *
 * <code>
 * @RunWith(MongoRestxSpecTestsRunner.class)
 * @FindSpecsIn("specs/city")
 * @MongoVersion(Version.Main.PRODUCTION)
 * public class CitySpecTest { }
 * </code>
 *
 * or
 *
 * <code>
 * @RunWith(MongoRestxSpecTestsRunner.class)
 * public class CitySpecTest extends RestxSpecTests {
 *     public CitySpecTest() {
 *         super(new RestxSpecRule(), RestxSpecTests.findSpecsIn("specs/city"));
 *     }
 * }
 * </code>
 *
 * or
 *
 * <code>
 * @RunWith(MongoRestxSpecTestsRunner.class)
 * @MongoVersion(Version.Main.PRODUCTION)
 * public class CitySpecTest extends RestxSpecTests {
 *     public CitySpecTest() {
 *         super(new RestxSpecRule(), RestxSpecTests.findSpecsIn("specs/city"));
 *     }
 * }
 * </code>
 */
public class MongoRestxSpecTestsRunner extends RestxSpecTestsRunner {

    private final TestRule rule;

    /**
     * @param pTestClass
     * @throws InitializationError
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     * @throws NoSuchFieldException
     * @throws IOException
     */
    public MongoRestxSpecTestsRunner(Class<?> pTestClass) throws InitializationError, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchFieldException, IOException {
        super(pTestClass);
        MongoVersion mongoVersion = getTestClass().getJavaClass().getAnnotation(MongoVersion.class);
        if (mongoVersion != null) {
            rule = new MongoEmbedRule(mongoVersion.value());
        } else {
            rule = new MongoEmbedRule();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<TestRule> classRules() {
        List<TestRule> rules = super.classRules();
        rules.add(rule);
        return rules;
    }

}
