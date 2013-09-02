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

## Overriding Admin Console security

### Overriding Restx admin password

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

### Overriding authentication mecanism

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
