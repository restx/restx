package restx.common;

import static restx.common.Types.isAssignableFrom;


import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.ArrayList;
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

	@Test
	public void isAssignableFrom_should_return_true_if_types_are_equals() {
		softly.assertThat(isAssignableFrom(String.class, String.class)).isTrue();
		softly.assertThat(isAssignableFrom(int.class, int.class)).isTrue();
		softly.assertThat(isAssignableFrom(
				new TypeReference<List<String>>() {}.getType(),
				new TypeReference<List<String>>() {}.getType())
		).isTrue();
		softly.assertThat(isAssignableFrom(
				new TypeReference<List<String>[]>() {}.getType(),
				new TypeReference<List<String>[]>() {}.getType())
		).isTrue();

		softly.assertThat(isAssignableFrom(String.class, Number.class)).isFalse();
		softly.assertThat(isAssignableFrom(
				new TypeReference<List<String>>() {}.getType(),
				new TypeReference<Map<String, Integer>>() {}.getType())
		).isFalse();
	}

	@Test
	public void isAssignableFrom_should_return_true_if_first_type_is_a_super_type_of_second_type() {
		softly.assertThat(isAssignableFrom(CharSequence.class, String.class));

		Type list = List.class;
		Type listOfString = new TypeReference<List<String>>() {}.getType();
		Type arrayListOfString = new TypeReference<ArrayList<String>>() {}.getType();

		softly.assertThat(isAssignableFrom(list, listOfString)).isTrue();
		softly.assertThat(isAssignableFrom(list, arrayListOfString)).isTrue();
		softly.assertThat(isAssignableFrom(listOfString, arrayListOfString)).isTrue();

		softly.assertThat(isAssignableFrom(listOfString, list)).isFalse();
		softly.assertThat(isAssignableFrom(arrayListOfString, listOfString)).isFalse();

		softly.assertThat(isAssignableFrom(String.class, CharSequence.class)).isFalse();


		// now lets play with some more complex cases

		// UnTypedImpl

		softly.assertThat(isAssignableFrom(
				new TypeReference<GenericInterface<String>>() {}.getType(),
				new TypeReference<UnTypedImpl<String>>() {}.getType()
		)).isTrue();

		softly.assertThat(isAssignableFrom(
				new TypeReference<GenericInterface<String>>() {}.getType(),
				new TypeReference<UnTypedImpl<Integer>>() {}.getType()
		)).isFalse();

		// MoreUnTypedImpl

		softly.assertThat(isAssignableFrom(
				new TypeReference<GenericInterface<String>>() {}.getType(),
				new TypeReference<MoreUnTypedImpl<String>>() {}.getType()
		)).isTrue();

		softly.assertThat(isAssignableFrom(
				new TypeReference<GenericInterface<String>>() {}.getType(),
				new TypeReference<MoreUnTypedImpl<Integer>>() {}.getType()
		)).isFalse();

		softly.assertThat(isAssignableFrom(
				new TypeReference<UnTypedImpl<String>>() {}.getType(),
				new TypeReference<MoreUnTypedImpl<String>>() {}.getType()
		)).isTrue();

		softly.assertThat(isAssignableFrom(
				new TypeReference<UnTypedImpl<String>>() {}.getType(),
				new TypeReference<MoreUnTypedImpl<Integer>>() {}.getType()
		)).isFalse();

		// TypedImpl

		softly.assertThat(isAssignableFrom(
				new TypeReference<GenericInterface<String>>() {}.getType(),
				TypedImpl.class
		)).isTrue();

		softly.assertThat(isAssignableFrom(
				new TypeReference<GenericInterface<Integer>>() {}.getType(),
				TypedImpl.class
		)).isFalse();

		// MoreTypedImpl

		softly.assertThat(isAssignableFrom(
				new TypeReference<GenericInterface<String>>() {}.getType(),
				MoreTypedImpl.class
		)).isTrue();

		softly.assertThat(isAssignableFrom(
				new TypeReference<GenericInterface<Integer>>() {}.getType(),
				MoreTypedImpl.class
		)).isFalse();

		softly.assertThat(isAssignableFrom(
				TypedImpl.class,
				MoreTypedImpl.class
		)).isTrue();

		// GenericTypedImpl

		softly.assertThat(isAssignableFrom(
				new TypeReference<GenericInterface<String>>() {}.getType(),
				new TypeReference<GenericTypedImpl<Integer>>() {}.getType()
		)).isTrue();

		softly.assertThat(isAssignableFrom(
				new TypeReference<GenericInterface<Integer>>() {}.getType(),
				new TypeReference<GenericTypedImpl<Integer>>() {}.getType()
		)).isFalse();

		// MoreGenericTypedImpl

		softly.assertThat(isAssignableFrom(
				new TypeReference<GenericInterface<String>>() {}.getType(),
				new TypeReference<MoreGenericTypedImpl<Integer>>() {}.getType()
		)).isTrue();

		softly.assertThat(isAssignableFrom(
				new TypeReference<GenericInterface<Integer>>() {}.getType(),
				new TypeReference<MoreGenericTypedImpl<Integer>>() {}.getType()
		)).isFalse();

		softly.assertThat(isAssignableFrom(
				new TypeReference<GenericTypedImpl<Integer>>() {}.getType(),
				new TypeReference<MoreGenericTypedImpl<Integer>>() {}.getType()
		)).isTrue();

		softly.assertThat(isAssignableFrom(
				new TypeReference<GenericTypedImpl<Integer>>() {}.getType(),
				new TypeReference<MoreGenericTypedImpl<Double>>() {}.getType()
		)).isTrue();

		softly.assertThat(isAssignableFrom(
				new TypeReference<GenericTypedImpl<String>>() {}.getType(),
				new TypeReference<MoreGenericTypedImpl<Double>>() {}.getType()
		)).isFalse();

		// SomethingMap and SuperMap

		softly.assertThat(isAssignableFrom(
				new TypeReference<GenericInterface<Double>>() {}.getType(),
				new TypeReference<SomethingMap<Double, Integer>>() {}.getType()
		)).isTrue();

		softly.assertThat(isAssignableFrom(
				new TypeReference<GenericInterface<String>>() {}.getType(),
				new TypeReference<SomethingMap<String, Integer>>() {}.getType()
		)).isTrue();

		softly.assertThat(isAssignableFrom(
				new TypeReference<GenericInterface<String>>() {}.getType(),
				new TypeReference<SomethingMap<Number, Integer>>() {}.getType()
		)).isFalse();

		softly.assertThat(isAssignableFrom(
				new TypeReference<GenericInterface<Double>>() {}.getType(),
				new TypeReference<SuperMap<Integer, Double>>() {}.getType()
		)).isTrue();

		softly.assertThat(isAssignableFrom(
				new TypeReference<GenericInterface<String>>() {}.getType(),
				new TypeReference<SuperMap<Integer, String>>() {}.getType()
		)).isTrue();

		softly.assertThat(isAssignableFrom(
				new TypeReference<GenericInterface<String>>() {}.getType(),
				new TypeReference<SuperMap<Integer, Number>>() {}.getType()
		)).isFalse();

		// FixedSomethingMap and FixedSuperMap

		softly.assertThat(isAssignableFrom(
				new TypeReference<GenericInterface<Double>>() {}.getType(),
				new TypeReference<FixedSuperMap<Integer, Long>>() {}.getType()
		)).isTrue();

		softly.assertThat(isAssignableFrom(
				new TypeReference<GenericInterface<Double>>() {}.getType(),
				new TypeReference<FixedSuperMap<Number, Double>>() {}.getType()
		)).isTrue();

		softly.assertThat(isAssignableFrom(
				new TypeReference<GenericInterface<Integer>>() {}.getType(),
				new TypeReference<FixedSuperMap<Number, Double>>() {}.getType()
		)).isFalse();
	}

	/*
		some classes and interfaces used in isAssignableFrom test cases
	 */

	public static interface GenericInterface<T> {}

	public static class UnTypedImpl<T> implements GenericInterface<T> {}

	public static class MoreUnTypedImpl<T> extends UnTypedImpl<T> {}

	public static class TypedImpl implements GenericInterface<String> {}

	public static class MoreTypedImpl extends TypedImpl {}

	public static class GenericTypedImpl<T> implements GenericInterface<String> {}

	public static class MoreGenericTypedImpl<T> extends GenericTypedImpl<Integer> {}

	public static class SomethingMap<A, B> implements GenericInterface<A> {}

	public static class SuperMap<A,B> extends SomethingMap<B, String> {}

	public static class FixedSomethingMap<A, B> implements GenericInterface<Double> {}

	public static class FixedSuperMap<A,B> extends FixedSomethingMap<B, String> {}
}
