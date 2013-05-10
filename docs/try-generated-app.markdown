---
layout: docs
title:  "Try and understand generated app"
---
# Try and understand generated app

As soon as you have chosen to generate a sample resource when [generating your RESTX app](shell-app-bootstrap.html) and once you have properly [imported the app in your IDE](ide.html), you are ready to try out your app.

<div class="note">
	<p>These instructions only work if you answer YES to the question "generate hello resource example".</p>
	<p>Still you will find information relevant for any RESTX app.</p>
</div>

## Launch the app

Locate the `<your.main.package>.AppServer` class in the src/main/java directory, and run it from your IDE as a regular Java application.

You should see something like this in your IDE console:
{% highlight console %}
21:57:46.692 [main             ] INFO  org.eclipse.jetty.server.Server - jetty-8.1.8.v20121106
LoginService=HashLoginService[null] identityService=org.eclipse.jetty.security.DefaultIdentityService@4f961bac
21:57:47.260 [main             ] INFO  restx.RestxMainRouterFactory - 
--------------------------------------
 -- RESTX READY
 -- 2 filters
 -- 15 routes
 -- for admin console,
 --   VISIT http://localhost:8080/api/@/ui/
 --
{% endhighlight %}

## First contact: the admin web console


### Admin Console
As the console invite you to do, open [http://localhost:8080/api/@/ui/](http://localhost:8080/api/@/ui/) in your browser, you should see the RESTX admin console home:

![RESTX admin console home](/images/docs/admin-home.png)

### API DOCS
Then go to the API DOCS page, as you can see it's listing several resources already available in your app:

<div class="note">
	<p>The routes starting with `/@/` are used for the admin console.</p>
	<p>This is a reserved path in RESTX, as long as you want to benefit from the admin console which is optional. To get rid of it you just need to remove dependencies on all the admin modules.</p>
</div>

![RESTX API DOCS home](/images/docs/admin-apidocs-home.png)

Now select the `/message` resource, the generated documentation tells you that this resource takes a query parameter called who, of type String. Note also the example which gives you an example of request and corresponding response:

<div class="note">
	<p>If the /message route is missing, double check your IDE setup, and especially the annotation processing settings.</p>
</div>

![hello resource in RESTX API DOCS](/images/docs/admin-apidocs-hello.png)

### Trying out the `/message` resource
Now you can try it out, use the `Try it out` button at the top of the page, and you will be able to provide the desired value for the query parameter, and see the result:

![Try hello resource in RESTX API DOCS](/images/docs/admin-apidocs-hello-try.png)

For Linux/MacOS users you can also try it out from the command line using [curl](http://curl.haxx.se/) or [httpie](https://github.com/jkbr/httpie):

{% highlight console %}
$ curl "http://localhost:8080/api/message?who=restx"
{"message":"hello restx, it's 15:53:23"}%
$ http "http://localhost:8080/api/message?who=restx"
HTTP/1.1 200 OK
Cache-Control: no-cache
Content-Length: 40
Content-Type: application/json; charset=UTF-8
Server: Jetty(8.1.8.v20121106)

{
    "message": "hello restx, it's 15:53:52"
}
{% endhighlight %}

Since this is a GET request, you can also simply open the URL in your browser: [http://localhost:8080/api/message?who=restx](http://localhost:8080/api/message?who=restx)

### Monitoring

Now go to the [monitoring page](http://localhost:8080/api/@/ui/monitor/) in the console, you will get a page listing a set of elements which are monitored:

![RESTX admin monitor home](/images/docs/admin-monitor.png)

You have mainly 2 categories of elements monitored by default: 
- `<BUILD>` corresponds to instanciation of the components of your application. Most compoenents being instantiated at startup time, this is useful to track which components is responsible for slowing down your app startup time. The time you see in the `last` `min` and `max` columns are in ms, and corresponds to the component instanciation, **without** taking into account its dependencies instanciation (so that it's easier to track down components actually taking time to instanciate).
- `<HTTP>` corresponds to HTTP requests handled by RESTX. The time indicated in the `last` `min` and `max` columns are in ms, and corresponds to the time spent to handle the request **in RESTX**. This does not take into accound HTTP container time (servlet container or other container).

Using the search box in the upper left you can easily filter lines based on the content of their `Label` column, so if you want only `<HTTP>` elements you just need to type `HTTP`.

For instance you can search for `/message` and you will see performance information for the `/message` resource:

![RESTX admin monitor filtered on /message](/images/docs/admin-monitor-message.png)

## Launching tests

The generated app contains an integration test for the simple resource defined. You can launch it by opening the `<your.main.package>.rest.HelloResourceSpecTest` class as a junit test.

You should see something like that:

![RESTX junit spec test run](/images/docs/junit-run-hello.png)

<div class="note">
	<p>Obviously you can also run the tests from your build tool. Both Maven and EasyAnt are supported out of the box, setting up other build tools should be really easy, RESTX is pure Java and tests are regular JUnit tests.</p>
	<p>Open issues or submit pull requests if you want to get more build tools supported out of the box.</p>
</div>

## Understanding the app

Now it's time to have a look at the main components generated for you.

### LOCs

First let's have a look at the lines of code:
{% highlight console %}
$ cloc .
      10 text files.
      10 unique files.
       2 files ignored.

http://cloc.sourceforge.net v 1.55  T=0.5 s (16.0 files/s, 278.0 lines/s)
-------------------------------------------------------------------------------
Language                     files          blank        comment           code
-------------------------------------------------------------------------------
Java                             5             14              7             73
XML                              2              3              0             35
YAML                             1              0              0              7
-------------------------------------------------------------------------------
SUM:                             8             17              7            115
-------------------------------------------------------------------------------
{% endhighlight %}

As you can see the generated code is minimal, and shouldn't be too difficult to understand.

### Resource definition

The resource definition is done in the class `<your.main.package>.rest.HelloResource`:
{% highlight java %}
@Component @RestxResource
public class HelloResource {
    @GET("/message")
    public Message sayHello(String who) {
        return new Message().setMessage(String.format(
                "hello %s, it's %s",
                who, DateTime.now().toString("HH:mm:ss")));
    }
}
{% endhighlight %}

Let's decompose its content:

The `@Component` annotation declares this class as an injectable component, and the `@RestxResource` declares it as a resource.

<div class="note">
	<p>To get more information on RESTX dependency injection mechanism, check <a href="ref-factory.html">RESTX Factory reference documentation</a>.</p>
</div>

Then the sole method defines a resource endpoint thanks to its `@GET` annotation (similar annotations are available for other HTTP verbs). The parameter `"/message"` tells that this endpoint is mounted on /message relative to RESTX base path (`/api` in this case, this is defined in the `web.xml`).

The parameter `String who` defines a query parameter (a parameter that will be provided after the `?` in the URL).

The content of the method is called when a matching request is received, and constructs a `Message` object and returns it, using [joda time](http://joda-time.sourceforge.net/) to get current date / time.

You can set a breakpoint in this method and run your app in debug to see when this is called. You can also use the `open call hierarchy` action of your IDE to see the caller of the method. Here is the generated code:

{% highlight java %}
new StdEntityRoute("default#HelloResource#sayHello", mapper, new StdRouteMatcher("GET", "/message")) {
            @Override
            protected Optional<?> doRoute(RestxRequest request, RestxRouteMatch match) throws IOException {
                return Optional.of(resource.sayHello(
                        /* [QUERY] who */ checkPresent(request.getQueryParam("who"), "query param who is required")
                ));
            }
}
{% endhighlight %}

As you can see the code generated by annotation processing is not too complex, you could even write it manually if you would like.

<div class="note">
	<p>Writing routes manually is definitely possible, though most of the time using annotation processing is fine, it's good to know that you can always fall back to a more low level code. All you need to do for that is declare a component implementing the <a href="https://github.com/restx/restx/blob/master/restx-core/src/main/java/restx/RestxRoute.java?source=c">RestxRoute</a> interface.</p>
</div>

<div class="note">
	<p>To get more information on RESTX REST endpoint definitions, check <a href="ref-core.html">RESTX REST endpoints reference documentation</a>.</p>
</div>


### Domain class: Message

The `Message` class is part of the application domain (it's also called an entity): 

{% highlight java %}
public class Message {
    private String message;

    public String getMessage() {
        return message;
    }

    public Message setMessage(String message) {
        this.message = message;
        return this;
    }

    @Override
    public String toString() {
        return "Message{" +
                "message='" + message + '\'' +
                '}';
    }
}
{% endhighlight %}

This is a plain Java bean: the `toString` method is not mandatory, and using fluent setter (which returns `this`) is not mandatory either.

<div class="note">
	<p>Binding to JSON is done using the <a href="http://wiki.fasterxml.com/JacksonHome">jackson library</a>, check theirs docs to see how to configure JSON mapping.</p>
	<p>You can also use Bean Validation (JSR 303) / <a href="http://www.hibernate.org/subprojects/validator.html">Hibernate Validator</a> annotations to add validation to your beans.</p>
</div>

### Resource Spec

What is called a spec in RESTX is a yaml file describing a resource behaviour, or a set of behaviours chained in a scenario.

In the generated app, you can check the `should_say_hello.spec.yaml` file in `src/test/resources/specs/hello`:
{% highlight yaml %}
title: should say hello
given:
  - time: 2013-03-31T14:33:18.272+02:00
wts:
  - when: GET message?who=xavier
    then: |
      {"message":"hello xavier, it's 14:33:18"}
{% endhighlight %}

The notation follows BDD terminology `given` `when` `then` (`wts` stands for When ThenS).

In the given section the state of the system before the HTTP requests is described. In this case we only specify the time in ISO format.
Then a list of `when` `then` pairs follows, the `when` specify HTTP request, the `then` HTTP response.

This spec is used for 2 things:

- example in the API docs
- integration test


<div class="note">
	<p>Because RESTX app follows REST principles, the server has no conversation state. Therefore any HTTP request can be tested in isolation.</p>
	<p>The principle of scenario is there mainly to avoid repeating the `given` part too frequently, or also to be able to verify that the system state change after an HTTP request, for example issue a `GET` after a `PUT` to verify that the new resource representation has been stored.</p>
</div>

### Resource Spec Test

To actually be able to run this spec as a test, it is necessary to write a JUnit test to run it:
{% highlight java %}
public class HelloResourceSpecTest {
    @ClassRule
    public static RestxSpecRule rule = new RestxSpecRule(
            AppServer.WEB_INF_LOCATION,
            AppServer.WEB_APP_LOCATION);

    @Test
    public void should_say_hello() throws Exception {
        rule.runTest("specs/hello/should_say_hello.spec.yaml");
    }
}
{% endhighlight %}

As you can see this code is very basic thanks to junit rule power, it merely tells basic information to start the server and to run the spec.

### AppServer

The `AppServer` class is the class used to run the app as a standard Java app. 
{% highlight java %}
public class AppServer {
    public static final String WEB_INF_LOCATION = "src/main/webapp/WEB-INF/web.xml";
    public static final String WEB_APP_LOCATION = "src/main/webapp";

    public static void main(String[] args) throws Exception {
        int port = Integer.valueOf(Optional.fromNullable(System.getenv("PORT")).or("8080"));
        WebServer server = new JettyWebServer(WEB_INF_LOCATION, WEB_APP_LOCATION, port, "0.0.0.0");
        System.setProperty("restx.baseUri", server.baseUrl() + "/api");
        server.startAndAwait();
    }
}
{% endhighlight %}

All it does is launch an embedded server (Jetty in this particular case, but Tomcat and SimpleFramework server are also supported).

<div class="note">
	<p>If you prefer to run your JavaEE web container of choice separately and use standard deploy mechanism, no problem, the generated app is already configured to be packaged as a standard war.</p>
</div>

### AppModule

The `AppModule` class is defined like this:
{% highlight java %}
@Module
public class AppModule {
    @Provides
    public SignatureKey signatureKey() {
         return new SignatureKey("4f768f23-703e-4268-9e9e-51d2e052b6a1 4082747839477764571 MyApp myapp".getBytes(Charsets.UTF_8));
    }
}
{% endhighlight %}

This class is mandatory to provide a `SignatureKey` used to sign content sent to the clients. The string is used as salt, it can be any content, but make sure to keep it private.

The `@Module` annotation indicates that this class is used as a RESTX module, able to define a set of components.

The `@Provides` annotation on the `signatureKey` method is a way to define a component instanciation programmatically. This kind of method can take arbitrary parameters, injected by the RESTX factory.

<div class="note">
	<p>To get more information on RESTX dependency injection mechanism, check <a href="ref-factory.html">RESTX Factory reference documentation</a>.</p>
</div>

### logback.xml

This file is used to configure logging:

{% highlight xml %}
<configuration>
    <appender name="FILE" class="ch.qos.logback.core.FileAppender">
        <file>app.log</file>
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%-17thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%-17thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <logger name="org.eclipse.jetty.server.AbstractConnector" level="WARN" />
    <logger name="org.eclipse.jetty.server.handler.ContextHandler" level="WARN" />
    <logger name="org.hibernate.validator.internal.engine.ConfigurationImpl" level="WARN" />
    <logger name="restx.factory.Factory" level="WARN" />

    <root level="info">
        <appender-ref ref="FILE"/>
        <appender-ref ref="STDOUT"/>
    </root>
</configuration>
{% endhighlight %}

### web.xml

This file is the standard JavaEE web descriptor. It's used to configure the RESTX servlet:

{% highlight xml %}
<web-app xmlns="http://java.sun.com/xml/ns/javaee"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd"
      version="3.0" metadata-complete="true">

    <servlet>
        <servlet-name>restx</servlet-name>
        <servlet-class>restx.servlet.RestxMainRouterServlet</servlet-class>
        <load-on-startup>1</load-on-startup>
    </servlet>
    <servlet-mapping>
        <servlet-name>restx</servlet-name>
        <url-pattern>/api/*</url-pattern>
    </servlet-mapping>
</web-app>
{% endhighlight %}

<div class="note">
	<p>This file is not needed if you use SimpleFramework integration rather than JavaEE web container.</p>
</div>

### RESTX module descriptor

RESTX uses its own module descriptor format, which is build tool agnostic. The file is called `md.restx.json`:

{% highlight json %}
{
    "module": "myapp:myapp:0.1-SNAPSHOT",
    "packaging": "war",

    "properties": {
        "java.version": "1.7",
        "restx.version": "0.2.5-SNAPSHOT"
    },

    "dependencies": {
        "compile": [
            "io.restx:restx-core:${restx.version}",
            "io.restx:restx-core-annotation-processor:${restx.version}",
            "io.restx:restx-factory:${restx.version}",
            "io.restx:restx-factory-admin:${restx.version}",
            "io.restx:restx-monitor-admin:${restx.version}",
            "io.restx:restx-server-jetty:${restx.version}",
            "io.restx:restx-apidocs:${restx.version}",
            "io.restx:restx-specs-admin:${restx.version}",
            "ch.qos.logback:logback-classic:1.0.9"
        ],
        "test": [
            "io.restx:restx-specs-tests:${restx.version}",
            "junit:junit:4.11"
        ]
    }
}
{% endhighlight %}

This file is used at build time only, its the source used by RESTX to:

- generate Maven POM or Ivy files
- manage app dependencies ([soon, watch issue if interested](https://github.com/restx/restx/issues/5))

<div class="note">
	<p>It isn't used at runtime, you can get rid of it if you prefer to manage building, running and deploying your app on your own.</p>
</div>

The file format is pretty straightforward to understand if you are familiar with Maven, Ivy or similar build / dependency management tools.


