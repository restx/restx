---
filename: ref-extension-points.md
layout: docs
title:  "RESTX Extension Points"
---
# RESTX Extension Points

## Intro

Restx Dependency injection allows to easily write plugins extending some key extension points.

This page is intended to describe how to change or improve some RESTX behaviours.

## RESTX Session cookie names

By default, RESTX will generate 2 cookies named `RestxSession` and `RestxSessionSignature` or,
prior to version 0.2.9, if an `app.name` `String @Component` is provided ([see here](#appName)), `RestxSession-${app.name}` and `RestxSessionSignature-${app.name}`.

If you want to be less explicit about the technology running on your server, this behaviour can be overriden by providing a `RestxSessionCookieDescriptor @Component`.
{% highlight java %}
@Module
public class MyModule {
    @Provides
    public RestxSessionCookieDescriptor restxSessionCookieDescriptor(){
        return new RestxSessionCookieDescriptor("session", "sessionSignature");
    }
}
{% endhighlight %}

<a id="appName"> </a>
Providing an `app.name String @Component` :
{% highlight java %}
@Module
public class MyModule {
    @Provides @Named("app.name")
    public String appName(){ return "foo"; }
}
{% endhighlight %}

## Writing your own RESTX Given statements

... and Recorder too !
I think it will need a dedicated page.

## Overriding Admin Console security

We should grab some content from [ref-security](ref-security.html) and put it here

## Providing new Object deserializers (`StringConverters`)

## Overriding Jackson `ObjectMapper`

## Adding new RESTX Server implementations

## Enhancing RESTX Shell

## Using another Bean validation implementation (than `Hibernate Validator`)
