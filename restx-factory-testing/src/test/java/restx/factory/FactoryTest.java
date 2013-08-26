package restx.factory;

import com.google.common.base.Optional;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author fcamblor
 */
public class FactoryTest {
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
}
