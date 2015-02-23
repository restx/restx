package restx.common;

import static restx.common.Types.isAssignableFrom;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;

import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
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
	public void isAssignableFrom_should_work_with_raw_types() {
		softly.assertThat(isAssignableFrom(CharSequence.class, String.class)).isTrue();
		softly.assertThat(isAssignableFrom(Number.class, Integer.class)).isTrue();
		softly.assertThat(isAssignableFrom(List.class, ArrayList.class)).isTrue();

		softly.assertThat(isAssignableFrom(Map.class, ArrayList.class)).isFalse();
		softly.assertThat(isAssignableFrom(Integer.class, Number.class)).isFalse();
	}

	@Test
	public void isAssignableFrom_should_return_true_for_a_reifiable_type_and_any_of_the_same_type_parameterized() {
		softly.assertThat(isAssignableFrom(
				List.class,
				new TypeReference<List<String>>() {}.getType()
		)).isTrue();

		softly.assertThat(isAssignableFrom(
				List.class,
				new TypeReference<List<Integer>>() {}.getType()
		)).isTrue();

		softly.assertThat(isAssignableFrom(
				Map.class,
				new TypeReference<List<String>>() {}.getType()
		)).isFalse();

		softly.assertThat(isAssignableFrom(
				Map.class,
				new TypeReference<Map<String, Integer>>() {}.getType()
		)).isTrue();
	}

	@Test
	public void isAssignableFrom_should_return_true_for_a_reifiable_type_and_any_sub_parameterized_type() {
		softly.assertThat(isAssignableFrom(
				List.class,
				new TypeReference<ArrayList<String>>() {}.getType()
		)).isTrue();

		softly.assertThat(isAssignableFrom(
				List.class,
				new TypeReference<AbstractList<Integer>>() {}.getType()
		)).isTrue();

		softly.assertThat(isAssignableFrom(
				Map.class,
				new TypeReference<ImmutableList<String>>() {}.getType()
		)).isFalse();

		softly.assertThat(isAssignableFrom(
				Map.class,
				new TypeReference<ImmutableMap<String, Integer>>() {}.getType()
		)).isTrue();
	}

	@Test
	public void isAssignableFrom_should_work_for_parameterized_interfaces_and_a_direct_implementation() {
		// List and AbstractList

		softly.assertThat(isAssignableFrom(
				new TypeReference<List<String>>() {}.getType(),
				new TypeReference<AbstractList<String>>() {}.getType()
		)).isTrue();

		// GenericInterface and UnTypedImpl

		softly.assertThat(isAssignableFrom(
				new TypeReference<GenericInterface<String>>() {}.getType(),
				new TypeReference<UnTypedImpl<String>>() {}.getType()
		)).isTrue();

		softly.assertThat(isAssignableFrom(
				new TypeReference<GenericInterface<String>>() {}.getType(),
				new TypeReference<UnTypedImpl<Integer>>() {}.getType()
		)).isFalse();

		// Map and AbstractMap

		softly.assertThat(isAssignableFrom(
				new TypeReference<Map<String, Number>>() {}.getType(),
				new TypeReference<AbstractMap<String, Number>>() {}.getType()
		)).isTrue();

		softly.assertThat(isAssignableFrom(
				new TypeReference<Map<String, Number>>() {}.getType(),
				new TypeReference<AbstractMap<Double, Number>>() {}.getType()
		)).isFalse();

		softly.assertThat(isAssignableFrom(
				new TypeReference<Map<String, Number>>() {}.getType(),
				new TypeReference<AbstractMap<String, Integer>>() {}.getType()
		)).isFalse();
	}

	@Test
	public void isAssignableFrom_should_work_for_parameterized_class_and_a_direct_sub_class() {
		// AbstractList and ArrayList

		softly.assertThat(isAssignableFrom(
				new TypeReference<AbstractList<String>>() {}.getType(),
				new TypeReference<ArrayList<String>>() {}.getType()
		)).isTrue();

		softly.assertThat(isAssignableFrom(
				new TypeReference<AbstractList<String>>() {}.getType(),
				new TypeReference<ArrayList<Number>>() {}.getType()
		)).isFalse();

		// UnTypedImpl and MoreUnTypedImpl

		softly.assertThat(isAssignableFrom(
				new TypeReference<UnTypedImpl<String>>() {}.getType(),
				new TypeReference<MoreUnTypedImpl<String>>() {}.getType()
		)).isTrue();

		softly.assertThat(isAssignableFrom(
				new TypeReference<UnTypedImpl<String>>() {}.getType(),
				new TypeReference<MoreUnTypedImpl<Integer>>() {}.getType()
		)).isFalse();

		// AbstractMap and HashMap

		softly.assertThat(isAssignableFrom(
				new TypeReference<AbstractMap<String, Number>>() {}.getType(),
				new TypeReference<HashMap<String, Number>>() {}.getType()
		)).isTrue();

		softly.assertThat(isAssignableFrom(
				new TypeReference<AbstractMap<String, Number>>() {}.getType(),
				new TypeReference<HashMap<Double, Number>>() {}.getType()
		)).isFalse();

		softly.assertThat(isAssignableFrom(
				new TypeReference<AbstractMap<String, Number>>() {}.getType(),
				new TypeReference<HashMap<String, Integer>>() {}.getType()
		)).isFalse();
	}

	@Test
	public void isAssignableFrom_should_work_with_not_direct_sub_types() {
		// GenericInterface and MoreUnTypedImpl

		softly.assertThat(isAssignableFrom(
				new TypeReference<GenericInterface<String>>() {}.getType(),
				new TypeReference<MoreUnTypedImpl<String>>() {}.getType()
		)).isTrue();

		softly.assertThat(isAssignableFrom(
				new TypeReference<GenericInterface<String>>() {}.getType(),
				new TypeReference<MoreUnTypedImpl<Integer>>() {}.getType()
		)).isFalse();
	}

	@Test
	public void isAssignableFrom_should_work_with_fixed_generic_interface_implementation() {
		// GenericInterface and StringTypedImpl

		softly.assertThat(isAssignableFrom(
				new TypeReference<GenericInterface<String>>() {}.getType(),
				StringTypedImpl.class
		)).isTrue();

		softly.assertThat(isAssignableFrom(
				new TypeReference<GenericInterface<Integer>>() {}.getType(),
				StringTypedImpl.class
		)).isFalse();

		// GenericInterface and MoreStringTypedImpl

		softly.assertThat(isAssignableFrom(
				new TypeReference<GenericInterface<String>>() {}.getType(),
				MoreStringTypedImpl.class
		)).isTrue();

		softly.assertThat(isAssignableFrom(
				new TypeReference<GenericInterface<Integer>>() {}.getType(),
				MoreStringTypedImpl.class
		)).isFalse();

		softly.assertThat(isAssignableFrom(
				StringTypedImpl.class,
				MoreStringTypedImpl.class
		)).isTrue();

		// GenericInterface and GenericStringTypedImpl

		softly.assertThat(isAssignableFrom(
				new TypeReference<GenericInterface<String>>() {}.getType(),
				new TypeReference<GenericStringTypedImpl<Integer>>() {}.getType()
		)).isTrue();

		softly.assertThat(isAssignableFrom(
				new TypeReference<GenericInterface<Integer>>() {}.getType(),
				new TypeReference<GenericStringTypedImpl<Integer>>() {}.getType()
		)).isFalse();

		// GenericInterface, GenericStringTypedImpl and MoreIntegerGenericStringTypedImpl

		softly.assertThat(isAssignableFrom(
				new TypeReference<GenericInterface<String>>() {}.getType(),
				new TypeReference<MoreIntegerGenericStringTypedImpl<Integer>>() {}.getType()
		)).isTrue();

		softly.assertThat(isAssignableFrom(
				new TypeReference<GenericInterface<Integer>>() {}.getType(),
				new TypeReference<MoreIntegerGenericStringTypedImpl<Integer>>() {}.getType()
		)).isFalse();

		softly.assertThat(isAssignableFrom(
				new TypeReference<GenericStringTypedImpl<Integer>>() {}.getType(),
				new TypeReference<MoreIntegerGenericStringTypedImpl<Integer>>() {}.getType()
		)).isTrue();

		softly.assertThat(isAssignableFrom(
				new TypeReference<GenericStringTypedImpl<Integer>>() {}.getType(),
				new TypeReference<MoreIntegerGenericStringTypedImpl<Double>>() {}.getType()
		)).isTrue();

		softly.assertThat(isAssignableFrom(
				new TypeReference<GenericStringTypedImpl<String>>() {}.getType(),
				new TypeReference<MoreIntegerGenericStringTypedImpl<Double>>() {}.getType()
		)).isFalse();
	}


	@Test
	public void isAssignableFrom_should_manage_to_match_generic_variable_from_sub_types_to_super_types() {
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

	@SuppressWarnings("unused")
	public static interface GenericInterface<T> {}

	public static class UnTypedImpl<T> implements GenericInterface<T> {}

	public static class MoreUnTypedImpl<T> extends UnTypedImpl<T> {}

	public static class StringTypedImpl implements GenericInterface<String> {}

	public static class MoreStringTypedImpl extends StringTypedImpl {}

	@SuppressWarnings("unused")
	public static class GenericStringTypedImpl<T> implements GenericInterface<String> {}

	@SuppressWarnings("unused")
	public static class MoreIntegerGenericStringTypedImpl<T> extends GenericStringTypedImpl<Integer> {}

	@SuppressWarnings("unused")
	public static class SomethingMap<A, B> implements GenericInterface<A> {}

	@SuppressWarnings("unused")
	public static class SuperMap<A,B> extends SomethingMap<B, String> {}

	@SuppressWarnings("unused")
	public static class FixedSomethingMap<A, B> implements GenericInterface<Double> {}

	@SuppressWarnings("unused")
	public static class FixedSuperMap<A,B> extends FixedSomethingMap<B, String> {}
}
