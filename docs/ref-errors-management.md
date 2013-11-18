---
filename: ref-errors-management.md
layout: docs
title:  "Error Management"
---
# RESTX business errors management

Restx provides a powerful way to handle errors in consistent way throughout your application.

## Defining Errors

RESTX introduce a kind of DSL to define your business errors.

You define your application errors using annotated enums, usually grouped in a class called Rules, which we recommend to define as an inner class of the domain class to which the rules apply.

The values of the enum are the parameters that sould be provided when raising such an error.

<div class="note">
<p>The use of enum has been chosen for definition readability and code completion when raising the error.</p>
<p>It should be considered as a DSL, not a real enum</p>
</div>

Here is an example:

{% highlight java %}
    public static class Rules {
        @ErrorCode(code = "USER-001", description = "must have a company")
        public static enum CompanyRef {
            @ErrorField("user key") KEY
        }
        @ErrorCode(code = "USER-002", description = "must have valid company - provided company key not found")
        public static enum ValidCompanyRef {
            @ErrorField("user key") KEY,
            @ErrorField("company ref") COMPANY_REF
        }
    }
{% endhighlight %}

Here we have defined 2 business errors:

- CompanyRef business error indicates a user must have a company, and takes one parameter called KEY which is the user key.
- ValidCompanyRef business error indicates that the company refecrenced by a user must exist

The codes provided in the annotations must be unique, and is used for easy referencing the business error.

## Raising errors

RESTX doesn't provide facility for checking the rule, so you have to do the check on your own. But then if your check fails you can raise the error like this:

{% highlight java %}
if (!companyResource.findCompanyByKey(user.getCompanyRef()).isPresent()) {
     throw RestxError.on(User.Rules.ValidCompanyRef.class)
             .set(User.Rules.ValidCompanyRef.KEY, user.getKey())
             .set(User.Rules.ValidCompanyRef.COMPANY_REF, user.getCompanyRef())
             .raise();
}
{% endhighlight %}

The API is fluent, and type safe, with pretty good code completion to provide the parameters.

## Consuming errors 

On the client side, errors are provided as:

- a 400 http status code (this can be configured using the `status` parameter on the `@ErrorCode` annotation)
- a json object detailing the error

the json object looks like this:

{% highlight console %}
{ "data" : { "COMPANY_REF" : "5288ffd2a0ee40e0f6b0ed8c",
      "KEY" : "5288ffd2a0ee40e0f6b0ed8c"
    },
  "description" : "must have valid company - provided company key not found",
  "errorCode" : "USER-002",
  "errorTime" : "2013-11-18T21:48:36.151Z",
  "id" : "c51f08ae-5fea-43b5-b5d6-7c6b11076011"
}
{% endhighlight %}

## Documenting errors

Error definitions are automatically documented in the admin console on `/@/ui/errors/`:

![Errors console](/images/docs/admin-errors.png)

As you can see the console provides the list of business errors, with:

- the error code
- the associated http status
- the description
- the associated parameters, with their names and description

<div class="go-next">
	<ul>
		<li><a href="ref-core.html"><i class="icon-cloud"> </i> REST support reference</a></li>
		<li><a href="ref-factory.html"><i class="icon-cogs"> </i> RESTX Factory reference</a></li>
		<li><a href="/community/"><i class="icon-beer"> </i> Community</a></li>
		<li><a href="/docs/"><i class="icon-book"> </i> Documentation</a></li>
	</ul>	
</div>


 
