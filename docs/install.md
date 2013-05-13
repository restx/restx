---
published: true
filename: install.md
layout: docs
title: Install

---

# Install

Installing RESTX is very straightforward as soon as you have a [Oracle Java 7 JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html) properly installed on your machine.

Note that installing RESTX is not necessary to use it, you can also [manually setup your Java app to use RESTX](manual-app-bootstrap.html).

## Windows
Installation can be done in 3 simple steps:
- [download](/download.html) the package (either as a zip or tar.gz, though we recommend zip for Windows)
- unpack the package in the directory of your choice, preferably a directory without a space. Beware, the package has no root directory. Example of installation directory: `c:\dev\tools\restx`, `c:\restx`
- add the directory to your PATH ([HowTo](http://www.computerhope.com/issues/ch000549.htm))
- open a terminal and run `restx`, you should be welcomed with a prompt like this one:
{% highlight console %}
===============================================================================
== WELCOME TO RESTX SHELL - {{ site.restx-version }} - type `help` for help on available commands
===============================================================================
{% endhighlight %}

## Linux / Mac OS

### Super easy / Single command way
For the lucky Linux / MacOS users, you can just run this command in your terminal:
{% highlight console %}
$ curl -s http://restx.io/install.sh | sh
{% endhighlight %}

[View source](https://github.com/restx/restx/blob/gh-pages/install.sh)

Then you should be able to run `restx`:
{% highlight console %}
$ restx
===============================================================================
== WELCOME TO RESTX SHELL - {{ site.restx-version }} - type `help` for help on available commands
===============================================================================
{% endhighlight %}


### Manual approach
if you prefer to stay in control of your installation, here are the installation instructions:
- [download](/download.html) the package (either as a zip or tar.gz, though we recommend tar.gz for Linux / Mac OS)
- unpack the package in the directory of your choice. Beware, the package has no root directory.
- create a symlink called restx to the restx script located in the installation folder OR add the directory to your PATH ([Linux](http://www.troubleshooters.com/linux/prepostpath.htm) [Mac OS X](http://keito.me/tutorials/macosx_path))

Here is an example of script to do that:
{% highlight console %}
mkdir ~/.restx
curl --progress-bar --fail "http://repo1.maven.org/maven2/io/restx/restx-package/0.2.4/restx-package-0.2.4.tar.gz" | tar -xzf - -C ~/.restx
ln -s ~/.restx/restx /usr/local/bin/restx
{% endhighlight %}

Then you should be able to run `restx`:
{% highlight console %}
$ restx
===============================================================================
== WELCOME TO RESTX SHELL - {{ site.restx-version }} - type `help` for help on available commands
===============================================================================
{% endhighlight %}

<div class="go-next">
	<ul>
		<li><a href="getting-started.html"><i class="icon-play"> </i> Getting started</a></li>
		<li><a href="/community/"><i class="icon-beer"> </i> Community</a></li>
		<li><a href="/docs/"><i class="icon-book"> </i> Documentation</a></li>
	</ul>	
</div>
