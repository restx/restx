package restx.apidocs.doclet;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class ApidocsDocletRunnerTest {

    @Test
    public void should_isLegacyDocletAvailable_return_true_when_called_with_1_8() {
        // Given
        ApidocsDocletRunner runner = new ApidocsDocletRunner();
        String javaSpecificationVersion = "1.8";

        // When
        boolean output = runner.isLegacyDocletAvailable(javaSpecificationVersion);

        // Then
        Assertions.assertThat(output).isTrue();
    }

    @Test
    public void should_isLegacyDocletAvailable_return_true_when_called_with_9() {
        // Given
        ApidocsDocletRunner runner = new ApidocsDocletRunner();
        String javaSpecificationVersion = "9";

        // When
        boolean output = runner.isLegacyDocletAvailable(javaSpecificationVersion);

        // Then
        Assertions.assertThat(output).isTrue();
    }

    @Test
    public void should_isLegacyDocletAvailable_return_true_when_called_with_1_7() {
        // Given
        ApidocsDocletRunner runner = new ApidocsDocletRunner();
        String javaSpecificationVersion = "1.7";

        // When
        boolean output = runner.isLegacyDocletAvailable(javaSpecificationVersion);

        // Then
        Assertions.assertThat(output).isTrue();
    }

    @Test
    public void should_isLegacyDocletAvailable_return_true_when_called_with_11() {
        // Given
        ApidocsDocletRunner runner = new ApidocsDocletRunner();
        String javaSpecificationVersion = "11";

        // When
        boolean output = runner.isLegacyDocletAvailable(javaSpecificationVersion);

        // Then
        Assertions.assertThat(output).isFalse();
    }

    @Test
    public void should_isLegacyDocletAvailable_return_false_when_called_with_null() {
        // Given
        ApidocsDocletRunner runner = new ApidocsDocletRunner();

        // When
        boolean output = runner.isLegacyDocletAvailable(null);

        // Then
        Assertions.assertThat(output).isFalse();
    }
}
