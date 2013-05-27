package restx.tests;

import com.google.common.collect.Lists;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import restx.specs.RestxSpec;
import restx.specs.RestxSpecLoader;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
* A list of specs to be run as tests by a RestxSpecTestsRunner.
*/
public class RestxSpecTests {
    public static List<RestxSpec> findSpecsIn(String location) throws IOException {
        RestxSpecLoader loader = new RestxSpecLoader();

        Set<String> specResources = new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(location))
                .setScanners(new ResourcesScanner())
                .build()
                .getResources(Pattern.compile(".*\\.spec\\.yaml"));

        List<RestxSpec> loaded = Lists.newArrayList();

        for (String specResource : specResources) {
            loaded.add(loader.load(specResource));
        }

        return loaded;
    }

    private final RestxSpecRule rule;
    private final List<RestxSpec> specs;

    public RestxSpecTests(RestxSpecRule rule, List<RestxSpec> specs) {
        this.rule = rule;
        this.specs = specs;
    }

    public  RestxSpecRule getRule() {
        return rule;
    }
    public List<RestxSpec> getSpecs() {
        return specs;
    }
}
