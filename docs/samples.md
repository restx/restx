---
filename: samples.md
layout: docs
title:  "Samples"
---
# RESTX Samples

Some samples are provided to demonstrate some of RESTX features.

Each sample is hosted in a separate github repo, so you can easily clone it or download its content.

<div class="note">
	<p>All samples can be imported in your IDE as described in <a href="ide.html">IDE support</a> documentation.</p>
</div>

## restx-samples-hello

This sample is the most basic, it's what is generated when you use the 'app new' command in the shell.
Check [Try generated app](try-generated-app.html) and [Understand generated app](generated-app-explained.html) page for details.

### Features demonstrated

- admin console
- api docs
- spec test
- spec as example
- servlet container integration

### Repo

[https://github.com/restx/restx-samples-hello](https://github.com/restx/restx-samples-hello)

## restx-samples-hellomongo

This sample is still pretty basic, compared to the `hello` sample it also demonstrates MongoDB integration.

The best way to learn how it is build is to look at the commits on github.

### Features demonstrated

- mongoDB integration with Jongo
- spec tests with Mongo Collections
- full CRUD

### Repo

[https://github.com/restx/restx-samples-hellomongo](https://github.com/restx/restx-samples-hellomongo)

## rxinvoice

This sample goes further than `restx-samples-hellomongo` to demonstrate restx with MongoDB integration in an invoice management app.

### Features demonstrated

- admin console
- api docs
- spec test 
- spec as example
- mongoDB integration with Jongo
- spec tests with Mongo Collections
- full CRUD
- user management
- user rights

### Repo

[https://github.com/xhanin/rxinvoice](https://github.com/xhanin/rxinvoice)


## restx-samples-beersample

This sample has been developed during the Couchbase workshop at BordeauxJUG. It demonstrates how to integrate RESTX with a CouchBase datastore.

### Features demonstrated

- couchbase as datastore

### Repo

[https://github.com/restx/restx-samples-beersample](https://github.com/restx/restx-samples-beersample)


## restx-samples-geektic

This sample is based on the code developed during [CodeStory](http://code-story.net/) at [Devoxx France](http://devoxx.fr) 2013.

This sample demonstrates not only a REST API but also the client with a nice UI.

### Features demonstrated

- mongoDB integration with Jongo
- using RESTX to serve static assets
- custom routes, without annotations 
- servletless server
- integrating a RESTX server to perform Selenium tests, based on FluentLenium / GhostDriver / PhantomJS
- AngularJS front end, LESS stylesheets, coffescript controllers

### Repo

[https://github.com/restx/restx-samples-geektic](https://github.com/restx/restx-samples-geektic)





