## Intro

Restx factory is a super fast components factory / dependency injection container, based on annotation processing and java 6 service loader.

See the FAQ for details on the why and what.

See the vocabulary sections for general concepts and vocabulary used within restx factory.

## Code

### Declaring a component using `@Component`

The simplest way to declare a component is by using the `@Component` annotation:
```java
@Component
public class MyComponent {
    public MyComponent(MyDependency dependency) {
       ...
    }
}
```

This will generate the corresponding machine and machine engine and declare it to the factory.

### Declaring a component using `@Provides`

If constructor injection is not enough, you can use the module and its provider methods feature:

```java
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
```

In this case the methods annotated with `@Provides` are used to build components of their returned types. You can annotate them with `@Named` to indicate the name of the component produced.
The provider methods can take arbitrary parameters, which are satisfied the same way as constructor parameters are injected (so you can use @Named to indicate their names).


### Getting a Factory

Here is a simple way to build a Factory:
```java
Factory factory = Factory.builder()
                .addFromServiceLoader().build();
```

This factory will load the machines declared in the `META-INF/services/restx.factory.FactoryMachine` files, using the standard ServiceLoader mechanism. But don't worry, you don't have to write these files manually, restx factory annotation processing can do it for you!

### Getting a component from the factory

Getting one component by type (note the use of `Optional` from guava, to indicate it may not find anything):
```java
Optional<NamedComponent<MyComponent>> component =
            factory.queryByClass(MyComponent.class).findOne();
```
This returns an optional named component (meaning that you also get its name). You can easily get the component itself:
```java
MyComponent component = factory.queryByClass(MyComponent.class)
                                    .findOne().get().getComponent();
```

Getting all components of a particular type:
```java
Set<MyComponent> components = factory.queryByClass(MyComponent.class).findAsComponents();
```

Getting one component by name:
```java
Optional<NamedComponent<MyComponent>> component =
            factory.queryByName(Name.of(MyComponent.class, "MyComponent")).findOne();
```

### Defining a component customizer

To customize components produced in the factory before they are stored in the warehouse and returned to queries, you just need to provide a component implementing `ComponentCustomizerEngine` or subclassing/using one of its implementation classes.

Here is an example:
```java
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
```

### Overriding a component produced by another machine

Overriding other machines is easy: all you need to is define your component with the same `restx.factory.Name`, in a machine with more priority.

Machines being ordered by order of priority in ascending order, lower priorities override higher priorities.
The priority by default is 0, so usually you override with a negative priority.

Beware that a `Name` is composed of the name as a string, and a class. So to override a component you need to produce it as a NamedComponent with the exact same `Name`.

### Declaring a machine using `@Machine` (Advanced)

You can declare a whole machine using the `@Machine` annotation. You shouldn't use it too often since it's more complex than other options, but it opens a lot of possibilities:

```java
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
```

In the end the only thing that this annotation does is declare the machine in the `META-INF/restx.factory.FactoryMachine` service loader file.

## Vocabulary

Restx factory borrows its vocabulary from factory and warehousing industry.

### Factory

The factory is the main unit of component production. You usually have only one factory. It is composed of a list of machines and one warehouse.

### Factory Machine

A machine is a core unit of production inside the factory. It can be used to produce different kind of components, generally sharing the same functional concepts. It is itself composed of machine engines.

### Machine Engine

A machine engine is the smallest production unit. It is used to produce a single kinf of components. This is the unit responsible to describe the Bill Of Materials (BOM) to build a component, and also responsible for building component boxes once the BOM is satisfied.

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

Despite you can use the factory without annotation processing, it shines when the the injection code is generated by the provided annotation processor. Having generated code for that means that you can use your IDE to view the callers of your constructor, and better understand how the dependencies are satisfied.

#### Dry run

Each component dependencies are expressed in what is called a Bill Of Material (BOM). It means that the container can analyze all the BOMs without actually instantiating the components. It means that you have very fast failure at startup in case of missing dependencies, and that we can isolate component instantiation times so that you know which component is wasting your startup time.

#### Container queries

The container can be queried for components by class or by names. More query types could easily be added. These queries are also what is used to express the dependencies.

#### Component Customization

You can easily register component customizers which can be used to customize components produced by the factory.
This allow contributions to or transformation of components, which is very useful for decoupling and plugins.
