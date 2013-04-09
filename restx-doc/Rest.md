## Intro

Restx core is a lightweight, annotation based REST framework.

See the FAQ for details on the why and what.


## Code

### Define routes with @RestxResource

The simplest way to declare a restx resource is by using the `@RestxResource` annotation and the set of HTTP verbs annotations for routes:
```java
@Component @RestxResource
public class HelloResource {
    @GET("/message")
    public Message sayHello(String who) {
        return new Message().setMessage(String.format(
                "hello %s, it's %s",
                who, DateTime.now().toString("HH:mm:ss")));
    }
}
```

If you already use JAX-RS, you shouldn't be lost.

The main difference with JAX-RS is that annotation processing is done at compile time, avoiding the classpath scanning at startup without giving up the resources discoveries.

Another difference is that Restx leverages information available at compile time such as parameter names and generic types. So in the example above a route is defined on `/message` with a query parameter named who.

It also provide sensible defaults for parameter types:

- if a parameter is found in resource path, it will be a path parameter
- otherwise, if the request is a GET or DELETE, other params are considered query params
- if the request is a POST or PUT, other params are considered as body params

Some examples:
```java
    @GET("/message/{id}")
    public Message sayHello(String id, // path param
                            String who // query param
                            ) {
        return new Message().setMessage(String.format(
                "hello %s, it's %s",
                who, DateTime.now().toString("HH:mm:ss")));
    }
```

```java
    @POST("/message/{id}")
    public Message sayHello(String id, // path param
                            Message msg // body param
                            ) {
        return msg.setMessage(String.format(
                "%s @ %s",
                msg.getMessage(), DateTime.now().toString("HH:mm:ss")));
    }
```

You can always override these defaults with `@Param` annotation.

Body parameters and objects returned are automatically mapped to json using jackson, use regular jackson annotations if you want to custimize it.

Restx leverages the `Optional<T>` type from guava to indicate if a parameter is optional or if a request may not find a result.

Here is an example of optional query parameter:
```java
    @GET("/message/{id}")
    public Message sayHello(String id, // path param
                            Optional<String> who // optional query param
                            ) {
        return new Message().setMessage(String.format(
                "hello %s, it's %s",
                who, DateTime.now().toString("HH:mm:ss")));
    }
```

And an example of optional result:
```java
    @GET("/message/{id}")
    public Optional<Message> findMessageById(String id) {
        return Optional.fromNullable(messagesDS.find(id));
    }
```

In this case if you return `Optional.absent()` a 404 error code will be returned.


If you want to return a json array, simply return an `Iterable<T>`:
```java
    @GET("/messages")
    public Iterable<Message> findAllMessages() {
        return messagesDS.findAll();
    }
```

### Define routes using the `RestxRoute` interface (Advanced)

Though most of the time you will be able to implement your REST API with resources defined with the annotations from RESTX, sometimes you may want to get full power back:
- if you want to return something else than json
- if you want to some fancy request matching
- for any case we simply didn't think about

In this case you can use a low level API, think servlet API but simpler (and RESTX can be used in containers which doesn't support the servlet API, like simpleframework).

Here is an example:
```java
@Component
public class FactoryDumpRoute extends StdRoute {
    private final Factory factory;

    @Inject
    public FactoryDumpRoute(Factory factory) {
        super("FactoryRoute", new StdRouteMatcher("GET", "/@/factory"));
        this.factory = factory;
    }

    @Override
    public void handle(RestxRouteMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
        resp.setContentType("text/plain");
        resp.getWriter().println(factory.dump());
    }
}

```
This example uses the StdRoute class as basis, but all you need to do is a class implementing the RestxRoute interface, which basically defines 2 methods:
```java
    Optional<RestxRouteMatch> match(RestxRequest req);
    void handle(RestxRouteMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException;
```

## FAQ

### Why?

Yes we know there's already plenty of web frameworks, especially in the java world. But first RESTX is not a web framework, but a REST framework, and there aren't so much frameworks in Java targetted at REST API developement.

Second, it all started from a frustration about the startup time with the stack we used to use: Spring MVC. With tomcat 7 embed an empty application startup time was around 7 seconds (mostly due to servlet classpath scanning), the best we got was 2 seconds startup time with jetty 8 embed.

2 seconds may sound ok for plenty of people used to work with application servers starting up in 30 seconds or more, but once you get used to web development where you hit refresh and instaly get feedback on your changes, 2 seconds is already too much. Especially for an empty app without even a DB connection, once your app get bigger startup time will always increase.

Some may argu that startup time isn't much of problem with hot reload support in java (for basic changes only) and excellent tools like jrebel. But startup time still impact your integration tests (and we like integration testing of REST API). It also has impact on platform starting your app (or new nodes) on demand, like many cloud platforms.

The solution to startup time was to do more at compile time (because you only recompile what has changed), and thus we started a POC with annotation processing.

Moreover having our own REST framework opened up the place to implement some old ideas like integration tests recording, or leveraging the integration tests in the REST doc itself.

