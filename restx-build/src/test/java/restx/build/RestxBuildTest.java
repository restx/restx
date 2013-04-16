package restx.build;

import org.junit.Test;

import java.io.*;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * User: xavierhanin
 * Date: 4/14/13
 * Time: 1:57 PM
 */
public class RestxBuildTest {
    private RestxJsonSupport json = new RestxJsonSupport();
    private MavenSupport maven = new MavenSupport();
    private IvySupport ivy = new IvySupport();

    @Test
    public void should_generate_simple_pom() throws Exception {
        shouldGenerate(json, "Module1.restx.json", maven, "Module1.pom.xml");
    }

    @Test
    public void should_generate_pom_with_fragment() throws Exception {
        shouldGenerate(json, "Module3.restx.json", maven, "Module3.pom.xml");
    }

    @Test
    public void should_generate_pom_with_war() throws Exception {
        shouldGenerate(json, "Module4.restx.json", maven, "Module4.pom.xml");
    }

    @Test
    public void should_parse_simple_pom() throws Exception {
        shouldGenerate(maven, "Module1.pom.xml", json, "Module1.restx.json");
    }

    @Test
    public void should_parse_simple_pom_with_war() throws Exception {
        shouldGenerate(maven, "Module4.pom.xml", json, "Module4.restx.json");
    }

    @Test
    public void should_generate_simple_ivy() throws Exception {
        shouldGenerate(json, "Module1.restx.json", ivy, "Module1.ivy");
    }

    @Test
    public void should_generate_simple_ivy_war() throws Exception {
        shouldGenerate(json, "Module4.restx.json", ivy, "Module4.ivy");
    }

    @Test
    public void should_generate_ivy_with_internal_dep() throws Exception {
        shouldGenerate(json, "Module2.restx.json", ivy, "Module2.ivy");
    }

    private void shouldGenerate(RestxBuild.Parser parser, String module, RestxBuild.Generator generator, String expected) throws IOException {
        ModuleDescriptor md = parser.parse(getClass().getResourceAsStream(module));
        StringWriter w = new StringWriter();
        generator.generate(md, w);
        assertThat(w.toString()).isEqualTo(RestxBuild.toString(getClass().getResourceAsStream(expected)));
    }

}
