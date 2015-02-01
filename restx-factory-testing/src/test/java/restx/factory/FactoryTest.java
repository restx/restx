package restx.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;


import com.google.common.base.Optional;
import org.junit.BeforeClass;
import org.junit.Test;

import restx.factory.TestComponentPriorities.V;
import restx.factory.TestInnerComponent.A;

/**
 * @author fcamblor
 */
public class FactoryTest {

    /**
     * ElementsFromConfig component can not be build, because of module TestMandatoryDependency
     * which use a missing dependency.
     */
    @BeforeClass
    public static void deactivateElementsFromConfig() {
        System.setProperty("restx.activation::restx.factory.FactoryMachine::ElementsFromConfig", "false");
    }

    @Test
    public void should_optional_dependency_works_and_not_be_injected_when_absent(){
        // check that we don't get a stack overflow error due to box closing the factory
        Factory factory = Factory.builder()
                .addMachine(new TestOptionalDependencyFactoryMachine())
                .build();

        Optional<NamedComponent<String>> result1NamedComp = factory.queryByName(Name.of(String.class, "optional.dep.result1")).findOne();
        assertThat(result1NamedComp.isPresent()).isEqualTo(true);
        assertThat(result1NamedComp.get().getComponent()).isEqualTo("absent");


        Optional<NamedComponent<String>> result2NamedComp = factory.queryByName(Name.of(String.class, "optional.dep.result2")).findOne();
        assertThat(result2NamedComp.isPresent()).isEqualTo(true);
        assertThat(result2NamedComp.get().getComponent()).isEqualTo("absent");

        factory.close();
    }

    @Test
    public void should_respect_provided_priorities_order() {
        Factory factory = Factory.builder().addFromServiceLoader().build();

        assertThat(factory.queryByClass(V.class).find())
                .extracting("priority", "component.val")
                .containsExactly(tuple(0, "B"), tuple(0, "C"), tuple(1, "A"));
    }

    @Test
    public void should_inject_named_component() {
        Factory factory = Factory.builder().addFromServiceLoader().build();

        assertThat(factory.queryByName(Name.of(String.class, "NCB")).findOneAsComponent())
                .isEqualTo(Optional.of("NCA -10 NamedComponentA"));
    }

    @Test
    public void should_inject_multiple_named_components() {
        Factory factory = Factory.builder().addFromServiceLoader().build();

        assertThat(factory.queryByName(Name.of(String.class, "NCMB")).findOneAsComponent())
                .isEqualTo(Optional.of("NCMA2 -10 NamedComponentA2;NCMA1 0 NamedComponentA1"));
    }

    @Test(expected = IllegalStateException.class)
    public void should_missing_mandatory_named_dependency_throws_an_exception(){
        // check that we don't get a stack overflow error due to box closing the factory
        Factory factory = Factory.builder()
                .addMachine(new TestMandatoryDependencyFactoryMachine())
                .build();

        factory.queryByName(Name.of(String.class, "mandatory.dep.result1")).findOne();
    }

    @Test(expected = IllegalStateException.class)
    public void should_missing_mandatory_class_dependency_throws_an_exception(){
        // check that we don't get a stack overflow error due to box closing the factory
        Factory factory = Factory.builder()
                .addMachine(new TestMandatoryDependencyFactoryMachine())
                .build();

        factory.queryByName(Name.of(String.class, "mandatory.dep.result2")).findOne();
    }

    @Test
    public void should_get_component_declared_as_inner_class() throws Exception {
        Factory factory = Factory.builder().addFromServiceLoader().build();

        assertThat(factory.queryByClass(A.class).findOne().isPresent()).isTrue();
    }

    @Test
    public void should_permit_to_force_component_produced_class() {
        Factory factory = Factory.builder().addFromServiceLoader().build();

        TestGreeting component = factory.getComponent(TestGreeting.class);
        assertThat(component.greet()).isEqualTo("hello");

        Optional<NamedComponent<TestGreeting>> one = factory.queryByClass(TestGreeting.class).findOne();
        assertThat(one.isPresent());
        assertThat(one.get().getName().getClazz()).isEqualTo(TestGreeting.class);
    }
}
