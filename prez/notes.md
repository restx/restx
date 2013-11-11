---
layout: default
title:  "RESTX prez notes"
---

# RESTX prez notes

## Prepare box

### clean ssh known hosts

vi ~/.ssh/known_hosts
> /demo.restx.io [return] dd :wq

### clean demo1 project

rm -rf ~/projects/demo1

### launch restx-site jekyll

cd ~/dev/wkspace/restx/restx-site && jekyll serve --watch

### open chrome with:

- [http://localhost:4000/prez/notes.html](http://localhost:4000/prez/notes.html)
- [http://slid.es/xavierhanin/restx](http://slid.es/xavierhanin/restx)

### open iterm

with 2 tabs shells in prez mode

### open idea

and close all projects

## Install

### create instance
[Compute Engine Console](https://cloud.google.com/console#/project/apps~psyched-ceiling-333/compute/instances)

new instance > demo1 - f1 micro - debian7

copy IP address > setup demo.restx.io in [OVH DNS](https://www.ovh.com/managerv3/)

### SSH
{% highlight console %}
gcutil --service_version="v1beta15" --project="psyched-ceiling-333" ssh  --zone="europe-west1-a" "demo1"
{% endhighlight %}

### Prepare machine
{% highlight console %}
sudo apt-get update
sudo apt-get install -y openjdk-7-jdk git nginx
{% endhighlight %}


Note: nginx is used mostly to serve on port 80 which makes it easier to open demo.rest.io, 
and to provide a web UI to play with the demo built.

replace `/etc/nginx/sites-available/default` by:
{% highlight console %}
server {
     # root is used when no location is matched
     root              /www;
     index index.html;
     # disable browser cache
     expires           0;
     location ^~ /api/ {
          proxy_pass      http://127.0.0.1:8080;
					TODO: check proxy reverse
     }
}{% endhighlight %}

add demo1 ui to /www

restart nginx:
/etc/init.d/nginx restart


### install restx
{% highlight console %}
curl -s http://restx.io/install.sh | sh
{% endhighlight %}

### install restx plugin
{% highlight console %}
restx
restx> shell install
1
{% endhighlight %}


## demo

### create app, and run
{% highlight console %}
restx> app new
{% endhighlight %}

Use 'x' as password for easy access.

### access

{% highlight console %}
http --session=u1 POST "http://demo.restx.io/api/sessions" principal:='{"name":"admin", "passwordHash":"9dd4e461268c8034f5c8564e155c67a6"}'
http --session=u1 GET "http://localhost:8080/api/message?who=bordeaux"
{% endhighlight %}

show HelloResource
explain
change message in hello resource with vim.
show result with autocompile

### api docs

[http://demo.restx.io/api/@/ui/](http://demo.restx.io/api/@/ui/)

Demo API-DOCS, Try out

show what happens with su user1 / user2

### mount fs
{% highlight console %}
cd ~/projects && mkdir demo1 && sshfs demo.restx.io: demo1 && cd demo1/demo1
{% endhighlight %}

### open in idea
{% highlight console %}
idea pom.xml
{% endhighlight %}

Add more endpoints

{% highlight java %}
@Component @RestxResource
public class HelloResource {
    private static List<Message> messages = new ArrayList<>();			
    @GET("/message")
    @RolesAllowed(UserRepository.User.HELLO_ROLE)
    public Message sayHello(String who) {
        return new Message().setMessage(String.format(
                "bonjour %s, il est %s",
                who, DateTime.now().toString("HH:mm:ss")));
    }
    @POST("/messages")
    public Message createMessage(Message message) {
        messages.add(message.setMessage("hello " + message.getMessage()));
        return message;
    }
    @GET("/messages")
    public Iterable<Message> findMessages() {
        return messages;
    }
}
{% endhighlight %}

POST {"message":"bordeaux"}

Add more fields

{% highlight java %}
public class Message {
    @Size(min = 4, message = "la taille mini est de 4")
    private String message;
    private String who;
    private DateTime when;
		[...]
}
{% endhighlight %}

{% highlight java %}
@POST("/messages")
public Message createMessage(Message message) {
    messages.add(message
            .setWhen(DateTime.now())
            .setWho(Objects.firstNonNull(message.getWho(),
                    RestxSession.current().getPrincipal().get().getName()))
    );
    return message;
}
{% endhighlight %}

### deploy on cloudbees

{% highlight console %}
mvn package && bees app:deploy target/*.war -a xhanin/restxdemo
{% endhighlight %}

http://restxdemo.xhanin.eu.cloudbees.net/

### Mongo

install mongo on box:
curl -L http://goo.gl/lbOqQo | sh

add dependencies:
compile
"io.restx:restx-jongo:${restx.version}",
test
"io.restx:restx-jongo-specs-tests:${restx.version}",

update pom (from box):
restx build generate pom

provide db name component in AppModule:
{% highlight java %}
@Provides @Named(JongoFactory.JONGO_DB_NAME)
public String dbName() {
    return "demo1";
}
{% endhighlight %}

add key to Message:
{% highlight java %}
public class Message {
    @Id @ObjectId
    private String key;
		[...]
}
{% endhighlight %}

review resource:
{% highlight java %}
@Component @RestxResource
public class HelloResource {
    private final JongoCollection messages;
    public HelloResource(@Named("messages") JongoCollection messages) {
        this.messages = messages;
    }
    @POST("/messages")
    public Message createMessage(Message message) {
        messages.get().save(message
                .setMessage("bonjour " + message.getMessage())
                .setWhen(DateTime.now())
                .setWho(Objects.firstNonNull(message.getWho(),
                        RestxSession.current().getPrincipal().get().getName())))
        ;
        return message;
    }
    @GET("/messages")
    public Iterable<Message> findMessages() {
        return messages.get().find().as(Message.class);
    }
}
{% endhighlight %}

### UI

{% highlight console %}
cp board.html index.html
{% endhighlight %}

### Show Monitor & Factory UI

### spec test

on box:
{% highlight console %}
shell install
2 3

restx spec test server
{% endhighlight %}

record tests / fixes


### load test

gatling test:
{% highlight scala %}
package basic

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import io.gatling.http.Headers.Names._
import scala.concurrent.duration._
import bootstrap._
import assertions._

class RestxDemo extends Simulation {

	val httpProtocol = http
		.baseURL("http://demo.restx.io:8080")

	val scn = scenario("RESTX demo get messages load test")
		.repeat(5) {
			exec(
				http("get messages")
					.get("/api/messages")
					)
                }

	setUp(scn.inject(ramp(300 users) over (10 seconds)))
		.protocols(httpProtocol)
		.assertions(
			global.successfulRequests.percent.is(100))
}
{% endhighlight %}

add @PermitAll on GET("/messages")

{% highlight console %}
restx app run --mode=prod --quiet
{% endhighlight %}

run gatling
{% highlight console %}
cd /dev/tools/gatling/gatling-charts-highcharts-2.0.0-M3a
bin/gatling.sh
{% endhighlight %}


[example results](gatling-results2/index.html)

Example logs
{% highlight console %}
22:38:22.656 [qtp1694213110-23 ] INFO  restx.StdRestxMainRouter - << [RESTX REQUEST] GET /messages >> 200 - 1.623 ms
22:38:22.659 [qtp1694213110-16 ] INFO  restx.StdRestxMainRouter - << [RESTX REQUEST] GET /messages >> 200 - 521.4 ?s
22:38:22.666 [qtp1694213110-21 ] INFO  restx.StdRestxMainRouter - << [RESTX REQUEST] GET /messages >> 200 - 508.9 ?s
22:38:22.676 [qtp1694213110-22 ] INFO  restx.StdRestxMainRouter - << [RESTX REQUEST] GET /messages >> 200 - 511.5 ?s
22:38:22.683 [qtp1694213110-19 ] INFO  restx.StdRestxMainRouter - << [RESTX REQUEST] GET /messages >> 200 - 690.4 ?s
22:38:22.688 [qtp1694213110-18 ] INFO  restx.StdRestxMainRouter - << [RESTX REQUEST] GET /messages >> 200 - 534.2 ?s
22:38:22.751 [qtp1694213110-17 ] INFO  restx.StdRestxMainRouter - << [RESTX REQUEST] GET /messages >> 200 - 503.9 ?s
22:38:22.756 [qtp1694213110-20 ] INFO  restx.StdRestxMainRouter - << [RESTX REQUEST] GET /messages >> 200 - 494.3 ?s
22:38:22.763 [qtp1694213110-23 ] INFO  restx.StdRestxMainRouter - << [RESTX REQUEST] GET /messages >> 200 - 553.8 ?s
22:38:22.768 [qtp1694213110-16 ] INFO  restx.StdRestxMainRouter - << [RESTX REQUEST] GET /messages >> 200 - 554.8 ?s
22:38:22.775 [qtp1694213110-21 ] INFO  restx.StdRestxMainRouter - << [RESTX REQUEST] GET /messages >> 200 - 487.2 ?s
22:38:22.777 [qtp1694213110-22 ] INFO  restx.StdRestxMainRouter - << [RESTX REQUEST] GET /messages >> 200 - 483.6 ?s
22:38:22.784 [qtp1694213110-19 ] INFO  restx.StdRestxMainRouter - << [RESTX REQUEST] GET /messages >> 200 - 489.5 ?s
22:38:22.791 [qtp1694213110-18 ] INFO  restx.StdRestxMainRouter - << [RESTX REQUEST] GET /messages >> 200 - 485.4 ?s
22:38:22.796 [qtp1694213110-17 ] INFO  restx.StdRestxMainRouter - << [RESTX REQUEST] GET /messages >> 200 - 529.7 ?s
22:38:22.803 [qtp1694213110-20 ] INFO  restx.StdRestxMainRouter - << [RESTX REQUEST] GET /messages >> 200 - 529.7 ?s
22:38:22.805 [qtp1694213110-23 ] INFO  restx.StdRestxMainRouter - << [RESTX REQUEST] GET /messages >> 200 - 515.5 ?s
22:38:22.815 [qtp1694213110-16 ] INFO  restx.StdRestxMainRouter - << [RESTX REQUEST] GET /messages >> 200 - 503.9 ?s
22:38:22.820 [qtp1694213110-21 ] INFO  restx.StdRestxMainRouter - << [RESTX REQUEST] GET /messages >> 200 - 574.3 ?s
22:38:22.829 [qtp1694213110-22 ] INFO  restx.StdRestxMainRouter - << [RESTX REQUEST] GET /messages >> 200 - 510.4 ?s
22:38:22.838 [qtp1694213110-19 ] INFO  restx.StdRestxMainRouter - << [RESTX REQUEST] GET /messages >> 200 - 512.6 ?s
22:38:22.845 [qtp1694213110-18 ] INFO  restx.StdRestxMainRouter - << [RESTX REQUEST] GET /messages >> 200 - 487.4 ?s
22:38:22.853 [qtp1694213110-17 ] INFO  restx.StdRestxMainRouter - << [RESTX REQUEST] GET /messages >> 200 - 527.2 ?s
22:38:22.871 [qtp1694213110-20 ] INFO  restx.StdRestxMainRouter - << [RESTX REQUEST] GET /messages >> 200 - 537.6 ?s
22:38:22.923 [qtp1694213110-23 ] INFO  restx.StdRestxMainRouter - << [RESTX REQUEST] GET /messages >> 200 - 489.9 ?s
{% endhighlight %}



## Stop Box

.04$ is enough for a demo!