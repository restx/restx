# REST.X - java rest framework


## What is rest.x?

rest.x is an opinionated Java rest framework:
- focused on REST, not a general web framework
   it means no templating mechanism, use it with your favorite JS framework!

- no reflection
   we prefer annotation processing, so that even with some magic you can always view the source.

- good performance
   everything is compiled, we always keep performance into account during the design, especially startup time.
   Still the framework is not async oriented, this is a design choice: most data access API are synchronous
   anyway (JDBC, Mongo java driver, ...) and it's easier to program, especially for Java developers.
   Async support is planned:
     - through web socket with fallback support (probably with Atmosphere)
     - through JavaRX support (not a priority)
   If what you want is a truly async oriented framework, have a look at vert.x or Play2

- amazing integration with MongoDB
   although not required, relying on Jackson and Jongo gives true symmetry between client and datastore payloads

- easy testing
   a human readable declarative end 2 end tests support with a recording feature easing non regression tests

- REST API documentation
   leveraging a customized version of swagger, as soon as you declare a end point it is documented

- ... with innovation inside (soon)
   the declarative tests are also used as a source of documentation, giving always working examples!

- admin web console
   basic monitoring, DI container exploration, API documentation, recording console, ...

- enterprise friendly
   pure Java, can be deployed as a simple servlet, you can even use it alongside another framework, good maven support,
   relationnal DB support (soon)

- ... but not only
   strong support for embedded launch, starts up in less than 0.5s (depends mostly on DB connection and class loading,
   by itself a restx server can start in less than 100ms)

- type safe DI
   features a small dependency injection engine, based on annotation processing and code generation

- strong integration with google guava and joda time
   because we can't live without them

- cloud friendly (soon)
   ready to deploy on many cloud offerings (Cloudbees, heroku, ...)

- easy to setup (soon)
   a command line helps to create new apps, run and deploy them on the cloud

- opinionated
   focused on JSON REST API, though being restful can be discussed: there is no help to support links between resources
   (so there is no strong support for discoverability), and we support only JSON UTF-8 representations (yes, you read
   correctly, no XML, ATOM, you name it)

## Show me some code

```java
@Component @RestxResource(group = "geo")
public class CityResource {
    private final JongoCollection cities;

    public CityResource(@Named("cities") JongoCollection cities) {
        this.cities = cities;
    }

    @GET("/cities/{key}")
    public Optional<City> findCityByKey(String key) {
        return Optional.fromNullable(cities.get().findOne(new ObjectId(key)).as(City.class));
    }

    @GET("/cities")
    public Iterable<City> findCitiesByZipCode(String zipCode) {
        return cities.get().find("{zipCode: #}", startingWith(zipCode)).as(City.class);
    }
}
```


