---
filename: manual-app-bootstrap.md
layout: docs
title:  "Bootstrapping a project manually"
---
# Bootstrapping a project manually
RESTX being a java framework, you can use it without actually installing the shell: use your favorite build tool / dependency manager, and a dependency on the modules you want.

Though note that in that case you won't benefit from the power of the shell which eases a lot of use cases, especially this process of bootstrapping a project. If you have [installed the RESTX shell](install.html) we strongly recommend using it to [get started](getting-started.html).

But if you want to remain independent from the shell, stay in control of the framework, or simply because you want to introduce it in an existing app, this documentation is made for you.

Once you will have performed these steps, also have a look at [IDE setup documentation](ide.html), you may need to enable annotation processing manually depending on your IDE.

## Declaring dependencies 

The minimum in most cases if you want to use RESTX REST support is to declare a dependency on [restx-core](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22restx-core%22) and [restx-core-annotation-processor](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22restx-core-annotation-processor%22).

Here is an example with Ivy:
{% highlight xml %}
<dependency org="io.restx" name="restx-core" rev="{{ site.restx-version }}" />
<dependency org="io.restx" name="restx-core-annotation-processor" rev="{{ site.restx-version }}" />
{% endhighlight %}

And an example with Maven:
{% highlight xml %}
<dependency>
    <groupId>io.restx</groupId>
    <artifactId>restx-core</artifactId>
    <version>{{ site.restx-version }}</version>
</dependency>
<dependency>
    <groupId>io.restx</groupId>
    <artifactId>restx-core-annotation-processor</artifactId>
    <version>{{ site.restx-version }}</version>
</dependency>
{% endhighlight %}

You get the idea, do this with your favorite build tool.

## web.xml configuration

If you want to use RESTX inside a JavaEE / servlet container, you will have to:

- declare a dependency on restx-servlet:
{% highlight xml %}
<dependency org="io.restx" name="restx-servlet" rev="{{ site.restx-version }}" />
{% endhighlight %}

- add this snippet to your web.xml:
{% highlight xml %}
<servlet>
    <servlet-name>restx</servlet-name>
    <servlet-class>restx.servlet.RestxMainRouterServlet</servlet-class>
    <load-on-startup>1</load-on-startup>
</servlet>
<servlet-mapping>
    <servlet-name>restx</servlet-name>
    <url-pattern>/api/*</url-pattern>
</servlet-mapping>
{% endhighlight %}

Note that you can change the servlet path to the one of your choice, mounting RESTX on `/api/*` is only what we use most of the time.

## configure logs

RESTX relies on SLF4J for logging, we recommend [logback](http://logback.qos.ch/) for the implementation.

All you need to do is:

- declare a dependency on logback-classic:
{% highlight xml %}
<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
    <version>1.0.9</version>
</dependency>
{% endhighlight %}

- define a logback.xml at the root of your classpath resources:
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

<div class="go-next">
	<ul>
		<li><a href="ide.html"><i class="icon-cog"> </i> Setup your IDE</a></li>
		<li><a href="getting-started.html"><i class="icon-play"> </i> Getting started</a></li>
		<li><a href="/community/"><i class="icon-beer"> </i> Community</a></li>
		<li><a href="/docs/"><i class="icon-book"> </i> Documentation</a></li>
	</ul>	
</div>
