---
filename: ref-factory.md
layout: docs
title:  "RESTX Factory"
---
# RESTX Factory

## Intro

Restx factory is a super fast components factory / dependency injection container, based on annotation processing and Java 6 service loader.

See the FAQ for details on the why and what.

See the vocabulary sections for general concepts and vocabulary used within restx factory.

## Code

### Declaring a component using `@Component`

The simplest way to declare a component is by using the `@Component` annotation:
{% highlight java %}
@Component
public class MyComponent {
    public MyComponent(MyDependency dependency) {
       ...
    }
}
{% endhighlight %}

This will generate the corresponding machine and machine engine and declare it to the factory.

### Declaring a component using `@Provides`

If constructor injection is not enough, you can use the module and its provider methods feature:

{% highlight java %}
@Module
public class MyAppModule {
    @Provides
    public RestxSession.Definition.Entry usersSessionEntry(@Named("users") final JongoCollection users) {
        return new RestxSession.Definition.Entry(
                    User.class,
                    RestxPrincipal.SESSION_DEF_KEY,
                    new CacheLoader<String, User>() {
            @Override
            public User load(String key) throws Exception {
                return users.get().findOne("{login: #}", key).as(User.class);
            }
        });
    }
    @Provides @Named(JongoFactory.JONGO_DB_NAME)
    public String jongoDbName() {
        return System.getProperty("mongo.db", "myappdb");
    }
}
{% endhighlight %}

In this case the methods annotated with `@Provides` are used to build components of their returned types. You can annotate them with `@Named` to indicate the name of the component produced.
The provider methods can take arbitrary parameters, which are satisfied the same way as constructor parameters are injected (so you can use @Named to indicate their names).


### Getting a Factory

Here is a simple way to build a Factory:
{% highlight java %}
Factory factory = Factory.builder()
                .addFromServiceLoader().build();
{% endhighlight %}

This factory will load the machines declared in the `META-INF/services/restx.factory.FactoryMachine` files, using the standard ServiceLoader mechanism. But don't worry, you don't have to write these files manually, restx factory annotation processing can do it for you!

### Getting a component from the factory

Getting one component by type (note the use of `Optional` from guava, to indicate it may not find anything):
{% highlight java %}
Optional<NamedComponent<MyComponent>> component =
            factory.queryByClass(MyComponent.class).findOne();
{% endhighlight %}
This returns an optional named component (meaning that you also get its name). You can easily get the component itself:
{% highlight java %}
MyComponent component = factory.queryByClass(MyComponent.class)
                                    .findOne().get().getComponent();
{% endhighlight %}

Getting all components of a particular type:
{% highlight java %}
Set<MyComponent> components = factory.queryByClass(MyComponent.class).findAsComponents();
{% endhighlight %}

Getting one component by name:
{% highlight java %}
Optional<NamedComponent<MyComponent>> component =
            factory.queryByName(Name.of(MyComponent.class, "MyComponent")).findOne();
{% endhighlight %}

### Defining a component alternative

Sometimes you want to define an alternative to some component that should replace the default component only under some circonstances. For instance if you want a specific component in dev mode, or have different implementations of a service which you would like to select at runtime only depending on your environment.

To do so restx factory uses a concept of alternative, which can be activated depending on the value of a string component in the factory.

Here is an example:
{% highlight java %}
@Alternative(to = RestxSpecRepository.class)
@When(name="restx.mode", value="dev")
public class DevRestxSpecRepository extends RestxSpecRepository {
    @Override
    synchronized ImmutableMap<String, RestxSpec> findAllSpecs() {
        return ImmutableMap.copyOf(buildSpecsMap(true));
    }
}
{% endhighlight %}

In this case the component `DevRestxSpecRepository` is an alternative to `RestxSpecRepository`, which should be activated only when a String component named `restx.mode` has the value `dev`.

### Defining a component alternative Tip

If activating a component alternative based ona string component doesn't seem enough for you, first consider using the `@Provides` mechanism to put a string component in the factory depending on more complex elements, and then using this string component to activate your alternative.

For instance, if you want to activate an alternative based on the content of a file in your filesystem, you could do something like this:
{% highlight java %}
@Module
public class MyAppModule {
    @Provides @Named("env.name")
    public String envName() {
        return Files.toString(new File("env.txt"), Charsets.UTF_8);
    }
}
{% endhighlight %}

And then define your alternative like this:
{% highlight java %}
@Alternative(to = MyComponent.class)
@When(name="env.name", value="bordeaux")
public class MyBordeauxComponent extends MyComponent {
    @Override
    String hello() {
        return "Hello Bordeaux";
    }
}
{% endhighlight %}

### Defining a component customizer

To customize components produced in the factory before they are stored in the warehouse and returned to queries, you just need to provide a component implementing `ComponentCustomizerEngine` or subclassing/using one of its implementation classes.

Here is an example:
{% highlight java %}
@Component
public class FrontObjectMapperCustomizer extends SingleComponentNameCustomizerEngine<ObjectMapper> {
    public FrontObjectMapperCustomizer() {
        super(0, FrontObjectMapperFactory.NAME);
    }
    @Override
    public NamedComponent<ObjectMapper> customize(NamedComponent<ObjectMapper> namedComponent) {
        namedComponent.getComponent().setPropertyNamingStrategy(PASCAL_CASE_TO_CAMEL_CASE);
        return namedComponent;
    }
}
{% endhighlight %}

### Overriding a component produced by another machine

Overriding other machines is easy: all you need to is define your component with the same `restx.factory.Name`, in a machine with more priority.

Machines being ordered by order of priority in ascending order, lower priorities override higher priorities.
The priority by default is 0, so usually you override with a negative priority.

Beware that a `Name` is composed of the name as a string, and a class. So to override a component you need to produce it as a NamedComponent with the exact same `Name`.

### Declaring a machine using `@Machine` (Advanced)

You can declare a whole machine using the `@Machine` annotation. You shouldn't use it too often since it's more complex than other options, but it opens a lot of possibilities:

{% highlight java %}
// machine to generate a single named String, don't do that, use @Provides instead
@Machine
public class MyAppModuleFactoryMachine extends DefaultFactoryMachine {
    public MyAppModuleFactoryMachine() {
        super(0,
                new NoDepsMachineEngine<String>(Name.of(String.class, "mongo.db"), BoundlessComponentBox.FACTORY) {
                    @Override public String doNewComponent(SatisfiedBOM satisfiedBOM) {
                        return System.getProperty("mongo.db", "myappdb");
                    }
                }
             );
    }
}
{% endhighlight %}

In the end the only thing that this annotation does is declare the machine in the `META-INF/restx.factory.FactoryMachine` service loader file.

### Defining a component alternative programmatically (advanced)

If you hit a wall with the simplistic approach to alternatives restx factory provides, it may be interesting to know how restx implement alternatives. The concept behind alternatives in restx is actually more powerful: restx factory supports factory of factory. So you can define a factory which builds other factories, then these factories will be available to build other components. An because when building other factory you can query the factory to find other components, you can take decisions based on the state of the factory.

One thing to remember when you do that is that restx will evaluate the conditions at factory load time (ie startup time in production).

As an example, here is the code generated when you use the `@Alternative` annotation:
{% highlight java %}
@Machine
public class DevRestxSpecRepositoryFactoryMachine extends SingleNameFactoryMachine<FactoryMachine> {
    public static final Name<RestxSpecRepository> NAME = Name.of(RestxSpecRepository.class, "RestxSpecRepository");
    public DevRestxSpecRepositoryFactoryMachine() {
        super(0, new StdMachineEngine<FactoryMachine>(
                    Name.of(FactoryMachine.class, "DevRestxSpecRepositoryRestxSpecRepositoryAlternative"), BoundlessComponentBox.FACTORY) {
                private Factory.Query<String> query = Factory.Query.byName(Name.of(String.class, "restx.mode")).optional();
                @Override
                protected FactoryMachine doNewComponent(SatisfiedBOM satisfiedBOM) {
                    if (satisfiedBOM.getOne(query).isPresent()
                            && satisfiedBOM.getOne(query).get().getComponent().equals("dev")) {
                        return new SingleNameFactoryMachine<RestxSpecRepository>(-1000,
                                        new StdMachineEngine<RestxSpecRepository>(NAME, BoundlessComponentBox.FACTORY) {
                                            @Override
                                            public BillOfMaterials getBillOfMaterial() {
                                                return new BillOfMaterials(ImmutableSet.<Factory.Query<?>>of());
                                            }
                                            @Override
                                            protected RestxSpecRepository doNewComponent(SatisfiedBOM satisfiedBOM) {
                                                return new DevRestxSpecRepository();
                                            }
                                        });
                    } else {
                        return NoopFactoryMachine.INSTANCE;
                    }
                }
                @Override
                public BillOfMaterials getBillOfMaterial() {
                    return BillOfMaterials.of(query);
                }
            });
    }
}
{% endhighlight %}

## Vocabulary

Restx factory borrows its vocabulary from factory and warehousing industry.

### Factory

The factory is the main unit of component production. You usually have only one factory. It is composed of a list of machines and one warehouse.

### Factory Machine

A machine is a core unit of production inside the factory. It can be used to produce different kind of components, generally sharing the same functional concepts. It is itself composed of machine engines.

### Machine Engine

A machine engine is the smallest production unit. It is used to produce a single kind of components. This is the unit responsible to describe the Bill Of Materials (BOM) to build a component, and also responsible for building component boxes once the BOM is satisfied.

### Component Box

A machine engine does not produce component directly, but it produces boxes. These boxes are put in the warehouse by the factory. A box can contain a limited number of components, or be boundless (which corresponds to a singleton scope).

### Component Customizer Engine and Component Customizer

A component customizer engine can be used to create customizers for particular components produced in the factory.
The customization is applied whenever a component is produced by the machine engine, before the component box is stored in the warehouse.


### Warehouse

The warehouse is where the component boxes are stored once they have been produced by an engine. The factory always check the warehouse first when you query for a component.
The warehouse can be queried for components, if you just want to get a component without creating it if it's not already built.


## FAQ

### Why yet another dependency injection engine?

We wanted a compile time oriented dependency injection engine, mostly to get blazing fast engine startup.
A hundred ms for the engine startup itself is already too slow in our metrics.

We tried dagger from the amazing guys from Square, but we were stuck with limitations, especially the lack of querying mecanisms in the container (dagger needed to know all the dependency injection roots at the time we investigated).

Therefore, and because writing a small DI engine is no rocket science, we decided to write our own.

### What features?

#### Injection types

restx factory supports only 2 types of injections:

  - constructor injection
  - provider method parameter injection (see `@Provides`)

Yes, you read it correctly, no setter injection, no field injection.

The advantage of constructor injection is that all depedencies are clearly expressed, and you can make your objects immutable, and doesn't require reflection to access the fields.

Besides injection, restx factory supports component customization, allowing you to customize a component after it is created and before it is delivered to other components / callers.

#### Annotation processor based

Despite you can use the factory without annotation processing, it shines when the injection code is generated by the provided annotation processor. Having generated code for that means that you can use your IDE to view the callers of your constructor, and better understand how the dependencies are satisfied.

#### Dry run

Each component dependencies are expressed in what is called a Bill Of Material (BOM). It means that the container can analyze all the BOMs without actually instantiating the components. It means that you have very fast failure at startup in case of missing dependencies, and that we can isolate component instantiation times so that you know which component is wasting your startup time.

#### Container queries

The container can be queried for components by class or by names. More query types could easily be added. These queries are also what is used to express the dependencies.

#### Component Customization

You can easily register component customizers which can be used to customize components produced by the factory.
This allow contributions to or transformation of components, which is very useful for decoupling and plugins.

#### Alternatives

You can easily declare components which are made available to the factory only under some conditions.

#### Factory of Factory

You can define a factory machine which in turns builds other factory machines. This is the mechanism used under the hood to provide the alternatives support. 
