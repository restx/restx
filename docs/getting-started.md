---
layout: docs
title:  "Getting Started"
---
# Getting Started

## Ready

To get ready starting development with RESTX you should first [install RESTX](install.html). This shouldn't take more than a few minutes (could be seconds on Linux/MacOS with good bandwidth!), so you probably already have `restx` ready in your PATH!

Then you will need to install some plugins. Indeed RESTX comes almost entirely nude when you install it, but don't worry installing plugin is super easy. You just need to launch the restx shell (by running the `restx` command), and use the `shell install` command:
{% highlight console %}
restx> shell install
:: loading settings :: url = jar:file:/Users/xavierhanin/.restx/lib/ivy-2.3.0.jar!/org/apache/ivy/core/settings/ivysettings.xml
looking for plugins...
found 3 available plugins
 [  1] io.restx:restx-core-shell:{{ site.restx-version }}
        core commands: generate new app, ...
 [  2] io.restx:restx-build-shell:{{ site.restx-version }}
        build commands: generate pom, ivy, ...
 [  3] io.restx:restx-specs-shell:{{ site.restx-version }}
        specs commands: run a specs server, ...
Which plugin would you like to install (eg '1 3 5')?
{% endhighlight %}

Then install at least the `restx-core-shell` plugin by typing `1`:

{% highlight console %}
1
installing io.restx:restx-core-shell:{{ site.restx-version }}...
:: loading settings :: url = jar:file:/Users/xavierhanin/.restx/lib/ivy-2.3.0.jar!/org/apache/ivy/core/settings/ivysettings.xml
installed io.restx:restx-core-shell:{{ site.restx-version }}
installed 1 plugins, restarting shell to take them into account
RESTARTING SHELL...

===============================================================================
== WELCOME TO RESTX SHELL - {{ site.restx-version }} - type `help` for help on available commands
===============================================================================
restx>
{% endhighlight %}

the `restx-core-shell` plugin is now ready to use! 

<div class="note">
	<p>As you can see installing plugins in the shell is super easy, remember to check periodically if there are new plugins to try out!</p>
</div>

## Steady

The first step is to bootstrap your first RESTX app. Launch the `restx` command in a directory where you want to create the app (it could be a `workspace` or `projects` directory) and run the `app new` command. It will ask you some questions, you can simply give the app name and use the default answer to all other questions:

{% highlight console %}
restx> app new
Welcome to RESTX APP bootstrap!
This command will ask you a few questions to generate your brand new RESTX app.
For any question you can get help by answering '??' (without the quotes).

App name? MyApp
group id [myapp]?
artifact id [myapp]?
main package [myapp]?
version [0.1-SNAPSHOT]?
generate ivy file [yes]?
generate maven pom [no]?
restx version [0.2.5-SNAPSHOT]?
signature key (to sign cookies) [4695455274730382645 myapp 45cbde67-4700-485b-b0ce-b1b161c0cf94 MyApp]?
default port [8080]?
base path [/api]?
generate hello resource example [Y/n]?
scaffolding app to `/Users/xavierhanin/Documents/tmp/myapp` ...
generating module.ivy ...
generating hello resource ...
Congratulations! - Your app is now ready in `/Users/xavierhanin/Documents/tmp/myapp`
{% endhighlight %}

<div class="note">
<p>If you want to learn more about this app generation, <a href="shell-app-bootstrap.html">check the related doc</a>.</p>
</div>

## Go!

Your app is now ready, you can open it in your favorite IDE, simply follow the instrutions provided here:

- [Intellij IDEA](ide-idea.html)
- [Eclipse](ide-eclipse.html)
- [Netbeans](ide-netbeans.html)

<div class="note">
	<p>Don't want to use an IDE? running the app directly from the shell is planned, watch <a href="https://github.com/restx/restx/issues/4">this issue</a> to show your interest!</p>
</div>

Once your IDE is properly setup, go on and [try out the generated app](try-generated-app.html)