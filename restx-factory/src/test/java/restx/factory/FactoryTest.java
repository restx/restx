package restx.factory;

import com.google.common.base.Optional;
import org.junit.Test;

import java.util.Set;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * User: xavierhanin
 * Date: 1/31/13
 * Time: 7:11 PM
 */
public class FactoryTest {

    @Test
    public void should_build_new_component_from_single_machine() throws Exception {
        Factory factory = Factory.builder().addMachine(new SingleNameFactoryMachine<String>(
                0, new NoDepsMachineEngine<String>(Name.of(String.class, "test"), BoundlessComponentBox.FACTORY) {
            @Override
            protected String doNewComponent(SatisfiedBOM satisfiedBOM) {
                return "value1";
            }
        })).build();

        Optional<NamedComponent<String>> component = factory.queryByName(Name.of(String.class, "test")).findOne();

        assertThat(component.isPresent()).isTrue();
        assertThat(component.get().getName()).isEqualTo(Name.of(String.class, "test"));
        assertThat(component.get().getComponent()).isEqualTo("value1");

        assertThat(factory.queryByName(Name.of(String.class, "test")).findOne().get()).isEqualTo(component.get());
    }

    @Test
    public void should_customize_component() throws Exception {
        Factory factory = Factory.builder()
                .addMachine(new SingletonFactoryMachine<>(0, NamedComponent.of(
                        String.class, "test", "hello")))
                .addMachine(new SingletonFactoryMachine<>(0, NamedComponent.of(ComponentCustomizerEngine.class, "cutomizerTest",
                        new ComponentCustomizerEngine() {
                    @Override
                    public <T> boolean canCustomize(Name<T> name) {
                        return name.getClazz() == String.class;
                    }

                    @Override
                    public <T> ComponentCustomizer<T> getCustomizer(Name<T> name) {
                        return new ComponentCustomizer<T>() {
                            @Override
                            public int priority() {
                                return 0;
                            }

                            @Override
                            public NamedComponent<T> customize(NamedComponent<T> namedComponent) {
                                return new NamedComponent<>(
                                        namedComponent.getName(), (T) (namedComponent.getComponent() + " world"));
                            }
                        };
                    }
                })))
                .build();

        Optional<NamedComponent<String>> component = factory.queryByName(Name.of(String.class, "test")).findOne();

        assertThat(component.isPresent()).isTrue();
        assertThat(component.get().getName()).isEqualTo(Name.of(String.class, "test"));
        assertThat(component.get().getComponent()).isEqualTo("hello world");

        assertThat(factory.queryByName(Name.of(String.class, "test")).findOne().get()).isEqualTo(component.get());
    }

    @Test
    public void should_customize_component_with_simple_customizer() throws Exception {
        Factory factory = Factory.builder()
                .addMachine(new SingletonFactoryMachine<>(0, NamedComponent.of(
                        String.class, "test", "hello")))
                .addMachine(new SingletonFactoryMachine<>(0, NamedComponent.of(ComponentCustomizerEngine.class, "cutomizerTest",
                        new SingleComponentClassCustomizerEngine<String>(0, String.class) {
                            @Override
                            public NamedComponent<String> customize(NamedComponent<String> namedComponent) {
                                return new NamedComponent<>(
                                        namedComponent.getName(), namedComponent.getComponent() + " world");
                            }
                })))
                .build();

        Optional<NamedComponent<String>> component = factory.queryByName(Name.of(String.class, "test")).findOne();

        assertThat(component.isPresent()).isTrue();
        assertThat(component.get().getComponent()).isEqualTo("hello world");
    }

    @Test
    public void should_customize_component_with_customizer_with_deps() throws Exception {
        Factory factory = Factory.builder()
                .addMachine(new SingletonFactoryMachine<>(0, NamedComponent.of(
                        String.class, "dep", "world")))
                .addMachine(new SingletonFactoryMachine<>(0, NamedComponent.of(
                        String.class, "test", "hello")))
                .addMachine(new SingleNameFactoryMachine<>(0, new StdMachineEngine<ComponentCustomizerEngine>(
                        Name.of(ComponentCustomizerEngine.class, "cutomizerTest"), BoundlessComponentBox.FACTORY) {
                    private Factory.Query<String> query = Factory.Query.byName(Name.of(String.class, "dep"));

                    @Override
                    public BillOfMaterials getBillOfMaterial() {
                        return BillOfMaterials.of(query);
                    }

                    @Override
                    protected ComponentCustomizerEngine doNewComponent(final SatisfiedBOM satisfiedBOM) {
                        return new SingleComponentClassCustomizerEngine<String>(0, String.class) {
                            @Override
                            public NamedComponent<String> customize(NamedComponent<String> namedComponent) {
                                return new NamedComponent<>(
                                        namedComponent.getName(), namedComponent.getComponent()
                                            + " " + satisfiedBOM.getOne(query).get().getComponent());
                            }
                        };
                    }
                }))
                .build();

        Optional<NamedComponent<String>> component = factory.queryByName(Name.of(String.class, "test")).findOne();
        assertThat(component.isPresent()).isTrue();
        assertThat(component.get().getComponent()).isEqualTo("hello world");
    }

    @Test
    public void should_build_component_lists_from_multiple_machines() throws Exception {
        Factory factory = Factory.builder()
                .addMachine(new SingleNameFactoryMachine(
                        0, new NoDepsMachineEngine<String>(Name.of(String.class, "test 1"), BoundlessComponentBox.FACTORY) {
                    @Override
                    protected String doNewComponent(SatisfiedBOM satisfiedBOM) {
                        return "value 1";
                    }
                }))
                .addMachine(new SingleNameFactoryMachine(
                        0, new NoDepsMachineEngine<String>(Name.of(String.class, "test 2"), BoundlessComponentBox.FACTORY) {
                    @Override
                    protected String doNewComponent(SatisfiedBOM satisfiedBOM) {
                        return "value 2";
                    }
                }))
                .build();

        Set<NamedComponent<String>> components = factory.queryByClass(String.class).find();

        assertThat(components).containsExactly(
                NamedComponent.of(String.class, "test 1", "value 1"),
                NamedComponent.of(String.class, "test 2", "value 2"));
    }

    @Test
    public void should_factory_be_queryable() throws Exception {
        Factory factory = Factory.builder().build();

        assertThat(factory.queryByClass(Factory.class).findAsComponents()).containsExactly(factory);
    }

    @Test
    public void should_allow_to_close_with_factory_queried() throws Exception {
        // check that we don't get a stack overflow error due to box closing the factory
        Factory factory = Factory.builder().build();

        assertThat(factory.queryByClass(Factory.class).findAsComponents()).containsExactly(factory);

        factory.close();
    }
}
