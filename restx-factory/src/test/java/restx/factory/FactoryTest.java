package restx.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static restx.factory.Factory.LocalMachines.threadLocal;


import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import org.junit.After;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.Set;
import restx.common.TypeReference;

/**
 * User: xavierhanin
 * Date: 1/31/13
 * Time: 7:11 PM
 */
public class FactoryTest {

    @Test
    public void should_compare_named_component() throws Exception {
        assertThat(Factory.NAMED_COMPONENT_COMPARATOR.compare(
                NamedComponent.of(String.class, "test1", 0, "A"),
                NamedComponent.of(String.class, "test2", 0, "A")
        )).isLessThan(0);
        assertThat(Factory.NAMED_COMPONENT_COMPARATOR.compare(
                NamedComponent.of(String.class, "test", 0, "A"),
                NamedComponent.of(String.class, "test2", 0, "A")
        )).isLessThan(0);
        assertThat(Factory.NAMED_COMPONENT_COMPARATOR.compare(
                NamedComponent.of(String.class, "test2", -10, "A"),
                NamedComponent.of(String.class, "test1", 0, "A")
        )).isLessThan(0);
    }

    @Test
    public void should_build_new_component_from_single_machine() throws Exception {
        Factory factory = Factory.builder().addMachine(testMachine()).build();

        Optional<NamedComponent<String>> component = factory.queryByName(Name.of(String.class, "test")).findOne();

        assertThat(component.isPresent()).isTrue();
        assertThat(component.get().getName()).isEqualTo(Name.of(String.class, "test"));
        assertThat(component.get().getComponent()).isEqualTo("value1");

        assertThat(factory.queryByName(Name.of(String.class, "test")).findOne().get()).isEqualTo(component.get());
    }

    @Test
    public void should_permit_to_query_component_by_name_using_types() throws Exception {
        Factory factory = Factory.builder().addMachine(testMachine()).build();

        // the cast permit to force the use of the name with Type signature
        Name<String> name = Name.of((Type) String.class, "test");
        Optional<NamedComponent<String>> component = factory.queryByName(name).findOne();

        assertThat(component.isPresent()).isTrue();
        assertThat(component.get().getName()).isEqualTo(Name.of(String.class, "test"));
        assertThat(component.get().getComponent()).isEqualTo("value1");

        assertThat(factory.queryByName(Name.of(String.class, "test")).findOne().get()).isEqualTo(component.get());
    }

    @Test
    public void should_permit_to_query_component_by_types() throws Exception {
        Factory factory = Factory.builder().addMachine(testMachine()).build();

        // first test the query by type signature with the Type
        Optional<NamedComponent<String>> component = factory.<String>queryByType(String.class).findOne();

        assertThat(component.isPresent()).isTrue();
        assertThat(component.get().getName()).isEqualTo(Name.of(String.class, "test"));
        assertThat(component.get().getComponent()).isEqualTo("value1");

        assertThat(factory.queryByName(Name.of(String.class, "test")).findOne().get()).isEqualTo(component.get());

        // the signature with raw type and types args might not be tested here, as String is not a parameterized type

        // finally with type reference
        component = factory.queryByType(new TypeReference<String>() {}).findOne();

        assertThat(component.isPresent()).isTrue();
        assertThat(component.get().getName()).isEqualTo(Name.of(String.class, "test"));
        assertThat(component.get().getComponent()).isEqualTo("value1");

        assertThat(factory.queryByName(Name.of(String.class, "test")).findOne().get()).isEqualTo(component.get());
    }

    @Test
    public void should_permit_to_query_generic_components_using_types() throws Exception {
        Factory factory = Factory.builder()
                .addMachine(testGenericStringMachine("generic"))
                .addMachine(testGenericIntegerMachine("generic"))
                .build();

        Optional<NamedComponent<GenericHolder<String>>> component = factory.<GenericHolder<String>>queryByType(GenericHolder.class, String.class).findOne();
        assertThat(component.isPresent()).isTrue();
        assertThat(component.get().getName().getName()).isEqualTo("generic");
        assertThat(component.get().getComponent().getValue()).isEqualTo("foo");

        Optional<NamedComponent<GenericHolder<Integer>>> component2 = factory.<GenericHolder<Integer>>queryByType(GenericHolder.class, Integer.class).findOne();
        assertThat(component2.isPresent()).isTrue();
        assertThat(component2.get().getName().getName()).isEqualTo("generic");
        assertThat(component2.get().getComponent().getValue()).isEqualTo(42);

        Optional<NamedComponent<GenericHolder<Integer>>> component3 = factory.queryByType(new TypeReference<GenericHolder<Integer>>() {}).findOne();
        assertThat(component3.isPresent()).isTrue();
        assertThat(component3.get().getName().getName()).isEqualTo("generic");
        assertThat(component3.get().getComponent().getValue()).isEqualTo(42);
    }

    @Test
     public void should_permit_to_query_generic_components_using_names() throws Exception {
        Factory factory = Factory.builder()
                .addMachine(testGenericStringMachine("generic"))
                .addMachine(testGenericIntegerMachine("generic"))
                .build();

        Optional<NamedComponent<GenericHolder<String>>> component = factory.queryByName(Name.<GenericHolder<String>>of("generic", GenericHolder
                .class, String.class)).findOne();
        assertThat(component.isPresent()).isTrue();
        assertThat(component.get().getName().getName()).isEqualTo("generic");
        assertThat(component.get().getComponent().getValue()).isEqualTo("foo");

        Optional<NamedComponent<GenericHolder<Integer>>> component2 = factory.queryByName(Name.<GenericHolder<Integer>>of("generic", GenericHolder
                .class, Integer.class)).findOne();
        assertThat(component2.isPresent()).isTrue();
        assertThat(component2.get().getName().getName()).isEqualTo("generic");
        assertThat(component2.get().getComponent().getValue()).isEqualTo(42);

        Optional<NamedComponent<GenericHolder<Integer>>> component3 = factory.queryByName(Name.of(new TypeReference<GenericHolder<Integer>>() {
        }, "generic")).findOne();
        assertThat(component3.isPresent()).isTrue();
        assertThat(component3.get().getName().getName()).isEqualTo("generic");
        assertThat(component3.get().getComponent().getValue()).isEqualTo(42);
    }

    @Test
    public void should_permit_to_query_generic_components_using_rawType() throws Exception {
        Factory factory = Factory.builder()
                .addMachine(testGenericStringMachine("genericstring"))
                .addMachine(testGenericIntegerMachine("genericinteger"))
                .build();

        Optional<NamedComponent<GenericHolder>> component = factory.queryByName(Name.of(GenericHolder.class, "genericstring")).findOne();
        assertThat(component.isPresent()).isTrue();
        assertThat(component.get().getName().getName()).isEqualTo("genericstring");
        assertThat(component.get().getComponent().getValue()).isEqualTo("foo");

        Optional<NamedComponent<GenericHolder>> component2 = factory.queryByName(Name.of(GenericHolder.class, "genericinteger")).findOne();
        assertThat(component2.isPresent()).isTrue();
        assertThat(component2.get().getName().getName()).isEqualTo("genericinteger");
        assertThat(component2.get().getComponent().getValue()).isEqualTo(42);
    }

    @Test
    public void should_not_allow_more_than_one_matching_component_if_raw_type_are_used() throws Exception {
        Factory factory = Factory.builder()
                .addMachine(testGenericStringMachine("genericstring"))
                .addMachine(testGenericIntegerMachine("genericinteger"))
                .build();

        try {
            factory.queryByClass(GenericHolder.class).findOne();
            fail("should throw exception");
        } catch (Factory.UnsatisfiedDependenciesException ignored) {}

        try {
            factory.queryByType(GenericHolder.class).findOne();
            fail("should throw exception");
        } catch (Factory.UnsatisfiedDependenciesException ignored) {}
    }

    @Test
    public void should_retrieve_highest_priority_for_raw_types_using_query_by_names() {
        Factory factory = Factory.builder()
                .addMachine(testGenericStringMachine("generic", -100))
                .addMachine(testGenericIntegerMachine("generic", 10))
                .build();
        Optional<NamedComponent<GenericHolder>> component = factory.queryByName(Name.of(GenericHolder.class, "generic")).findOne();
        assertThat(component.isPresent()).isTrue();
        assertThat(component.get().getName().getName()).isEqualTo("generic");
        assertThat(component.get().getComponent().getValue()).isEqualTo("foo");

        factory = Factory.builder()
                .addMachine(testGenericStringMachine("generic", 10))
                .addMachine(testGenericIntegerMachine("generic", -1000))
                .build();
        component = factory.queryByName(Name.of(GenericHolder.class, "generic")).findOne();
        assertThat(component.isPresent()).isTrue();
        assertThat(component.get().getName().getName()).isEqualTo("generic");
        assertThat(component.get().getComponent().getValue()).isEqualTo(42);
    }

    @Test
    public void should_retrieve_highest_priority_using_query_by_names() {
        Factory factory = Factory.builder()
                .addMachine(testMachine("common_name", "foo", -100))
                .addMachine(testMachine("common_name", "bar", 10))
                .build();
        Optional<NamedComponent<String>> component = factory.queryByName(Name.of(String.class, "common_name")).findOne();
        assertThat(component.isPresent()).isTrue();
        assertThat(component.get().getName().getName()).isEqualTo("common_name");
        assertThat(component.get().getComponent()).isEqualTo("foo");

        factory = Factory.builder()
                .addMachine(testMachine("common_name", "foo", 100))
                .addMachine(testMachine("common_name", "bar", 10))
                .build();
        component = factory.queryByName(Name.of(String.class, "common_name")).findOne();
        assertThat(component.isPresent()).isTrue();
        assertThat(component.get().getName().getName()).isEqualTo("common_name");
        assertThat(component.get().getComponent()).isEqualTo("bar");
    }

    @Test
    public void should_concat_new_single_machine() throws Exception {
        Factory factory = Factory.builder().addMachine(testMachine()).build();
        factory = factory.concat(new SingletonFactoryMachine<>(0, NamedComponent.of(String.class, "c2", "v2")));

        assertThat(factory.getComponent(Name.of(String.class, "test"))).isEqualTo("value1");
        assertThat(factory.getComponent(Name.of(String.class, "c2"))).isEqualTo("v2");
    }

    @Test
    public void should_build_with_warehouse() throws Exception {
        Factory factory = Factory.builder().addMachine(testMachine()).addMachine(testMachine("test3")).build();
        factory.getComponent(Name.of(String.class, "test")); // this will build the component and put it into the warehouse
        factory = Factory.builder()
                .addMachine(testMachine("test2"))
                .addWarehouseProvider(factory.getWarehouse())
                .build();

        assertThat(factory.getComponent(Name.of(String.class, "test"))).isEqualTo("value1");
        assertThat(factory.getComponent(Name.of(String.class, "test2"))).isEqualTo("value1");
        assertThat(factory.queryByName(Name.of(String.class, "test3")).optional().findOne().isPresent()).isFalse();
    }

    @Test
    public void should_start_auto_startable() throws Exception {
        LifecycleComponent lifecycleComponent = new LifecycleComponent();
        Factory factory = Factory.builder().addMachine(new SingletonFactoryMachine<>(0,
                NamedComponent.of(
                        AutoStartable.class, "t", lifecycleComponent))).build();
        factory.start();

        assertThat(lifecycleComponent.started).isTrue();
    }

    @Test
    public void should_start_auto_startable_in_order() throws Exception {
        final LifecycleComponent lifecycleComponent1 = new LifecycleComponent();
        final boolean[] l2Started = new boolean[1];
        final boolean[] wasL1Started = new boolean[1];
        Factory factory = Factory.builder()
                .addMachine(new SingletonFactoryMachine<>(10, NamedComponent.of(
                        AutoStartable.class, "t0", new AutoStartable() {
                            @Override
                            public void start() {
                                wasL1Started[0] = lifecycleComponent1.started;
                                l2Started[0] = true;
                            }
                        }
                )))
                .addMachine(new SingletonFactoryMachine<>(0, NamedComponent.of(
                        AutoStartable.class, "t1", lifecycleComponent1)))
                .build();
        factory.start();

        assertThat(lifecycleComponent1.started).isTrue();
        assertThat(l2Started[0]).isTrue();
        assertThat(wasL1Started[0]).isTrue();
    }

    @Test
    public void should_prepare_auto_preparable() throws Exception {
        LifecycleComponent lifecycleComponent = new LifecycleComponent();
        Factory factory = Factory.builder().addMachine(new SingletonFactoryMachine<>(0,
                NamedComponent.of(
                        AutoPreparable.class, "t", lifecycleComponent))).build();
        factory.prepare();

        assertThat(lifecycleComponent.prepared).isTrue();
    }

    @Test
    public void should_not_close_closeable_from_other_warehouse() throws Exception {
        LifecycleComponent lifecycleComponent = new LifecycleComponent();
        Factory factory = Factory.builder().addMachine(new SingletonFactoryMachine<>(0,
                NamedComponent.of(
                        AutoStartable.class, "t", lifecycleComponent))).build();
        factory.start();
        assertThat(lifecycleComponent.started).isTrue();

        Factory otherFactory = Factory.builder()
                .addMachine(testMachine())
                .addWarehouseProvider(factory.getWarehouse())
                .build();
        assertThat(otherFactory.getComponents(AutoStartable.class)).containsOnly(lifecycleComponent);

        otherFactory.close();
        assertThat(lifecycleComponent.closed).isFalse();
        factory.close();
        assertThat(lifecycleComponent.closed).isTrue();
    }

    @Test
    public void should_build_new_component_with_deps() throws Exception {
        Factory factory = Factory.builder()
                .addMachine(testMachine())
                .addMachine(new SingleNameFactoryMachine<>(
                        0, new StdMachineEngine<String>(Name.of(String.class, "test2"), BoundlessComponentBox.FACTORY) {
                    private Factory.Query<String> stringQuery = Factory.Query.byName(Name.of(String.class, "test"));

                    @Override
                    public BillOfMaterials getBillOfMaterial() {
                        return BillOfMaterials.of(stringQuery);
                    }

                    @Override
                    protected String doNewComponent(SatisfiedBOM satisfiedBOM) {
                        return satisfiedBOM.getOne(stringQuery).get().getComponent() + " value2";
                    }
                }))
                .build();

        Optional<NamedComponent<String>> component = factory.queryByName(Name.of(String.class, "test2")).findOne();

        assertThat(component.isPresent()).isTrue();
        assertThat(component.get().getName()).isEqualTo(Name.of(String.class, "test2"));
        assertThat(component.get().getComponent()).isEqualTo("value1 value2");
        assertThat(factory.getComponent(Name.of(String.class, "test2"))).isEqualTo("value1 value2");

        assertThat(factory.queryByName(Name.of(String.class, "test2")).findOne().get()).isEqualTo(component.get());
    }

    @Test
    public void should_fail_with_missing_deps() throws Exception {
        SingleNameFactoryMachine<String> machine = machineWithMissingDependency();
        Factory factory = Factory.builder().addMachine(machine).build();

        try {
            factory.queryByName(Name.of(String.class, "test")).findOne();
            fail("should raise exception when asking for a component with missing dependency");
        } catch (IllegalStateException e) {
            assertThat(e)
                .hasMessageStartingWith(
                    "\n" +
                            "  QueryByName{name=Name{name='test', type=java.lang.String[]}}\n" +
                            "    |       \\__=> Name{name='test', type=java.lang.String[]}\n" +
                            "    |\n" +
                            "    +-> QueryByName{name=Name{name='missing', type=java.lang.String[]}}\n" +
                            "          |\n" +
                            "          +--: Name{name='missing', type=java.lang.String[]} can't be satisfied")
            ;
        }
    }

    @Test
    public void should_fail_with_similar_components() throws Exception {
        SingleNameFactoryMachine<String> machine = machineWithMissingDependency();
        Factory factory = Factory.builder().addMachine(machine).addMachine(testMachine("ASIMILARCOMPONENT")).build();

        try {
            factory.queryByName(Name.of(String.class, "test")).findOne();
            fail("should raise exception when asking for a component with missing dependency");
        } catch (IllegalStateException e) {
            assertThat(e)
                    .hasMessageContaining("ASIMILARCOMPONENT")
            ;
        }
    }

    @Test
    public void should_find_one_when_multiple_available_fail() throws Exception {
        Factory factory = Factory.builder().addMachine(testMachine("test")).addMachine(testMachine("test2")).build();

        try {
            factory.queryByClass(String.class).findOne();
            fail("should raise exception when asking for one component with multiple available");
        } catch (IllegalStateException e) {
            assertThat(e)
                    .hasMessage("more than one component is available for query QueryByClass{componentClass=java.lang.String[]}.\n" +
                            " Please select which one you want with a more specific query,\n" +
                            " or by deactivating one of the available components.\n" +
                            " Available components:\n" +
                            " - NamedComponent{name=Name{name='test', type=java.lang.String[]}, priority=0, component=value1}\n" +
                            "         [Activation key: 'restx.activation::java.lang.String::test']\n" +
                            " - NamedComponent{name=Name{name='test2', type=java.lang.String[]}, priority=0, component=value1}\n" +
                            "         [Activation key: 'restx.activation::java.lang.String::test2']\n")
            ;
        }
    }

    @Test
    public void should_warn_about_missing_annotated_machine() throws Exception {
        Factory factory = Factory.builder().addFromServiceLoader().build();

        assertThat(factory.dump()).contains(TestMissingAnnotatedMachine.class.getName());
    }

    @Test
    public void should_dump_list_overrider() throws Exception {
        SingleNameFactoryMachine<String> machine1 = testMachine();
        SingleNameFactoryMachine<String> machine2 = overridingMachine();
        Factory factory = Factory.builder()
                .addMachine(machine1)
                .addMachine(machine2)
                .build();

        assertThat(factory.dump()).contains("OVERRIDING:\n         " + machine1);
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
                            @SuppressWarnings("unchecked")
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
    public void should_handle_machine_factory_to_build_conditional_component() throws Exception {
        SingleNameFactoryMachine<FactoryMachine> alternativeMachine = alternativeMachine();

        Factory factory = Factory.builder()
                .addMachine(new SingletonFactoryMachine<>(0, NamedComponent.of(String.class, "mode", "dev")))
                .addMachine(alternativeMachine)
                .build();

        Optional<NamedComponent<String>> component = factory.queryByName(Name.of(String.class, "test")).optional().findOne();
        assertThat(component.isPresent()).isTrue();
        assertThat(component.get().getComponent()).isEqualTo("hello");

        factory = Factory.builder()
                        .addMachine(new SingletonFactoryMachine<>(0, NamedComponent.of(String.class, "mode", "prod")))
                        .addMachine(alternativeMachine)
                        .build();

        component = factory.queryByName(Name.of(String.class, "test")).optional().findOne();
        assertThat(component.isPresent()).isFalse();
    }

    @Test
    public void should_handle_machine_factory_to_build_alternative() throws Exception {
        SingleNameFactoryMachine<FactoryMachine> alternativeMachine = alternativeMachine();

        Factory factory = Factory.builder()
                .addMachine(new SingletonFactoryMachine<>(0, NamedComponent.of(String.class, "mode", "dev")))
                .addMachine(new SingletonFactoryMachine<>(0, NamedComponent.of(String.class, "test", "default")))
                .addMachine(alternativeMachine)
                .build();

        Optional<NamedComponent<String>> component = factory.queryByName(Name.of(String.class, "test")).optional().findOne();
        assertThat(component.isPresent()).isTrue();
        assertThat(component.get().getComponent()).isEqualTo("hello");

        factory = Factory.builder()
                        .addMachine(new SingletonFactoryMachine<>(0, NamedComponent.of(String.class, "mode", "prod")))
                        .addMachine(new SingletonFactoryMachine<>(0, NamedComponent.of(String.class, "test", "default")))
                        .addMachine(alternativeMachine)
                        .build();

        component = factory.queryByName(Name.of(String.class, "test")).optional().findOne();
        assertThat(component.isPresent()).isTrue();
        assertThat(component.get().getComponent()).isEqualTo("default");
    }

    @Test
    public void should_handle_machine_factory_with_dependencies_on_other_machine_factory() throws Exception {
        SingleNameFactoryMachine<FactoryMachine> alternativeMachine = alternativeMachine();
        SingleNameFactoryMachine<FactoryMachine> dependentMachine = new SingleNameFactoryMachine<>(0, new StdMachineEngine<FactoryMachine>(
                Name.of(FactoryMachine.class, "machineFactoryTest2"), BoundlessComponentBox.FACTORY) {
            private Factory.Query<String> query = Factory.Query.byName(Name.of(String.class, "test"));

            @Override
            public BillOfMaterials getBillOfMaterial() {
                return BillOfMaterials.of(query);
            }

            @Override
            protected FactoryMachine doNewComponent(final SatisfiedBOM satisfiedBOM) {
                return new SingletonFactoryMachine<>(0, NamedComponent.of(
                        String.class, "test2", satisfiedBOM.getOne(query).get().getComponent() + " world"));
            }
        });

        Factory factory = Factory.builder()
                .addMachine(new SingletonFactoryMachine<>(0, NamedComponent.of(String.class, "mode", "dev")))
                .addMachine(alternativeMachine)
                .addMachine(dependentMachine)
                .build();

        Optional<NamedComponent<String>> component = factory.queryByName(Name.of(String.class, "test2")).optional().findOne();
        assertThat(component.isPresent()).isTrue();
        assertThat(component.get().getComponent()).isEqualTo("hello world");
    }

    private SingleNameFactoryMachine<FactoryMachine> alternativeMachine() {
        return new AlternativesFactoryMachine<>(0, Name.of(String.class, "mode"),
                ImmutableMap.of("dev", new SingletonFactoryMachine<>(-100, NamedComponent.of(
                        String.class, "test", "hello"))), BoundlessComponentBox.FACTORY);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void should_build_component_lists_from_multiple_machines() throws Exception {
        Factory factory = Factory.builder()
                .addMachine(new SingleNameFactoryMachine<>(
                        0, new NoDepsMachineEngine<String>(Name.of(String.class, "test 1"), BoundlessComponentBox.FACTORY) {
                    @Override
                    protected String doNewComponent(SatisfiedBOM satisfiedBOM) {
                        return "value 1";
                    }
                }))
                .addMachine(new SingleNameFactoryMachine<>(
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
    @SuppressWarnings("unchecked")
    public void should_respect_component_priorities() throws Exception {
        Factory factory = Factory.builder()
                .addMachine(new SingleNameFactoryMachine<>(
                        0, new NoDepsMachineEngine<String>(Name.of(String.class, "test 1"), 1, BoundlessComponentBox.FACTORY) {
                    @Override
                    protected String doNewComponent(SatisfiedBOM satisfiedBOM) {
                        return "value 1";
                    }
                }))
                .addMachine(new SingleNameFactoryMachine<>(
                        0, new NoDepsMachineEngine<String>(Name.of(String.class, "test 2"), 0, BoundlessComponentBox.FACTORY) {
                    @Override
                    protected String doNewComponent(SatisfiedBOM satisfiedBOM) {
                        return "value 2";
                    }
                }))
                .build();

        Set<NamedComponent<String>> components = factory.queryByClass(String.class).find();

        assertThat(components).containsExactly(
                NamedComponent.of(String.class, "test 2", "value 2"),
                NamedComponent.of(String.class, "test 1", "value 1"));
    }

    @Test
    public void should_factory_be_queryable() throws Exception {
        Factory factory = Factory.builder().build();

        assertThat(factory.queryByClass(Factory.class).findAsComponents()).containsExactly(factory);
        assertThat(factory.getComponent(Factory.class)).isEqualTo(factory);
    }

    @Test
    public void should_allow_to_close_with_factory_queried() throws Exception {
        // check that we don't get a stack overflow error due to box closing the factory
        Factory factory = Factory.builder().build();

        assertThat(factory.queryByClass(Factory.class).findAsComponents()).containsExactly(factory);

        factory.close();
    }

    @Test
    public void should_register_and_unregister() throws Exception {
        Factory factory = Factory.builder().build();
        assertThat(Factory.getFactory(factory.getId())).isEqualTo(Optional.absent());
        Factory.register(factory.getId(), factory);
        assertThat(Factory.getFactory(factory.getId())).isEqualTo(Optional.of(factory));
        Factory.unregister(factory.getId(), factory);
        assertThat(Factory.getFactory(factory.getId())).isEqualTo(Optional.absent());
    }

    @Test
    public void should_get_default_instance() throws Exception {
        Factory factory = Factory.getInstance();

        assertThat(factory).isNotNull().isSameAs(Factory.getInstance());
    }

    @Test
    public void should_use_local_machines() throws Exception {
        threadLocal().set("test", "myvalue");
        Factory factory = Factory.newInstance();

        assertThat(factory.getComponent(Name.of(String.class, "test"))).isEqualTo("myvalue");
        threadLocal().clear();
        factory = Factory.newInstance();
        assertThat(factory.queryByName(Name.of(String.class, "test")).optional().findOne().isPresent()).isFalse();
    }

    @Test
    public void should_deactivate_component() throws Exception {
        threadLocal().set("name1", new A("a1"));
        threadLocal().set("name2", new A("a2"));
        Factory factory = Factory.newInstance();
        assertThat(factory.getComponents(A.class)).extracting("a").containsExactly("a1", "a2");
        assertThat(factory.queryByName(Name.of(A.class, "name2")).findOne().isPresent()).isTrue();

        threadLocal().set(Factory.activationKey(A.class, "name2"), "false");
        factory = Factory.newInstance();
        assertThat(factory.getComponents(A.class)).extracting("a").containsExactly("a1");
        assertThat(factory.queryByName(Name.of(A.class, "name2")).findOne().isPresent()).isFalse();
    }

    @Test
    public void should_deactivate_component2() throws Exception {
        threadLocal().set("name1", new A("a1"));
        threadLocal().set("name2", new A("a2"));
        Factory factory = Factory.newInstance();
        assertThat(factory.queryByName(Name.of(Object.class, "name2")).findOne().isPresent()).isTrue();

        threadLocal().set(Factory.activationKey(A.class, "name2"), "false");
        factory = Factory.newInstance();
        assertThat(factory.queryByName(Name.of(Object.class, "name2")).findOne().isPresent()).isFalse();
    }

    @Test
    public void should_allow_to_deactivate_components_from_provided_warehouse() throws Exception {
        Factory factory = Factory.builder().addMachine(
                new SingletonFactoryMachine<>(0, NamedComponent.of(A.class, "a", new A("v1")))).build();

        Set<A> components = factory.getComponents(A.class);
        assertThat(components).hasSize(1).extracting("a").containsExactly("v1");

        Factory newFactory = Factory.builder().addWarehouseProvider(factory.getWarehouse())
                .addMachine(new SingletonFactoryMachine<>(0,
                        NamedComponent.of(String.class, Factory.activationKey(A.class, "a"), "false")))
                .addMachine(new SingletonFactoryMachine<>(0, NamedComponent.of(A.class, "b", new A("v2")))).build();

        components = newFactory.getComponents(A.class);
        assertThat(components).hasSize(1).extracting("a").containsExactly("v2");
    }

    @After
    public void teardown() {
        threadLocal().clear();
    }

    private SingleNameFactoryMachine<String> testMachine() {
        return testMachine("test");
    }

    private SingleNameFactoryMachine<String> testMachine(String name) {
        return new SingleNameFactoryMachine<>(
                0, new NoDepsMachineEngine<String>(Name.of(String.class, name), BoundlessComponentBox.FACTORY) {
            @Override
            protected String doNewComponent(SatisfiedBOM satisfiedBOM) {
                return "value1";
            }
        });
    }

    private SingleNameFactoryMachine<String> testMachine(String name, final String returnValue, int priority) {
        return new SingleNameFactoryMachine<>(
                priority, new NoDepsMachineEngine<String>(Name.of(String.class, name), priority, BoundlessComponentBox.FACTORY) {
            @Override
            protected String doNewComponent(SatisfiedBOM satisfiedBOM) {
                return returnValue;
            }
        });
    }

	private SingleNameFactoryMachine<GenericHolder<String>> testGenericStringMachine(String name) {
        return testGenericStringMachine(name, 0);
    }

    private SingleNameFactoryMachine<GenericHolder<String>> testGenericStringMachine(String name, int priority) {
        return new SingleNameFactoryMachine<>(
                priority, new NoDepsMachineEngine<GenericHolder<String>>(Name.<GenericHolder<String>>of(name, GenericHolder.class, String.class), priority, BoundlessComponentBox.FACTORY) {
            @Override
            protected GenericHolder<String> doNewComponent(SatisfiedBOM satisfiedBOM) {
                return new GenericHolder<>("foo");
            }
        });
    }

    private SingleNameFactoryMachine<GenericHolder<Integer>> testGenericIntegerMachine(String name) {
        return testGenericIntegerMachine(name, 0);
    }

    private SingleNameFactoryMachine<GenericHolder<Integer>> testGenericIntegerMachine(String name, int priority) {
        return new SingleNameFactoryMachine<>(
                priority, new NoDepsMachineEngine<GenericHolder<Integer>>(Name.<GenericHolder<Integer>>of(name, GenericHolder.class, Integer.class), BoundlessComponentBox.FACTORY) {
            @Override
            protected GenericHolder<Integer> doNewComponent(SatisfiedBOM satisfiedBOM) {
                return new GenericHolder<>(42);
            }
        });
    }

    static class GenericHolder<T> {
		final T value;

		GenericHolder(T value) {
			this.value = value;
		}

		public T getValue() {
			return value;
		}
	}

    private SingleNameFactoryMachine<String> machineWithMissingDependency() {
        return new SingleNameFactoryMachine<>(
                0, new StdMachineEngine<String>(Name.of(String.class, "test"), BoundlessComponentBox.FACTORY) {
            @Override
            public BillOfMaterials getBillOfMaterial() {
                return BillOfMaterials.of(Factory.Query.byName(Name.of(String.class, "missing")));
            }

            @Override
            protected String doNewComponent(SatisfiedBOM satisfiedBOM) {
                throw new RuntimeException("shouldn't be called");
            }
        });
    }

    private SingleNameFactoryMachine<String> overridingMachine() {
        return new SingleNameFactoryMachine<>(
                -10, new NoDepsMachineEngine<String>(Name.of(String.class, "test"), BoundlessComponentBox.FACTORY) {
            @Override
            protected String doNewComponent(SatisfiedBOM satisfiedBOM) {
                return "value1";
            }
        });
    }

    private static class LifecycleComponent implements AutoStartable, AutoCloseable, AutoPreparable {
        private boolean closed;
        private boolean started;
        private boolean prepared;

        @Override
        public void close() throws Exception {
            closed = true;
        }

        @Override
        public void start() {
            started = true;
        }

        @Override
        public void prepare() {
            prepared = true;
        }
    }

    private static class A {
        String a;

        private A(String a) {
            this.a = a;
        }

        public String getA() {
            return a;
        }
    }
}
