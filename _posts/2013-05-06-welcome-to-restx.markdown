---
layout: post
title:  "Welcome to RESTX!"
date:   2013-05-06 21:43:24
categories: restx
---

RESTX is a brand new REST framework, we are happy you're stopping by!

RESTX is still in its early infancy, but you can already get a taste of it...

### Write this: 

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

that:

{% highlight java %}
public class Message {
    private String message;

    public String getMessage() {  return message;  }

    public Message setMessage(final String message) {
        this.message = message;
        return this;
    }
}
{% endhighlight %}

and that:

{% highlight yaml %}
title: should say hello
given:
  - time: 2013-03-31T14:33:18.272+02:00
wts:
  - when: GET message?who=xavier
    then: |
      {"message":"hello xavier, it's 14:33:18"}
{% endhighlight %}

### and get that:

- a server starting blazingly fast
{% highlight console %}
22:22:33.015 [main             ] INFO  r.s.simple.simple.SimpleWebServer - starting web server
22:22:33.500 [main             ] INFO  o.h.validator.internal.util.Version - HV000001: Hibernate Validator 5.0.0.Final
22:22:33.605 [main             ] INFO  restx.RestxMainRouterFactory - 
--------------------------------------
 -- RESTX READY
 -- 2 filters
 -- 15 routes
 -- for admin console,
 --   VISIT http://localhost:8086/api/@/ui/
 --
{% endhighlight %}

- a working implementation of your REST endpoint
{% highlight console %}
[restx] http "http://localhost:8086/api/message?who=xavier"                                                                                                                                                           22:24:25  ☁  master ☂ ✭
HTTP/1.1 200 OK
Cache-Control: no-cache
Connection: keep-alive
Content-Type: application/json; charset=UTF-8
Transfer-Encoding: chunked

{
    "message": "hello xavier, it's 22:25:00"
}
{% endhighlight %}

- documentation of your API with examples inside...
![REST API documentation with RESTX example](/images/posts/api-doc-1.png)

- ... that you can try directly within the browser
![trying REST API with RESTX example](/images/posts/api-doc-2.png)

- an automated integration test
![automated integration test with RESTX](/images/posts/hello-integration-test.png)

- an admin console with...

- basic monitoring UI

- beans graph visualization

- and more...

