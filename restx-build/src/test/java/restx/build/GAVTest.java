package restx.build;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * User: xavierhanin
 * Date: 4/14/13
 * Time: 2:26 PM
 */
public class GAVTest {
    @Test
    public void should_parse_and_to_string_be_consistent() throws Exception {
        shouldBeConsistent("fr.4sh.pom-parents:4sh-uberpom:0.8");
        shouldBeConsistent("restx:restx-common:0.2-SNAPSHOT");
        shouldBeConsistent("joda-time:joda-time:${joda-time.version}");
    }

    @Test
    public void should_parse_classifier_artifact(){
        shouldBeConsistent("restx:restx-ui:0.2:zip:jdk8");
        shouldBeConsistent("restx:restx-ui:0.2:jar:jdk8");
    }

    @Test
    public void should_parse_optional_artifact(){
        shouldBeConsistent("restx:restx-ui:0.2!optional");
        shouldBeConsistent("restx:restx-ui:0.2:zip!optional");
        shouldBeConsistent("restx:restx-ui:0.2:jar:jdk8!optional");
    }

    @Test
    public void should_parse_artifact_with_type() throws Exception {
        shouldBeConsistent("restx:restx-ui:0.2:zip");
    }

    private void shouldBeConsistent(String gav) {
        assertThat(GAV.parse(gav).toParseableString()).isEqualTo(gav);
    }
}
