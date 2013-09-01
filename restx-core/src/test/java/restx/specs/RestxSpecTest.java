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
        RestxSpec spec = new RestxSpec("title 1",
                ImmutableList.<Given>of(),
                ImmutableList.<When>of());

        assertThat(spec.getTitle()).isEqualTo("title 1");
        assertThat(spec.withTitle("title 2").getTitle()).isEqualTo("title 2");
        assertThat(spec.getTitle()).isEqualTo("title 1");
    }

    @Test
    public void should_get_store_file() throws Exception {
        RestxSpec spec = new RestxSpec("title 1",
                ImmutableList.<Given>of(),
                ImmutableList.<When>of());
        assertThat(spec.getStoreFile(Optional.<String>absent(), Optional.<String>absent()))
                .isEqualTo(new File("src/main/resources/specs/title_1.spec.yaml"));
        assertThat(spec.getStoreFile(Optional.of("test1"), Optional.<String>absent()))
                .isEqualTo(new File("src/main/resources/specs/test1/title_1.spec.yaml"));
        assertThat(spec.getStoreFile(Optional.of("test1"), Optional.of("another title")))
                .isEqualTo(new File("src/main/resources/specs/test1/another_title.spec.yaml"));
    }
}
