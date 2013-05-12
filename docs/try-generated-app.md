---
filename: try-generated-app.md
layout: docs
title:  "Try generated app"
---
# Try generated app

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

Now that you have tried the generated app, it's time to [understand its sources](generated-app-explained.html).

<div class="go-next">
	<ul>
		<li><a href="generated-app-explained.html"><i class="icon-cogs"> </i> Understand generated app</a></li>
		<li><a href="/community/"><i class="icon-beer"> </i> Community</a></li>
		<li><a href="/docs/"><i class="icon-book"> </i> Documentation</a></li>
	</ul>	
</div>
