package restx.common;

import static org.assertj.core.api.Assertions.assertThat;
import static restx.common.MoreClasses.getInheritedClasses;


import org.junit.Test;

import java.io.Closeable;
import java.io.Externalizable;
import java.io.FileOutputStream;
import java.io.Flushable;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.*;

/**
 * Test cases for {@link MoreClasses}
 *
 * @author apeyrard
 */
public class MoreClassesTest {

	@Test
	public void should_not_find_inherited_classes_for_primitive_types() {
		assertThat(getInheritedClasses(void.class)).isEmpty();
		assertThat(getInheritedClasses(int.class)).isEmpty();
		assertThat(getInheritedClasses(double.class)).isEmpty();
	}

	public static class DummyClass { }

	@Test
	public void should_at_least_find_Object_class_for_classes_without_explicit_inheritance() {
		assertThat(getInheritedClasses(DummyClass.class)).containsExactly(Object.class);
	}

	@Test
	public void should_work_for_interfaces() {
		assertThat(getInheritedClasses(Externalizable.class)).containsExactly(Serializable.class);
	}

	@Test
	public void should_find_inherited_interfaces() {
		assertThat(getInheritedClasses(Number.class)).containsOnly(Serializable.class, Object.class);
		assertThat(getInheritedClasses(AbstractCollection.class)).containsOnly(Collection.class, Iterable.class, Object.class);
	}

	public static class A { }
	public static class B extends A { }
	public static class C extends B { }

	@Test
	public void should_find_inherited_super_classes() {
		assertThat(getInheritedClasses(B.class)).containsOnly(A.class, Object.class);
		assertThat(getInheritedClasses(C.class)).containsOnly(B.class, A.class, Object.class);
	}

	@Test
	public void should_find_superclasses_and_interfaces() {
		assertThat(getInheritedClasses(ArrayList.class)).containsOnly(
				AbstractList.class,
				AbstractCollection.class,
				Object.class,
				List.class,
				Collection.class,
				Iterable.class,
				Serializable.class,
				RandomAccess.class,
				Cloneable.class,
				SequencedCollection.class
		);

		assertThat(getInheritedClasses(FileOutputStream.class)).containsOnly(
				OutputStream.class,
				Object.class,
				Closeable.class,
				AutoCloseable.class,
				Flushable.class
		);
	}
}
