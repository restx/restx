package restx.factory;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
}