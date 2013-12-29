package restx.specs;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * User: xavierhanin
 * Date: 5/28/13
 * Time: 8:03 AM
 */
public class RestxSpecTest {
    @Test
    public void should_set_title() throws Exception {
        RestxSpec spec = newRestxSpec("title 1",
                ImmutableList.<Given>of(),
                ImmutableList.<When<?>>of());

        assertThat(spec.getTitle()).isEqualTo("title 1");
        assertThat(spec.withTitle("title 2").getTitle()).isEqualTo("title 2");
        assertThat(spec.getTitle()).isEqualTo("title 1");
    }

    @Test
    public void should_get_store_file() throws Exception {
        RestxSpec spec = newRestxSpec("title 1",
                ImmutableList.<Given>of(),
                ImmutableList.<When<?>>of());

        RestxSpec.Storage storage = RestxSpec.Storage.with(new RestxSpec.StorageSettings() {
            @Override
            public String recorderBasePath() {
                return "src/main/resources";
            }

            @Override
            public String recorderBaseSpecPath() {
                return "specs";
            }
        });

        assertThat(storage.getStoreFile(storage.buildPath(Optional.<String>absent(), "title 1")))
                .isEqualTo(new File("src/main/resources/specs/title_1.spec.yaml"));
        assertThat(storage.getStoreFile(storage.buildPath(Optional.of("specs/test1"), "title 1")))
                .isEqualTo(new File("src/main/resources/specs/test1/title_1.spec.yaml"));
    }

    private RestxSpec newRestxSpec(String title, ImmutableList<Given> givens, ImmutableList<When<?>> whens) {
        return new RestxSpec(title, title, givens, whens);
    }
}
