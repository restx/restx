package restx.build;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;

import static org.assertj.core.api.Assertions.*;

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
    public void should_generate_simple_pom_with_external_properties() throws Exception {
        shouldGenerate(json, "Module5.restx.json", maven, "Module5.pom.xml");
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
    public void should_generate_simple_ivy_with_external_properties() throws Exception {
        shouldGenerate(json, "Module5.restx.json", ivy, "Module5.ivy");
    }

    @Test
    public void should_generate_simple_ivy_war() throws Exception {
        shouldGenerate(json, "Module4.restx.json", ivy, "Module4.ivy");
    }

    @Test
    public void should_generate_ivy_with_internal_dep() throws Exception {
        shouldGenerate(json, "Module2.restx.json", ivy, "Module2.ivy");
    }

    @Test
    public void should_generate_pom_with_type() throws Exception {
        shouldGenerate(json, "Module6.restx.json", maven, "Module6.pom.xml");
    }

    private void shouldGenerate(RestxBuild.Parser parser, String module, RestxBuild.Generator generator, String expected) throws IOException {
        URL resource = getClass().getResource(module);
        ModuleDescriptor md;
        if (resource.getProtocol().equals("file")) {
            File f;
            try {
              f = new File(resource.toURI());
            } catch(URISyntaxException e) {
              f = new File(resource.getPath());
            }
            md = parser.parse(f.toPath());
        } else {
            try (InputStream stream = resource.openStream()) {
                md = parser.parse(stream);
            }
        }
        StringWriter w = new StringWriter();
        generator.generate(md, w);
        assertThat(w.toString()).isEqualTo(RestxBuildHelper.toString(getClass().getResourceAsStream(expected)));
    }

}
