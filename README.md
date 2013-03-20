# REST.X - java rest framework


## What is rest.x?

rest.x is an opinionated Java rest framework:
- focused on REST, not a general web framework
   it means no templating mechnism, use it with your favorite JS framework!
- no reflection
   we prefer annotation processing, so that even with some magic you can always view the source
- great integration with MongoDB
   although not required, relying on Jackson and Jongo gives true symetry between client datastore payloads
- easy testing
   a human readable declarative end 2 end tests support with a recording feature easing non regression tests
- REST API documentation
   leveraging a customized version of swagger, as soon as you declare a end point it is documented
- ... with innovation inside
   the declarative tests are also used as a source of documentation, giving always working examples!
- admin web console
   basic monitoring, DI container exploration, API documentation, ...
- enterprise friendly
   pure Java, can be deployed as a simple servlet, you can even use it alongside another framework
- ... but not only
   strong support for embedded launch, usually starts up in less than 1s
- type safe DI
   features a small dependency injection engine, based on annotation processing and code generation
- strong integration with google guava and joda time
   because we can't live without them
- cloud friendly (soon)
   ready to deploy on many cloud offerings (Cloudbees, heroku, ...)
- easy to setup (soon)
   a comand line helps to create new apps, run and deploy them on cloud offering


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


