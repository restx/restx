---
layout: docs
title:  "Features"
filename: features.md
---
# Features

## focused on REST

   Building a RESTful API is dead easy with RESTX, it has all the defaults you can expect from a REST framework: serving / consuming JSON is super easy, query and path parameters are almost too simple to use, you get a strong routing and filtering mechnism and no conversation state is kept on the web server (aka stateless web server). 

   This implies that it's not a general web framework, i.e., there is no templating mechanism. It's better use with a JavaScript framework on the client side, and you can always use any other templating mechanism to serve your pages.

## lightweight

  RESTX is based on a "no reflection" architecture, instead it uses annotation processing and compile-time source generation. Thanks to this approach we eliminate complex call stacks and the many Proxy / Reflection calls, which makes it very lightweight in terms of runtime overhead compared to programming directly against the servlet API. There's also (almost) no classpath scanning (which leads to a faster startup time).

  Another reason for choosing annotation processing over reflection is that even with some "magic" you can always view the generated source, you can clearly see in your IDE the caller of your API methods.

## modular and pluggable

  RESTX is very modular, you pick up only what you want. Moreover, it has a very simple plugin architecture, which makes it easy to use existing plugins as well as writing your own that you can contribute to the community!

## blazing fast

  Everything is compiled, we always keep performance into account during the design, especially startup time.

  However, the framework is not async-oriented. This is a design choice: most data access APIs are synchronous anyway (JDBC, Mongo java driver, ...) and it's easier to program, especially for Java developers.

  Async support is planned:

   - through web socket with fallback support (probably with [Atmosphere](http://async-io.org/))
   - through [RXJava](https://github.com/Netflix/RxJava) support (not a priority)

If you'd prefer a truly async-oriented framework, have a look at [vert.x](http://vertx.io), [Play2](http://www.playframework.com/) or [NodeJS](http://nodejs.org).

## amazing integration with MongoDB...

   relying on Jackson and Jongo gives true symmetry between client and datastore payloads, plus some very cool features like data recording for easy integration testing 

## ... but not only

   restx is not specific to MongoDB, you can use it with any datastore with a Java API. There's already a [sample project using couchbase](https://github.com/restx/restx-samples-beersample), and you can also use it with a relational database.

## easy testing

   a human readable declarative end to end tests support with a recording feature easing non regression tests

## REST API documentation

   as soon as you declare an endpoint, it is documented in the web console

## ... with innovation inside

   the declarative tests are also used as a source of documentation, giving always working examples!

## admin web console

   monitoring, DI container exploration, API documentation, recording console, ...

## enterprise friendly

   pure Java, can be deployed as a simple servlet, you can even use it alongside another framework, good maven support, relationnal DB support (soon), integration with spring (soon)

## ... but not only

   strong support for embedded/main launch, starts up in less than 0.5s, a very simple build tool agnostic module declaration with support for [easyant](http://ant.apache.org/easyant/), very easy to integrate with NoSQL database

## type safe DI

   features a small dependency injection engine, based on annotation processing and code generation

## strong integration with google guava and joda time

   because we can't live without them

## cloud friendly (soon)

   ready to deploy on many cloud offerings (Cloudbees, heroku, ...)

## easy to setup

   a command line helps to create new apps, run and deploy them on the cloud
	 
<div class="go-next">
<ul>
	<li><a href="install.html"><i class="icon-cloud-download"> </i> Install</a></li>
	<li><a href="getting-started.html"><i class="icon-play"> </i> Getting started</a></li>
	<li><a href="/community/"><i class="icon-beer"> </i> Community</a></li>
	<li><a href="/docs/"><i class="icon-book"> </i> Documentation</a></li>
</ul>	
</div>
	 
