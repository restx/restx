---
filename: ref-security.md
layout: docs
title:  "Security"
---
# RESTX Security support

Restx provides its own session and security support. The rationale for that is that Restx fully embraces the "no conversation state on web application server" moto, which is incompatible with standard Java session (and therefore security) management.

<div class="note">
	<p>When used in a servlet container, you can use the container security features. But then you rely on the container session management which is stateful and requires more complex load balancing management and HA tuning.</p>
</div>

The session mechanism in Restx is based on signed client cookies storing only a set of session keys, the server being able to reproduce the full session state based only on these keys. The keys being signed, they can't be tampered on the client side.

The security mechanisms relies on this session facilty and provides convenient authorization management together with a very flexible authentication support.

## Authentication

### Basics

You can access the currently authenticated principal using `RestxSession#getPrincipal()`:
{% highlight java %}
Optional<RestxPrincipal> principal = RestxSession.current().getPrincipal();
{% endhighlight %}

It doesn't assume how this has been set, so authentication is very flexible, you only need to call `RestxSession#authenticateAs(RestxPrincipal principal)` like this in your code:
{% highlight java %}
RestxSession.current().authenticateAs(principal);
{% endhighlight %}

If you already have an authentication mechanism, call that method once you get an authenticated principal (that is to say a user or a third party app or anything else that you consider can authenticate in your app).

### Using `restx-security-basic`

If you don't already have such authentication mechanism, Restx provides a basic authentication facility in the `restx-security-basic` module. 

<div class="note">
	<p>We recommend that you read these articles before dealing with password storage and general authentication issues:</p>
	<ul>
<li><a href="http://codingkilledthecat.wordpress.com/2012/09/04/some-best-practices-for-web-app-authentication/">some-best-practices-for-web-app-authentication</a></li>
<li><a href="http://stackoverflow.com/questions/348109/is-double-hashing-a-password-less-secure-than-just-hashing-it-once">is-double-hashing-a-password-less-secure-than-just-hashing-it-once</a></li>
<li><a href="http://www.mindrot.org/projects/jBCrypt/">jBCrypt</a></li>
</ul>
</div>

All you need to do is:

- add a dependency on the module
- provide an implementation of `BasicPrincipalAuthenticator`

`BasicPrincipalAuthenticator` defines 2 methods that you need to implement:

- `Optional<? extends RestxPrincipal> authenticate(String name, String passwordHash, ImmutableMap<String, ?> principalData)`
- `Optional<? extends RestxPrincipal> findByName(String name)` 

<div class="note">
	<p>For a complete example of a BasicAuthenticator using mongo storage, you can have a look at the rxinvoice example, and especially its <a href="https://github.com/xhanin/rxinvoice/blob/master/src/main/java/rxinvoice/AppModule.java#L34">AppModule</a> and <a href="https://github.com/xhanin/rxinvoice/blob/master/src/main/java/rxinvoice/rest/UserResource.java">UserResource</a>.</p>
</div>


The `restx-security-basic` module provides the following endpoints:

#### POST /sessions 

Using json object like this:
{% highlight javascript %}
{
	"principal": {
		"name": "myprincipal",
		"passwordHash": "1285ec167b6e4dd404c2a11b488e05cd"
	}
}
{% endhighlight %}

if the user / passwordHash is valid (according to your implementation of `BasicPrincipalAuthenticator`), the principal is authenticated for the duration of the session, otherwise a `401` error is returned.

#### GET /sessions/current

Returns the currently opened session if any, 401 if no session is opened

#### DELETE /sessions/current

Finish the current session, i.e. logout the currently authenticated principal.

### Sudoing, aka "run as"

Restx supports out of the box a `su` mechanism, which lets you override the current session (and especially the principal) with arbitrary data for the duration of a single request.

To use this mechanism you just need to:

- be authenticated with a Principal with `restx-admin` role.
- use the HTTP Header `RestxSu` and give it the content of the session keys you want to override

This mechanism is used in the API-DOCS console, allowing to easily tests your requests for arbitrary principals.

Here is an example:

{% highlight console %}
GET /api/sessions/current

RestxSu: { "principal": "test" }
{% endhighlight %}

## Authorizations

## Basics

Handling authorizations on your Restx endpoints is very easy. First, Restx endpoints are secured by default: it means that you need to have a principal authenticated to use a Restx endpoint.

So if you have this:
{% highlight java %}
@GET("/greetings/{who}")
public Message sayHello(String who) {
	return new Message(who);
}
{% endhighlight %}

calling `GET /greetings/world` will return a 401 http code by default.

If you want to make an endpoint public, you need to use the @PermitAll annotation:
{% highlight java %}
@PermitAll 
@GET("/greetings/{who}")
public Message sayHello(String who) {
	return new Message(who);
}
{% endhighlight %}

Note that you can also use that annotation at class level rather than method level, to make all endpoints in a class public.

Restx also allows to specify a role for the principal, using the @RolesAllowed annotation:
{% highlight java %}
@RolesAllowed("admin")
@GET("/greetings/{who}")
public Message sayHello(String who) {
	return new Message(who);
}
{% endhighlight %}
With that not only Restx will check that a principal is authenticated, but it will also check that it has the `admin` role. In case a principal is authenticated but does not have the `admin` role, a `403` HTTP status code will be returned.

### Advanced

For more advanced authorizations, have a look at the `RestxSecurityManager` and `Permission` types, which let you define and use very flexible authorizations.


<div class="go-next">
	<ul>
		<li><a href="ref-core.html"><i class="icon-cloud"> </i> REST support reference</a></li>
		<li><a href="ref-factory.html"><i class="icon-cogs"> </i> RESTX Factory reference</a></li>
		<li><a href="/community/"><i class="icon-beer"> </i> Community</a></li>
		<li><a href="/docs/"><i class="icon-book"> </i> Documentation</a></li>
	</ul>	
</div>




 