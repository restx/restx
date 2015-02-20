package restx.common;

import static org.assertj.core.api.Assertions.assertThat;


import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * Test cases for {@link Types} methods.
 */
public class TypesTest {

	@Rule
	public JUnitSoftAssertions softly = new JUnitSoftAssertions();

	@Test
	public void getRawType_should_return_the_class_of_non_parametrized_types() {
		softly.assertThat(Types.getRawType(Integer.class)).isEqualTo(Integer.class);
		softly.assertThat(Types.getRawType(String.class)).isEqualTo(String.class);
		softly.assertThat(Types.getRawType(int.class)).isEqualTo(int.class);
	}

	@Test
	public void getRawType_should_return_the_reified_class_of_parametrized_types() {
		softly.assertThat(Types.getRawType(new TypeReference<List<String>>() {}.getType()))
				.isEqualTo(List.class);
		softly.assertThat(Types.getRawType(new TypeReference<Map<String, Integer>>() {}.getType()))
				.isEqualTo(Map.class);
	}

	@Test
	public void getRawType_should_return_the_class_of_array_types() throws NoSuchMethodException {
		softly.assertThat(Types.getRawType(new TypeReference<List<String>[]>() {}.getType()))
				.isEqualTo(List[].class);
	}
}