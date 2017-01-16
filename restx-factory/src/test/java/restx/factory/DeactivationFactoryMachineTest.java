package restx.factory;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


import restx.common.TypeReference;

public class DeactivationFactoryMachineTest {
    @Test
    public void should_deactivate_components() throws Exception {
        Name<Integer> one = Name.of(Integer.class, "one");
        Name<Integer> two = Name.of(Integer.class, "two");
        Factory factory = Factory.builder()
                .addMachine(new SingletonFactoryMachine<>(0, new NamedComponent(one, 1)))
                .addMachine(new SingletonFactoryMachine<>(0, new NamedComponent(two, 2)))
                .addMachine(DeactivationFactoryMachine.forNames(one))
                .build();

        assertThat(factory.getComponents(Integer.class)).containsExactly(2);
    }

    @Test
    public void should_deactivate_parameterized_components() throws Exception {
        Name<Generic<Integer>> one = Name.of(new TypeReference<Generic<Integer>>(){}, "one");
        Name<Generic<Integer>> two = Name.of(new TypeReference<Generic<Integer>>(){}, "two");
        Factory factory = Factory.builder()
                .addMachine(new SingletonFactoryMachine<>(0, new NamedComponent<>(one, new Generic<>(1))))
                .addMachine(new SingletonFactoryMachine<>(0, new NamedComponent<>(two, new Generic<>(2))))
                .addMachine(DeactivationFactoryMachine.forNames(one))
                .build();

        assertThat(factory.getComponents(new TypeReference<Generic<Integer>>() {}))
                .containsExactly(
                        new Generic<>(2)
                );
    }

    public static class Generic<T> {
        final T obj;

        public Generic(T obj) {
            this.obj = obj;
        }

        public T getObj() {
            return obj;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof Generic))
                return false;

            Generic<?> generic = (Generic<?>) o;

            return obj.equals(generic.obj);

        }

        @Override
        public int hashCode() {
            return obj.hashCode();
        }
    }
}