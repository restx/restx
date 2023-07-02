# RESTX - the lightweight Java REST framework

[![Build Status](https://github.com/restx/restx/actions/workflows/restx-ci.yml/badge.svg?branch=master)](https://github.com/restx/restx/actions/workflows/restx-ci.yml)

RESTX is a full lightweight disrupting stack, which includes Swagger-like ui & considers REST specs tests as docs.

It shares similarities with modern frameworks like Play! like hot compile and a very productive experience, but focused on REST and pure Java.

It's licensed under the very commercial friendly Apache License 2, and is actively maintained by a community of developers.

You can get more details from the web site at http://restx.io/

Here you will find the build instructions if you want to build RESTX yourself, and why not contribute to the project.

## Build

RESTX requires Java 17.

You can build it using either Maven Wrapper.

With Maven:

`./mvnw install`

## Project organisation

RESTX is decomposed in a set of modules, each one following the traditional Java project layout (main sources in `src/main`, test sources in `src/test`).

The main modules are `restx-core` and `restx-factory`.

Here is a brief summary of each module:

### Main modules:

- `restx-common`: Some shared utilities, only few ones, relying on Guava we already get a lot of nice utilities from there.
- `restx-factory`: RESTX Dependency Injection (DI) container. Brought as transitive dependency from restx-core.
- `restx-classloader`: Hot reload / hot compile support
- `restx-apidocs-doclet`: Some javadoc doclets used when generating apidocs
- `restx-core`: Core module, includes the REST framework, base security, JSON support, ...

*By relying on `restx-core` module, every modules described above will be retrieved as transitive dependencies*

- `restx-core-annotation-processor`: Annotation processing to generate routers based on RESTX core annotations. Needed at compile/build time only.


### Optional dependencies providing specific features

- `restx-i18n`: I18n Support
- `restx-security-basic`: A basic implementation of security, still enough in many cases but you can remove it at will
- `restx-specs-tests`: Enables using RESTX specs as JUnit tests.
- `restx-specs-server`: Enables using RESTX specs as HTTP mocks (running a server serving spec files responses given spec files requests).
- `restx-factory-testing`: A module dedicated to test `restx-factory` features involving annotation processing.
- `restx-validation`: Bean validation support (based on `hibernate-validator` implementation) for POJOs BODY parameters
- `restx-webjars`: Webjars support through urls like `/@/webjars/*`


#### MongoDB support through Jongo API:

- `restx-jongo`: Main MongoDB support through Jongo API.
- `restx-jongo-specs-tests`: Support of Jongo in your specs (recording and running).


#### Admin console modules (these are not required modules and are intended only for administration/monitoring) :

- `restx-admin`: The pluggable RESTX admin web console.
- `restx-apidocs`: The famous API Docs web console, as a plugin for `restx-admin`.
- `restx-monitor-admin`: Poor's man app monitoring web console, plugin for `restx-admin`.
- `restx-factory-admin`: RESTX Factory admin console, plugin for `restx-admin`.
- `restx-log-admin`: Easy configuration of logback logging from admin console, plugin for `restx-admin`.
- `restx-specs-admin`: RESTX Specs recording and running web console, plugin for `restx-admin`.
- `restx-i18n-admin`: Easy setting of your i18n translations from the web console, plugin for `restx-admin`.
- `restx-stats-admin`: Used to collect stats as explained [here](http://restx.io/stats.html)


### Servers support:

- `restx-servlet`: Servlet 5+ adapter for RESTX, allowing to embed RESTX in any servlet 5+ container.
- `restx-server-jetty11`: Embedded Jetty 11 support.
- `restx-server-tomcat`: Embedded Tomcat support.
- `restx-server-simple`: SimpleFramework adapter for RESTX, this is the lightest and fastest solution.
- `restx-server-testing`: JUnit tests for all the supported embedded servers.

`restx-server-*` are needed only if you want to be able to run restx as a standalone app rather than deploying it
If you want to deploy in a web server, you will ` restx-servlet `


### Metrics:

- `restx-monitor-codahale`: Codahale metrics for monitor module. Not compatible with Google App Engine.


### Others:

- `restx-samplest`: both a sample of individual features and JUnit tests of them
- `restx-samplest-kotlin`: Same as `restx-samplest` but for demo-ing restx with kotlin support
- `restx-annotation-processors-package`: assembly module for annotation processor only, if you prefer to setup annotation processing manually with `-proc` javac option
- `restx-barbarywatch`: MacOSX filesystem watching that actually works. Only module with GPL license, but no other module depend on it, it's detected at runtime, and used only during development


## Contributing

Contributions are welcome, fork the repo, push your changes to a branch and send a Pull Request.

To be sure the PR will be merged please discuss it on the google group before, or create an issue on GitHub to initiate the discussion.

