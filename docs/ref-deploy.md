---
filename: ref-deploy.md
layout: docs
title:  "Deploying a RESTX app"
---
# Deploying a RESTX app

There are actually many different ways to deploy a restx application, you can choose the one that best fits your needs:

## As a war in any servlet 2.5+ container 

by default when you generate an app with restx or if you [create one manually following the documentation](/docs/manual-app-bootstrap.html), your app will be ready to package as a war and deploy in any servlet 2.5+ container like tomcat 6+, or PaaS solutions supporting war formats like [Cloudbees](http://www.cloudbees.com/).

To package the app as a war you can use your build tool of choice, restx comes with support for Maven and EasyAnt so you can package your app with maven package for instance.

## Running as a regular Java app

by default when you generate an app with restx it also generates a Main class which is a simple launcher allowing to launch your app as a regular Java application with the java executable from the JRE. Launching as a regular java app can be interesting if you don't want a full blown web container, or in contexts where a main is requested, eg some PaaS solutions like [Heroku](https://www.heroku.com/).

This main actually launches an embedded web container, meaning that the web container will be run inside your app. 

### embedded servers options

Restx comes with 3 flavours of this option:

- jetty embedded
- tomcat embedded
- simple framework

by default your Main will use jetty embedded, but it's easy to switch. The choice between them may be due to performance, memory footprint, container features, or anything else. Adding support for other web connector/container is pretty easy, check for example how [simpleframework support](https://github.com/restx/restx/tree/master/restx-server-simple) is implemented.

### running the app

To run this java app, you can either use regular java tools, and for instance generate what is sometimes called an uberjar with all the dependencies.

You can also run it using restx shell:

- using `restx deps install + app run --mode=prod` command, which will install your app dependencies based on information found in `md.restx.json` and then run it
- asking the shell to generate a start script for you, using `restx deps install + app compile + app generate-start-script` command which will prepare everything and generate a start.sh and start.bat launch scripts. Then you can use the script for instance with `export VM_OPTIONS=-Drestx.mode=prod && ./start.sh`


## Dev vs Prod

When running an app for production, you just need to make sure that you use the prod mode. Using app run this is not the default because the Main class itself sets a default to dev mode. With the shell this is what the `--mode=prod` is for, but you can also use `-Drestx.mode=prod` when launching your JVM.

This mode is important for production because in dev mode restx do many things which are useful only in dev, and have huge impact on performance:

- hot compilation of your sources
- hot reloading of changed classes
- using a [Factory](/docs/ref-factory.html) (restx dependency injection container) per request (which means throwing away all your components at each request, which is useful in dev to make sure your app is truly stateless, but obviously degrades performance)



