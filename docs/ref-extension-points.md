---
filename: ref-extension-points.md
layout: docs
title:  "RESTX Extension Points"
---
# RESTX Extension Points

## Intro

Restx Dependency injection allows to easily write plugins extending some key extension points.

This page is intended to describe how to change or improve default RESTX behaviours.

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
We strongly encourage you to provide an `app.name String @Component` allowing to distinguish your restx app from another restx app :
{% highlight java %}
@Module
public class MyModule {
    @Provides @Named("app.name")
    public String appName(){ return "foo"; }
}
{% endhighlight %}

## Adding session definition entry

Restx is a stateless server which relies on a signed cookie sent to the client (see previous section).

The information sent in this cookie is a simple `Map<String,String>` JSON object, where every entry corresponds to a `RestxSession.Definition.Entry`.

Thus, if you want to add your own information sent in the Restx cookie, you will have to provide additionnal `RestxSession.Definition.Entry @Components`.

A `RestxSession.Definition.Entry` is made of 3 things :
- A unique key name
- A type `T` : Restx will transform the cookie map's value to this type
- A `Guava CacheLoader` instance which will load a `T` instance, given the `String` cookie value, only if not found in the cache

Here is an example of a `RestxSession.Definition.Entry` production :
{% highlight java %}
@Module
public class MyModule {
    @Provides @Named("principal")
    public RestxSession.Definition.Entry principalSessionEntry(final BasicPrincipalAuthenticator authenticator) {
        return new RestxSession.Definition.Entry(RestxPrincipal.class, "principal",
                new CacheLoader<String, RestxPrincipal>() {
            @Override
            public RestxPrincipal load(String key) throws Exception {
                return authenticator.findByName(key).orNull();
            }
        });
    }
}
{% endhighlight %}

This will generate a Session cookie similar to :
{% highlight js %}
RestxSession: {"_expires":"2013-09-15T18:30:41.234+02:00","principal":"admin"}
{% endhighlight %}

Thus, when the following code will be called :
{% highlight java %}
RestxPrincipal principal = RestxSession.current().get(RestxPrincipal.class, "principal");
{% endhighlight %}

The `admin` value will be fetched into the `RestxSession.Definition.Entry` cache and, if not found,
the `load('admin')` method will be called to resolve the corresponding `RestxPrincipal`.

This is a design which allows :
- To define an exhaustive 'session dictionnary' : you won't be able to add things to the session without providing an appropriate `Definition.Entry`
- To only *lazy load* session entries, and cache them, without an expensive cost (only `String` as values)
- To easily scale your application (server is truly stateless)


## Admin Console security

### Restx admin password

By default, access to restx admin console is allowed to user with `login=admin` & `password=juma`.

If you want to override this default password, you can either :

- Override the raw password by providing a `restx.admin.password` `String @Component`
{% highlight java %}
@Module
public class MyModule {
    @Provides @Named("restx.admin.password")
    public String restxAdminPassword() { return "new-password"; }
}
{% endhighlight %}

- Or directly override the password hash by providing a `restx.admin.passwordHash` `String @Component`
{% highlight java %}
@Module
public class MyModule {
    @Provides @Named("restx.admin.passwordHash")
    public String restxAdminPasswordHash() { Hashing.md5().hashString("new-password", Charsets.UTF_8).toString(); }
}
{% endhighlight %}
<div class="note">
	<p>
	If you override the `restx.admin.passwordHash` hashing strategy (not relying on md5() anymore), you will need to override the `restx/admin/js/login.js` file
	by overriding the `authenticate()` scope function, particularly to replace the `SparkMD5.hash(password)` statement.
	</p>
</div>

### Authentication mecanism

As stated on [ref-security](ref-security.html) page, you can provide your own `BasicPrincipalAuthenticator @Component` by :
- Using `restx-security-basic` dependency
- And declaring the component :
{% highlight java %}
@Module
public class MyModule {
    @Provides
    public BasicPrincipalAuthenticator basicPrincipalAuthenticator(final UserRepository userRepository) {
        return new BasicPrincipalAuthenticator() {
            @Override
            public Optional<? extends RestxPrincipal> findByName(String name) {
                return userRepository.findUserByName(name);
            }
            @Override
            public Optional<? extends RestxPrincipal> authenticate(String name, String passwordHash, ImmutableMap<String, ?> principalData) {
                boolean rememberMe = Boolean.valueOf((String) principalData.get("rememberMe"));
                Optional<? extends RestxPrincipal> user = userRepository.findUserByNameAndPasswordHash(name, passwordHash);
                if (user.isPresent()) {
                    RestxSession.current().expires(rememberMe ? Duration.standardDays(30) : Duration.ZERO);
                }
                return user;
            }
        };
    }
}
{% endhighlight %}

## Writing your own RESTX Given statements

... and Recorder too !
I think it will need a dedicated page.

## Adding some Administration pages

## Providing new Object deserializers (`StringConverters`)

## Overriding Jackson `ObjectMapper`

## Adding new RESTX Server implementations

## Enhancing RESTX Shell

## Using another Bean validation implementation (than `Hibernate Validator`)
